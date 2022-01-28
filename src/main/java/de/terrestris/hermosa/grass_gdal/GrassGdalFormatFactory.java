/*
 * Copyright 2019-present terrestris GmbH & Co. KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
