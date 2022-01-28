package de.terrestris.hermosa.grass_gdal;

import org.gdal.gdal.Driver;
import org.gdal.gdal.gdal;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFactorySpi;
import org.geotools.util.logging.Logging;

import java.util.logging.Logger;

/**
 * The SPI format factory for the GRASS GDAL coverage datastore.
 */
public class GrassGdalFormatFactory implements GridFormatFactorySpi {

    private static final Logger LOGGER = Logging.getLogger(GridFormatFactorySpi.class);

    static {
        LOGGER.info("Initializing gdal...");
        gdal.AllRegister();
        LOGGER.info("Initialized gdal.");
    }

    @Override
    public AbstractGridFormat createFormat() {
        LOGGER.info("Creating GRASS GDAL format.");
        return new GrassGdalFormat();
    }

    @Override
    public boolean isAvailable() {
        // this prevents the datastore to be used with other GDAL drivers, it is unknown if using the other drivers would
        // work (this should be tested at some point!)
        Driver grass = gdal.GetDriverByName("GRASS");
        LOGGER.info("GRASS driver available: " + (grass != null));
        return grass != null;
    }

}
