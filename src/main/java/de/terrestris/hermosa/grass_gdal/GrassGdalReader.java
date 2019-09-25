package de.terrestris.hermosa.grass_gdal;

import org.geotools.coverage.Category;
import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.TypeMap;
import org.geotools.coverage.grid.*;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.data.DataSourceException;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.image.io.ImageIOExt;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.util.factory.Hints;
import org.opengis.coverage.ColorInterpretation;
import org.opengis.coverage.grid.Format;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.datum.PixelInCell;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class GrassGdalReader extends AbstractGridCoverage2DReader {

    public GrassGdalReader(Object o) throws DataSourceException {
        this(o, null);
    }

    public GrassGdalReader(Object o, Hints hints) throws DataSourceException {
        super(o, hints);
        coverageFactory = new GridCoverageFactory();
        crs = DefaultGeographicCRS.WGS84;
        // instantiate the bounds based on the default CRS
        originalEnvelope = new GeneralEnvelope(CRS.getEnvelope(crs));
        originalEnvelope.setCoordinateReferenceSystem(crs);
        originalGridRange = new GeneralGridEnvelope(originalEnvelope, PixelInCell.CELL_CENTER);
        coverageName = "peter";
    }

    @Override public Format getFormat() {
        return new GrassGdalFormat();
    }

    @Override public GridCoverage2D read(GeneralParameterValue[] parameters) throws IllegalArgumentException, IOException {
        BufferedImage img = ImageIO.read(new File("/tmp/peter.png"));

        ReferencedEnvelope envelope = null;
        String coverageName = "PETER!";
        try {
            envelope = new ReferencedEnvelope(5, 10, 7, 20, CRS.parseWKT("GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.0174532925199433,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]]\n"));
        } catch (FactoryException e) {
            e.printStackTrace();
        }
        final GridCoverageFactory factory =
                CoverageFactoryFinder.getGridCoverageFactory(null);

        int numBands = 4;

        // define Bands
        final GridSampleDimension[] bands = new GridSampleDimension[numBands];
        Set<String> bandNames = new HashSet<>();
        for (int i = 0; i < numBands; i++) {
            final ColorInterpretation colorInterpretation = TypeMap.getColorInterpretation(img.getColorModel(), i);
            if (colorInterpretation == null)
                throw new IOException("Unrecognized sample dimension type");
            Category[] categories = null;
            String bandName = colorInterpretation.name();
            // make sure we create no duplicate band names
            if (colorInterpretation == ColorInterpretation.UNDEFINED
                    || bandNames.contains(bandName)) {
                bandName = "Band" + (i + 1);
            }
            bands[i] = new GridSampleDimension(bandName, categories, null);
        }


        final Map<String, Object> properties = new HashMap<>();
        GridCoverage2D peter = factory.create(coverageName, img, envelope, bands, null, properties);
        return peter;
    }

}
