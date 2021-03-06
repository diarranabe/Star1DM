package com.diarranabe.star.star1dm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;

/**
 * Created by diarranabe on 04/01/2018.
 */

public class CheckStarDataService extends Service {
    public static final long DELAY = 30*1000*60;// Delai avant de lancer le service pour permettre que le premier
                                                    // soit télechargé et inseré
    public static final long PERIOD = 60*60*1000; // Intervalle de temps de vérification de nouvelle version
    private String DATA_URL1 = "";
    private String DATA_URL2 = "";
    private String DATA_URL1_LAST_UPDATE = "";
    private String DATA_URL2_LAST_UPDATE = "";


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        checkStarVersions();
        return Service.START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
                            Log.d("STARX", "check versions start");

                            getVersionsInfos();

                            Log.d("STARX", "Service ok ! ");
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

        MainActivity.button.setTextSize(18);
        MainActivity.button.setText("Ouvrez la notification pour mettre les données à jour");
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
                                Log.d("STARX", "new data url1: " + DATA_URL1 + ", date: " + DATA_URL1_LAST_UPDATE);
                                Log.d("STARX", "new data url1: " + DATA_URL2 + ", date: " + DATA_URL2_LAST_UPDATE);
                            }
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


    /**
     * @param in      : buffer with the php result
     * @param bufSize : size of the buffer
     * @return : the string corresponding to the buffer
     */
    public static String InputStreamToString(InputStream in, int bufSize) {
        final StringBuilder out = new StringBuilder();
        final byte[] buffer = new byte[bufSize];
        try {
            for (int ctr; (ctr = in.read(buffer)) != -1; ) {
                out.append(new String(buffer, 0, ctr));
            }
        } catch (IOException e) {
            throw new RuntimeException("Ne peut pas convertir un stream en string", e);
        }
        // On retourne la chaine contenant les donnees de l'InputStream
        return out.toString();
    }

    public static String InputStreamToString(InputStream in) {
        // On appelle la methode precedente avec une taille de buffer par defaut
        return InputStreamToString(in, 1024);
    }

}