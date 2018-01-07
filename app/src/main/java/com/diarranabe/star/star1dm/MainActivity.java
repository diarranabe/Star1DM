package com.diarranabe.star.star1dm;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import tables.Trip;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static android.os.Environment.getExternalStorageDirectory;


public class MainActivity extends AppCompatActivity {

    //initialize our progress dialog/bar
    private ProgressDialog mProgressDialog;
    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    DatabaseHelper databaseHelper;

    //Absolu path whre file are unZip
    private String exportPath = "";

    private String testUrl = "https://data.explore.star.fr/api/records/1.0/search/?dataset=tco-busmetro-horaires-gtfs-versions-td&sort=-debutvalidite";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHelper = new DatabaseHelper(getApplicationContext());
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);
        Log.d("STARX", "start");
      /*  if (DatabaseHelper.getVersions(getApplicationContext()).get(0).equals(Constants.DEFAULT_FIRST_VERSION)){ // premier lancement
//            loadFirstFileData();
        }*/
       onNewIntent(getIntent());
        Intent intent = new Intent(this, CheckStarDataService.class);
        startService(intent);


//        databaseHelper.insertStopTimes();
//getJson2(testUrl);


//fragment3Query();

//        testStopsProvider();

        /*Cursor cursor = databaseHelper.getWritableDatabase().query("stoptime",null,null,null,null,null,null,null);
        while (cursor!= null && (cursor.moveToFirst())){
            cursor.moveToFirst();
*//*
                Log.d("STARXTEST", cursor.getInt(0)+","+cursor.getInt(1)+","+cursor.getInt(2)+
                        ","+cursor.getString(3)+
                        "--------------------------Received from provider ..." );
        }*/

//        fragment2Query();

        Log.d("STARX", "end");
    }




    /**
     * Losrqu'on clic sur la notif de mise à jour
     *
     * @param intent
     */
    public void onNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey(getString(R.string.data1_url_id))
                    && extras.containsKey(getString(R.string.data2_url_id))
                    && extras.containsKey(getString(R.string.data1_date_id))
                    && extras.containsKey(getString(R.string.data2_date_id))
                    ) {
                String file1 = extras.getString(getString(R.string.data1_url_id));
                String file2 = extras.getString(getString(R.string.data2_url_id));
                String date1 = extras.getString(getString(R.string.data1_date_id));
                String date2 = extras.getString(getString(R.string.data2_date_id));
                Log.d("STARX", " notif msg : " + file1 + ", date: " + date1);
                Log.d("STARX", " notif msg : " + file2 + ", date: " + date2);

                // Telecharger et Ajouter les nouvelles données
                databaseHelper.getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + StarContract.BusRoutes.CONTENT_PATH);
                databaseHelper.getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + StarContract.Calendar.CONTENT_PATH);
                databaseHelper.getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + StarContract.Stops.CONTENT_PATH);
                databaseHelper.getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + StarContract.StopTimes.CONTENT_PATH);
                databaseHelper.getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + StarContract.Trips.CONTENT_PATH);
                databaseHelper.getWritableDatabase().execSQL(Constants.CREATE_BUS_ROUTE_TABLE);
                databaseHelper.getWritableDatabase().execSQL(Constants.CREATE_CALENDAR_TABLE);
                databaseHelper.getWritableDatabase().execSQL(Constants.CREATE_STOPS_TABLE);
                databaseHelper.getWritableDatabase().execSQL(Constants.CREATE_STOP_TIMES_TABLE);
                databaseHelper.getWritableDatabase().execSQL(Constants.CREATE_TRIPS_TABLE);

                downZip(file1);
