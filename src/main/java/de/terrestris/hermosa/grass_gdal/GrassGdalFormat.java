package de.terrestris.hermosa.grass_gdal;

import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.geotools.data.DataSourceException;
import org.geotools.parameter.*;
import org.geotools.util.factory.Hints;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.filter.Filter;
import org.opengis.parameter.GeneralParameterDescriptor;

import java.util.HashMap;

public class GrassGdalFormat extends AbstractGridFormat {

    public GrassGdalFormat() {
        HashMap<String, String> map = new HashMap<>();
        map.put("name", "Peter");
        MatrixParameterDescriptors descriptor = new MatrixParameterDescriptors(map);
        readParameters = new MatrixParameters(descriptor);
    }

    @Override public AbstractGridCoverage2DReader getReader(Object o) {
        try {
            return new GrassGdalReader(o);
        } catch (DataSourceException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override public AbstractGridCoverage2DReader getReader(Object o, Hints hints) {
        try {
            return new GrassGdalReader(o, hints);
        } catch (DataSourceException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override public GridCoverageWriter getWriter(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override public boolean accepts(Object o, Hints hints) {
        System.out.println("accepts");
        return true;
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
