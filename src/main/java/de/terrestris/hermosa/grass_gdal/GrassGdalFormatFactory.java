package de.terrestris.hermosa.grass_gdal;

import org.gdal.gdal.Driver;
import org.gdal.gdal.gdal;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFactorySpi;

/**
 * The SPI format factory for the GRASS GDAL coverage datastore.
 */
public class GrassGdalFormatFactory implements GridFormatFactorySpi {

    static {
        gdal.AllRegister();
    }

    @Override
    public AbstractGridFormat createFormat() {
        return new GrassGdalFormat();
    }

    @Override
    public boolean isAvailable() {
        // this prevents the datastore to be used with other GDAL drivers, it is unknown if using the other drivers would
        // work (this should be tested at some point!)
        Driver grass = gdal.GetDriverByName("GRASS");
        return grass != null;
    }

}