//                downZip(file2);

                DatabaseHelper dh = new DatabaseHelper(getApplicationContext());
                /**
                 * Mettre les versions à jour
                 */
                dh.updateVersions(getApplicationContext(), extras);
            }
        }
    }

    /**
     * connexion to Star Api to get url of zip and id of traject
     *
     * @param url
     * @return
     */
    public void getJson(String url) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("" + url, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.e("XXXX", "" + response.toString());

                try {
                    JSONArray reords = response.getJSONArray("records");

                    JSONObject object1 = (JSONObject) reords.get(0);

                    JSONObject object2 = (JSONObject) object1.get("fields");

                    downZip(object2.get("url").toString());

                    Log.e("XXXX", "" + object2.get("url").toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //Json object is returned as a response
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Log.e("XXXX", "==> PROBLEME DE CHARGEMENRT <==");
            }
        });

    }

    /**
     * Permite to download Zip file
     *
     * @param zipFileUrl
     */
    public void downZip(String zipFileUrl) {
        final String sourceFilname = "" + zipFileUrl;
        AsyncHttpClient client = new AsyncHttpClient();
        String[] allowedType = {
                "application/zip"
        };
        client.get(sourceFilname, new BinaryHttpResponseHandler(allowedType) {

            @Override
            public void onStart() {
                super.onStart();
                showDialog(DIALOG_DOWNLOAD_PROGRESS);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] binaryData) {

                Log.e("STARX", "success start");

                try {

                    //Splitting a File Name from SourceFileName
                    String DestinationName = sourceFilname.substring(sourceFilname.lastIndexOf('/') + 1, sourceFilname.length());
                    DatabaseHelper.INIT_FOLDER_PATH = "star1dm/" + DestinationName.substring(0, DestinationName.lastIndexOf(".")) + "/";
                    //Saving a File into Download Folder
                    File DEVICE_ROOT_FOLDER = getExternalStorageDirectory();
                    String INIT_FOLDER_PATH = "star1dm/";

                    File file = new File((DEVICE_ROOT_FOLDER + "/" + INIT_FOLDER_PATH));

                    File _f = new File(file, DestinationName);

//                    DatabaseHelper.INIT_FOLDER_PATH = DestinationName + "/";

                    FileOutputStream output = new FileOutputStream(_f);

                    Log.e("STARX", "success try");
                    output.write(binaryData);
                    output.close();
                    Log.e("STARX", "" + _f);

                    // Debut du deziping
                    exportPath = _f.getAbsolutePath();
                    exportPath = exportPath.replace(".zip", "");
                    Log.e("STARX", "==> " + exportPath);

                    DatabaseHelper.DOWNLOAD_PATH = exportPath;
                    exportPath = exportPath + "/";

                    // decropress file in folder whith id name
                    DecompressFast df = new DecompressFast(_f.getAbsolutePath(), exportPath);
                    df.unzip();

                    /**
                     * Inserer les données télechargées
                     */
                    DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
                    databaseHelper.insertAll();
                    dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
                    mProgressDialog.dismiss();


                } catch (IOException e) {
                    Log.e("STARX", "success catch");
                    e.printStackTrace();
                }
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
                int val = (int) ((bytesWritten * 100) / totalSize);
                Log.d("STARX", "downloading ..... " + val);
                mProgressDialog.setProgress(val);
                mProgressDialog.getCurrentFocus();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] binaryData, Throwable error) {

                Log.e("STARX", "==> " + error);

            }

            @Override
            public void onFinish() {
                super.onFinish();
//                dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
            }
        });
    }


    /**
     * Telecharge le premier fichier
     */
    public void loadFirstFileData() {
        Log.d("STARX", "first file start  ");
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(Constants.DATA_SOURCE_URL, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("STARX", "first file success start ");

                try {
                    JSONArray reords = response.getJSONArray("records");

                    /**
                     * Traitement du fichier
                     */
                    JSONObject file1 = (JSONObject) reords.get(0);
                    JSONObject file12 = (JSONObject) file1.get("fields");
                    JSONObject fichier1 = (JSONObject) file12.get("fichier");
                    String last_sync1 = (String) fichier1.get("last_synchronized");
                    String file_url = file12.get("url").toString();

                    /**
                     * Telecharger et ajouter les dnées dans la bd
                     */
                    downZip(file_url);

                    Log.d("STARX", "first file inserted in the database");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //Json object is returned as a response
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Log.e("STARX", "==> PROBLEME DE CHARGEMENRT <==");
            }
        });
    }

    // Storage Permissions variables
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Methode to get permision to user .
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have read or write permission
        int writePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    /**
     * Progresse Bar
     *
     * @param id
     * @return
     */
    @Override
    protected ProgressDialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_DOWNLOAD_PROGRESS: //we set this to 0
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage("Downloading file…");
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setMax(100);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(true);
                mProgressDialog.show();
                return mProgressDialog;
            default:
                return null;
        }
    }

    /**
     * Requete du fragment 1
     */
    public void fragment1Query() {
        Cursor cursor = this.getContentResolver().query(StarContract.BusRoutes.CONTENT_URI,
                null, null, null,
                StarContract.BusRoutes.BusRouteColumns.ROUTE_ID);

        int i = 0;
        if (cursor.moveToFirst()) {
            do {
                tables.BusRoute item = new tables.BusRoute(
                        cursor.getString(cursor.getColumnIndex(StarContract.BusRoutes.BusRouteColumns.ROUTE_ID)),
                        cursor.getString(cursor.getColumnIndex(StarContract.BusRoutes.BusRouteColumns.SHORT_NAME)),
                        cursor.getString(cursor.getColumnIndex(StarContract.BusRoutes.BusRouteColumns.LONG_NAME)),
                        cursor.getString(cursor.getColumnIndex(StarContract.BusRoutes.BusRouteColumns.DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndex(StarContract.BusRoutes.BusRouteColumns.TYPE)),
                        cursor.getString(cursor.getColumnIndex(StarContract.BusRoutes.BusRouteColumns.COLOR)),
                        cursor.getString(cursor.getColumnIndex(StarContract.BusRoutes.BusRouteColumns.TEXT_COLOR))
                );
                i++;
                Log.d("STARXBR", i + "-Received from provider ..." + item);
            } while (cursor.moveToNext());
        }
    }

    public void fragment2Query() {
        String[] selargs = {"0005", "0"};
        Cursor cursor = this.getContentResolver().query(StarContract.Stops.CONTENT_URI,
                null, null, selargs,
                null);
        if (cursor.moveToFirst()) {
            do {
                tables.Stop item = new tables.Stop(
                        cursor.getString(cursor.getColumnIndex(StarContract.Stops.StopColumns.STOP_ID)),
                        cursor.getString(cursor.getColumnIndex(StarContract.Stops.StopColumns.NAME)),
                        cursor.getString(cursor.getColumnIndex(StarContract.Stops.StopColumns.DESCRIPTION)),
                        cursor.getFloat(cursor.getColumnIndex(StarContract.Stops.StopColumns.LATITUDE)),
                        cursor.getFloat(cursor.getColumnIndex(StarContract.Stops.StopColumns.LONGITUDE)),
                        cursor.getString(cursor.getColumnIndex(StarContract.Stops.StopColumns.WHEELCHAIR_BOARDING))
                );
                Log.d("STARXTEST", " ------------------- Received from provider ..." + item.getName());
            } while (cursor.moveToNext());
        }

    }

    /**
     * Les horaires de passage à un arrêt
     * en input : id_arret, id_route, date(20180105), heure(21:59:00)
     * Accès à toutes les colonnes de stop,stoptime,trip,calendar pour chaque ligne
     */
    private void fragment3Query() {
        Uri stopsUri;
        stopsUri = Uri.withAppendedPath(StarContract.AUTHORITY_URI, "stop_trips");
        String[] selargs = {"1258", "0005", "20180105","21:59:00"};
        Cursor cursor = getContentResolver().query(stopsUri,
                null, null, selargs,
                StarContract.Trips.TripColumns.TRIP_ID);
        if (cursor!= null && (cursor.moveToFirst())){
            do {
                tables.StopTime item = new tables.StopTime(
                        cursor.getInt(cursor.getColumnIndex(StarContract.StopTimes.StopTimeColumns.TRIP_ID)),
                        cursor.getString(cursor.getColumnIndex(StarContract.StopTimes.StopTimeColumns.ARRIVAL_TIME)),
                        cursor.getString(cursor.getColumnIndex(StarContract.StopTimes.StopTimeColumns.DEPARTURE_TIME)),
                        cursor.getInt(cursor.getColumnIndex(StarContract.StopTimes.StopTimeColumns.STOP_ID)),
                        cursor.getString(cursor.getColumnIndex(StarContract.StopTimes.StopTimeColumns.STOP_SEQUENCE))
                );
                Log.d("STARXTEST", "from provider ..." + item);
            } while (cursor.moveToNext());
        }
    }


    /**
     * Les horaires de passage d'un trip sur tous les arrêt à partir d'une heure donnée jusqu'au terminus
     * en input : id_trip, heure(21:59:00)
     * Accès à toutes les colonnes de stoptime, stop, trip sont disponibles pour chaque ligne
     */
    private void fragment4Query() {
        Uri stopsUri;
        stopsUri = Uri.withAppendedPath(StarContract.AUTHORITY_URI, StarContract.StopTimes.CONTENT_ITEM_TYPE);
        String[] selargs = {"19061", "22:30:00"};
        CursorLoader cl = new CursorLoader(this,stopsUri,
                null, null, selargs,
                null);
        Cursor cursor = cl.loadInBackground();
        Log.d("STARXTEST", "from provider ... start" );
        while (cursor.moveToNext()) {
            tables.StopTime item = new tables.StopTime(
                    cursor.getInt(cursor.getColumnIndex(StarContract.StopTimes.StopTimeColumns.TRIP_ID)),
                    cursor.getString(cursor.getColumnIndex(StarContract.StopTimes.StopTimeColumns.ARRIVAL_TIME)),
                    cursor.getString(cursor.getColumnIndex(StarContract.StopTimes.StopTimeColumns.DEPARTURE_TIME)),
                    cursor.getInt(cursor.getColumnIndex(StarContract.StopTimes.StopTimeColumns.STOP_ID)),
                    cursor.getString(cursor.getColumnIndex(StarContract.StopTimes.StopTimeColumns.STOP_SEQUENCE))
            );
                Log.d("STARXTEST", "from provider ..." +item);
        }
        Log.d("STARXTEST", "from provider ...end");
    }

    public void testStopsProvider() {
        Uri stopsUri = Uri.withAppendedPath(StarContract.AUTHORITY_URI, StarContract.Stops.CONTENT_PATH + "/");
        Cursor cursor = managedQuery(stopsUri,
                null, null, null,
                StarContract.Stops.StopColumns.STOP_ID);
        if (cursor.moveToFirst()) {
            do {
                tables.Stop item = new tables.Stop(
                        cursor.getString(cursor.getColumnIndex(StarContract.Stops.StopColumns.STOP_ID)),
                        cursor.getString(cursor.getColumnIndex(StarContract.Stops.StopColumns.NAME)),
                        cursor.getString(cursor.getColumnIndex(StarContract.Stops.StopColumns.DESCRIPTION)),
                        cursor.getFloat(cursor.getColumnIndex(StarContract.Stops.StopColumns.LATITUDE)),
                        cursor.getFloat(cursor.getColumnIndex(StarContract.Stops.StopColumns.LONGITUDE)),
                        cursor.getString(cursor.getColumnIndex(StarContract.Stops.StopColumns.WHEELCHAIR_BOARDING))
                );
                Log.d("STARXTEST", "Received from provider ..." + item);
            } while (cursor.moveToNext());
        }
    }

    public void testTripsProvider() {
        Log.d("STARX", "Test TripsProvider");
        Uri tripsUri = Uri.withAppendedPath(StarContract.AUTHORITY_URI, StarContract.Trips.CONTENT_PATH + "");
        Cursor cursor = managedQuery(tripsUri,
                null, null, null,
                StarContract.Trips.TripColumns.ROUTE_ID);

        int i = 0;
        if (cursor.moveToFirst()) {
            do {
                Trip item = new Trip(
                        cursor.getInt(cursor.getColumnIndex(StarContract.Trips.TripColumns.TRIP_ID)),
                        cursor.getInt(cursor.getColumnIndex(StarContract.Trips.TripColumns.ROUTE_ID)),
                        cursor.getInt(cursor.getColumnIndex(StarContract.Trips.TripColumns.SERVICE_ID)),
                        cursor.getString(cursor.getColumnIndex(StarContract.Trips.TripColumns.HEADSIGN)),
                        cursor.getInt(cursor.getColumnIndex(StarContract.Trips.TripColumns.DIRECTION_ID)),
                        cursor.getString(cursor.getColumnIndex(StarContract.Trips.TripColumns.BLOCK_ID)),
                        cursor.getString(cursor.getColumnIndex(StarContract.Trips.TripColumns.WHEELCHAIR_ACCESSIBLE))
                );
                i++;
                Log.d("STARXTEST", i + "-Received from provider ..." + item);
            } while (cursor.moveToNext());
        }
    }

    public void testCalendarProvider() {
        // Retrieve student records
        Uri calendarUri = Uri.withAppendedPath(StarContract.AUTHORITY_URI, StarContract.Calendar.CONTENT_PATH + "");
        Cursor cursor = managedQuery(calendarUri,
                null, null, null,
                StarContract.Calendar.CalendarColumns.START_DATE);
        long count = 0;
        if (cursor.moveToFirst()) {
            do {
                count++;
                tables.Calendar item = new tables.Calendar(
                        cursor.getInt(cursor.getColumnIndex(StarContract.Calendar.CalendarColumns.SERVICE_ID)),
                        cursor.getString(cursor.getColumnIndex(StarContract.Calendar.CalendarColumns.MONDAY)),
                        cursor.getString(cursor.getColumnIndex(StarContract.Calendar.CalendarColumns.TUESDAY)),
                        cursor.getString(cursor.getColumnIndex(StarContract.Calendar.CalendarColumns.WEDNESDAY)),
                        cursor.getString(cursor.getColumnIndex(StarContract.Calendar.CalendarColumns.THURSDAY)),
                        cursor.getString(cursor.getColumnIndex(StarContract.Calendar.CalendarColumns.FRIDAY)),
                        cursor.getString(cursor.getColumnIndex(StarContract.Calendar.CalendarColumns.SATURDAY)),
                        cursor.getString(cursor.getColumnIndex(StarContract.Calendar.CalendarColumns.SUNDAY)),
                        cursor.getString(cursor.getColumnIndex(StarContract.Calendar.CalendarColumns.START_DATE)),
                        cursor.getString(cursor.getColumnIndex(StarContract.Calendar.CalendarColumns.END_DATE))
                );
                Log.d("STARXCA", count + "-Received from provider ..." + item);
            } while (cursor.moveToNext());
        }
    }

    public void testStopTimesProvider() {
        Log.d("STARX", "Test StopTimesProvider");
        Uri tripsUri = Uri.withAppendedPath(StarContract.AUTHORITY_URI, StarContract.StopTimes.CONTENT_PATH + "");
        Cursor cursor = getContentResolver().query(tripsUri,
                null, null, null,
                StarContract.StopTimes.StopTimeColumns.TRIP_ID);
        long count = 0;
        if (cursor!= null && (cursor.moveToFirst())){
            do {
                count++;
                tables.StopTime item = new tables.StopTime(
                        cursor.getInt(cursor.getColumnIndex(StarContract.StopTimes.StopTimeColumns.TRIP_ID)),
                        cursor.getString(cursor.getColumnIndex(StarContract.StopTimes.StopTimeColumns.ARRIVAL_TIME)),
                        cursor.getString(cursor.getColumnIndex(StarContract.StopTimes.StopTimeColumns.DEPARTURE_TIME)),
                        cursor.getInt(cursor.getColumnIndex(StarContract.StopTimes.StopTimeColumns.STOP_ID)),
                        cursor.getString(cursor.getColumnIndex(StarContract.StopTimes.StopTimeColumns.STOP_SEQUENCE))
                );
                Log.d("STARXTEST", count + "Received from provider ..." + item);
            } while (cursor.moveToNext());
        }
    }


    public void getJson2(String url) {
        AsyncHttpClient client = new AsyncHttpClient();
        final ArrayList<String> listResult = new ArrayList<String>();
        client.get("" + url, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                try {

                    Log.e("XXXX", " + baka " + response.toString());
                    JSONArray reords = response.getJSONArray("records");

                    JSONObject object1 = (JSONObject) reords.get(0);

                    JSONObject object2 = (JSONObject) object1.get("fields");

                    listResult.add(object2.get("url").toString());
                    listResult.add(object2.get("id").toString());

                    //   downZip(object2.get("url").toString());

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //Json object is returned as a response
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Log.e("XXXX", "==> PROBLEME DE CHARGEMENRT <==");
            }
        });

    }
}
