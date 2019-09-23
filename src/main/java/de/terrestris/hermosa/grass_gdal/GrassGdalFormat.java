package de.terrestris.hermosa.grass_gdal;

import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.geotools.util.factory.Hints;
import org.opengis.coverage.grid.GridCoverageWriter;

public class GrassGdalFormat extends AbstractGridFormat {

    @Override public AbstractGridCoverage2DReader getReader(Object o) {
        return null;
    }

    @Override public AbstractGridCoverage2DReader getReader(Object o, Hints hints) {
        return null;
    }

    @Override public GridCoverageWriter getWriter(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override public boolean accepts(Object o, Hints hints) {
        return false;
    }

    @Override public GeoToolsWriteParams getDefaultImageIOWriteParameters() {
        throw new UnsupportedOperationException();
    }

    @Override public GridCoverageWriter getWriter(Object o, Hints hints) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        return "GRASS";
    }

    @Override
    public String getDescription() {
        return "Based on a GRASS location";
    }

}
