package com.egloos.hyunyi.musicinfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by Hyun-Yi on 2015-06-04.
 */
public class musicInfoBCR extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        if (!prefs.getBoolean("IS_SERVICE_ON", false)&&!prefs.getBoolean("AUTO_START", false)) return;

        String action = intent.getAction();
        Bundle b = intent.getExtras();
        String artist = null;
        String album = null;
        String track = null;
        Boolean playing = null;
        String player_type = null;

        //Log.i("musicInfo", "musicInfoBCR ON! " + intent.toString());

        Boolean isPlaying = intent.getExtras().getBoolean("playing");

        //Log.v("musicInfo", action + " / " + isPlaying.toString());

        Log.d("musicInfo", intent.toString());
        for (String key : b.keySet()) {
            Object value = b.get(key);
            if (value!=null) Log.d("musicInfo", String.format("%s %s (%s)", key, value.toString(), value.getClass().getName()));
        } //intent 열람용


        Intent i = new Intent(context, MainService.class);
        Bundle info = new Bundle();

        if (action.contains("playstatechanged")||action.contains("metachanged")) {

            artist = b.getString("artist");
            album = b.getString("album");
            track = b.getString("track");
            playing = b.getBoolean("playing");
            //player_type = "genie";

            if (b.containsKey("currentSongLoaded")) {
                player_type = "google";
                Log.d("musicInfo", "currentSongLoaded is exist. GOOGLE!");
            } else if (b.containsKey("playstate")) {
                player_type = "melon";
                Log.d("musicInfo", "currentSongLoaded is not exist. Melon!");
            } else {
                player_type = "genie";
                Log.d("musicInfo", "currentSongLoaded is not exist. GENIE!");
            }



        } else if (action.contains("playbackstatechanged")||action.contains("metadatachanged")) {
            artist = b.getString("artist");
            album = b.getString("album");
            track = b.getString("track");
            playing = b.getBoolean("playstate");
            player_type = "spotify";

        } else if (action.contains("com.sonyericsson.music.playbackcontrol.ACTION_TRACK_STARTED")) {

            artist = b.getString("ARTIST_NAME");
            album = b.getString("ALBUM_NAME");
            track = b.getString("TRACK_NAME");
            playing = true;
            player_type = "bluetooth";

        } else if (action.contains("com.sonyericsson.music.playbackcontrol.ACTION_PAUSED")) {

            artist = b.getString("ARTIST_NAME");
            album = b.getString("ALBUM_NAME");
            track = b.getString("TRACK_NAME");
            playing = false;
            player_type = "bluetooth";

        } else {
            /*
            Intent i = new Intent(context, MainService.class);
            Log.i("musicInfo", "STOP!" + i.toString());
            context.stopService(i);
            */
        }

        Log.v("musicInfo", artist + ":" + album + ":" + track);
        //if ((artist==null)||(album==null)||(track==null)) return;

        info.putString("artist", artist);
        info.putString("album", album);
        info.putString("track", track);
        info.putBoolean("playing", playing);
        info.putString("player type", player_type);

        i.putExtras(info);
        context.startService(i);
    }
}
