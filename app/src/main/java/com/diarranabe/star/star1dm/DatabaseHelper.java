package com.diarranabe.star.star1dm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import tables.BusRoute;
import tables.Stop;
import tables.StopeTimes;


/**
 * Created by diarranabe on 20/11/2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper implements StarContract{

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
                Log.d("XXXX","stop "+ cursor.getString(0));
                Log.d("XXXX","stop "+ cursor.getString(1));
                Log.d("XXXX","stop "+ cursor.getString(2));
                Log.d("XXXX","stop "+ cursor.getString(3));
            }while (cursor.moveToNext());
        }

        Log.d("XXXX","db cretaed");
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


    public static final String DATABASE_CREATE_TABLE_STOP_TIMES = "CREATE TABLE IF NOT EXISTS"+ StarContract.StopTimes.CONTENT_PATH +
            "("+ StarContract.StopTimes.StopTimeColumns.TRIP_ID + " INTEGER NOT NULL PRIMARY KEY, "+
            StarContract.StopTimes.StopTimeColumns.ARRIVAL_TIME+", TEXT"+
            StarContract.StopTimes.StopTimeColumns.DEPARTURE_TIME+", TEXT "+
            StarContract.StopTimes.StopTimeColumns.STOP_ID+", INTEGER"+
            StarContract.StopTimes.StopTimeColumns.STOP_SEQUENCE+", TEXT );";
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



    public ArrayList<tables.Calendar> loadCalendarData() throws IOException {
        ArrayList<tables.Calendar> calendars = new ArrayList<>();
        FileReader file = new FileReader("calendarFileName");
        BufferedReader buffer = new BufferedReader(file);
        String line = "";
        while ((line = buffer.readLine()) != null) {
            String[] str = line.split(",");
            calendars.add(new tables.Calendar(str[0],str[1],str[2],str[3],str[4],str[5],str[6],str[7],str[8]));
        }
        return calendars;
    }

    public ArrayList<tables.BusRoute> loadBusRoutesData() throws IOException {
        ArrayList<tables.BusRoute> busRoutes = new ArrayList<>();
        FileReader file = new FileReader("busRoutesFileName");
        BufferedReader buffer = new BufferedReader(file);
        String line = "";
        while ((line = buffer.readLine()) != null) {
            String[] str = line.split(",");
            busRoutes.add(new tables.BusRoute(str[0],str[1],str[2],str[3],str[4],str[5]));
        }
        return busRoutes;
    }

    public ArrayList<tables.Stop> loadStopsData() throws IOException {
        ArrayList<tables.Stop> stops = new ArrayList<>();
        FileReader file = new FileReader("stopsFileName");
        BufferedReader buffer = new BufferedReader(file);
        String line = "";
        while ((line = buffer.readLine()) != null) {
            String[] str = line.split(",");
            stops.add(new tables.Stop(str[0],str[1],Float.valueOf(str[2]),Float.valueOf(str[3]),str[4]));
        }
        return stops;
    }

    public ArrayList<tables.StopeTimes> loadStopTimesData() throws IOException {
        ArrayList<tables.StopeTimes> stopeTimes = new ArrayList<>();
        FileReader file = new FileReader("stopTimesFileName");
        BufferedReader buffer = new BufferedReader(file);
        String line = "";
        while ((line = buffer.readLine()) != null) {
            String[] str = line.split(",");
            stopeTimes.add(new tables.StopeTimes(Integer.valueOf(str[0]),str[1],str[2],Integer.valueOf(str[3]),str[4]));
        }
        return stopeTimes;
    }

    public ArrayList<tables.Trips> loadTripsData() throws IOException {
        ArrayList<tables.Trips> trips = new ArrayList<>();
        FileReader file = new FileReader("tripsFileName");
        BufferedReader buffer = new BufferedReader(file);
        String line = "";
        while ((line = buffer.readLine()) != null) {
            String[] str = line.split(",");
            trips.add(new tables.Trips(Integer.valueOf(str[0]),Integer.valueOf(str[1]),str[2],Integer.valueOf(str[3]),Integer.valueOf(str[4]),str[5]));
        }
        return trips;
    }
}
