package de.terrestris.hermosa.grass_gdal;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.osr.SpatialReference;
import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.grid.GeneralGridEnvelope;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.data.DataSourceException;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.util.factory.Hints;
import org.geotools.util.logging.Logging;
import org.opengis.coverage.grid.Format;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.datum.PixelInCell;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.logging.Logger;

/**
 * Coverage reader class to read coverages from gdal. This is actually GRASS agnostic, but currently supports only
 * UInt16 rasters with one band.
 */
public class GrassGdalReader extends AbstractGridCoverage2DReader {

    private static final Logger LOGGER = Logging.getLogger(GrassGdalReader.class);

    static {
        gdal.AllRegister();
    }

    private final int width;

    private final int height;

    private final int numBands;

    private final File file;

    private double resx;

    private double resy;

    public GrassGdalReader(Object o) throws DataSourceException {
        this(o, null);
    }

    public GrassGdalReader(Object o, Hints hints) throws DataSourceException {
        super(o, hints);
        file = (File) o;
        coverageFactory = new GridCoverageFactory();
        crs = DefaultGeographicCRS.WGS84;
        // instantiate the bounds based on the default CRS
        originalEnvelope = new GeneralEnvelope(CRS.getEnvelope(crs));
        originalEnvelope.setCoordinateReferenceSystem(crs);
        originalGridRange = new GeneralGridEnvelope(originalEnvelope, PixelInCell.CELL_CENTER);
        coverageName = file.getName();
        Dataset dataset = gdal.OpenShared(file.getAbsolutePath(), gdalconstConstants.GA_ReadOnly);
        width = dataset.getRasterXSize();
        height = dataset.getRasterYSize();
        String crsWkt;
        String projRef = dataset.GetProjectionRef();
        if (projRef != null) {
            SpatialReference spatialReference = new SpatialReference(projRef);
            crsWkt = spatialReference.ExportToPrettyWkt();
            spatialReference.delete();
            try {
                crs = CRS.parseWKT(crsWkt);
            } catch (FactoryException e) {
                LOGGER.info("CRS WKT could not be parsed, ignoring.");
                LOGGER.fine(e.getMessage() + ExceptionUtils.getStackTrace(e));
            }
        }
        calculateEnvelope(dataset);
        numBands = dataset.getRasterCount();
        dataset.delete(); // this closes the dataset...
    }

    @Override public Format getFormat() {
        return new GrassGdalFormat();
    }

    @Override public GridCoverage2D read(GeneralParameterValue[] parameters) throws IllegalArgumentException, IOException {
        Dataset dataset = gdal.OpenShared(file.getAbsolutePath(), gdalconstConstants.GA_ReadOnly);
        try {
            int[] imageBounds = new int[]{0, 0, width, height};

            for (GeneralParameterValue value : parameters) {
                if (value.getDescriptor().getName().getCode().equals("ReadGridGeometry2D")) {
                    GridGeometry2D geometry2D = (GridGeometry2D) ((ParameterValue) value).getValue();
                    GeneralEnvelope bbox = GeneralEnvelope.toGeneralEnvelope(geometry2D.getEnvelope2D());
                    imageBounds = calculateRequiredPixels(bbox);
                }
            }

            ByteBuffer byteBuffer = dataset.GetRasterBand(1)
                .ReadRaster_Direct(imageBounds[0], imageBounds[1], imageBounds[2], imageBounds[3],
                    gdalconstConstants.GDT_UInt16);
            if (byteBuffer == null) {
                return null;
            }
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

            // TODO:
            // GRASS supports GDT_UInt16/32, float and double, so these need to be added here
            ShortBuffer buffer = byteBuffer.asShortBuffer();
            float[][] matrix = new float[imageBounds[3]][imageBounds[2]];
            for (int x = 0; x < imageBounds[2]; ++x) {
                for (int y = 0; y < imageBounds[3]; ++y) {
                    matrix[y][x] = Short.toUnsignedInt(buffer.get(y * imageBounds[2] + x));
                }
            }

            final GridCoverageFactory factory = CoverageFactoryFinder.getGridCoverageFactory(null);

            return factory.create(file.getName(), matrix, calculateSubEnvelope(imageBounds));
        } finally {
            if (dataset != null) {
                dataset.delete();
            }
        }
    }

    private int[] calculateRequiredPixels(GeneralEnvelope bbox) {
        double origMinX = originalEnvelope.getMinimum(0);
        double minx = Math.max(bbox.getMinimum(0), origMinX);
        double origMinY = originalEnvelope.getMinimum(1);
        double miny = Math.max(bbox.getMinimum(1), origMinY);
        double origMaxX = originalEnvelope.getMaximum(0);
        double maxx = Math.min(bbox.getMaximum(0), origMaxX);
        double origMaxY = originalEnvelope.getMaximum(1);
        double maxy = Math.min(bbox.getMaximum(1), origMaxY);
        double diff = minx - origMinX;
        double x = diff / resx;
        diff = Math.abs(maxy - origMaxY);
        double y = diff / Math.abs(resy);
        diff = maxx - minx;
        double width = diff / resx;
        diff = maxy - miny;
        double height = diff / Math.abs(resy);
        return new int[]{(int) Math.floor(x), (int) Math.floor(y), (int) Math.ceil(width), (int) Math.ceil(height)};
    }

    private GeneralEnvelope calculateSubEnvelope(int[] imageCoordinates) {
        double minx = originalEnvelope.getMinimum(0) + imageCoordinates[0] * resx;
        double maxx = minx + imageCoordinates[2] * resx;
        double maxy = originalEnvelope.getMaximum(1) - Math.abs(imageCoordinates[1] * resy);
        double miny = maxy - Math.abs(imageCoordinates[3] * resy);

        GeneralEnvelope generalEnvelope = new GeneralEnvelope(new double[]{minx, miny}, new double[]{maxx, maxy});
        generalEnvelope.setCoordinateReferenceSystem(crs);
        return generalEnvelope;
    }

    private void calculateEnvelope(Dataset dataset) {
        double[] transform = new double[6];
        dataset.GetGeoTransform(transform);
        double maxx = transform[0] + transform[1] * width + transform[2] * height;
        double miny = transform[3] + transform[5] * height + transform[4] * width;
        double minx = transform[0];
        double maxy = transform[3];
        resx = transform[1];
        resy = transform[5];

        originalEnvelope = new GeneralEnvelope(new double[]{minx, miny}, new double[]{maxx, maxy});
        originalEnvelope.setCoordinateReferenceSystem(crs);
        originalGridRange = new GeneralGridEnvelope(originalEnvelope, PixelInCell.CELL_CENTER);
    }

}
