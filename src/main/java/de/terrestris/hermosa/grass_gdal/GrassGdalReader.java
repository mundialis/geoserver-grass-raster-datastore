package de.terrestris.hermosa.grass_gdal;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.data.DataSourceException;
import org.geotools.util.factory.Hints;
import org.opengis.coverage.grid.Format;
import org.opengis.parameter.GeneralParameterValue;

import java.io.IOException;

public class GrassGdalReader extends AbstractGridCoverage2DReader {

    public GrassGdalReader(Object o) throws DataSourceException {
        this(o, null);
    }

    public GrassGdalReader(Object o, Hints hints) throws DataSourceException {
        super(o, hints);
        // TODO set envelope, coverage name
    }

    @Override public Format getFormat() {
        return null;
    }

    @Override public GridCoverage2D read(GeneralParameterValue[] parameters) throws IllegalArgumentException, IOException {
        return null;
    }

}
