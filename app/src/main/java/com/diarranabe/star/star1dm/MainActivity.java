package com.diarranabe.star.star1dm;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

import java.util.ArrayList;



public class MainActivity extends AppCompatActivity {
TextView textView;
    private String testUrl = "https://data.explore.star.fr/api/records/1.0/search/?dataset=tco-busmetro-horaires-gtfs-versions-td";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.texte);
        DatabaseHelper db = new DatabaseHelper(this);

        //String txt = getJSON(testUrl);
//        textView.setText(populate().get(0).toString());

        String url = "https://data.explore.star.fr/api/records/1.0/search/?dataset=tco-busmetro-horaires-gtfs-versions-td&sort=-debutvalidite";
        getJson(url);

        Log.d("XXXX","end");
    }



    public ArrayList<String> getJson(String url){
        AsyncHttpClient client = new AsyncHttpClient();
        final ArrayList<String> listResult = new ArrayList<String>() ;
        client.get(""+url,new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                try {
                    JSONArray reords =  response.getJSONArray("records") ;

                    JSONObject object1 = (JSONObject) reords.get(0);

                    JSONObject object2 = (JSONObject) object1.get("fields");

                    listResult.add(object2.get("url").toString()) ;
                    listResult.add(object2.get("id").toString()) ;

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //Json object is returned as a response
            }
        });
        return listResult ;
    }





}
