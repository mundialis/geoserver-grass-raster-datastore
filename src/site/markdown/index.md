# GeoServer GRASS raster datastore

Combining the C- and Java-tribe FOSS4G greatness of [GRASS](https://grass.osgeo.org/) and
[GeoServer](http://geoserver.org/).

![GRASS loves GeoServer!](./images/grass-heart-geoserver.svg)

_________________

## About

The [GRASS raster datastore](https://github.com/mundialis/geoserver-grass-raster-datastore) makes it possible to use
GRASS raster datastores as sources from within a GeoServer instance. This way it becomes very easy to publish GRASS
data as web services through GeoServer.

The GeoServer GRASS raster datastore -- what a name, suggestions for something more catchy welcome -- was initially
developed by the people from [mundialis](https://www.mundialis.de/) and [terrestris](https://www.terrestris.de/).

## Building from source

Prerequisites: Java 11 and maven 3.5

* build with `mvn install`
* copy into Geoserver's `WEB-INF/lib` to enable

## Download

You can also download released versions from the [terrestris nexus server](https://nexus.terrestris.de/#browse/browse:public:de%2Fterrestris%2Fgeoserver-grass-raster-datastore).

## Usage

You can create datastore pointing to a single raster by pointing the file to an appropriate entry in the GRASS `cellhd`
directory. If you have a timeseries raster dataset, you can also point the file to the sqlite database containing the
timeseries information (found in `tgis/sqlite.db` inside the mapset containing the timeseries).

In case of a timeseries raster dataset you may get multiple layers in case you have multiple timeseries stored in the
database. When publishing a layer, make sure to enable WMS-TIME-support by checking the box in the dimensions tab.

## Contact

Please make sure to get in [contact with us](https://www.mundialis.de/en/contact/) if you have feedback about this
project or if want to contribute. We're looking forward to hearing from you!





