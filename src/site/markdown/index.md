# GeoServer GRASS raster datastore

Combining the C- and Java-tribe FOSS4G greatness of [GRASS](https://grass.osgeo.org/) and
[GeoServer](http://geoserver.org/).

![GRASS loves GeoServer!](../resources/images/grass-heart-geoserver.svg)

_________________

## About

The [GeoServer GRASS raster datastore](https://github.com/mundialis/geoserver-grass-raster-datastore) makes it possible
to use GRASS raster datastores as sources from within a GeoServer instance. This way it becomes very easy to publish
GRASS data as web services through GeoServer.

The GeoServer GRASS raster datastore -- what a name, suggestions for something more catchy welcome -- was initially
developed by the people from [mundialis](https://www.mundialis.de/) and [terrestris](https://www.terrestris.de/).

## Building from source

Prerequisites: Java 11 and maven 3.5

* build with `mvn install`
* copy into GeoServer's `WEB-INF/lib` to enable

## Download

You can also download released versions from the [terrestris nexus server](https://nexus.terrestris.de/#browse/browse:public:de%2Fterrestris%2Fgeoserver-grass-raster-datastore).

## Usage

In GRASS GIS, data are stored in a simple hierarchical directory structure consisting of GRASS "database"
(directory with projects), "location(s)" (projects) and "mapset(s)" (subprojects). A "location" is defined by its
coordinate reference system (CRS). Each "location" can have many "mapsets" for managing different
aspects of a project or project's subregions. When creating a new Location, GRASS GIS automatically
creates a special Mapset called PERMANENT where the core data for the project can be stored.

To access data, specific map files have to be specified:
- Raster data: you can create a datastore pointing to a single raster map by pointing the file to the map name in the GRASS mapset `cellhd` subdirectory.
- Raster time series: In this case, besides accessing the raster maps directly, you can also point the file to the SQLite
database containing the time series information (found in `tgis/sqlite.db` inside the mapset containing the timeseries).
Note: In case of a raster time series dataset you may get multiple layers in case you have multiple timeseries stored in
the database. When publishing a layer, make sure to enable WMS-TIME-support by checking the box in the dimensions tab.

## Contact

Please make sure to get in [contact with us](https://www.mundialis.de/en/contact/) if you have feedback about this
project or if you want to contribute. We're looking forward to hearing from you!

## Acknowledgements

This work has been co-financed under Grant Agreement Connecting Europe Facility (CEF) Telecom project 2018-EU-IA-0095
by the European Union (https://ec.europa.eu/inea/en/connecting-europe-facility/cef-telecom/2018-eu-ia-0095).

This work has been partly developed as joint contribution of mundialis and terrestris to the
[mFUND](https://www.bmvi.de/SharedDocs/DE/Artikel/DG/mfund-projekte/fair.html) project
[Anwenderfreundliche Bereitstellung von Klima- und Wetterdaten – FAIR](https://www.fair-opendata.de/).
