package de.terrestris.hermosa.grass_gdal;

import org.junit.jupiter.api.Assertions;

class GrassGdalFormatFactoryTest {

    @org.junit.jupiter.api.Test
    void isAvailable() {
        Assertions.assertTrue(new GrassGdalFormatFactory().isAvailable());
    }

}
