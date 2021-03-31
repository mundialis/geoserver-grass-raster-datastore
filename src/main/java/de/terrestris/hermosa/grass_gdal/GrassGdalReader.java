package de.terrestris.hermosa.grass_gdal;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.osr.SpatialReference;
import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.grid.GeneralGridEnvelope;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.data.DataSourceException;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.util.factory.Hints;
import org.geotools.util.logging.Logging;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridEnvelope;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterValue;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.operation.MathTransform;
import org.sqlite.SQLiteConfig;

import javax.media.jai.RasterFactory;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.*;
import java.sql.*;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.terrestris.hermosa.grass_gdal.GrassGdalReader.GdalTypes.UInt16;
import static java.time.ZoneOffset.UTC;

/**
 * Coverage reader class to read coverages from gdal. This is actually GRASS agnostic, but currently supports only
 * rasters with one band and expects the values in little endian byte order.
 */
public class GrassGdalReader extends AbstractGridCoverage2DReader {

    private static final Logger LOGGER = Logging.getLogger(GrassGdalReader.class);

    enum GdalTypes {
        Byte,
        UInt16,
        UInt32,
        Float64,
        Float32
    }

    private static final Map<Integer, GdalTypes> GDAL_TYPES_MAP = new HashMap<>();

    private static final Map<Integer, Integer> DATABUFFER_TYPES_MAP = new HashMap<>();

    private static final Pattern CMD_REGEXP = Pattern.compile("maps=\"(.[^\"]*)\"");

    static {
        GDAL_TYPES_MAP.put(gdalconstConstants.GDT_Byte, GdalTypes.Byte);
        GDAL_TYPES_MAP.put(gdalconstConstants.GDT_UInt16, UInt16);
        GDAL_TYPES_MAP.put(gdalconstConstants.GDT_UInt32, GdalTypes.UInt32);
        GDAL_TYPES_MAP.put(gdalconstConstants.GDT_Float32, GdalTypes.Float32);
        GDAL_TYPES_MAP.put(gdalconstConstants.GDT_Float64, GdalTypes.Float64);
        DATABUFFER_TYPES_MAP.put(gdalconstConstants.GDT_Byte, DataBuffer.TYPE_BYTE);
        DATABUFFER_TYPES_MAP.put(gdalconstConstants.GDT_UInt16, DataBuffer.TYPE_SHORT);
        DATABUFFER_TYPES_MAP.put(gdalconstConstants.GDT_UInt32, DataBuffer.TYPE_INT);
        DATABUFFER_TYPES_MAP.put(gdalconstConstants.GDT_Float32, DataBuffer.TYPE_FLOAT);
        DATABUFFER_TYPES_MAP.put(gdalconstConstants.GDT_Float64, DataBuffer.TYPE_DOUBLE);
    }

    private int width;

    private int height;

    private final File file;

    private double resx;

    private double resy;

    private int numBands;

    private final Map<String, List<String>> rasters = new HashMap<>();

    private final Map<String, String> fileNames = new HashMap<>();

    private final Map<String, List<Instant>> times = new HashMap<>();

    /**
     * Construct a new GrassGdalReader without hints.
     *
     * @param o the file object
     * @throws DataSourceException if anything goes wrong
     * @throws FactoryException    if anything goes wrong
     */
    GrassGdalReader(Object o) throws DataSourceException, FactoryException {
        this(o, null);
    }

    /**
     * Construct a new GrassGdalReader.
     *
     * @param o     the file object
     * @param hints some hints
     * @throws DataSourceException if anything goes wrong
     * @throws FactoryException    if anything goes wrong
     */
    GrassGdalReader(Object o, Hints hints) throws DataSourceException, FactoryException {
        super(o, hints);
        file = (File) o;
        coverageFactory = new GridCoverageFactory();
        crs = CRS.decode("EPSG:32119");
        // instantiate the bounds based on the default CRS
        originalEnvelope = new GeneralEnvelope(CRS.getEnvelope(crs));
        originalEnvelope.setCoordinateReferenceSystem(crs);
        originalGridRange = new GeneralGridEnvelope(originalEnvelope, PixelInCell.CELL_CENTER);
        coverageName = file.getName();
        if (file.getName().endsWith(".db")) {
            initializeFromDB();
        } else {
            initialize(file);
        }
    }

