package de.terrestris.hermosa.grass_gdal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GrassGdalFormatFactoryTest {

    @Test
    void isAvailable() {
        Assertions.assertTrue(new GrassGdalFormatFactory().isAvailable());
    }

}
