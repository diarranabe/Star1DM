package com.diarranabe.star.star1dm;

/**
 * Created by diarranabe on 20/11/2017.
 */

public class Constants implements StarContract {
    public static final String CREATE_BUS_ROUTE_TABLE = "CREATE TABLE IF NOT EXISTS " + BusRoutes.CONTENT_PATH +
            "(" + BusRoutes.BusRouteColumns.ROUTE_ID + " TEXT NOT NULL PRIMARY KEY," +
            BusRoutes.BusRouteColumns.SHORT_NAME + " TEXT, " +
            BusRoutes.BusRouteColumns.LONG_NAME + " TEXT, " +
            BusRoutes.BusRouteColumns.DESCRIPTION + " TEXT, " +
            BusRoutes.BusRouteColumns.TYPE + " TEXT," +
            BusRoutes.BusRouteColumns.COLOR + " TEXT, " +
            BusRoutes.BusRouteColumns.TEXT_COLOR + " TEXT );";

    public static final String CREATE_TRIPS_TABLE = "CREATE TABLE IF NOT EXISTS " + Trips.CONTENT_PATH +
            "(" + Trips.TripColumns.TRIP_ID + " INTEGER  NOT NULL PRIMARY KEY," +
            Trips.TripColumns.ROUTE_ID + " INTEGER," +
            Trips.TripColumns.SERVICE_ID + " INTEGER," +
            Trips.TripColumns.HEADSIGN + " TEXT," +
            Trips.TripColumns.DIRECTION_ID + " INTEGER," +
            Trips.TripColumns.BLOCK_ID + " TEXT," +
            Trips.TripColumns.WHEELCHAIR_ACCESSIBLE + " TEXT );";

    public static final String CREATE_STOPS_TABLE = "CREATE TABLE IF NOT EXISTS " + Stops.CONTENT_PATH +
            "(" + Stops.StopColumns.STOP_ID + " TEXT NOT NULL PRIMARY KEY, " +
            Stops.StopColumns.NAME + " TEXT," +
            Stops.StopColumns.DESCRIPTION + " TEXT," +
            Stops.StopColumns.LATITUDE + " INTEGER, " +
            Stops.StopColumns.LONGITUDE + " INTEGER," +
            Stops.StopColumns.WHEELCHAIR_BOARDING + " TEXT );";

    public static final String CREATE_STOP_TIMES_TABLE = "CREATE TABLE IF NOT EXISTS " + StarContract.StopTimes.CONTENT_PATH +
            "(" + StarContract.StopTimes.StopTimeColumns.TRIP_ID + " INTEGER," +
            StarContract.StopTimes.StopTimeColumns.ARRIVAL_TIME + " TEXT," +
            StarContract.StopTimes.StopTimeColumns.DEPARTURE_TIME + " TEXT," +
            StarContract.StopTimes.StopTimeColumns.STOP_ID + " INTEGER," +
            StarContract.StopTimes.StopTimeColumns.STOP_SEQUENCE + " TEXT );";

    public static final String CREATE_CALENDAR_TABLE = "CREATE TABLE IF NOT EXISTS " + Calendar.CONTENT_PATH +
            "(" + Calendar.CalendarColumns.SERVICE_ID + " INTEGER," +
            Calendar.CalendarColumns.MONDAY + " TEXT," +
            Calendar.CalendarColumns.TUESDAY + " TEXT," +
            Calendar.CalendarColumns.WEDNESDAY + " TEXT," +
            Calendar.CalendarColumns.THURSDAY + " TEXT," +
            Calendar.CalendarColumns.FRIDAY + " TEXT," +
            Calendar.CalendarColumns.SATURDAY + " TEXT," +
            Calendar.CalendarColumns.SUNDAY + " TEXT," +
            Calendar.CalendarColumns.START_DATE + " TEXT," +
            Calendar.CalendarColumns.END_DATE + " TEXT" +
            " );";

    /**
     * La table qui stock les versions
     */
    public static final String VERSIONS_TABLE = "versions";
    public static final String VERSIONS_FILE_NAME_COL = "file_name";
    public static final String VERSIONS_FILE_VERSION_COL = "file_version";
    public static final String CREATE_VERSIONS_TABLE = "CREATE TABLE IF NOT EXISTS " + VERSIONS_TABLE +
            "( " + VERSIONS_FILE_NAME_COL + " TEXT , " +
            VERSIONS_FILE_VERSION_COL + " TEXT" +
            " );";

    /**
     * Version par defaut
     */
    public static final String DEFAULT_FIRST_VERSION = "0001";

    public static String DATA_SOURCE_URL = "https://data.explore.star.fr/api/records/1.0/search/?dataset=tco-busmetro-horaires-gtfs-versions-td&sort=-debutvalidite";
}
