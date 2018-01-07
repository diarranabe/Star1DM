package com.diarranabe.star.star1dm;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by diarranabe on 20/11/2017.
 */

public class StarProvider extends ContentProvider implements StarContract {
    SQLiteDatabase database;
    private static final int ALL_BUS_ROUTES = 1;
    private static final int BUS_ROUTE_STOPS = 11;
    private static final int BUS_ROUTE_BY_ITEM2 = 12;
    private static final int ALL_CALENDARS_ID = 2;
    private static final int CALENDAR_BY_ITEM_ID = 21;

    private static final int ALL_STOPS_ID = 3;
    private static final int TRIP_STOPS_TIMES_TO_TERM = 5;
    private static final int STOP_TRIPS = 31;

    private static final int TRIP_BY_ITEM_ID = 51;
    private static final int ALL_STOP_TIMES_ID = 4;
    private static final int STOP_TIME_BY_ITEM_ID = 41;
    private static final int ROUTE_DETAILS = 6;

    private static final UriMatcher starUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        /**
         * Toutes les routes
         */
        starUriMatcher.addURI(StarContract.AUTHORITY, BusRoutes.CONTENT_PATH, ALL_BUS_ROUTES);

        /**
         * Tous les arrêts d'une route
         */
        starUriMatcher.addURI(StarContract.AUTHORITY, Stops.CONTENT_TYPE, BUS_ROUTE_STOPS);

        /**
         * Tous les arrêts d'un trip
         */
        starUriMatcher.addURI(StarContract.AUTHORITY,  "stop_trips", STOP_TRIPS);

        /**
         * Tous les arrêts jusqu'au terminus
         */
        starUriMatcher.addURI(StarContract.AUTHORITY, StarContract.StopTimes.CONTENT_ITEM_TYPE, TRIP_STOPS_TIMES_TO_TERM);

        starUriMatcher.addURI(StarContract.AUTHORITY, BusRoutes.CONTENT_PATH + "/#/#", BUS_ROUTE_BY_ITEM2);
        starUriMatcher.addURI(StarContract.AUTHORITY, Calendar.CONTENT_PATH, ALL_CALENDARS_ID);
        starUriMatcher.addURI(StarContract.AUTHORITY, Calendar.CONTENT_PATH + "/#", CALENDAR_BY_ITEM_ID);
        starUriMatcher.addURI(StarContract.AUTHORITY, Stops.CONTENT_PATH, ALL_STOPS_ID);
        starUriMatcher.addURI(StarContract.AUTHORITY, StopTimes.CONTENT_PATH, ALL_STOP_TIMES_ID);
        starUriMatcher.addURI(StarContract.AUTHORITY, StopTimes.CONTENT_PATH + "/#", STOP_TIME_BY_ITEM_ID);
        starUriMatcher.addURI(StarContract.AUTHORITY, Trips.CONTENT_PATH + "/#", TRIP_BY_ITEM_ID);
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        /**
         * Create a write able database which will trigger its
         * creation if it doesn't already exist.
         */
        database = dbHelper.getWritableDatabase();
        return (database == null) ? false : true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        SQLiteQueryBuilder queryBuilder2 = new SQLiteQueryBuilder();

        Cursor cursor;
        switch (starUriMatcher.match(uri)) {
            case ALL_BUS_ROUTES:
                queryBuilder.setTables(BusRoutes.CONTENT_PATH);
                cursor = getAllBusRoutes();
                cursor = queryBuilder.query(
                        database,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            case BUS_ROUTE_STOPS:
               return getRouteStops(uri, selectionArgs);
            case STOP_TRIPS:
                return getStopTripsTimes(uri,selectionArgs);
            case TRIP_STOPS_TIMES_TO_TERM:
                return getTripStopTimesToTerminus(uri,selectionArgs);
            case ALL_STOP_TIMES_ID:
                queryBuilder.setTables(StopTimes.CONTENT_PATH);
                break;
            case STOP_TIME_BY_ITEM_ID:
                queryBuilder.setTables(StopTimes.CONTENT_PATH);
                queryBuilder.appendWhere(StopTimes.StopTimeColumns.TRIP_ID + "=" + uri.getPathSegments().get(1));
                break;
            case TRIP_BY_ITEM_ID:
                queryBuilder.setTables(Trips.CONTENT_PATH);
                queryBuilder.appendWhere(Trips.TripColumns.ROUTE_ID + "=" + uri.getPathSegments().get(1));
                break;
            default:
        }

        /**
         * register to watch a content URI for changes
         */

        return null;
    }


