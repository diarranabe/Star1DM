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

import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;

/**
 * Created by diarranabe on 04/01/2018.
 */

public class CheckStarDataService extends Service {
    private String DATA_SOURCE_URL = "https://data.explore.star.fr/api/records/1.0/search/?dataset=tco-busmetro-horaires-gtfs-versions-td&sort=-debutvalidite";
    private long attempt = 0;
    private String DATA_URL1 = "";
    private String DATA_URL2 = "";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO do something useful
        callAsynchronousTask();
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        //TODO for communication return IBinder implementation
        return null;
    }


    public void callAsynchronousTask() {
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            getJson();
                            Log.d("STARX", "My Service ok ! " + attempt);
                            attempt++;
//                            PerformBackgroundTask performBackgroundTask = new PerformBackgroundTask();
                            // PerformBackgroundTask this class is the class that extends AsynchTask
//                            performBackgroundTask.execute();
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 10000, 50000); //execute in every 50000 ms
    }

    private void notifyActivity() {

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("New data available")
                        .setContentText("Update the database");

        Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(getString(R.string.data_url_brodcast_id), DATA_URL1);
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
    public void  getJson() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(DATA_SOURCE_URL, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                try {
                    JSONArray reords = response.getJSONArray("records");

                    JSONObject object1 = (JSONObject) reords.get(0);

                    JSONObject object2 = (JSONObject) object1.get("fields");

                    String newData = (String) object2.get("url").toString();

                    if (!DATA_URL1.equals(newData)){
                        DATA_URL1 = newData;
                        notifyActivity();
                        Log.d("STARX", "new data url " + DATA_URL1);
                    }
                    Log.d("STARX", "database is up to date ");
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