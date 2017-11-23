package com.diarranabe.star.star1dm;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import static android.system.Os.close;


public class MainActivity extends AppCompatActivity {

    //initialize our progress dialog/bar
    private ProgressDialog mProgressDialog;
    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;

    //Absolu path whre file are unZip
    private String exportPath = "" ;

    private String testUrl = "https://data.explore.star.fr/api/records/1.0/search/?dataset=tco-busmetro-horaires-gtfs-versions-td&sort=-debutvalidite";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);
        DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        //databaseHelper.onCreate(db);

        Cursor cursor = null;
        try {
            db = databaseHelper.getReadableDatabase();
            databaseHelper.onCreate(db);
            databaseHelper.insertStops();

        } finally {

        }
/*

        Log.e("XXXX","Downlod zip");
        downZip("https://data.opendatasoft.com/explore/dataset/tco-busmetro-horaires-gtfs-versions-td@keolis-rennes/files/b26bfe4a51cc73721585d61b740cf04e/download/");
        Log.e("XXXX","Downlod zip");
*/

       /* try {
            ArrayList<tables.Calendar> ca = DatabaseHelper.loadCalendarData("calendar.txt");
            ArrayList<tables.BusRoute> br = DatabaseHelper.loadBusRoutesData("routes.txt");
            ArrayList<tables.Stop> stops = DatabaseHelper.loadStopsData("stops.txt");
//            ArrayList<tables.StopTime> stopsTimes = DatabaseHelper.loadStopTimesData("stop_times.txt");
            ArrayList<tables.Trips> trips = DatabaseHelper.loadTripsData("trips.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }*/
       /* File fil = new File(Environment.getExternalStorageDirectory(),"agency.txt");
        FileReader file = null;
        try {
            Log.d("XXXX","load start : ");
            file = new FileReader(fil);
        } catch (FileNotFoundException e) {
            Log.d("XXXX","load fail : ");

            e.printStackTrace();
        }
        BufferedReader buffer = new BufferedReader(file);
        String line = "";
        try {
            while ((line = buffer.readLine()) != null) {
                Log.d("XXXX","content : "+line);
            }
        } catch (IOException e) {
            Log.d("XXXX","show exception : ");

            e.printStackTrace();
        }*/
    }


    /**
     * Permite to download Zip file
     *
     * @param ursZuip
     */
    public void downZip(String ursZuip) {
        final String SourceFilname = "" + ursZuip;
        AsyncHttpClient client = new AsyncHttpClient();
        String[] allowedType = {

                "application/zip"
        };
        client.get(SourceFilname, new BinaryHttpResponseHandler(allowedType) {

            @Override
            public void onStart() {
                super.onStart();
                showDialog(DIALOG_DOWNLOAD_PROGRESS);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] binaryData) {

                Log.e("XXXX", "success start");

                try {

                    //Splitting a File Name from SourceFileName
                    String DestinationName = SourceFilname.substring(SourceFilname.lastIndexOf('/') + 1, SourceFilname.length());
                    //Saving a File into Download Folder
                    File _f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), DestinationName);


                    Log.e("XXXX", "success before crash");

                    FileOutputStream output = new FileOutputStream(_f);// Stopped here


                    Log.e("XXXX", "success try");
                    output.write(binaryData);
                    output.close();
                    Log.e("XXXX", "" + _f);

                    // Debut du deziping
                    exportPath = _f.getAbsolutePath();
                    exportPath = exportPath.replace(".zip", "");
                    Log.e("XXXX", "==> " + exportPath);

                    exportPath = exportPath + "/" ;

                    // decropress file in folder whith id name
                    DecompressFast df = new DecompressFast(_f.getAbsolutePath(), exportPath);
                    df.unzip();


                } catch (IOException e) {
                    Log.e("XXXX", "success catch");

                    e.printStackTrace();
                }
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
                int val = (int) ((bytesWritten * 100) / totalSize);
                Log.d("XXXX", "downloading ..... "+val);
                mProgressDialog.setProgress(val);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] binaryData, Throwable error) {

                Log.e("XXXX", "==> " + error);

            }

            @Override
            public void onFinish() {
                super.onFinish();
                dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
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
     *  Methode to get permision to user .
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

}