    private Cursor getRouteStops(Uri uri, String[] selectionArgs) {
        SQLiteQueryBuilder queryBuilder2 = new SQLiteQueryBuilder();
        queryBuilder2.setTables(Trips.CONTENT_PATH);
        queryBuilder2.appendWhere(Trips.TripColumns.ROUTE_ID + " = " + selectionArgs[0]);
        queryBuilder2.appendWhere(" AND " + selectionArgs[1] + " = " + Trips.CONTENT_PATH + "." + Trips.TripColumns.DIRECTION_ID);
        Cursor c = queryBuilder2.query(
                database,
                null,
                null,
                null,
                null,
                null,
                null, "1");
        String trip = "";
        if (c.moveToFirst()) {
            do {
                trip = c.getString(c.getColumnIndex(Trips.TripColumns.TRIP_ID));
            } while (c.moveToNext());
        }
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(Stops.CONTENT_PATH + "," + StopTimes.CONTENT_PATH + "," + Trips.CONTENT_PATH);
        queryBuilder.appendWhere(Stops.CONTENT_PATH + "." + Stops.StopColumns.STOP_ID + "=" + StopTimes.CONTENT_PATH + "." + StopTimes.StopTimeColumns.STOP_ID);
        queryBuilder.appendWhere(" AND " + StopTimes.CONTENT_PATH + "." + StopTimes.StopTimeColumns.TRIP_ID + "=" + Trips.CONTENT_PATH + "." + Trips.TripColumns.TRIP_ID);
        queryBuilder.appendWhere(" AND " + selectionArgs[0] + " = " + Trips.CONTENT_PATH + "." + Trips.TripColumns.ROUTE_ID);
        queryBuilder.appendWhere(" AND " + Trips.CONTENT_PATH + "." + Trips.TripColumns.TRIP_ID + " = " + trip);
        Cursor cursor = queryBuilder.query(
                database,
                null,
                null,
                null,
                Stops.StopColumns.NAME,
                null,
                StopTimes.StopTimeColumns.ARRIVAL_TIME);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    private Cursor getStopTripsTimes(Uri uri, String[] selectionArgs) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        long date = Long.valueOf(selectionArgs[2]);
        queryBuilder.setTables(Stops.CONTENT_PATH + "," + StopTimes.CONTENT_PATH + "," + Trips.CONTENT_PATH+ "," + Calendar.CONTENT_PATH);
        Log.e("STARX","Les passages à un arrêt");
        queryBuilder.appendWhere(Stops.CONTENT_PATH + "." + Stops.StopColumns.STOP_ID + "=" + StopTimes.CONTENT_PATH + "." + StopTimes.StopTimeColumns.STOP_ID);
        queryBuilder.appendWhere(" AND " + StopTimes.CONTENT_PATH + "." + StopTimes.StopTimeColumns.TRIP_ID + "=" + Trips.CONTENT_PATH + "." + Trips.TripColumns.TRIP_ID);
        queryBuilder.appendWhere(" AND " + Calendar.CONTENT_PATH + "." + Calendar.CalendarColumns.SERVICE_ID+ " = " +  Trips.CONTENT_PATH+"."+Trips.TripColumns.SERVICE_ID);
        queryBuilder.appendWhere(" AND " + Stops.CONTENT_PATH + "." + Stops.StopColumns.STOP_ID+ " = '" + selectionArgs[0]+"'" );
        queryBuilder.appendWhere(" AND " + Trips.CONTENT_PATH + "." + Trips.TripColumns.ROUTE_ID+ " = '" + selectionArgs[1] +"'");
        queryBuilder.appendWhere(" AND " + Calendar.CONTENT_PATH + "." + Calendar.CalendarColumns.END_DATE+ " <= '" + (date)+"'" );
        queryBuilder.appendWhere(" AND " + Calendar.CONTENT_PATH + "." + Calendar.CalendarColumns.END_DATE+ " > " + "'"+(date-1)+"'" );
        queryBuilder.appendWhere(" AND " + StopTimes.CONTENT_PATH + "." + StopTimes.StopTimeColumns.ARRIVAL_TIME+ " >= '" + selectionArgs[3]+"'" );
        Cursor cursor = queryBuilder.query(
                database,
                null,
                null,
                null,
                null,
                null,
                StopTimes.CONTENT_PATH+"."+StopTimes.StopTimeColumns.ARRIVAL_TIME);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    private Cursor getTripStopTimesToTerminus(Uri uri, String[] selectionArgs) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(Stops.CONTENT_PATH + "," + StopTimes.CONTENT_PATH + "," + Trips.CONTENT_PATH);
        Log.e("STARX","Les passages d'un trip aux arrêts jusqu'au terminus à partir d'une heure");
        queryBuilder.appendWhere(Stops.CONTENT_PATH + "." + Stops.StopColumns.STOP_ID + "=" + StopTimes.CONTENT_PATH + "." + StopTimes.StopTimeColumns.STOP_ID);
        queryBuilder.appendWhere(" AND " + StopTimes.CONTENT_PATH + "." + StopTimes.StopTimeColumns.TRIP_ID + "=" + Trips.CONTENT_PATH + "." + Trips.TripColumns.TRIP_ID);
        queryBuilder.appendWhere(" AND " + Stops.CONTENT_PATH + "." + Stops.StopColumns.STOP_ID+ " = " + StopTimes.CONTENT_PATH+"."+StopTimes.StopTimeColumns.STOP_ID );
        queryBuilder.appendWhere(" AND " + Trips.CONTENT_PATH + "." + Trips.TripColumns.TRIP_ID+ " = '" + selectionArgs[0] +"'");
        queryBuilder.appendWhere(" AND " + StopTimes.CONTENT_PATH + "." + StopTimes.StopTimeColumns.ARRIVAL_TIME+ " >= '" + selectionArgs[1]+"'" );
        Cursor cursor = queryBuilder.query(
                database,
                null,
                null,
                null,
                null,
                null,
                StopTimes.CONTENT_PATH+"."+StopTimes.StopTimeColumns.ARRIVAL_TIME);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (starUriMatcher.match(uri)) {
            case ALL_BUS_ROUTES:
                return BusRoutes.CONTENT_TYPE;
            case BUS_ROUTE_STOPS:
                return Stops.CONTENT_TYPE;
            case BUS_ROUTE_BY_ITEM2:
                return BusRoutes.CONTENT_ITEM_TYPE;
            case ALL_CALENDARS_ID:
                return Calendar.CONTENT_TYPE;
            case CALENDAR_BY_ITEM_ID:
                return Calendar.CONTENT_ITEM_TYPE;
            case ALL_STOPS_ID:
                return Stops.CONTENT_TYPE;
            case STOP_TRIPS:
                return Stops.CONTENT_ITEM_TYPE;
            case ALL_STOP_TIMES_ID:
                return StopTimes.CONTENT_TYPE;
            case STOP_TIME_BY_ITEM_ID:
                return StopTimes.CONTENT_ITEM_TYPE;
            case TRIP_STOPS_TIMES_TO_TERM:
                return StopTimes.CONTENT_ITEM_TYPE;
            case TRIP_BY_ITEM_ID:
                return Trips.CONTENT_ITEM_TYPE;
        }
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    private Cursor getAllBusRoutes() {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(BusRoutes.CONTENT_PATH);
        Cursor cursor = queryBuilder.query(
                database,
                null,
                null,
                null,
                null,
                null,
                BusRoutes.BusRouteColumns.ROUTE_ID);
        return cursor;
    }
}
