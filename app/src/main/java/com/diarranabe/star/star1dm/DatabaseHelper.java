package com.diarranabe.star.star1dm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import tables.BusRoute;
import tables.Stop;
import tables.StopTime;

import static android.os.Environment.getExternalStorageDirectory;


/**
 * Created by diarranabe on 20/11/2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper implements StarContract {

    private static File DEVICE_ROOT_FOLDER = getExternalStorageDirectory();
    public SQLiteDatabase database;
    private static String DATA_NAME = "starBus";
    private static final int DATA_BASE_VERSION = 1;

    /**
     * Paths used to load csv files
     */
    public static String INIT_FOLDER_PATH = "star1dm/"; // From the root folder of the device
    private static String CALENDAR_CSV_FILE = "calendar.txt";
    private static String BUS_ROUTES_CSV_FILE = "routes.txt";
    private static String STOPS_CSV_FILE = "stops.txt";
    private static String STOP_TIMES_CSV_FILE = "stop_times.txt";
    private static String TRIPS_CSV_FILE = "trips.txt";

    public DatabaseHelper(Context context) {
        super(context, DATA_NAME, null, DATA_BASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Constants.CREATE_BUS_ROUTE_TABLE);
        db.execSQL(Constants.CREATE_CALENDAR_TABLE);
        db.execSQL(Constants.CREATE_STOP_TIMES_TABLE);
        db.execSQL(Constants.CREATE_STOPS_TABLE);
        db.execSQL(Constants.CREATE_TRIPS_TABLE);
        Log.d("STARX", "db cretaed");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + BusRoutes.CONTENT_PATH);
        db.execSQL("DROP TABLE IF EXISTS " + Trips.CONTENT_PATH);
        db.execSQL("DROP TABLE IF EXISTS " + Stops.CONTENT_PATH);
        db.execSQL("DROP TABLE IF EXISTS " + StopTimes.CONTENT_PATH);
        db.execSQL("DROP TABLE IF EXISTS " + Calendar.CONTENT_PATH);
        onCreate(db);
    }

    public List<String> allTableNames() {
        List<String> result = new ArrayList<String>();
        String selectQuery = "select name from sqlite_master where type = 'table'";
        Cursor cursor = this.getReadableDatabase().rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndex("name"));
                result.add(name);
            } while (cursor.moveToNext());
        }
        return result;
    }

    /**
     * Insert a BusRoute in the db
     *
     * @param route
     */
    public void insertRoute(BusRoute route) {
        database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BusRoutes.BusRouteColumns.ROUTE_ID, route.getRoute_id());
        values.put(BusRoutes.BusRouteColumns.SHORT_NAME, route.getShortName());
        values.put(BusRoutes.BusRouteColumns.LONG_NAME, route.getLongName());
        values.put(BusRoutes.BusRouteColumns.DESCRIPTION, route.getDescription());
        values.put(BusRoutes.BusRouteColumns.TYPE, route.getType());
        values.put(BusRoutes.BusRouteColumns.COLOR, route.getColor());
        values.put(BusRoutes.BusRouteColumns.TEXT_COLOR, route.getTextColor());
        database.insert(BusRoutes.CONTENT_PATH, null, values);
    }

    /**
     * Insert all BusRoute from the csv file to the db
     */
    public void insertBusRoutes() {
        ArrayList<tables.BusRoute> items = new ArrayList<tables.BusRoute>();
        try {
            items = loadBusRoutesData();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (tables.BusRoute item : items) {
            Log.d("STARX", "Inserting ...." + item.toString());
            insertRoute(item);
        }
    }

    public void insertTrip(tables.Trips trip) {
        database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Trips.TripColumns.BLOCK_ID, trip.getBlockId());
        values.put(Trips.TripColumns.SERVICE_ID, trip.getServiceId());
        values.put(Trips.TripColumns.HEADSIGN, trip.getHeadSign());
        values.put(Trips.TripColumns.DIRECTION_ID, trip.getDirectionId());
        values.put(Trips.TripColumns.BLOCK_ID, trip.getBlockId());
        values.put(Trips.TripColumns.WHEELCHAIR_ACCESSIBLE, trip.getWheelchairAccessible());
        database.insert(Trips.CONTENT_PATH, null, values);
    }

    /**
     * Insert all Trips from the csv file to the db
     */
    public void insertTrips() {
        ArrayList<tables.Trips> items = new ArrayList<tables.Trips>();
        try {
            items = loadTripsData();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (tables.Trips item : items) {
            Log.d("STARX", "Inserting ...." + item.toString());
            insertTrip(item);
        }
    }

    /**
     * Insert a Stop in the db
     *
     * @param stop
     */
    public void insertStop(Stop stop) {
        database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Stops.StopColumns.STOP_ID, stop.getId());
        values.put(Stops.StopColumns.NAME, stop.getName());
        values.put(Stops.StopColumns.DESCRIPTION, stop.getDescription());
        values.put(Stops.StopColumns.LATITUDE, stop.getLatitude());
        values.put(Stops.StopColumns.LONGITUDE, stop.getLongitude());
        values.put(Stops.StopColumns.WHEELCHAIR_BOARDING, stop.getWheelChairBoalding());
        database.insert(Stops.CONTENT_PATH, null, values);
        Log.e("XXXX", "insertion OK");
    }

    /**
     * Insert all Stops from the csv file to the db
     */
    public void insertStops() {
        ArrayList<tables.Stop> items = new ArrayList<tables.Stop>();
        try {
            items = loadStopsData();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (tables.Stop item : items) {
            Log.d("STARX", "Inserting ...." + item.toString());
            insertStop(item);
        }
    }

    /**
     * Insert a StopTime in the db
     *
     * @param stopTime
     */
    public void insertStopTime(StopTime stopTime) {
        database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(StopTimes.StopTimeColumns.TRIP_ID, stopTime.getTripId());
        values.put(StopTimes.StopTimeColumns.ARRIVAL_TIME, stopTime.getArrivalTime());
        values.put(StopTimes.StopTimeColumns.DEPARTURE_TIME, stopTime.getDepartureTme());
        values.put(StopTimes.StopTimeColumns.STOP_ID, stopTime.getStopId());
        values.put(StopTimes.StopTimeColumns.STOP_SEQUENCE, stopTime.getStopSequence());
        database.insert(StopTimes.CONTENT_PATH, null, values);
    }

    /**
     * Insert all StopTimes from the csv file to the db
     */
    public void insertStopTimes() {
        ArrayList<tables.StopTime> items = new ArrayList<tables.StopTime>();
        try {
            items = loadStopTimesData();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (StopTime item : items) {
            Log.d("STARX", "Inserting ...." + item.toString());
            insertStopTime(item);
        }
    }

    /**
     * Insert a Calendar in the db
     *
     * @param calendar
     */
    public void insertCalendar(tables.Calendar calendar) {
        database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Calendar.CalendarColumns.MONDAY, calendar.getMonday());
        values.put(Calendar.CalendarColumns.TUESDAY, calendar.getTuesday());
        values.put(Calendar.CalendarColumns.WEDNESDAY, calendar.getWednesday());
        values.put(Calendar.CalendarColumns.THURSDAY, calendar.getThursday());
        values.put(Calendar.CalendarColumns.FRIDAY, calendar.getFriday());
        values.put(Calendar.CalendarColumns.SATURDAY, calendar.getSaturday());
        values.put(Calendar.CalendarColumns.SUNDAY, calendar.getSunday());
        values.put(Calendar.CalendarColumns.START_DATE, calendar.getStartDate());
        values.put(Calendar.CalendarColumns.END_DATE, calendar.getEndDate());
        database.insert(Calendar.CONTENT_PATH, null, values);
    }

    /**
     * Insert all Calendars from the csv file to the db
     */
    public void insertCalendars() {
        ArrayList<tables.Calendar> items = new ArrayList<tables.Calendar>();
        try {
            items = loadCalendarData();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (tables.Calendar item : items) {
            Log.d("STARX", "Inserting ...." + item.toString());
            insertCalendar(item);
        }
    }

    /**
     * Loads Calendars from the csv file
     *
     * @return
     * @throws IOException
     */
    public static ArrayList<tables.Calendar> loadCalendarData() throws IOException {
        Log.d("STARXC", "start loading... " + DEVICE_ROOT_FOLDER+"/"+INIT_FOLDER_PATH+CALENDAR_CSV_FILE);
        ArrayList<tables.Calendar> calendars = new ArrayList<>();
        FileReader file = new FileReader(new File(DEVICE_ROOT_FOLDER, INIT_FOLDER_PATH + CALENDAR_CSV_FILE));
        BufferedReader buffer = new BufferedReader(file);
        String line = "";
        int i = 0;
        while ((line = buffer.readLine()) != null) {
            if (i != 0) {
                String[] str = line.split(",");
                tables.Calendar calendar = new tables.Calendar(
                        str[0].replaceAll("\"", ""),
                        str[1].replaceAll("\"", ""),
                        str[2].replaceAll("\"", ""),
                        str[3].replaceAll("\"", ""),
                        str[4].replaceAll("\"", ""),
                        str[5].replaceAll("\"", ""),
                        str[6].replaceAll("\"", ""),
                        str[7].replaceAll("\"", ""),
                        str[8].replaceAll("\"", ""));
                calendars.add(calendar);
                Log.d("STARXC", "loaded... " + calendar.toString());
            }
            i++;
        }
        Log.d("STARXC", i + " calendars loaded");
        return calendars;
    }

    /**
     * Loads BusRoutes from the csv file
     *
     * @return
     * @throws IOException
     */
    public static ArrayList<tables.BusRoute> loadBusRoutesData() throws IOException {
        Log.d("STARXC", "start loading... " + DEVICE_ROOT_FOLDER+"/"+INIT_FOLDER_PATH+BUS_ROUTES_CSV_FILE);
        ArrayList<tables.BusRoute> busRoutes = new ArrayList<>();
        FileReader file = new FileReader(new File(DEVICE_ROOT_FOLDER, INIT_FOLDER_PATH + BUS_ROUTES_CSV_FILE));
        BufferedReader buffer = new BufferedReader(file);
        String line = "";
        int i = 0;
        while ((line = buffer.readLine()) != null) {
            if (i != 0) {
                String[] str = line.split(",");
                tables.BusRoute br = new tables.BusRoute(
                        str[0].replaceAll("\"", ""),
                        str[2].replaceAll("\"", ""),
                        str[3].replaceAll("\"", ""),
                        str[4].replaceAll("\"", ""),
                        str[5].replaceAll("\"", ""),
                        str[7].replaceAll("\"", ""),
                        str[8].replaceAll("\"", ""));
                busRoutes.add(br);
                Log.d("STARXBR", "loaded... " + br.toString());
            }
            i++;
        }
        Log.d("STARXBR", i + " busRoutes loaded");
        return busRoutes;
    }

    /**
     * Loads Stops from the csv file
     *
     * @return
     * @throws IOException
     */
    public static ArrayList<tables.Stop> loadStopsData() throws IOException {
        Log.d("STARXC", "start loading... " + DEVICE_ROOT_FOLDER+"/"+INIT_FOLDER_PATH+STOPS_CSV_FILE);
        ArrayList<tables.Stop> stops = new ArrayList<>();
        FileReader file = new FileReader(new File(DEVICE_ROOT_FOLDER, INIT_FOLDER_PATH + STOPS_CSV_FILE));
        Log.e("STARXS", " Absolut Path for file" + file.toString());
        BufferedReader buffer = new BufferedReader(file);
        String line = "";
        int i = 0;
        while ((line = buffer.readLine()) != null) {
            if (i != 0) {
                String[] str = line.split(",");
                String id = str[0].replaceAll("\"", "");
                String name = str[2].replaceAll("\"", "");
                String description = str[3].replaceAll("\"", "");
                float latitude = Float.valueOf(str[4].replace('"', ' '));
                float longitude = Float.valueOf(str[5].replace('"', ' '));
                String wheelChairBoalding = str[11].replaceAll("\"", "");
                tables.Stop stop = new tables.Stop(id,name, description, latitude, longitude, wheelChairBoalding);
                stops.add(stop);
                Log.d("STARXS", "loaded... " + stop.toString());
            }
            i++;
        }
        Log.d("STARXS", i + " stops loaded");
        return stops;
    }

    /**
     * Loads StopTimes from the csv file
     *
     * @return
     * @throws IOException
     */
    public static ArrayList<StopTime> loadStopTimesData() throws IOException {
        Log.d("STARXC", "start loading... " + DEVICE_ROOT_FOLDER+"/"+INIT_FOLDER_PATH+STOP_TIMES_CSV_FILE);
        ArrayList<StopTime> stopTimes = new ArrayList<>();
        FileReader file = new FileReader(new File(DEVICE_ROOT_FOLDER, INIT_FOLDER_PATH + STOP_TIMES_CSV_FILE));
        BufferedReader buffer = new BufferedReader(file);
        String line = "";
        int i = 0;
        while ((line = buffer.readLine()) != null) {
            if (i != 0) {
                String[] str = line.split(",");
                int tripId = Integer.valueOf(str[0].replaceAll("\"", ""));
                int stopId = Integer.valueOf(str[3].replaceAll("\"", ""));
                StopTime stopTime = new StopTime(
                        tripId, str[1].replaceAll("\"", ""),
                        str[2].replaceAll("\"", ""),
                        stopId,
                        str[4].replaceAll("\"", ""));
                stopTimes.add(stopTime);
                Log.d("STARXST", "loaded... " + stopTime.toString());
            }
            i++;
        }
        Log.d("STARXST", i + " stopTimes loaded");
        return stopTimes;
    }

    /**
     * Loads Trips from the csv file
     *
     * @return
     * @throws IOException
     */
    public static ArrayList<tables.Trips> loadTripsData() throws IOException {
        Log.d("STARXC", "start loading... " + DEVICE_ROOT_FOLDER+"/"+INIT_FOLDER_PATH+ TRIPS_CSV_FILE);
        ArrayList<tables.Trips> trips = new ArrayList<>();
        FileReader file = new FileReader(new File(DEVICE_ROOT_FOLDER, INIT_FOLDER_PATH + TRIPS_CSV_FILE));
        BufferedReader buffer = new BufferedReader(file);
        String line = "";
        int i = 0;
        while ((line = buffer.readLine()) != null) {
            if (i != 0) {
                String[] str = line.split(",");
                int routeId = Integer.valueOf(str[0].replaceAll("\"", ""));
                int serviceId = Integer.valueOf(str[1].replaceAll("\"", ""));
                int direction = Integer.valueOf(str[5].replaceAll("\"", ""));
                String block = str[6].replaceAll("\"", "");
                tables.Trips trip = new tables.Trips(routeId,
                        serviceId,
                        str[3].replaceAll("\"", ""),
                        direction,
                        block,
                        str[8].replaceAll("\"", ""));
                trips.add(trip);
                Log.d("STARXT", "loaded... " + trip.toString());
            }
            i++;
        }
        Log.d("STARXT", i + " trips loaded");
        return trips;
    }

    /**
     * Load BusRoutes from database
     * @return
     */
    public ArrayList<tables.BusRoute> getBusRoutesFromDatabase() {
        String selectQuery = "SELECT  * FROM " + StarContract.BusRoutes.CONTENT_PATH;
        ArrayList<tables.BusRoute> data = new ArrayList<>();
        Cursor cursor = this.getWritableDatabase().rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                tables.BusRoute item = new tables.BusRoute(
                        cursor.getString(cursor.getColumnIndex(BusRoutes.BusRouteColumns.ROUTE_ID)),
                        cursor.getString(cursor.getColumnIndex(StarContract.BusRoutes.BusRouteColumns.SHORT_NAME)),
                        cursor.getString(cursor.getColumnIndex(StarContract.BusRoutes.BusRouteColumns.LONG_NAME)),
                        cursor.getString(cursor.getColumnIndex(StarContract.BusRoutes.BusRouteColumns.DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndex(StarContract.BusRoutes.BusRouteColumns.TYPE)),
                        cursor.getString(cursor.getColumnIndex(StarContract.BusRoutes.BusRouteColumns.COLOR)),
                        cursor.getString(cursor.getColumnIndex(StarContract.BusRoutes.BusRouteColumns.TEXT_COLOR))
                );
                data.add(item);
                Log.d("STARX", "load from db..." + item);
            } while (cursor.moveToNext());
            Log.d("STARX", "-----   " + data.size() + " BusRoutes loaded form database ");
        }
        cursor.close();
        return data;
    }

    /**
     * Load Calendars from database
     * @return
     */
    public ArrayList<tables.Calendar> getCalendarsFromDatabase() {
        String selectQuery = "SELECT  * FROM " + StarContract.Calendar.CONTENT_PATH;
        ArrayList<tables.Calendar> data = new ArrayList<>();
        Cursor cursor = this.getWritableDatabase().rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                tables.Calendar item = new tables.Calendar(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getString(8)
                );
                data.add(item);
                Log.d("STARX", "load from db..." + item);
            } while (cursor.moveToNext());
            Log.d("STARX", "-----   " + data.size() + " calendars loaded form database");
        }
        cursor.close();
        return data;
    }

    /**
     * Load Stops from database
     * @return
     */
    public ArrayList<tables.Stop> getStopsFromDatabase() {
        String selectQuery = "SELECT  * FROM " + StarContract.Stops.CONTENT_PATH;
        ArrayList<tables.Stop> data = new ArrayList<>();
        Cursor cursor = this.getWritableDatabase().rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                tables.Stop item = new tables.Stop(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getFloat(3),
                        cursor.getFloat(4),
                        cursor.getString(5)
                );
                data.add(item);
                Log.d("STARX", "load from database..." + item);
            } while (cursor.moveToNext());
            Log.d("STARX", "-----   " + data.size() + " Stops loaded form database ");
        }
        cursor.close();
        return data;
    }

    /**
     * Load StopTimes from database
     * @return
     */
    public ArrayList<tables.StopTime> getStopTimesFromDatabase() {
        String selectQuery = "SELECT  * FROM " + StarContract.StopTimes.CONTENT_PATH;
        ArrayList<tables.StopTime> data = new ArrayList<>();
        Cursor cursor = this.getWritableDatabase().rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                tables.StopTime item = new tables.StopTime(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getInt(3),
                        cursor.getString(4)
                );
                data.add(item);
                Log.d("STARX", "load from database..." + item);
            } while (cursor.moveToNext());
            Log.d("STARX", "-----   " + data.size() + " StopTimes loaded form database");
        }
        cursor.close();
        return data;
    }

    /**
     * Load Trips from database
     * @return
     */
    public ArrayList<tables.Trips> getTripsFromDatabase() {
        Log.d("STARX", "loading trips from database..." );
        String selectQuery = "SELECT  * FROM " + StarContract.Trips.CONTENT_PATH;
        ArrayList<tables.Trips> data = new ArrayList<>();
        Cursor cursor = this.getWritableDatabase().rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                tables.Trips item = new tables.Trips(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getString(2),
                        cursor.getInt(3),
                        cursor.getString(4),
                        cursor.getString(5)
                );
                data.add(item);
                Log.d("STARX", "load from database..." + item);
            } while (cursor.moveToNext());
            Log.d("STARX", "-----   " + data.size() + " Trips loaded form database ");
        }
        cursor.close();
        return data;
    }
}