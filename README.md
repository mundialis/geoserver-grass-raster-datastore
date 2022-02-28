## GeoServer GRASS raster datastore

Prerequisites: Java 11 and maven 3.5

* build with `mvn install`
* copy into Geoserver's `WEB-INF/lib` to enable

## Download

You can also download released versions from [here](https://nexus.terrestris.de/#browse/browse:public:de%2Fterrestris%2Fgeoserver-grass-raster-datastore).

## Usage

You can create datastore pointing to a single raster by pointing the file to an appropriate entry in the GRASS `cellhd`
directory. If you have a timeseries raster dataset, you can also point the file to the sqlite database containing the
timeseries information (found in `tgis/sqlite.db` inside the mapset containing the timeseries).

In case of a timeseries raster dataset you may get multiple layers in case you have multiple timeseries stored in the
database. When publishing a layer, make sure to enable WMS-TIME-support by checking the box in the dimensions tab.
