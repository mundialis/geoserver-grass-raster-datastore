package de.terrestris.hermosa.grass_gdal;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.geotools.data.DataSourceException;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.geotools.parameter.ParameterGroup;
import org.geotools.util.factory.Hints;
import org.geotools.util.logging.Logging;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.referencing.FactoryException;

import java.io.File;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * The GridFormat extension point class for the GRASS GDAL datastore.
 */
public class GrassGdalFormat extends AbstractGridFormat {

    private static final Logger LOGGER = Logging.getLogger(GrassGdalFormat.class);

    GrassGdalFormat() {
        mInfo = new HashMap<>();
        mInfo.put("name", "GRASS GDAL");
        mInfo.put("description", "GRASS GDAL format");
        mInfo.put("vendor", "terrestris");
        mInfo.put("version", "0.0.1");

        // reading parameters
        readParameters = new ParameterGroup(
            new DefaultParameterDescriptorGroup(mInfo, new GeneralParameterDescriptor[]{READ_GRIDGEOMETRY2D}));
    }

    @Override public AbstractGridCoverage2DReader getReader(Object o) {
        try {
            return new GrassGdalReader(o);
        } catch (DataSourceException e) {
            LOGGER.warning("Could not create a GDAL GRASS reader: " + e.getMessage());
            LOGGER.fine("Stack trace: " + ExceptionUtils.getStackTrace(e));
        } catch (FactoryException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override public AbstractGridCoverage2DReader getReader(Object o, Hints hints) {
        try {
            return new GrassGdalReader(o, hints);
        } catch (DataSourceException e) {
            LOGGER.warning("Could not create a GDAL GRASS reader: " + e.getMessage());
            LOGGER.fine("Stack trace: " + ExceptionUtils.getStackTrace(e));
        } catch (FactoryException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override public GridCoverageWriter getWriter(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override public boolean accepts(Object o, Hints hints) {
        File file = (File) o;
        synchronized (this) {
            Dataset dataset = gdal.OpenShared(file.getAbsolutePath(), gdalconstConstants.GA_ReadOnly);
            if (dataset == null) {
                return false;
            }
            dataset.delete();
        }
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
