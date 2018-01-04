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
    private static final int ALL_BUS_ROUTES_ID = 1;
    private static final int BUS_ROUTE_BY_ITEM_ID = 11;
    private static final int BUS_ROUTE_BY_ITEM2 = 12;
    private static final int ALL_CALENDARS_ID = 2;
    private static final int CALENDAR_BY_ITEM_ID = 21;
    private static final int ALL_STOPS_ID = 3;
    private static final int STOP_BY_ITEM_ID = 31;
    private static final int ALL_STOP_TIMES_ID = 4;
    private static final int STOP_TIME_BY_ITEM_ID = 41;
    private static final int ALL_TRIPS_ID = 5;
    private static final int TRIP_BY_ITEM_ID = 51;

    private static final UriMatcher starUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        starUriMatcher.addURI(StarContract.AUTHORITY, BusRoutes.CONTENT_PATH, ALL_BUS_ROUTES_ID);
        starUriMatcher.addURI(StarContract.AUTHORITY, BusRoutes.CONTENT_PATH + "/#", BUS_ROUTE_BY_ITEM_ID);
        starUriMatcher.addURI(StarContract.AUTHORITY, BusRoutes.CONTENT_PATH + "/#/#", BUS_ROUTE_BY_ITEM2);
        starUriMatcher.addURI(StarContract.AUTHORITY, Calendar.CONTENT_PATH, ALL_CALENDARS_ID);
        starUriMatcher.addURI(StarContract.AUTHORITY, Calendar.CONTENT_PATH + "/#", CALENDAR_BY_ITEM_ID);
        starUriMatcher.addURI(StarContract.AUTHORITY, Stops.CONTENT_PATH, ALL_STOPS_ID);
        starUriMatcher.addURI(StarContract.AUTHORITY, Stops.CONTENT_PATH + "/#", STOP_BY_ITEM_ID);
        starUriMatcher.addURI(StarContract.AUTHORITY, StopTimes.CONTENT_PATH, ALL_STOP_TIMES_ID);
        starUriMatcher.addURI(StarContract.AUTHORITY, StopTimes.CONTENT_PATH + "/#", STOP_TIME_BY_ITEM_ID);
        starUriMatcher.addURI(StarContract.AUTHORITY, Trips.CONTENT_PATH, ALL_TRIPS_ID);
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

        Cursor cursor;
        switch (starUriMatcher.match(uri)) {
            case ALL_BUS_ROUTES_ID:
                queryBuilder.setTables(BusRoutes.CONTENT_PATH);
                cursor = getAllBusRoutes();
                break;
            case BUS_ROUTE_BY_ITEM_ID:
                queryBuilder.setTables(BusRoutes.CONTENT_PATH);
                queryBuilder.appendWhere(BusRoutes.BusRouteColumns.ROUTE_ID + "='" + uri.getPathSegments().get(1) + "'");
                break;
            case BUS_ROUTE_BY_ITEM2:
                Log.d("STARX","ID_ITEM2");
                queryBuilder.setTables(BusRoutes.CONTENT_PATH +
                        " INNER JOIN " + Trips.CONTENT_PATH + " ON " +
                        BusRoutes.CONTENT_PATH + "." + BusRoutes.BusRouteColumns.ROUTE_ID + " = " + Trips.CONTENT_PATH + "." + Trips.TripColumns.ROUTE_ID);
                queryBuilder.appendWhere(BusRoutes.CONTENT_PATH + "." + BusRoutes.BusRouteColumns.ROUTE_ID + "='" + uri.getPathSegments().get(1) + "'");
                break;
            case ALL_CALENDARS_ID:
                queryBuilder.setTables(Calendar.CONTENT_PATH);
                break;
            case CALENDAR_BY_ITEM_ID:
                queryBuilder.setTables(Calendar.CONTENT_PATH);
                queryBuilder.appendWhere(Calendar.CalendarColumns.END_DATE + "=" + uri.getPathSegments().get(1));
                break;
            case ALL_STOPS_ID:
                queryBuilder.setTables(Stops.CONTENT_PATH);
                break;
            case STOP_BY_ITEM_ID:
                queryBuilder.setTables(Stops.CONTENT_PATH);
                queryBuilder.appendWhere(Stops.StopColumns.STOP_ID + "=" + uri.getPathSegments().get(1));
                break;
            case ALL_STOP_TIMES_ID:
                queryBuilder.setTables(StopTimes.CONTENT_PATH);
                break;
            case STOP_TIME_BY_ITEM_ID:
                queryBuilder.setTables(StopTimes.CONTENT_PATH);
                queryBuilder.appendWhere(StopTimes.StopTimeColumns.TRIP_ID + "=" + uri.getPathSegments().get(1));
                break;
            case ALL_TRIPS_ID:
                queryBuilder.setTables(Trips.CONTENT_PATH);
                break;
            case TRIP_BY_ITEM_ID:
                queryBuilder.setTables(Trips.CONTENT_PATH);
                queryBuilder.appendWhere(Trips.TripColumns.ROUTE_ID + "=" + uri.getPathSegments().get(1));
                break;
            default:
        }

        cursor = queryBuilder.query(
                database,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);

        /**
         * register to watch a content URI for changes
         */
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (starUriMatcher.match(uri)) {
            case ALL_BUS_ROUTES_ID:
                return BusRoutes.CONTENT_TYPE;
            case BUS_ROUTE_BY_ITEM_ID:
                return BusRoutes.CONTENT_ITEM_TYPE;
            case BUS_ROUTE_BY_ITEM2:
                return BusRoutes.CONTENT_ITEM_TYPE;
            case ALL_CALENDARS_ID:
                return Calendar.CONTENT_TYPE;
            case CALENDAR_BY_ITEM_ID:
                return Calendar.CONTENT_ITEM_TYPE;
            case ALL_STOPS_ID:
                return Stops.CONTENT_TYPE;
            case STOP_BY_ITEM_ID:
                return Stops.CONTENT_ITEM_TYPE;
            case ALL_STOP_TIMES_ID:
                return StopTimes.CONTENT_TYPE;
            case STOP_TIME_BY_ITEM_ID:
                return StopTimes.CONTENT_ITEM_TYPE;
            case ALL_TRIPS_ID:
                return Trips.CONTENT_TYPE;
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

    private Cursor getAllBusRoutes(){
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