    private void initializeFromDB() throws DataSourceException {
        String db = "jdbc:sqlite:" + file.getAbsolutePath();
        SQLiteConfig sqLiteConfig = new SQLiteConfig();
        Properties properties = sqLiteConfig.toProperties();
        properties.setProperty(SQLiteConfig.Pragma.DATE_STRING_FORMAT.pragmaName, "yyyy-MM-dd HH:mm:ss");
        try {
            String datasetSql = "select id, command from strds_metadata";
            String sql = "select id, name, mapset, temporal_type from raster_base";
            String absoluteSql = "select start_time, end_time from raster_absolute_time where id = ?";
            Connection conn = DriverManager.getConnection(db, properties);
            PreparedStatement stmt = conn.prepareStatement(datasetSql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String id = rs.getString("id");
                String cmd = rs.getString("command");
                Matcher matcher = CMD_REGEXP.matcher(cmd);
                if (matcher.find()) {
                    String[] files = matcher.group(1).split(",");
                    rasters.put(id, Arrays.asList(files));
                }
            }
            rs.close();
            stmt.close();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString("name");
                String mapset = rs.getString("mapset");
                String type = rs.getString("temporal_type");
                if (!type.equals("absolute")) {
                    continue;
                }
                File rasterFile = file.getParentFile().getParentFile().getParentFile();
                rasterFile = new File(rasterFile, mapset);
                rasterFile = new File(rasterFile, "cellhd");
                rasterFile = new File(rasterFile, name);
                fileNames.put(rs.getString("id"), rasterFile.getAbsolutePath());
            }
            rs.close();
            stmt.close();
            stmt = conn.prepareStatement(absoluteSql);
            for (String id : fileNames.keySet()) {
                stmt.setString(1, id);
                rs = stmt.executeQuery();
                if (rs.next()) {
                    Timestamp start = rs.getTimestamp("start_time");
                    Timestamp end = rs.getTimestamp("end_time");
                    Instant startTime = Instant.ofEpochMilli(start.getTime());
                    Instant endTime = Instant.ofEpochMilli(end.getTime());
                    List<Instant> list = new ArrayList<>();
                    list.add(startTime);
                    list.add(endTime);
                    times.put(id, list);
                }
                rs.close();
            }
            stmt.close();
            initialize(new File((String) fileNames.values().toArray()[0]));
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.log(Level.WARNING, "Unable to open sqlite db: " + e.getMessage());
            LOGGER.log(Level.FINE, "Stack trace:", e);
        }
    }

    private void initialize(File file) throws DataSourceException {
        synchronized (GrassGdalReader.class) {
            Dataset dataset = gdal.OpenShared(file.getAbsolutePath(), gdalconstConstants.GA_ReadOnly);
            if (dataset == null || !dataset.GetDriver().getShortName().equals("GRASS")) {
                throw new DataSourceException("The file is not a valid GRASS raster.");
            }
            try {
                width = dataset.getRasterXSize();
                height = dataset.getRasterYSize();
                String crsWkt;
                String projRef = dataset.GetProjectionRef();
                if (projRef != null) {
                    SpatialReference spatialReference = new SpatialReference(projRef);
                    crsWkt = spatialReference.ExportToPrettyWkt();
                    spatialReference.delete();
                    try {
                        crs = CRS.parseWKT(crsWkt);
                    } catch (FactoryException e) {
                        LOGGER.info("CRS WKT could not be parsed, ignoring.");
                        LOGGER.fine(e.getMessage() + ExceptionUtils.getStackTrace(e));
                    }
                }
                calculateEnvelope(dataset);
                numBands = dataset.getRasterCount();
            } finally {
                dataset.delete(); // this closes the dataset...
            }
        }
    }

    @Override
    public Format getFormat() {
        return new GrassGdalFormat();
    }

    @Override
    public GridCoverage2D read(String coverageName, GeneralParameterValue[] parameters) throws IllegalArgumentException, IOException {
        Dataset dataset = null;
        synchronized (GrassGdalReader.class) {
            File rasterFile = file;
            if (file.getName().endsWith(".db")) {
                rasterFile = new File(fileNames.values().toArray(new String[0])[0]);
            }
            try {
                int[] imageBounds = new int[]{0, 0, width, height};

                for (GeneralParameterValue value : parameters) {
                    if (value.getDescriptor().getName().getCode().equals("ReadGridGeometry2D")) {
                        GridGeometry2D geometry2D = ((ParameterValue<GridGeometry2D>) value).getValue();
                        GeneralEnvelope bbox = GeneralEnvelope.toGeneralEnvelope(geometry2D.getEnvelope2D());
                        imageBounds = calculateRequiredPixels(bbox);
                    }
                    if (value.getDescriptor().getName().getCode().equals("TIME")) {
                        List list = (List) ((ParameterValue)value).getValue();
                        Date date = (Date) list.get(0);
                        Instant time = Instant.ofEpochMilli(date.getTime());
                        for (Map.Entry<String, List<Instant>> item : times.entrySet()) {
                            if (!time.isAfter(item.getValue().get(1)) && !time.isBefore(item.getValue().get(0))) {
                                String id = item.getKey();
                                File candidate = new File(fileNames.get(id));
                                if (rasters.get(coverageName).contains(candidate.getName())) {
                                    rasterFile = candidate;
                                }
                            }
                        }
                    }
                }
                dataset = gdal.OpenShared(rasterFile.getAbsolutePath(), gdalconstConstants.GA_ReadOnly);
                Band band = dataset.GetRasterBand(1);
                int dataType = band.getDataType();
                Integer dataBufferType = DATABUFFER_TYPES_MAP.get(dataType);
                LOGGER.fine("Using gdal type " + GDAL_TYPES_MAP.get(dataType));
                LOGGER.fine("Using data buffer type " + dataBufferType);
                WritableRaster raster = RasterFactory
                    .createBandedRaster(dataBufferType, imageBounds[2], imageBounds[3], numBands, null);

                for (int i = 0; i < numBands; ++i) {
                    copyBand(dataset.GetRasterBand(i + 1), i, imageBounds, raster);
                }

                final GridCoverageFactory factory = CoverageFactoryFinder.getGridCoverageFactory(null);

                return factory.create(file.getName(), raster, calculateSubEnvelope(imageBounds));
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Unable to create GRASS coverage. Original exception:", e);
                throw e;
            } finally {
                if (dataset != null) {
                    dataset.delete();
                }
            }
        }
    }

    @Override
    public GridCoverage2D read(GeneralParameterValue[] parameters) throws IllegalArgumentException, IOException {
        return read(coverageName, parameters);
    }

    private void copyBand(Band band, int bandIndex, int[] imageBounds, WritableRaster raster) {
        int dataType = band.getDataType();
        ByteBuffer byteBuffer = band
            .ReadRaster_Direct(imageBounds[0], imageBounds[1], imageBounds[2], imageBounds[3], dataType);
        if (byteBuffer == null) {
            return;
        }
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        switch (GDAL_TYPES_MAP.get(dataType)) {
            case Byte: {
                byte[] bytes = new byte[imageBounds[2] * imageBounds[3]];
                byteBuffer.get(bytes);
                int[] ints = new int[bytes.length];
                for (int i = 0; i < ints.length; ++i) {
                    ints[i] = Short.toUnsignedInt(bytes[i]);
                }
                raster.setSamples(0, 0, imageBounds[2], imageBounds[3], bandIndex, ints);
                break;
            }
            case UInt16: {
                ShortBuffer buffer = byteBuffer.asShortBuffer();
                short[] shorts = new short[imageBounds[2] * imageBounds[3]];
                buffer.get(shorts);
                int[] ints = new int[shorts.length];
                for (int i = 0; i < ints.length; ++i) {
                    ints[i] = Short.toUnsignedInt(shorts[i]);
                }
                raster.setSamples(0, 0, imageBounds[2], imageBounds[3], bandIndex, ints);
                break;
            }
            case UInt32: {
                IntBuffer buffer = byteBuffer.asIntBuffer();
                int[] ints = new int[imageBounds[2] * imageBounds[3]];
                buffer.get(ints);
                raster.setSamples(0, 0, imageBounds[2], imageBounds[3], bandIndex, ints);
                break;
            }
            case Float64: {
                DoubleBuffer buffer = byteBuffer.asDoubleBuffer();
                double[] doubles = new double[imageBounds[2] * imageBounds[3]];
                buffer.get(doubles);
                raster.setSamples(0, 0, imageBounds[2], imageBounds[3], bandIndex, doubles);
                break;
            }
            case Float32:
                FloatBuffer buffer = byteBuffer.asFloatBuffer();
                float[] floats = new float[imageBounds[2] * imageBounds[3]];
                buffer.get(floats);
                raster.setSamples(0, 0, imageBounds[2], imageBounds[3], bandIndex, floats);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + GDAL_TYPES_MAP.get(dataType));
        }
    }

    private int[] calculateRequiredPixels(GeneralEnvelope bbox) {
        double origMinX = originalEnvelope.getMinimum(0);
        double minx = Math.max(bbox.getMinimum(0), origMinX);
        double origMinY = originalEnvelope.getMinimum(1);
        double miny = Math.max(bbox.getMinimum(1), origMinY);
        double origMaxX = originalEnvelope.getMaximum(0);
        double maxx = Math.min(bbox.getMaximum(0), origMaxX);
        double origMaxY = originalEnvelope.getMaximum(1);
        double maxy = Math.min(bbox.getMaximum(1), origMaxY);
        double diff = minx - origMinX;
        double x = diff / resx;
        diff = Math.abs(maxy - origMaxY);
        double y = diff / Math.abs(resy);
        diff = maxx - minx;
        double width = diff / resx;
        diff = maxy - miny;
        double height = diff / Math.abs(resy);
        // make sure to at least have a 1 pixel box to avoid crashes later on
        int targetWidth = Math.max((int) Math.ceil(width), 1);
        int targetHeight = Math.max((int) Math.ceil(height), 1);
        return new int[]{(int) Math.floor(x), (int) Math.floor(y), targetWidth, targetHeight};
    }

    private GeneralEnvelope calculateSubEnvelope(int[] imageCoordinates) {
        double minx = originalEnvelope.getMinimum(0) + imageCoordinates[0] * resx;
        double maxx = minx + imageCoordinates[2] * resx;
        double maxy = originalEnvelope.getMaximum(1) - Math.abs(imageCoordinates[1] * resy);
        double miny = maxy - Math.abs(imageCoordinates[3] * resy);

        GeneralEnvelope generalEnvelope = new GeneralEnvelope(new double[]{minx, miny}, new double[]{maxx, maxy});
        generalEnvelope.setCoordinateReferenceSystem(crs);
        return generalEnvelope;
    }

    private void calculateEnvelope(Dataset dataset) {
        double[] transform = new double[6];
        dataset.GetGeoTransform(transform);
        double maxx = transform[0] + transform[1] * width + transform[2] * height;
        double miny = transform[3] + transform[5] * height + transform[4] * width;
        double minx = transform[0];
        double maxy = transform[3];
        resx = transform[1];
        resy = transform[5];

        originalEnvelope = new GeneralEnvelope(new double[]{minx, miny}, new double[]{maxx, maxy});
        originalEnvelope.setCoordinateReferenceSystem(crs);
        originalGridRange = new GeneralGridEnvelope(originalEnvelope, PixelInCell.CELL_CENTER);
    }

    @Override
    public String[] getMetadataNames(String coverageName) {
        return new String[]{
            GridCoverage2DReader.HAS_TIME_DOMAIN,
            GridCoverage2DReader.TIME_DOMAIN
        };
    }

    @Override
    public String getMetadataValue(String coverageName, String name) {
        if (name.equals(HAS_TIME_DOMAIN)) {
            return "true";
        }
        if (name.equals(TIME_DOMAIN)) {
            DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
                .withZone(UTC);
            Instant min = null;
            Instant max = null;
            for (List<Instant> list : times.values()) {
                if (min == null) {
                    min = list.get(0);
                }
                if (max == null) {
                    max = list.get(1);
                }
                if (min.isAfter(list.get(0))) {
                    min = list.get(0);
                }
                if (max.isBefore(list.get(1))) {
                    max = list.get(1);
                }
            }
            return formatter.format(min) + "/" + formatter.format(max);
        }
        return null;
    }

    @Override
    public Set<ParameterDescriptor<List>> getDynamicParameters(String coverageName) {
        return new HashSet<>();
    }

    @Override
    public String[] getGridCoverageNames() {
        if (!rasters.isEmpty()) {
            return rasters.keySet().toArray(new String[0]);
        }
        return super.getGridCoverageNames();
    }

    @Override
    public GeneralEnvelope getOriginalEnvelope(String coverageName) {
        return super.getOriginalEnvelope(this.coverageName);
    }

    @Override
    public GridEnvelope getOriginalGridRange(String coverageName) {
        return super.getOriginalGridRange(this.coverageName);
    }

    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem(String coverageName) {
        return super.getCoordinateReferenceSystem(this.coverageName);
    }

    @Override
    public MathTransform getOriginalGridToWorld(String coverageName, PixelInCell pixInCell) {
        return super.getOriginalGridToWorld(this.coverageName, pixInCell);
    }

}
