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

import tables.StopTime;

/**
 * Created by diarranabe on 20/11/2017.
 */

public class StarProvider extends ContentProvider implements StarContract {
    SQLiteDatabase database;
    private static final int ALL_BUS_ROUTES = 1;
    private static final int BUS_ROUTE_BY_SHORT_NAME = 11;
    private static final int ALL_CALENDARS = 2;
    private static final int CALENDAR_BY_ID = 21;
    private static final int ALL_STOPS = 3;
    private static final int STOP_BY_ID = 31;
    private static final int ALL_STOP_TIMES = 4;
    private static final int STOP_TIME_BY_ID = 41;
    private static final int ALL_TRIPS = 5;
    private static final int TRIP_BY_ID = 51;

    private static final UriMatcher starUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        starUriMatcher.addURI(StarContract.AUTHORITY, BusRoutes.CONTENT_PATH, ALL_BUS_ROUTES);
        starUriMatcher.addURI(StarContract.AUTHORITY, BusRoutes.CONTENT_PATH + "/#", BUS_ROUTE_BY_SHORT_NAME);
        starUriMatcher.addURI(StarContract.AUTHORITY, Calendar.CONTENT_PATH, ALL_CALENDARS);
        starUriMatcher.addURI(StarContract.AUTHORITY, Calendar.CONTENT_PATH + "/#", CALENDAR_BY_ID);
        starUriMatcher.addURI(StarContract.AUTHORITY, Stops.CONTENT_PATH, ALL_STOPS);
        starUriMatcher.addURI(StarContract.AUTHORITY, Stops.CONTENT_PATH + "/#", STOP_BY_ID);
        starUriMatcher.addURI(StarContract.AUTHORITY, StopTimes.CONTENT_PATH, ALL_STOP_TIMES);
        starUriMatcher.addURI(StarContract.AUTHORITY, StopTimes.CONTENT_PATH + "/#", STOP_TIME_BY_ID);
        starUriMatcher.addURI(StarContract.AUTHORITY, Trips.CONTENT_PATH, ALL_TRIPS);
        starUriMatcher.addURI(StarContract.AUTHORITY, Trips.CONTENT_PATH + "/#", TRIP_BY_ID);
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

        switch (starUriMatcher.match(uri)) {
            case ALL_BUS_ROUTES:
                queryBuilder.setTables(BusRoutes.CONTENT_PATH);
                break;
            case BUS_ROUTE_BY_SHORT_NAME:
                queryBuilder.setTables(BusRoutes.CONTENT_PATH);
//                queryBuilder.appendWhere(BusRoutes.BusRouteColumns.SHORT_NAME + "=" + uri.getPathSegments().get(1));
                break;
            case ALL_CALENDARS:
                queryBuilder.setTables(Calendar.CONTENT_PATH);
                break;
            case CALENDAR_BY_ID:
                queryBuilder.setTables(Calendar.CONTENT_PATH);
//                queryBuilder.appendWhere(Calendar.CalendarColumns._ID + "=" + uri.getPathSegments().get(1));
                break;
            case ALL_STOPS:
                queryBuilder.setTables(Stops.CONTENT_PATH);
                break;
            case STOP_BY_ID:
                queryBuilder.setTables(Stops.CONTENT_PATH);
//                queryBuilder.appendWhere(Stops.StopColumns._ID + "=" + uri.getPathSegments().get(1));
                break;
            case ALL_STOP_TIMES:
                queryBuilder.setTables(StopTimes.CONTENT_PATH);
                break;
            case STOP_TIME_BY_ID:
                queryBuilder.setTables(StopTimes.CONTENT_PATH);
//                queryBuilder.appendWhere(StopTimes.StopTimeColumns._ID + "=" + uri.getPathSegments().get(1));
                break;
            case ALL_TRIPS:
                queryBuilder.setTables(Trips.CONTENT_PATH);
                break;
            case TRIP_BY_ID:
                queryBuilder.setTables(Trips.CONTENT_PATH);
//                queryBuilder.appendWhere(Trips.TripColumns._ID + "=" + uri.getPathSegments().get(1));
                break;
            default:
        }

        Cursor cursor = queryBuilder.query(
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
            case ALL_BUS_ROUTES:
                return BusRoutes.CONTENT_TYPE;
            case BUS_ROUTE_BY_SHORT_NAME:
                return BusRoutes.CONTENT_ITEM_TYPE;
            case ALL_CALENDARS:
                return Calendar.CONTENT_TYPE;
            case CALENDAR_BY_ID:
                return Calendar.CONTENT_ITEM_TYPE;
            case ALL_STOPS:
                return Stops.CONTENT_TYPE;
            case STOP_BY_ID:
                return Stops.CONTENT_ITEM_TYPE;
            case ALL_STOP_TIMES:
                return StopTimes.CONTENT_TYPE;
            case STOP_TIME_BY_ID:
                return StopTimes.CONTENT_ITEM_TYPE;
            case ALL_TRIPS:
                return Trips.CONTENT_TYPE;
            case TRIP_BY_ID:
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
}
