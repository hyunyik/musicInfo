package com.egloos.hyunyi.musicinfo;

import android.app.ActionBar;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.etsy.android.grid.ExtendableListView;
import com.etsy.android.grid.StaggeredGridView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//import java.io.IOException;
//import java.io.InputStream;
//import java.net.MalformedURLException;
//import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Hyun-Yi on 2015-05-28.
 */
public class LinkPopUp extends Activity implements View.OnClickListener {

    private ImageView ArtistImage;
    private ImageView bPopUpClose;
    private TextView tArtistName;
    private LinearLayout lLinkList;
    private RelativeLayout lArtistLink;
    private FrameLayout imageFrame;
    private ImageView iBottomPanel;
    private TextView tEchoNest;
    private TextView tAttribution;

    private boolean mHasRequestedMore;

    private StaggeredGridView mGridView;
    private StaggeredViewAdapter mAdapter;

    private ArrayList<JSONObject> mData;
    private JSONArray jVideoArray;

    private float mTouchX, mTouchY;

    //private boolean isMove;

    private ImageLoader imageLoader;

    private int artistNum;
    private JSONArray jArtists;

    private DisplayImageOptions imageOptions ;
    private ImageLoaderConfiguration config;

    private int SGVtransition;

    Boolean echonest_complete;
    Boolean lastFM_complete;

