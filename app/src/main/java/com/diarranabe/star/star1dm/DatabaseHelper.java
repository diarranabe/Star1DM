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
import tables.StopeTimes;

import static android.os.Environment.getExternalStorageDirectory;


/**
 * Created by diarranabe on 20/11/2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper implements StarContract{

    private static final String INIT_FOLDER_PATH = "star1dm/";
    private SQLiteDatabase database;
    private static  String DATA_NAME ="starBus";
    private static final int DATA_BASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATA_NAME, null, DATA_BASE_VERSION);
        database = getReadableDatabase();
        onCreate(database);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Constants.CREATE_BUS_ROUTE_TABLE);
        db.execSQL(Constants.CREATE_CALENDAR_TABLE);
        db.execSQL(Constants.CREATE_STOP_TIMES_TABLE);
        db.execSQL(Constants.CREATE_STOPS_TABLE);
        db.execSQL(Constants.CREATE_TRIPS_TABLE);

        Stop stop = new Stop("stop1", "descr", 10,15,"str");
        insertStops(stop);
        Cursor cursor = db.rawQuery("select * from "+ Stops.CONTENT_PATH,null);
        if(cursor.moveToFirst()){
            do {
                Log.d("STARX","stop "+ cursor.getString(0));
                Log.d("STARX","stop "+ cursor.getString(1));
                Log.d("STARX","stop "+ cursor.getString(2));
                Log.d("STARX","stop "+ cursor.getString(3));
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

    public void insertBusRoute(BusRoute busRoutes)
    {
        database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BusRoutes.BusRouteColumns.SHORT_NAME, busRoutes.getShortName());
        values.put(BusRoutes.BusRouteColumns.LONG_NAME, busRoutes.getLongName());
        values.put(BusRoutes.BusRouteColumns.DESCRIPTION, busRoutes.getDescription());
        values.put(BusRoutes.BusRouteColumns.TYPE, busRoutes.getType());
        values.put(BusRoutes.BusRouteColumns.COLOR, busRoutes.getColor());
        values.put(BusRoutes.BusRouteColumns.TEXT_COLOR, busRoutes.getTextColor());
        database.insert(BusRoutes.CONTENT_PATH,null, values);
    }

    public void insertTrips(tables.Trips trips )
    {
        database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Trips.TripColumns.BLOCK_ID, trips.getBlockId() );
        values.put(Trips.TripColumns.SERVICE_ID, trips.getServiceId() );
        values.put(Trips.TripColumns.HEADSIGN, trips.getHeadSign() );
        values.put(Trips.TripColumns.DIRECTION_ID, trips.getDirectionId() );
        values.put(Trips.TripColumns.BLOCK_ID, trips.getBlockId() );
        values.put(Trips.TripColumns.WHEELCHAIR_ACCESSIBLE, trips.getWheelchairAccessible() );
        database.insert(Trips.CONTENT_PATH,null, values);
    }

    public void insertStops(Stop stop)
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


    public void insertStopTimes(StopeTimes stopTimes)
    {
        database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(StopTimes.StopTimeColumns.TRIP_ID, stopTimes.getTripId());
        values.put(StopTimes.StopTimeColumns.ARRIVAL_TIME, stopTimes.getArrivalTime());
        values.put(StopTimes.StopTimeColumns.DEPARTURE_TIME, stopTimes.getDepartureTme());
        values.put(StopTimes.StopTimeColumns.STOP_ID, stopTimes.getStopId());
        values.put(StopTimes.StopTimeColumns.STOP_SEQUENCE, stopTimes.getStopSequence());
        database.insert(StopTimes.CONTENT_PATH,null,values);

    }

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

    public static ArrayList<tables.StopeTimes> loadStopTimesData(String path) throws IOException {
        Log.d("STARXST","start loading... "+path);
        ArrayList<tables.StopeTimes> stopeTimes = new ArrayList<>();
        FileReader file = new FileReader(new File(getExternalStorageDirectory(), INIT_FOLDER_PATH +path));
        BufferedReader buffer = new BufferedReader(file);
        String line = "";
        int i = 0;
        while ((line = buffer.readLine()) != null) {
            if (i!=0){
                String[] str = line.split(",");
                int tripId = Integer.valueOf(str[0].replaceAll("\"", ""));
                int stopId = Integer.valueOf(str[3].replaceAll("\"", ""));
                tables.StopeTimes stopeTime = new tables.StopeTimes(tripId,str[1],str[2],stopId,str[4]);
                stopeTimes.add(stopeTime);
                Log.d("STARXST","loaded... "+stopeTime.toString());
            }
            i++;
             }
        Log.d("STARXST",i+" stopTimes loaded");
        return stopeTimes;
    }

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
                Log.d("STARXT","start loading... "+line);
                //int routeId, int serviceId,       String headSign,               int directionId, int blockId,       String wheelchairAccessible) {
                //route_id,    service_id,  trip_id,trip_headsign,  trip_short_name,direction_id,   block_id,  shape_id,wheelchair_accessible,bikes_allowed
                int routeId = Integer.valueOf(str[0].replaceAll("\"", ""));
                int serviceId = Integer.valueOf(str[1].replaceAll("\"", ""));
                int direction = Integer.valueOf(str[5].replaceAll("\"", ""));
                Log.d("STARXT","stp^lm:... "+routeId+","+serviceId+","+direction+","+
                        Float.valueOf(str[6].replaceAll("\"","")));

                int block = Integer.valueOf(str[6].replaceAll("\"", ""));
                tables.Trips trip = new tables.Trips(routeId,
                        serviceId,
                        str[3],
                        direction,
                        block,
                        str[8]);
                Log.d("STARXT","start loading... ");
                trips.add(trip);
                Log.d("STARXT","loaded... "+trip.toString());
            }
            i++;
        }
        Log.d("STARXT",i+" trips loaded");
        return trips;
    }
}
