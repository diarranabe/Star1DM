package com.diarranabe.star.star1dm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
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

public class DatabaseHelper extends SQLiteOpenHelper implements StarContract{

    public SQLiteDatabase database;
    private static  String DATA_NAME ="starBus";
    private static final int DATA_BASE_VERSION = 1;

    /**
     * Paths used to load csv files
     */
    private static final String INIT_FOLDER_PATH = "star1dm/"; // From the root folder of the device
    private static  String CALENDAR_CSV_FILE ="calendar.txt";
    private static  String BUS_ROUTES_CSV_FILE ="routes.txt";
    private static  String STOPS_CSV_FILE ="stops.txt";
    private static  String STOP_TIMES_CSV_FILE ="stop_times.txt";
    private static  String TRIPS_CSV_FILE ="trips.txt";


    public DatabaseHelper(Context context) {
        super(context, DATA_NAME, null, DATA_BASE_VERSION);
//        database = getReadableDatabase();
//        onCreate(database);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Constants.CREATE_BUS_ROUTE_TABLE);
        db.execSQL(Constants.CREATE_CALENDAR_TABLE);
        db.execSQL(Constants.CREATE_STOP_TIMES_TABLE);
        db.execSQL(Constants.CREATE_STOPS_TABLE);
        db.execSQL(Constants.CREATE_TRIPS_TABLE);

        Stop stop = new Stop("stop1", "descr", 10,15,"str");
        //insertStop(stop);
        Cursor cursor = db.rawQuery("select * from "+ Stops.CONTENT_PATH,null);
        int i = 0;
        if(cursor.moveToFirst()){
            do {
                i++;
                Log.d("STARTEST","stop init ....."+i);
                tables.Stop st = new tables.Stop(cursor.getString(0),cursor.getString(1),cursor.getInt(2),
                        cursor.getInt(1),cursor.getString(1));
                Log.d("STARTEST","stop "+ st.toString());
            }while (cursor.moveToNext());
        }

        Log.d("STARX","db cretaed");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS "+ BusRoutes.CONTENT_PATH);
        db.execSQL("DROP TABLE IF EXISTS "+ Trips.CONTENT_PATH);
        db.execSQL("DROP TABLE IF EXISTS "+ Stops.CONTENT_PATH);
        db.execSQL("DROP TABLE IF EXISTS "+ StopTimes.CONTENT_PATH);
        db.execSQL("DROP TABLE IF EXISTS "+ Calendar.CONTENT_PATH);
        onCreate(db);
    }

    public List<String> allTableNames()
    {
        List<String> result = new ArrayList<String>();
        String selectQuery = "select name from sqlite_master where type = 'table'";
        Cursor cursor = this.getReadableDatabase().rawQuery(selectQuery,null);
        if(cursor.moveToFirst())
        {
            do {
                String name = cursor.getString(cursor.getColumnIndex("name"));
                result.add(name);
            }while (cursor.moveToNext());
        }
        return result;
    }