    public LinkPopUp() {
        jArtists = new JSONArray();
        artistNum = 0;
    }

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Bundle b = getIntent().getExtras();

        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);

        imageOptions = new DisplayImageOptions.Builder().cacheInMemory(false).cacheOnDisk(true).bitmapConfig(Bitmap.Config.RGB_565).imageScaleType(ImageScaleType.EXACTLY).showImageOnLoading(R.drawable.connecting_anim).build();
        config = new ImageLoaderConfiguration.Builder(getApplicationContext()).diskCacheExtraOptions(480, 320, null).defaultDisplayImageOptions(imageOptions).build();

        if (imageLoader==null) imageLoader = ImageLoader.getInstance();
        if (!imageLoader.isInited()) imageLoader.init(config);

        setContentView(R.layout.link_dialog);

        imageFrame = (FrameLayout) findViewById(R.id.image_frame);
        iBottomPanel = (ImageView) findViewById(R.id.bottom_panel);

        lArtistLink = (RelativeLayout) findViewById(R.id.l_artist_link);

        bPopUpClose = (ImageView) findViewById(R.id.b_popup_close);
        bPopUpClose.setOnClickListener(this);

        tEchoNest = (TextView) findViewById(R.id.t_echo_nest);
        tAttribution = (TextView) findViewById(R.id.t_attribution);

        tArtistName = (TextView) findViewById(R.id.t_artist_name);
        tArtistName.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //isMove = false;
                        mTouchX = event.getRawX();
                        mTouchY = event.getRawY();
                        break;

                    case MotionEvent.ACTION_UP:
                        //Log.i("musicInfo", "ACTION_UP! TouchY:" + mTouchY + " UpY: " + event.getRawY() + " Move: " + (mTouchY - event.getRawY()));
                        int RawY = (int) event.getRawY();
                        int dY = (int) (mTouchY - event.getRawY());
                        if (dY > 200) {
                            imageLoader.resume();
                            //Log.i("musicInfo", "mGridView TOP = " + mGridView.getTop() + " mGridView TransitionY =" + mGridView.getTranslationY());
                            while (mGridView.getTranslationY() > tArtistName.getHeight()) {
                                //Log.i("musicInfo", "Moving UP! mGridView: " + mGridView.getTranslationY() + " tArtistName: " + tArtistName.getTranslationY());
                                mGridView.setTranslationY(mGridView.getTranslationY() - 1);
                                tArtistName.setTranslationY(tArtistName.getTranslationY() - 1);

                            }
                        } else if (dY > 5 && dY <= 200 || (dY <= 5 && tArtistName.getTranslationY() < 0)) {
                            while (tArtistName.getTranslationY() < 0) {
                                //Log.i("musicInfo", "Moving DOWN! mGridView: " + mGridView.getTranslationY() + " tArtistName: " + tArtistName.getTranslationY());
                                mGridView.setTranslationY(mGridView.getTranslationY() + 1);
                                tArtistName.setTranslationY(tArtistName.getTranslationY() + 1);
                            }
                            imageLoader.pause();
                        }
                        break;

                    case MotionEvent.ACTION_MOVE:
                        //isMove = true;

                        //int top = tArtistName.getTop();
                        //int bottom = tArtistName.getBottom();
                        //int left = tArtistName.getLeft();
                        //int right = tArtistName.getRight();

                        int x = (int) (event.getRawX() - mTouchX);
                        int y = (int) (event.getRawY() - mTouchY);

                        final int num = 5;
                        if (y > -num && y < num) {
                            //isMove = false;
                            break;
                        } else if (y > 0) {

                        } else {
                            imageFrame.bringChildToFront(mGridView);
                            tArtistName.setTranslationY((float) y);
                            mGridView.setTranslationY((float) (SGVtransition + y));

                            //Log.i("musicInfo", String.format("T: %d, B: %d", mGridView.getTop(), mGridView.getBottom()));

                        }


                        break;
                }
                return true;
            }
        });

        ArtistImage = (ImageView) findViewById(R.id.artist_image);


        lLinkList = (LinearLayout) findViewById(R.id.l_link_list);

        mGridView = (StaggeredGridView) findViewById(R.id.SG_view);

        SGVtransition = 900;

        mGridView.setTranslationY(SGVtransition);

        JSONObject artist_info = null;
        try {
            artist_info = new JSONObject(b.getString("artist_info"));
            Log.i("musicInfo", "LinkPopUp. OnCreate" + artist_info.toString());
            displayArtistInfo(artist_info);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (artist_info!=null) jArtists.put(artist_info);
        if (prefs.getBoolean("SimilarOn", false)) {
            getSimilarArtistsInfo((String) tArtistName.getText());
            ArtistImage.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            //isMove = false;
                            mTouchX = event.getRawX();
                            mTouchY = event.getRawY();
                            break;

                        case MotionEvent.ACTION_UP:
                            //Log.i("musicInfo", "ACTION_UP! TouchY:" + mTouchY + " UpY: " + event.getRawY() + " Move: " + (mTouchY - event.getRawY()));
                            int dX = (int) (mTouchX - event.getRawX());
                            if (dX > 5) {
                                if (jArtists.length() > 1) {
                                    imageLoader.stop();
                                    try {
                                        if (artistNum == 0) {
                                            artistNum = jArtists.length() - 1;
                                        } else {
                                            artistNum--;
                                        }

                                        displayArtistInfo(jArtists.getJSONObject(artistNum));
                                        imageLoader.resume();

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }


                            } else if (dX < -5) {
                                if (jArtists.length() > 1) {
                                    imageLoader.stop();
                                    try {
                                        if (artistNum == jArtists.length() - 1) {
                                            artistNum = 0;
                                        } else {
                                            artistNum++;
                                        }
                                        displayArtistInfo(jArtists.getJSONObject(artistNum));
                                        imageLoader.resume();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }

                            }

                            break;

                        case MotionEvent.ACTION_MOVE:

                            break;
                    }

                    return true;
                }
            });
        }




        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                //Log.d("musicInfo", "SGV onScrollStateChanged:" + scrollState);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                //Log.d("musicInfo", "SGV onScroll firstVisibleItem:" + firstVisibleItem + " visibleItemCount:" + visibleItemCount + " totalItemCount:" + totalItemCount);
                // our handling
                if (!mHasRequestedMore) {
                    int lastInScreen = firstVisibleItem + visibleItemCount;
                    if (lastInScreen >= totalItemCount) {
                        //Log.d("musicInfo", "SGV onScroll lastInScreen - so load more");
                        mHasRequestedMore = true;
                        onLoadMoreItems();
                    }
                }
            }

            private void onLoadMoreItems() {
                if (jVideoArray == null) return;
                final ArrayList<JSONObject> sampleData = generateImageData(jVideoArray);
                for (JSONObject data : sampleData) {
                    mAdapter.add(data);
                }
                // stash all the data in our backing store
                mData.addAll(sampleData);
                // notify the adapter that we can update now
                mAdapter.notifyDataSetChanged();
                mHasRequestedMore = false;
            }
        });

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), "Open the Video...", Toast.LENGTH_SHORT).show();
                StaggeredViewAdapter.ViewHolder vh = (StaggeredViewAdapter.ViewHolder) view.getTag();

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse((String) vh.url));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                //finish();

            }

        });

        //int[] lArtistLinkPosition = new int[2];
        //int[] tArtistNamePosition = new int[2];

        //lArtistLink.getLocationOnScreen(lArtistLinkPosition);
        //tArtistName.getLocationOnScreen(tArtistNamePosition);


        //Log.d("musicInfo", "SGV transition = " + SGVtransition);
    }

    @Override
    public void onClick(View v) {
        if (v==bPopUpClose) {
            finish();
        } else {
            /*
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(Uri.parse((String)v.getTag()));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            */
        }
    }


    private ArrayList<JSONObject> generateImageData(JSONArray jVideoArray) {
        if (jVideoArray==null) return null;
        ArrayList<JSONObject> listData = new ArrayList<JSONObject>();

        for (int i=0;i<jVideoArray.length();i++) {
            try {
                listData.add(jVideoArray.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return listData;
    }

    private void displayArtistInfo(JSONObject j) throws JSONException {
        if (imageLoader==null) imageLoader = ImageLoader.getInstance();
        if (!imageLoader.isInited()) imageLoader.init(config);

        Log.i("musicInfo", "LinkPopUp. displayArtistInfo " + j.toString());

        //JSONObject j_artist_info = j.getJSONObject("artist");

        final String artist_name = j.getString("name");

        tArtistName.setText(artist_name);
        final JSONObject urls = j.optJSONObject("urls");
        final JSONArray videos = j.optJSONArray("video");
        final JSONArray images = j.optJSONArray("images");

        final String msg = artist_name.replaceAll("\\p{Punct}", " ").replaceAll("\\p{Space}", "+");

        AsyncHttpClient client = new AsyncHttpClient();

        String fmURL = "http://ws.audioscrobbler.com/2.0/?method=artist.getinfo&artist=" + msg + "&autocorrect[1]&format=json&api_key=ca4c10f9ae187ebb889b33ba12da7ee9";
        Log.i("musicInfo", fmURL);

        client.get(fmURL, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String r = new String(responseBody);
                ArrayList<String> image_urls = new ArrayList<String>();

                try {
                    Log.i("musicInfo", "Communicating with LastFM...");

                    JSONObject json = new JSONObject(r);
                    Log.i("musicInfo", json.toString());

                    json = json.getJSONObject("artist");
                    Log.i("musicInfo", json.toString());

                    JSONArray artist_images = json.optJSONArray("image");
                    Log.i("musicInfo", artist_images.toString());



                    for (int i = 0; i < artist_images.length(); i++) {
                        JSONObject j = artist_images.getJSONObject(i);
                        Log.i("musicInfo", j.optString("size"));
                        if (j.optString("size").contains("extralarge")) {
                            image_urls.add(j.optString("#text"));
                            //b.putString("fm_image", j.getString("#text"));
                            //Log.i("musicInfo", j.getString("#text"));
                            break;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(images!=null) {
                    for (int i = 0; i < images.length(); i++) {
                        JSONObject image = null;
                        try {
                            image = images.getJSONObject(i);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        int width = image.optInt("width", 0);
                        int height = image.optInt("height", 0);
                        String url = image.optString("url", "");
                        Log.i("musicInfo", i + ": " + url);
                        if ((width * height > 10000) && (width * height < 100000) && (!url.contains("userserve-ak"))) {
                            //if ((width>300&&width<100)&&(height>300&&height<1000)&&(!url.contains("userserve-ak"))) {
                            image_urls.add(url);
                            Log.i("musicInfo", "Selected: " + url);
                            //available_images.put(image);
                        }
                    }
                }

                int random = (int) (Math.random() * image_urls.size());
                final String f_url = image_urls.get(random);

                Log.i("musicInfo", "Total image#=" + image_urls.size() + " Selected image#=" + random + " " + f_url);

                if (image_urls.size()>0) {
                    imageLoader.displayImage(f_url, ArtistImage, new ImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {

                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

                            lLinkList.removeAllViews();
                            //String attr = fImage.optJSONObject("license").optString("attribution");
                            //tAttribution.setText("Credit. " + ((attr == null) || (attr.contains("n/a")) ? "Unknown" : attr));
                            if (urls != null) {
                                String[] jsonName = {"wikipedia_url", "mb_url", "lastfm_url", "official_url", "twitter_url"};
                                for (int i = 0; i < jsonName.length; i++) {
                                    if ((urls.optString(jsonName[i]) != null) && (urls.optString(jsonName[i]) != "")) {
                                        Log.d("musicinfo", "Link URL: " + urls.optString(jsonName[i]));
                                        TextView tv = new TextView(getApplicationContext());
                                        tv.setTextSize(11);
                                        tv.setPadding(16, 16, 16, 16);
                                        tv.setTextColor(Color.LTGRAY);
                                        tv.setTypeface(Typeface.SANS_SERIF);
                                        tv.setGravity(Gravity.CENTER_VERTICAL);

                                        switch (jsonName[i]) {
                                            case "official_url":
                                                tv.setText("HOME.");
                                                break;
                                            case "wikipedia_url":
                                                tv.setText("WIKI.");
                                                break;
                                            case "mb_url":
                                                tv.setText("Music Brainz.");
                                                break;
                                            case "lastfm_url":
                                                tv.setText("Last FM.");
                                                break;
                                            case "twitter_url":
                                                tv.setText("Twitter.");
                                                break;
                                        }

                                        try {
                                            tv.setTag(urls.getString(jsonName[i]));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        tv.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent();
                                                intent.setAction(Intent.ACTION_VIEW);
                                                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                                                intent.setData(Uri.parse((String) v.getTag()));
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                                Toast.makeText(getApplicationContext(), "Open the Link...", Toast.LENGTH_SHORT).show();
                                                //finish();
                                            }
                                        });
                                        lLinkList.addView(tv);
                                    }
                                }
                            } else {
                                TextView tv = new TextView(getApplicationContext());
                                tv.setTextSize(11);
                                tv.setPadding(16, 16, 16, 16);
                                tv.setTextColor(Color.LTGRAY);
                                tv.setTypeface(Typeface.SANS_SERIF);
                                tv.setGravity(Gravity.CENTER_VERTICAL);
                                tv.setText("Sorry, No Link Here...");
                                lLinkList.addView(tv);
                            }


                            if (videos != null) {
                                jVideoArray = videos;
                                mAdapter = new StaggeredViewAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, generateImageData(videos));
                                //if (mData == null) {
                                mData = generateImageData(videos);
                                //}

                                //mAdapter.clear();

                                for (JSONObject data : mData) {
                                    mAdapter.add(data);
                                }
                                mGridView.setAdapter(mAdapter);
                            } else {

                            }

                            adjBottomColor(((ImageView) view).getDrawable());
                        }

                        @Override
                        public void onLoadingCancelled(String imageUri, View view) {

                        }
                    });
                } else {
                    ArtistImage.setImageResource(R.drawable.lamb_no_image_available);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    private void displayArtistInfo_bk(JSONObject j) throws JSONException {
        if (imageLoader==null) imageLoader = ImageLoader.getInstance();
        if (!imageLoader.isInited()) imageLoader.init(config);

        Log.i("musicInfo", "LinkPopUp. displayArtistInfo " + j.toString());

        JSONObject j_artist_info = j.getJSONObject("artist");

        tArtistName.setText(j_artist_info.getString("name"));
        final JSONObject urls = j_artist_info.optJSONObject("urls");
        final JSONArray videos = j_artist_info.optJSONArray("video");
        final JSONArray images = j_artist_info.optJSONArray("images");
        final String fm_image = j.optString("fm_image");
        final JSONArray available_images = new JSONArray();
        ArrayList<String> image_urls = new ArrayList<String>();

        if (fm_image!=null) { image_urls.add(fm_image); }

        Log.i("musicInfo", images.toString());

        if (images!=null) {
            for (int i=0;i<images.length();i++) {
                JSONObject image = images.getJSONObject(i);
                int width = image.optInt("width", 0);
                int height = image.optInt("height", 0);
                String url = image.optString("url", "");
                Log.i("musicInfo", i + ": " + url);
                if ((width*height>10000)&&(width*height<100000)&&(!url.contains("userserve-ak"))) {
                    //if ((width>300&&width<100)&&(height>300&&height<1000)&&(!url.contains("userserve-ak"))) {
                    image_urls.add(url);
                    Log.i("musicInfo", "Selected: " + url);
                    //available_images.put(image);
                }
            }

            int random = (int) (Math.random() * image_urls.size());
            final String f_url = image_urls.get(random);

            //int random = (int) (Math.random() * available_images.length());
            //final JSONObject fImage = available_images.length()>0?available_images.getJSONObject(random > images.length() ? 0 : random):images.getJSONObject(0);

            Log.i("musicInfo", "Total image#=" + available_images.length() + " Selected image#=" + random + " " + f_url);

            imageLoader.displayImage(f_url, ArtistImage, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {

                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

                    lLinkList.removeAllViews();
                    //String attr = fImage.optJSONObject("license").optString("attribution");
                    //tAttribution.setText("Credit. " + ((attr == null) || (attr.contains("n/a")) ? "Unknown" : attr));
                    if (urls != null) {
                        String[] jsonName = {"wikipedia_url", "mb_url", "lastfm_url", "official_url", "twitter_url"};
                        for (int i = 0; i < jsonName.length; i++) {
                            if ((urls.optString(jsonName[i]) != null) && (urls.optString(jsonName[i]) != "")) {
                                Log.d("musicinfo", "Link URL: " + urls.optString(jsonName[i]));
                                TextView tv = new TextView(getApplicationContext());
                                tv.setTextSize(11);
                                tv.setPadding(16, 16, 16, 16);
                                tv.setTextColor(Color.LTGRAY);
                                tv.setTypeface(Typeface.SANS_SERIF);
                                tv.setGravity(Gravity.CENTER_VERTICAL);
                                tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                                switch (jsonName[i]) {
                                    case "official_url":
                                        tv.setText("HOME.");
                                        break;
                                    case "wikipedia_url":
                                        tv.setText("WIKI.");
                                        break;
                                    case "mb_url":
                                        tv.setText("Music Brainz.");
                                        break;
                                    case "lastfm_url":
                                        tv.setText("Last FM.");
                                        break;
                                    case "twitter_url":
                                        tv.setText("Twitter.");
                                        break;
                                }

                                try {
                                    tv.setTag(urls.getString(jsonName[i]));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                tv.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent();
                                        intent.setAction(Intent.ACTION_VIEW);
                                        intent.addCategory(Intent.CATEGORY_BROWSABLE);
                                        intent.setData(Uri.parse((String) v.getTag()));
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        Toast.makeText(getApplicationContext(), "Open the Link...", Toast.LENGTH_SHORT).show();
                                        //finish();
                                    }
                                });
                                lLinkList.addView(tv);
                            }
                        }
                    } else {
                        TextView tv = new TextView(getApplicationContext());
                        tv.setTextSize(11);
                        tv.setPadding(16, 16, 16, 16);
                        tv.setTextColor(Color.LTGRAY);
                        tv.setTypeface(Typeface.SANS_SERIF);
                        tv.setGravity(Gravity.CENTER_VERTICAL);
                        tv.setText("Sorry, No Link Here...");
                        lLinkList.addView(tv);
                    }


                    if (videos != null) {
                        jVideoArray = videos;
                        mAdapter = new StaggeredViewAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, generateImageData(videos));
                        //if (mData == null) {
                        mData = generateImageData(videos);
                        //}

                        //mAdapter.clear();

                        for (JSONObject data : mData) {
                            mAdapter.add(data);
                        }
                        mGridView.setAdapter(mAdapter);
                    } else {

                    }

                    adjBottomColor(((ImageView) view).getDrawable());
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {

                }
            });
        }


    }

    private void adjBottomColor(Drawable d) {
        Bitmap b = ((BitmapDrawable) d).getBitmap();
        int b_sum = 0;
        int g_sum = 0;
        int r_sum = 0;

        for (int i=0;i<10;i++) {
            int x = (int)((double)b.getWidth()*Math.random());
            int y = (int)((double)b.getHeight()*Math.random());
            int pixel = b.getPixel(x, y);

            int redValue = Color.red(pixel);
            int blueValue = Color.blue(pixel);
            int greenValue = Color.green(pixel);

            b_sum += blueValue;
            g_sum += greenValue;
            r_sum += redValue;
        }

        int av_r = r_sum/10;
        int av_g = g_sum/10;
        int av_b = b_sum/10;

        iBottomPanel.setBackgroundColor(Color.rgb(av_r, av_g, av_b));
        ArtistImage.setBackgroundColor(Color.rgb(av_r, av_g, av_b));

        int ave_sum = (av_b + av_g + av_r);
        Log.d("musicInfo", "ave_sum = " + ave_sum);

        if (ave_sum>400) {
            tEchoNest.setTextColor(Color.parseColor("#8f363c6b"));
            for (int i=0;i<lLinkList.getChildCount();i++) {
                ((TextView)lLinkList.getChildAt(i)).setTextColor(Color.DKGRAY);
            }
        } else {
            tEchoNest.setTextColor(Color.parseColor("#8fc9cfff"));
            for (int i=0;i<lLinkList.getChildCount();i++) {
                ((TextView)lLinkList.getChildAt(i)).setTextColor(Color.LTGRAY);
            }
        }
    }

    @Override
    protected  void onStop() {
        imageLoader.stop();
        super.onStop();
    }


    @Override
    protected  void onDestroy() {
        imageLoader.destroy();
        mAdapter = null;
        unbindDrawables(ArtistImage);
        unbindDrawables(mGridView);

        /*
        Drawable d = ArtistImage.getDrawable();
        if(d instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable)d).getBitmap();
            bitmap.recycle();
            bitmap = null;
        }
        d.setCallback(null);
        */

        System.gc();
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

    private void getSimilarArtistsInfo(String artist_name) {
        artist_name = artist_name.replaceAll("\\p{Punct}", " ").replaceAll("\\p{Space}", "+");

        AsyncHttpClient client = new AsyncHttpClient();

        String URL = "http://developer.echonest.com/api/v4/artist/similar?api_key=4WFJ845O28OVPQQ9T&name=" + artist_name + "&bucket=urls&bucket=images&bucket=video";
        //Log.v("musicInfo", URL);

        client.get(URL, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String r = new String(responseBody);
                try {
                    JSONObject json = new JSONObject(r);
                    json = json.getJSONObject("response");
                    JSONObject status = json.getJSONObject("status");

                    if (status.getString("message").contains("Success")) {
                        JSONArray artists = json.getJSONArray("artists");

                        for (int i=0;i<artists.length();i++) {
                            jArtists.put(artists.getJSONObject(i));
                            //Log.d("musicInfo", "#" + i + " " + artists.getJSONObject(i).getString("name"));
                        }
                    }

                    Toast.makeText(getApplicationContext(), R.string.recommendations, Toast.LENGTH_SHORT).show();

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
