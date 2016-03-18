package com.egloos.hyunyi.musicinfo;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by HYUNYIK on 2015-12-24.
 */
public class ArtistInfo {

    private String artist_name;
    Boolean echonest_complete;
    Boolean lastFM_complete;
    JSONObject jArtistInfo;

    public ArtistInfo(String artist_name) {
        this.artist_name = artist_name;
    }

    private void getArtistInfo(final String artist_name) {

        //if(msg==null) msg="";

        echonest_complete = false;
        lastFM_complete = false;

        final String msg = artist_name.replaceAll("\\p{Punct}", " ").replaceAll("\\p{Space}", "+");
        final String fm_name = artist_name.replaceAll("\\p{Space}", "");

        jArtistInfo = new JSONObject();

        Log.i("musicInfo", msg + " " + fm_name);

        AsyncHttpClient client = new AsyncHttpClient();

        String URL = "http://developer.echonest.com/api/v4/artist/profile?api_key=4WFJ845O28OVPQQ9T&name=" + msg + "&bucket=urls&bucket=images&bucket=video";
        Log.i("musicInfo", URL);

        client.get(URL, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String r = new String(responseBody);
                try {
                    JSONObject json = new JSONObject(r);
                    json = json.getJSONObject("response");
                    JSONObject status = json.getJSONObject("status");

                    if (status.getString("message").contains("Success")) {
                        //JSONObject artist = json.getJSONObject("artist");
                        //b.putString("artist_info", artist.toString());

                        jArtistInfo.put("artist", json.getJSONObject("artist"));

                        echonest_complete = true;
                        if (lastFM_complete==true) {
                            //initLinkPopUp(artist_name);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });


        String fmURL = "http://ws.audioscrobbler.com/2.0/?method=artist.getinfo&artist=" + msg + "&autocorrect[1]&format=json&api_key=ca4c10f9ae187ebb889b33ba12da7ee9";
        Log.i("musicInfo", fmURL);

        client.get(fmURL, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String r = new String(responseBody);
                try {
                    Log.i("musicInfo", "Communicating with LastFM...");

                    JSONObject json = new JSONObject(r);
                    Log.i("musicInfo", json.toString());

                    json = json.getJSONObject("artist");
                    Log.i("musicInfo", json.toString());

                    JSONArray artist_images = json.optJSONArray("image");
                    Log.i("musicInfo", artist_images.toString());

                    for (int i=0;i<artist_images.length();i++) {
                        JSONObject j = artist_images.getJSONObject(i);
                        Log.i("musicInfo", j.optString("size"));
                        if (j.optString("size").contains("extralarge")) {
                            jArtistInfo.put("fm_image", j.getString("#text"));
                            //b.putString("fm_image", j.getString("#text"));
                            //Log.i("musicInfo", j.getString("#text"));
                            break;
                        }
                    }

                    lastFM_complete = true;
                    if (echonest_complete==true) {
                        //initLinkPopUp(artist_name);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });



    }

}