    /**
     * Insert a BusRoute in the db
     * @param route
     */
    public void insertRoute(BusRoute route)
    {
        database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BusRoutes.BusRouteColumns.SHORT_NAME, route.getShortName());
        values.put(BusRoutes.BusRouteColumns.LONG_NAME, route.getLongName());
        values.put(BusRoutes.BusRouteColumns.DESCRIPTION, route.getDescription());
        values.put(BusRoutes.BusRouteColumns.TYPE, route.getType());
        values.put(BusRoutes.BusRouteColumns.COLOR, route.getColor());
        values.put(BusRoutes.BusRouteColumns.TEXT_COLOR, route.getTextColor());
        database.insert(BusRoutes.CONTENT_PATH,null, values);
    }

    /**
     * Insert all BusRoute from the csv file to the db
     */
    public void insertBusRoutes(){
        ArrayList<tables.BusRoute> busRoutes = new ArrayList<tables.BusRoute>();
        try {
            busRoutes = loadBusRoutesData(BUS_ROUTES_CSV_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (tables.BusRoute busRoute : busRoutes){
            insertRoute(busRoute);
        }
    }

    public void insertTrip(tables.Trips trip )
    {
        database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Trips.TripColumns.BLOCK_ID, trip.getBlockId() );
        values.put(Trips.TripColumns.SERVICE_ID, trip.getServiceId() );
        values.put(Trips.TripColumns.HEADSIGN, trip.getHeadSign() );
        values.put(Trips.TripColumns.DIRECTION_ID, trip.getDirectionId() );
        values.put(Trips.TripColumns.BLOCK_ID, trip.getBlockId() );
        values.put(Trips.TripColumns.WHEELCHAIR_ACCESSIBLE, trip.getWheelchairAccessible() );
        database.insert(Trips.CONTENT_PATH,null, values);
    }

    /**
     * Insert all Trips from the csv file to the db
     */
    public void insertTrips(){
        ArrayList<tables.Trips> items = new ArrayList<tables.Trips>();
        try {
            items = loadTripsData(TRIPS_CSV_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (tables.Trips item : items){
            insertTrip(item);
        }
    }

    /**
     * Insert a Stop in the db
     * @param stop
     */
    public void insertStop(Stop stop)
    {
        database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Stops.StopColumns.NAME, stop.getName());
        values.put(Stops.StopColumns.DESCRIPTION, stop.getDescription());
        values.put(Stops.StopColumns.LATITUDE, stop.getLatitude());
        values.put(Stops.StopColumns.LONGITUDE, stop.getLongitude());
        values.put(Stops.StopColumns.WHEELCHAIR_BOARDING, stop.getWheelChairBoalding());
        database.insert(Stops.CONTENT_PATH,null, values);
    }

    /**
     * Insert all Stops from the csv file to the db
     */
    public void insertStops(){
        ArrayList<tables.Stop> items = new ArrayList<tables.Stop>();
        try {
            items = loadStopsData(STOPS_CSV_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (tables.Stop item : items){
            insertStop(item);
        }
    }

    /**
     * Insert a StopTime in the db
     * @param stopTime
     */
    public void insertStopTime(StopTime stopTime)
    {
        database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(StopTimes.StopTimeColumns.TRIP_ID, stopTime.getTripId());
        values.put(StopTimes.StopTimeColumns.ARRIVAL_TIME, stopTime.getArrivalTime());
        values.put(StopTimes.StopTimeColumns.DEPARTURE_TIME, stopTime.getDepartureTme());
        values.put(StopTimes.StopTimeColumns.STOP_ID, stopTime.getStopId());
        values.put(StopTimes.StopTimeColumns.STOP_SEQUENCE, stopTime.getStopSequence());
        database.insert(StopTimes.CONTENT_PATH,null,values);
    }

    /**
     * Insert all StopTimes from the csv file to the db
     */
    public void insertStopTimes(){
        ArrayList<tables.StopTime> items = new ArrayList<tables.StopTime>();
        try {
            items = loadStopTimesData(STOP_TIMES_CSV_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (StopTime item : items){
            insertStopTime(item);
        }
    }

    /**
     * Insert a Calendar in the db
     * @param calendar
     */
    public void insertCalendar(tables.Calendar calendar)
    {
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
        database.insert(Calendar.CONTENT_PATH,null,values);
    }

    /**
     * Insert all Calendars from the csv file to the db
     */
    public void insertCalendars(){
        ArrayList<tables.Calendar> items = new ArrayList<tables.Calendar>();
        try {
            items = loadCalendarData(CALENDAR_CSV_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (tables.Calendar item : items){
            insertCalendar(item);
        }
    }

    /**
     * Loads Calendars from the csv file
     * @param path
     * @return
     * @throws IOException
     */
    public static ArrayList<tables.Calendar> loadCalendarData(String path) throws IOException {
        Log.d("STARXC","start loading... "+path);
        ArrayList<tables.Calendar> calendars = new ArrayList<>();
        FileReader file = new FileReader(new File(getExternalStorageDirectory(), INIT_FOLDER_PATH +path));
        BufferedReader buffer = new BufferedReader(file);
        String line = "";
        int i =0;
        while ((line = buffer.readLine()) != null) {
            if (i!=0){
                String[] str = line.split(",");
                tables.Calendar calendar = new tables.Calendar(str[0],str[1],str[2],str[3],str[4],str[5],str[6],str[7],str[8]);
                calendars.add(calendar);
                Log.d("STARXC","loaded... "+calendar.toString());
            }
            i++;
        }
        Log.d("STARXC",i+" calendars loaded");
        return calendars;
    }

    /**
     * Loads BusRoutes from the csv file
     * @param path
     * @return
     * @throws IOException
     */
    public static ArrayList<tables.BusRoute> loadBusRoutesData(String path) throws IOException {
        Log.d("STARXBR","start loading... "+path);
        ArrayList<tables.BusRoute> busRoutes = new ArrayList<>();
        FileReader file = new FileReader(new File(getExternalStorageDirectory(), INIT_FOLDER_PATH +path));
        BufferedReader buffer = new BufferedReader(file);
        String line = "";
        int i=0;
        while ((line = buffer.readLine()) != null) {
            if (i !=0){
                String[] str = line.split(",");
                tables.BusRoute br = new tables.BusRoute(str[0],str[1],str[2],str[3],str[4],str[5]);
                busRoutes.add(br);
                Log.d("STARXBR","loaded... "+br.toString());
            }
            i++;
        }
        Log.d("STARXBR",i+" busRoutes loaded");
        return busRoutes;
    }

    /**
     * Loads Stops from the csv file
     * @param path
     * @return
     * @throws IOException
     */
    public static ArrayList<tables.Stop> loadStopsData(String path) throws IOException {
        Log.d("STARXS","start loading... "+path);
        ArrayList<tables.Stop> stops = new ArrayList<>();
        FileReader file = new FileReader(new File(getExternalStorageDirectory(), INIT_FOLDER_PATH +path));
        BufferedReader buffer = new BufferedReader(file);
        String line = "";
        int i=0;
        while ((line = buffer.readLine()) != null) {
            if (i !=0){
                String[] str = line.split(",");
                String name = str[2];
                String description = str[3];
                float latitude = Float.valueOf(str[4].replace('"', ' '));
                float longitude = Float.valueOf(str[5].replace('"', ' '));
                String wheelChairBoalding = str[11];
                tables.Stop stop = new tables.Stop(name,description,latitude,longitude,wheelChairBoalding);
                stops.add(stop);
                Log.d("STARXS","loaded... "+stop.toString());
            }
            i++;
        }
        Log.d("STARXS",i+" stops loaded");
        return stops;
    }

    /**
     * Loads StopTimes from the csv file
     * @param path
     * @return
     * @throws IOException
     */
    public static ArrayList<StopTime> loadStopTimesData(String path) throws IOException {
        Log.d("STARXST","start loading... "+path);
        ArrayList<StopTime> stopTimes = new ArrayList<>();
        FileReader file = new FileReader(new File(getExternalStorageDirectory(), INIT_FOLDER_PATH +path));
        BufferedReader buffer = new BufferedReader(file);
        String line = "";
        int i = 0;
        while ((line = buffer.readLine()) != null) {
            if (i!=0){
                String[] str = line.split(",");
                int tripId = Integer.valueOf(str[0].replaceAll("\"", ""));
                int stopId = Integer.valueOf(str[3].replaceAll("\"", ""));
                StopTime stopTime = new StopTime(tripId,str[1],str[2],stopId,str[4]);
                stopTimes.add(stopTime);
                Log.d("STARXST","loaded... "+ stopTime.toString());
            }
            i++;
             }
        Log.d("STARXST",i+" stopTimes loaded");
        return stopTimes;
    }

    /**
     * Loads Trips from the csv file
     * @param path
     * @return
     * @throws IOException
     */
    public static ArrayList<tables.Trips> loadTripsData(String path) throws IOException {
        Log.d("STARXT","start loading... "+path);
        ArrayList<tables.Trips> trips = new ArrayList<>();
        FileReader file = new FileReader(new File(getExternalStorageDirectory(), INIT_FOLDER_PATH +path));
        BufferedReader buffer = new BufferedReader(file);
        String line = "";
        int i = 0;
        while ((line = buffer.readLine()) != null) {
            if (i!=0){
                String[] str = line.split(",");
                int routeId = Integer.valueOf(str[0].replaceAll("\"", ""));
                int serviceId = Integer.valueOf(str[1].replaceAll("\"", ""));
                int direction = Integer.valueOf(str[5].replaceAll("\"", ""));
                String block = str[6].replaceAll("\"", "");
                tables.Trips trip = new tables.Trips(routeId,
                        serviceId,
                        str[3],
                        direction,
                        block,
                        str[8]);
                trips.add(trip);
                Log.d("STARXT","loaded... "+trip.toString());
            }
            i++;
        }
        Log.d("STARXT",i+" trips loaded");
        return trips;
    }
}
