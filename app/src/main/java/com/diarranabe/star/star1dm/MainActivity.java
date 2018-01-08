package com.diarranabe.star.star1dm;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static android.os.Environment.getExternalStorageDirectory;


public class MainActivity extends AppCompatActivity {

    //initialize our progress dialog/bar
    private ProgressDialog mProgressDialog;
    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    DatabaseHelper databaseHelper;

    private static String PREF = "frist";
    SharedPreferences sharedPreferences;

    private static final long DELAY = 30*1000*60;// Delai avant de lancer le service
    private static final long PERIOD = 24*60*60*1000; // Intervalle de temps de vérification de nouvelle version
    private long attempt = 0;
    private String DATA_URL1 = "";
    private String DATA_URL2 = "";
    private String DATA_URL1_LAST_UPDATE = "";
    private String DATA_URL2_LAST_UPDATE = "";

    //Absolu path whre file are unZip
    private String exportPath = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHelper = new DatabaseHelper(getApplicationContext());
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);
        sharedPreferences = getSharedPreferences(PREF, MODE_PRIVATE);
        onNewIntent(getIntent());
        Intent intent = new Intent(this, CheckStarDataService.class);
        startService(intent);
        Log.d("STARX", "start");


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
                downloadDezip(file1, file2);

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
    public void getJsonInfos(String url) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("" + url, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.e("XXXX", "" + response.toString());

                try {
                    JSONArray reords = response.getJSONArray("records");

                    JSONObject object1 = (JSONObject) reords.get(0);

                    JSONObject object2 = (JSONObject) object1.get("fields");

                    //  downloadDezip(object2.get("url").toString());

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
    public void downloadDezip(String zipFileUrl, final String file2) {
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

                    /**
                     * Lance le deuxième fichier
                     */
                    downloadDezip(file2);
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
            }
        });
    }


    public void downloadDezip(String zipFileUrl) {
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
            }
        });
    }

    /**
     * Telecharge le premier fichier
     */
    public void loadFirstFileData() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(Constants.DATA_SOURCE_URL, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
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
                        downloadDezip(file_url);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_EXTERNAL_STORAGE :
                if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    if (DatabaseHelper.getVersions(getApplicationContext()).get(0).equals(Constants.DEFAULT_FIRST_VERSION)){ // premier lancement
                        loadFirstFileData();
                    }
                }else{
                    Toast.makeText(this,"Permission obligatoire pour charger les données", Toast.LENGTH_LONG).show();
                }
                break;

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

                    //   downloadDezip(object2.get("url").toString());

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

    public void updateClick(View view) {
        checkStarVersions();
    }

    public void checkStarVersions() {
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            getVersionsInfos();
                            attempt++;
                        } catch (Exception e) {
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, DELAY, PERIOD);
    }

    private void notifyActivity() {

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("New data are available")
                        .setContentText("Update the database");

        Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(getString(R.string.data1_url_id), DATA_URL1);
        bundle.putString(getString(R.string.data2_url_id), DATA_URL2);
        bundle.putString(getString(R.string.data1_date_id), DATA_URL1_LAST_UPDATE);
        bundle.putString(getString(R.string.data2_date_id), DATA_URL2_LAST_UPDATE);
        resultIntent.putExtras(bundle);

        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        getApplicationContext(),
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        // Sets an ID for the notification
        int mNotificationId = 001;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }

    /**
     * connexion to Star Api to get url of zip and id of traject
     *
     * @return
     */
    public void getVersionsInfos() {
        AsyncHttpClient client = new AsyncHttpClient();

        client.get(""+Constants.DATA_SOURCE_URL,
                new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            JSONArray reords = response.getJSONArray("records");
                            /**
                             *Traitement du premier fichier
                             */
                            JSONObject file1 = (JSONObject) reords.get(0);
                            JSONObject file12 = (JSONObject) file1.get("fields");
                            JSONObject fichier1 = (JSONObject) file12.get("fichier");
                            String last_sync1 = (String) fichier1.get("last_synchronized");
                            String newData1 = file12.get("url").toString();

                            /**
                             * Traitement du second fichier
                             */
                            JSONObject file2 = (JSONObject) reords.get(1);
                            JSONObject file22 = (JSONObject) file2.get("fields");
                            JSONObject fichier2 = (JSONObject) file22.get("fichier");
                            String last_sync2 = (String) fichier2.get("last_synchronized");
                            String newData2 = file22.get("url").toString();

                            ArrayList<String> versions = DatabaseHelper.getVersions(getApplicationContext());
                            if (!(versions.get(0).equals(last_sync1)) || !(versions.get(1).equals(last_sync2))) {
                                DATA_URL1 = newData1;
                                DATA_URL2 = newData2;
                                DATA_URL1_LAST_UPDATE = last_sync1;
                                DATA_URL2_LAST_UPDATE = last_sync2;
                                notifyActivity();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers, responseString, throwable);
                        Log.e("STARX", "==> PROBLEME DE CHARGEMENRT <==");
                    }
                });
    }
}
