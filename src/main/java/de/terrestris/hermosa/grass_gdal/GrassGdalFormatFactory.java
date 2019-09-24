package de.terrestris.hermosa.grass_gdal;

import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFactorySpi;

public class GrassGdalFormatFactory implements GridFormatFactorySpi {

    @Override public AbstractGridFormat createFormat() {

        return new GrassGdalFormat();
    }

    @Override public boolean isAvailable() {
        return true;
    }

}
