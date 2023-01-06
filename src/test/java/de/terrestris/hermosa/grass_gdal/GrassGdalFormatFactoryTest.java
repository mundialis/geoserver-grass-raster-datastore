package de.terrestris.hermosa.grass_gdal;

import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GrassGdalFormatFactoryTest {

  @Test
  void createFormat() {
    assertEquals(1,1);
    // Needed grass und gdal-grass
    // -Djava.library.path=/pfad/zu/grass (so von GRASS)
    // https://github.com/terrestris/docker-geoserver/blob/master/Dockerfile
    // GrassGdalFormatFactory factory = new GrassGdalFormatFactory();
    // try {
    //   GrassGdalFormat format = (GrassGdalFormat) factory.createFormat();
    //   String formatName = format.getName();
    //   assertEquals("GRASS GDAL", formatName);
    // } catch (ClassCastException cce) {
    //   fail();
    // }
  }

  @Test
  void isAvailable() {

    assertEquals(1,1);
  }
}
