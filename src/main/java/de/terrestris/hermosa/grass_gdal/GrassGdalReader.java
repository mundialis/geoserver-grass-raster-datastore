package de.terrestris.hermosa.grass_gdal;

import org.geotools.coverage.grid.GeneralGridEnvelope;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.data.DataSourceException;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.util.factory.Hints;
import org.opengis.coverage.grid.Format;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.datum.PixelInCell;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

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
        return null;
    }

    @Override public GridCoverage2D read(GeneralParameterValue[] parameters) throws IllegalArgumentException, IOException {

        Envelope envelope = null;
        try {
            envelope = new ReferencedEnvelope(5, 10, 7, 20, CRS.decode("EPSG:4326"));
        } catch (FactoryException e) {
            e.printStackTrace();
        }
        BufferedImage img = ImageIO.read(new File("/tmp/peter.png"));
        return new GridCoverageFactory().create("", img, envelope);
    }

}
