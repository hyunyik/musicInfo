package com.egloos.hyunyi.musicinfo;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.*;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;

import static com.egloos.hyunyi.musicinfo.R.drawable.control_anim;

public class MainService extends Service {
    private View mView;
    private WindowManager mManager;
    private WindowManager.LayoutParams mParams;
    private float mTouchX, mTouchY;
    private int mViewX, mViewY;

    private boolean isMove = false;

    private TextView albumInfo;
    private TextView songInfo;
    private ImageView bLyrics;
    private RelativeLayout lMusicInfo;
    private LinearLayout lArtistList;
    private ImageView bMusicInfoClose;
    private ImageView bMusicInfoOpen;
    private ImageView iLambFoot;
    private ImageView iLambBody;
    //private RelativeLayout lFloatingView;
    private NotificationCompat.Builder mBuilder;
    private RelativeLayout lControlBox;

    private String artist;
    private String album;
    private String track;
    private String player_type;
    private int defaultX;
    private float density;
    private Boolean isPlaying;
    private ImageView control_balloon_button;
    private ImageView control_balloon_background;
    private JSONObject jArtistInfo;
    private Bundle b = new Bundle();

    Boolean echonest_complete;
    Boolean lastFM_complete;


    //private boolean isServiceRunning;

    private View.OnTouchListener mViewTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {



            SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
            //Log.d("musicInfo", event.toString());
            switch (event.getAction()&MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    Log.d("musicInfo", "Single-touch!");
                    lMusicInfo.setSelected(false);
                    lMusicInfo.setSelected(true);

                    isMove = false;

                    mTouchX = event.getRawX();
                    mTouchY = event.getRawY();
                    mViewX = mParams.x;
                    mViewY = mParams.y;
                    break;

                case MotionEvent.ACTION_POINTER_DOWN:
                    Log.d("musicInfo", "Multi-touch!");
                    if (lControlBox.getVisibility()==View.GONE) {
                        lControlBox.setVisibility(View.VISIBLE);
                    } else {
                        lControlBox.setVisibility(View.GONE);
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    if (!isMove) {
                        return false;
                        /*
                        Rect lyric_r = new Rect();
                        bLyrics.getHitRect(lyric_r);

                        Rect b_open_r = new Rect();
                        bMusicInfoOpen.getHitRect(b_open_r);

                        int[] location = new int[2];

                        mView.getLocationOnScreen(location);

                        int touch_x = (int) (event.getRawX() - location[0]);
                        int touch_y = (int) (event.getRawY() - location[1]);

                        if (lyric_r.contains(touch_x, touch_y)) {
                            Toast.makeText(getApplicationContext(), "Open Lyrics...", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.addCategory(Intent.CATEGORY_BROWSABLE);
                            intent.setData(Uri.parse((String) bLyrics.getTag()));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else if (b_open_r.contains(touch_x, touch_y)) {
                            lMusicInfo.setVisibility(View.VISIBLE);
                            bMusicInfoOpen.setVisibility(View.GONE);

                            if ((iLambFoot.getVisibility()==View.INVISIBLE)&&(iLambBody.getTranslationX()<0)) {
                                iLambFoot.setVisibility(View.VISIBLE);
                                iLambBody.setTranslationX(0);
                                bMusicInfoOpen.setTranslationX(0);
                                bLyrics.setTranslationX(0);
                            }

                        }

                        */

                    } else {
                        int x = (int) (event.getRawX() - mTouchX);
                        int y = (int) (event.getRawY() - mTouchY);
                        int minD = 30;
                        int tx;
                        Boolean OnMoveX;
                        Boolean OffMoveX;

                        switch (prefs.getInt("Location", 0)) {
                            case 0:
                                //minD = -10;
                                OnMoveX = (x<-minD);
                                OffMoveX = (x>minD);
                                tx = -42;
                                break;
                            case 1:
                                //minD = 10;
                                OnMoveX = (x>minD);
                                OffMoveX = (x<-minD);
                                tx = 42;
                                break;
                            default:
                                //minD = -10;
                                OnMoveX = (x<-minD);
                                OffMoveX = (x>minD);
                                tx = -42;
                                break;
                        }

                        if ((Math.abs((double)x/(double)y)>1)&&OnMoveX) {
                            //iLambBody.setTranslationX(tx);
                            //bMusicInfoOpen.setTranslationX(tx);
                            //bLyrics.setTranslationX(tx);
                            mView.setTranslationX(tx);
                            //iLambFoot.setVisibility(View.INVISIBLE);
                            bMusicInfoOpen.setVisibility(View.VISIBLE);
                            lMusicInfo.setVisibility(View.GONE);
                            lControlBox.setVisibility(View.GONE);
                            mParams.width = (int)(50*density);
                            mManager.updateViewLayout(mView, mParams);
                        } else if ((Math.abs((double)x/(double)y)>1)&&OffMoveX) {
                            mView.setTranslationX(0);
                            //iLambFoot.setVisibility(View.VISIBLE);
                            //iLambBody.setTranslationX(0);
                            //bMusicInfoOpen.setTranslationX(0);
                            //bLyrics.setTranslationX(0);


                        }

                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    isMove = true;

                    int x = (int) (event.getRawX() - mTouchX);
                    int y = (int) (event.getRawY() - mTouchY);

                    final int num = 5;
                    if ((x > -num && x < num) && (y > -num && y < num)) {
                        isMove = false;
                        break;
                    }

                    mParams.x = defaultX;

                    mParams.y = mViewY + y;

                    mManager.updateViewLayout(mView, mParams);


                    break;
            }

            return false;
        }
    };


    public MainService() {
        //Log.i("musicInfo", "In MainService Constructor...");
        /*
        metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        density = metrics.density;
        */
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);


        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        density = metrics.density;

        switch (prefs.getInt("Location", 0)) {
            case 0:
                defaultX = 0;
                break;
            case 1:
                defaultX = (int) ((float)metrics.heightPixels * density) - 215;
                break;
            default:
                defaultX = 0;
                break;
        }

        //Log.i("musicInfo", "In MainService onCreate()...");

        //isServiceRunning = true;

        LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        int hGravity;

        switch (prefs.getInt("Location", 0)) {
            case 0:
                mView = mInflater.inflate(R.layout.floating_image, null);
                hGravity = Gravity.LEFT;
                break;
            case 1:
                mView = mInflater.inflate(R.layout.floating_image_right, null);
                hGravity = Gravity.RIGHT;
                break;
            default:
                mView = mInflater.inflate(R.layout.floating_image, null);
                hGravity = Gravity.LEFT;
                break;
        }


        mView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        lMusicInfo = (RelativeLayout) mView.findViewById(R.id.l_musicInfo);
        //lMusicInfo.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        //lMusicInfo.setBackgroundResource(R.drawable.msg_balloon);

        lArtistList = (LinearLayout) mView.findViewById(R.id.l_artist_list);

        albumInfo = (TextView) mView.findViewById(R.id.album_info);
        songInfo = (TextView) mView.findViewById(R.id.song_info);

        bLyrics = (ImageView) mView.findViewById(R.id.bt_lyrics);
        //bLyrics.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        //bLyrics.setImageResource(R.drawable.ballon_lyrics);

        //bMusicInfoClose.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        bMusicInfoClose = (ImageView) mView.findViewById(R.id.bt_musicInfo_close);

        bMusicInfoOpen = (ImageView) mView.findViewById(R.id.bt_musicInfo_open);
        //bMusicInfoOpen.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        //bMusicInfoOpen.setImageResource(R.drawable.ballon_info);

        iLambFoot = (ImageView) mView.findViewById(R.id.lamb_foot);
        //iLambFoot.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        //iLambFoot.setImageResource(R.drawable.music_lamb_foot_r);

        iLambBody = (ImageView) mView.findViewById(R.id.lamb_body);
        //iLambBody.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        //iLambBody.setImageResource(R.drawable.music_lamb_r);



        bLyrics.setVisibility(View.GONE);
        bMusicInfoOpen.setVisibility(View.GONE);

        lControlBox = (RelativeLayout) mView.findViewById(R.id.control_box);
        lControlBox.setVisibility(View.GONE);

        control_balloon_button = (ImageView) mView.findViewById(R.id.control_ballon_button);
        control_balloon_button.setTag("");
        control_balloon_background = (ImageView) mView.findViewById(R.id.control_ballon_background);

        FrameLayout bStartPlayer = (FrameLayout) mView.findViewById(R.id.control_ballon);
        bStartPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                playerToggle();
                control_balloon_button.setImageResource(R.drawable.control_anim);
                AnimationDrawable ad = (AnimationDrawable) control_balloon_button.getDrawable();
                ad.start();
                /*
                Intent i = new Intent("com.android.music.musicservicecommand");
                i.putExtra("command", "play");
                sendBroadcast(i);


                long eventtime = SystemClock.uptimeMillis();

                Intent downintent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
                KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0);
                downintent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
                sendOrderedBroadcast(downintent, null);
                */
            }
        });

        mView.setOnTouchListener(mViewTouchListener);

        bMusicInfoClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mParams.y = mParams.y + lMusicInfo.getHeight();
                //mManager.updateViewLayout(mView, mParams);
                lMusicInfo.setVisibility(View.INVISIBLE);
                bMusicInfoOpen.setVisibility(View.VISIBLE);
                lControlBox.setVisibility(View.GONE);
                mParams.width = (int)(50*density);
                mManager.updateViewLayout(mView, mParams);
            }
        });

        bLyrics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Open Lyrics...", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse((String) bLyrics.getTag()));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });


        bMusicInfoOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mParams.y = mParams.y - lMusicInfo.getHeight();
                //mManager.updateViewLayout(mView, mParams);
                lMusicInfo.setVisibility(View.VISIBLE);
                bMusicInfoOpen.setVisibility(View.GONE);
                mParams.width = (int)(215*density);
                mManager.updateViewLayout(mView, mParams);

                /*
                if ((iLambFoot.getVisibility()==View.INVISIBLE)&&(iLambBody.getTranslationX()<0)) {
                    iLambFoot.setVisibility(View.VISIBLE);
                    iLambBody.setTranslationX(0);
                    bMusicInfoOpen.setTranslationX(0);
                    bLyrics.setTranslationX(0);
                }
                */
            }
        });

        mParams = new WindowManager.LayoutParams((int)(215*density), WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        mParams.gravity = Gravity.TOP | Gravity.LEFT;
        mParams.windowAnimations = android.R.style.Animation;

        mManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mManager.addView(mView, mParams);


        int a = 255 * prefs.getInt("TRANSPARENCY", 100) / 100;
        lMusicInfo.getBackground().setAlpha(a);
        iLambBody.getDrawable().setAlpha(a);
        iLambFoot.getDrawable().setAlpha(a);
        bLyrics.getDrawable().setAlpha(a);
        bMusicInfoOpen.getDrawable().setAlpha(a);
        control_balloon_background.getDrawable().setAlpha(a);

        mParams.x = defaultX;
        mParams.y = prefs.getInt("mViewY", 0);
        mManager.updateViewLayout(mView, mParams);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("IS_SERVICE_ON", true);
        editor.commit();



        if (mBuilder==null) {
            Intent stopIntent = new Intent(getApplicationContext(), MainService.class);
            stopIntent.setComponent(new ComponentName("com.egloos.hyunyi.musicinfo", "com.egloos.hyunyi.musicinfo.MainService"));
            stopIntent.setAction("com.egloos.hyunyi.musicinfo.service.STOP");
            PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, stopIntent, 0);

            mBuilder = new NotificationCompat.Builder(getApplicationContext()).setSmallIcon(R.drawable.ballon_info_s).setContentTitle("Music Lamb").setContentText("Music Lamb is running...").setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.music_lamb)).setAutoCancel(true);
            mBuilder.setContentIntent(pendingIntent);
            this.startForeground(3333, mBuilder.build());
        }
    }

    @Override
    public void onDestroy() {

        if (mView != null) {
            mManager.removeView(mView);
            mView = null;
        }
        //unregisterReceiver(myBroadcastReceiver);
        //isServiceRunning = false;
        unbindDrawables(iLambFoot);
        unbindDrawables(iLambBody);
        unbindDrawables(lMusicInfo);
        unbindDrawables(bLyrics);

        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("IS_SERVICE_ON", false);
        editor.putInt("mViewX", mParams.x);
        editor.putInt("mViewY", mParams.y);
        editor.commit();
        super.onDestroy();

    }

    private void unbindDrawables(View view) {
        if (view == null)
            return;

        if (view instanceof ImageView) {
            ((ImageView) view).setImageDrawable(null);
            //Log.d("musicInfo", view.toString() + " was nullified!");
        }
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
            //Log.d("musicInfo", view.toString() + " background was nullified!");
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            view.setBackgroundResource(0);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Log.i("musicInfo", "In onStartCommand!");

        Bundle b = intent.getExtras();

        /*

        if((b!=null)&&b.getString("command", null)!=null) {
            if (b.getString("command")=="play") {
                isPlaying = true;
                control_balloon_button.setImageResource(R.drawable.control_play);
            } else {
                isPlaying = false;
                control_balloon_button.setImageResource(R.drawable.control_pause);
            }
        }

        */

        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);

        int tTime = prefs.getInt("TickTime", 5);

        if (intent.getAction()=="com.egloos.hyunyi.musicinfo.SET_TRANSPARENCY") {
            //Log.d("musicInfo", "Set transparency = " + intent.getIntExtra("transparency", 100));
            int a = 255 * intent.getIntExtra("transparency", 100) / 100;
            lMusicInfo.getBackground().setAlpha(a);
            iLambBody.getDrawable().setAlpha(a);
            iLambFoot.getDrawable().setAlpha(a);
            bLyrics.getDrawable().setAlpha(a);
            bMusicInfoOpen.getDrawable().setAlpha(a);
            control_balloon_background.getDrawable().setAlpha(a);
            return 0;
        }

        if (intent.getAction()=="offMusicInfo") {
            mParams.width = (int)(50*density);
            mManager.updateViewLayout(mView, mParams);
            lMusicInfo.setVisibility(View.GONE);
            return 0;
        }

        if (intent.getAction()=="com.egloos.hyunyi.musicinfo.service.STOP") stopSelf();
        if (b==null) return 0;

        if (b.containsKey("playing")) {
            Log.d("musicinfo", control_balloon_button.getResources().toString());

            if (b.getBoolean("playing", false)) {
                if (!control_balloon_button.getTag().equals("pause")) control_balloon_button.setImageResource(R.drawable.control_pause);
                control_balloon_button.setTag("pause");
                Log.d("musicInfo", "Play");
            } else {
                if (!control_balloon_button.getTag().equals("playing")) control_balloon_button.setImageResource(R.drawable.control_play);
                control_balloon_button.setTag("playing");
                Log.d("musicInfo", "Pause");
            }
        } else {
            Log.d("musicInfo", "No Value");
        }

        if((b.getString("artist")==null)||(b.getString("album")==null)||(b.getString("track")==null))  return 0;

        if((b.getString("artist")==artist)&&(b.getString("album")==album)&&(b.getString("track")==track))  return 0;

        artist = b.getString("artist");
        album = b.getString("album");
        track = b.getString("track");
        player_type = b.getString("player type");

        albumInfo.setText(album);
        albumInfo.setSelected(true);

        songInfo.setText(track);
        songInfo.setSelected(true);

        String artists[] = null;
        lArtistList.removeAllViews();

        if (artist!=null) {
            artists = artist.split(",\\p{Space}");
            int anyParenthesis = artist.indexOf("(");
            if (anyParenthesis!=-1) {

            }
            //artists = artist.split(",\\p{Space}|\\p{Space}&\\p{Space}|\\p{Space}and\\p{Space}");
            //artists = artist.split("\\p{Space}&\\p{Space}");
            //artists = artist.split("\\p{Space}and\\p{Space}");

            for (int i=0;i<artists.length;i++) {
                TextView tv = new TextView(mView.getContext());
                if (artists[i] != null) {
                    getArtistInfo(artists[i]);
                    tv.setTag(artists[i]);
                }

                if (i == 0) {
                    tv.setText("By " + artists[i]);
                } else {
                    tv.setText("Feat. " + artists[i]);
                }

                tv.setTextSize(11);
                tv.setTextColor(Color.DKGRAY);
                tv.setGravity(Gravity.RIGHT);
                tv.setPadding(3, 3, 3, 0);
                lArtistList.addView(tv);
            }
        }

        if (artist!=null) artist = artists[0].replaceAll("\\p{Space}","\\_");
        if (track!=null) track = track.replaceAll("\\p{Space}","\\_");

        SendByHttp("action=lyrics&artist=" + artist + "&song=" + track);

        if (prefs.getBoolean("TickerOn", false)&&(lMusicInfo.getVisibility()!=View.VISIBLE)) {
            mParams.width = (int)(215*density);
            mManager.updateViewLayout(mView, mParams);
            lMusicInfo.setVisibility(View.VISIBLE);
            TimerTask mTask = new TimerTask() {
                @Override
                public void run() {
                    Intent i = new Intent(getApplicationContext(), MainService.class);
                    i.setAction("offMusicInfo");
                    startService(i);
                }
            };

            Timer mTimer = new Timer();
            mTimer.schedule(mTask, tTime*1000);

        }

        return START_REDELIVER_INTENT;
    }

    private void SendByHttp(String msg) {
        if(msg!=null) {

            //msg = msg.replaceAll("\\p{Punct}", "");

            String URL = "http://lyrics.wikia.com/api.php?" + msg + "&fmt=json";
            //Log.v("musicInfo", URL);
            if (!Patterns.WEB_URL.matcher(URL).matches()) return;

            AsyncHttpClient client = new AsyncHttpClient();

            client.get(URL, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String r = new String(responseBody);
                    r = r.replace("song = ", "");
                    Log.i("musicInfo", "HttpResp: " + r);

                    JSONObject json;
                    try {
                        json = new JSONObject(r);
                        if (!json.getString("lyrics").contains("Not found")) {
                            bLyrics.setVisibility(View.VISIBLE);
                            bLyrics.setTag(json.getString("url"));
                        } else {
                            bLyrics.setVisibility(View.GONE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    responseBody = null;
                    r = null;
                    json = null;

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                }
            });
        }
    }

    private void getArtistInfo(final String artist_name) {

        //if(msg==null) msg="";

        //echonest_complete = false;
        //lastFM_complete = false;

        final String msg = artist_name.replaceAll("\\p{Punct}", " ").replaceAll("\\p{Space}", "+");
        //final String fm_name = artist_name.replaceAll("\\p{Space}", "");

        jArtistInfo = new JSONObject();

        //Log.i("musicInfo", msg + " " + fm_name);

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
                        JSONObject artist = json.getJSONObject("artist");

                        //b.putString("artist_info", artist.toString());

                        /*
                        jArtistInfo.put("artist", json.getJSONObject("artist"));

                        echonest_complete = true;
                        if (lastFM_complete==true) {
                            initLinkPopUp(artist_name);
                        }
                        */


                        final Bundle b = new Bundle();
                        b.putString("artist_info", artist.toString());

                        TextView tv = (TextView) lArtistList.findViewWithTag(artist_name);
                        if (tv!=null) {
                            tv.setTextColor(Color.BLUE);
                            tv.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent popupIntent = new Intent(getApplicationContext(), LinkPopUp.class);
                                    popupIntent.putExtras(b);
                                    PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, popupIntent, PendingIntent.FLAG_ONE_SHOT);
                                    try {
                                        pi.send();
                                    } catch (PendingIntent.CanceledException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }

                        responseBody = null;
                        r = null;
                        json = null;
                        status = null;
                        artist = null;

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });

        /*
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
                        initLinkPopUp(artist_name);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
        */


    }

    /*

    private void initLinkPopUp (final String artist_name) {
        final Bundle b = new Bundle();
        TextView tv = (TextView) lArtistList.findViewWithTag(artist_name);
        b.putString("artist_info", jArtistInfo.toString());
        Log.i("musicInfo", jArtistInfo.toString());
        if (tv!=null) {
            tv.setTextColor(Color.BLUE);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent popupIntent = new Intent(getApplicationContext(), LinkPopUp.class);
                    popupIntent.putExtras(b);
                    PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, popupIntent, PendingIntent.FLAG_ONE_SHOT);
                    try {
                        pi.send();
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    */

    private void playerToggle() {
        /*
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri u = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,"1");
        getApplicationContext().startActivity(i);
        */


        if (player_type==null) return;

        long eventtime = SystemClock.uptimeMillis();

        Intent downintent;
        KeyEvent downEvent;

        switch (player_type) {
            case "spotify":
                downintent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
                downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0);
                downintent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
                downintent.setPackage("com.spotify.music");
                sendBroadcast(downintent, null);
                Log.d("musicInfo", "Send command to Spotify." + downintent.toString());
                break;

            case "genie":
                downintent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
                downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0);
                downintent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
                downintent.setPackage("com.ktmusic.geniemusic");
                sendBroadcast(downintent, null);
                Log.d("musicInfo", "Send command to GENIE." + downintent.toString());

                break;

            case "melon":
                downintent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
                downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0);
                downintent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
                downintent.setPackage("com.iloen.melon");
                sendBroadcast(downintent, null);
                Log.d("musicInfo", "Send command to Melon." + downintent.toString());

                break;

            case "google":

                Intent i = new Intent("com.android.music.musicservicecommand");
                i.putExtra("command", "togglepause");
                i.setPackage("com.google.android.music");
                //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                sendBroadcast(i);
                Log.d("musicInfo", "Toggle Google Music..." + i.toString());

                //long eventtime = SystemClock.uptimeMillis();

                //Intent downintent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
                //KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0);

                /*
                downintent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
                downintent.setPackage("com.google.android.music");
                sendOrderedBroadcast(downintent, null);
                Log.d("musicInfo", "Send command to GOOGLE MUSIC." + downintent.toString());
                */

                break;

            case "xx":
                Intent genie_i = new Intent("com.android.music.musicservicecommand");

                List<ResolveInfo> resInfo = getApplicationContext().getPackageManager().queryBroadcastReceivers(genie_i, 0);
                if (!resInfo.isEmpty()) {
                    for (ResolveInfo info : resInfo) {
                        Log.d("musicInfo", "BCRs" + info.toString());
                    }
                } else {
                    Log.d("musicInfo", "No BCRs");
                }


                resInfo = getApplicationContext().getPackageManager().queryIntentActivities(genie_i, 0);
                if (!resInfo.isEmpty()) {
                    for (ResolveInfo info : resInfo) {
                        Log.d("musicInfo", "iActivities" + info.toString());
                    }
                } else {
                    Log.d("musicInfo", "No iActivities");
                }

                genie_i.putExtra("command", "pause");
                genie_i.setPackage("com.ktmusic.geniemusic");
                //genie_i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                sendBroadcast(genie_i);
                Log.d("musicInfo", "Toggle Genie Music..." + genie_i.toString());

                break;

            default:
                //Intent i = new Intent("com.android.music.musicservicecommand");
                //i.putExtra("command", "togglepause");
                //i.setPackage("com.google.android.music");
                //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //sendBroadcast(i);
                //Log.d("musicInfo", "Toggle Google Music..." + i.toString());
                break;
        }


        /*
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "play");
        sendBroadcast(i);


        long eventtime = SystemClock.uptimeMillis();

        Intent downintent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0);
        downintent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
        sendOrderedBroadcast(downintent, null);
        */
    }
}
