package de.terrestris.hermosa.grass_gdal;

import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFactorySpi;

/**
 * The SPI format factory for the GRASS GDAL coverage datastore.
 */
public class GrassGdalFormatFactory implements GridFormatFactorySpi {

    @Override public AbstractGridFormat createFormat() {
        return new GrassGdalFormat();
    }

    @Override public boolean isAvailable() {
        // TODO should be tweaked to check for working GDAL and a working gdal-grass plugin
        return true;
    }

}
