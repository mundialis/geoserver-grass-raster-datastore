/*
 * Copyright 2023-present terrestris GmbH & Co. KG
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

import de.terrestris.utils.io.ZipUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

public class GrassGdalReaderTest {

  public static final GenericContainer geoserver = new GenericContainer(DockerImageName.parse("terrestris/geoserver:2.24.0"))
    .withExposedPorts(8080)
    .withFileSystemBind("target", "/opt/additional_libs", BindMode.READ_ONLY)
    .withFileSystemBind("target/geoserver_data", "/opt/geoserver_data", BindMode.READ_WRITE)
    .waitingFor(Wait.forHttp("/geoserver"));

  @BeforeAll
  public static void init() throws IOException {
    URL url = new URL("https://nexus.terrestris.de/repository/raw-public/geoserver-grass-raster-datastore%2Fdatadir.zip%2Fdatadir.zip");
    File file = Files.createTempFile("test", "grass").toFile();
    IOUtils.copy(url, file);
    IOUtils.copy(new URL("https://repo1.maven.org/maven2/org/gdal/gdal/3.4.0/gdal-3.4.0.jar"), new File("target/gdal.jar"));
    File target = new File("target");
    ZipUtils.zip(new File(target, "grass.jar"), new File("target/classes"), true);
    ZipUtils.unzip(file, target);
    file.delete();
    geoserver.start();
  }

  @Test
  public void testGrassGdalReader() throws IOException, InterruptedException {
      String request = "/geoserver/test/wms?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&FORMAT=image%2Fjpeg&TRANSPARENT=true&STYLES&LAYERS=test%3Atest&exceptions=application%2Fvnd.ogc.se_xml&SRS=EPSG%3A25832&WIDTH=768&HEIGHT=768&BBOX=0.03728098677702735%2C0.05592148016554099%2C0.9320246694256837%2C0.9506651628141973";
      String url = String.format("http://%s:%s", geoserver.getHost(), geoserver.getFirstMappedPort()) + request;
      BufferedImage image = ImageIO.read(new URL(url));
      Assertions.assertNotNull(image);
  }

}
