# GeoServer GRASS raster datastore

Combining the C- and Java-tribe FOSS4G greatness of [GRASS](https://grass.osgeo.org/) and
[GeoServer](http://geoserver.org/).

[![GRASS loves GeoServer!](https://mundialis.github.io/geoserver-grass-raster-datastore/images/grass-heart-geoserver.svg)](https://mundialis.github.io/geoserver-grass-raster-datastore)

[ℹ️ Homepage](https://mundialis.github.io/geoserver-grass-raster-datastore/)

## Prerequisites

You'll need Java 11 and maven 3.5

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

For more usage instructions, have a [look at the homepage](https://mundialis.github.io/geoserver-grass-raster-datastore/).

## Acknowledgements

This work has been co-financed under Grant Agreement Connecting Europe Facility (CEF) Telecom project 2018-EU-IA-0095
by the European Union (https://ec.europa.eu/inea/en/connecting-europe-facility/cef-telecom/2018-eu-ia-0095).

This work has been partly developed as joint contribution of mundialis and terrestris to the
[mFUND](https://www.bmvi.de/SharedDocs/DE/Artikel/DG/mfund-projekte/fair.html) project
[Anwenderfreundliche Bereitstellung von Klima- und Wetterdaten – FAIR](https://www.fair-opendata.de/).
