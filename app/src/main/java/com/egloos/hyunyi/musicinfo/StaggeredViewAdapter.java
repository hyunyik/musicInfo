package com.egloos.hyunyi.musicinfo;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

import com.etsy.android.grid.util.DynamicHeightImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by Hyun-Yi on 2015-06-02.
 */
public class StaggeredViewAdapter extends ArrayAdapter<JSONObject> {
    private static final String TAG = "StaggeredViewAdapter";

    private final LayoutInflater mLayoutInflater;
    private final Random mRandom;
    private static final SparseArray<Double> sPositionHeightRatios = new SparseArray<Double>();

    private ImageLoader imageLoader;
    private DisplayImageOptions imageOptions ;
    private ImageLoaderConfiguration config;

    public StaggeredViewAdapter(Context context, int textViewResourceId, ArrayList<JSONObject> objects) {
        super(context, textViewResourceId, objects);
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mRandom = new Random();

        imageOptions = new DisplayImageOptions.Builder().cacheInMemory(false).cacheOnDisk(true).bitmapConfig(Bitmap.Config.RGB_565).imageScaleType(ImageScaleType.EXACTLY).build();
        config = new ImageLoaderConfiguration.Builder(getContext()).diskCacheExtraOptions(480, 320, null).defaultDisplayImageOptions(imageOptions).build();

        if (imageLoader==null) imageLoader = ImageLoader.getInstance();
        if (!imageLoader.isInited()) imageLoader.init(config);
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.dynamic_image_view, parent, false);
            vh = new ViewHolder();
            vh.imgView = (DynamicHeightImageView) convertView.findViewById(R.id.imgView);
            vh.tTitle = (TextView) convertView.findViewById(R.id.t_title);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        double positionHeight = getPositionRatio(position);

        vh.imgView.setHeightRatio(positionHeight);

        //if (imageLoader!=null) imageLoader.destroy();

        JSONObject j = getItem(position);

        try {
            imageLoader.displayImage(j.getString("image_url"), vh.imgView);
            vh.tTitle.setText(j.getString("title"));
            vh.url = j.getString("url");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return convertView;
    }

    static class ViewHolder {
        DynamicHeightImageView imgView;
        TextView tTitle;
        String url;
    }

    private double getPositionRatio(final int position) {
        double ratio = sPositionHeightRatios.get(position, 0.0);
        if (ratio==0) {
            ratio = getRandomHeightRatio();
            sPositionHeightRatios.append(position, ratio);
            //Log.d(TAG, "getPositionRatio:" + position + " ratio:" + ratio);
        }
        return ratio;
    }

    private double getRandomHeightRatio() {
        return (mRandom.nextDouble()/2.0) + 1.0;
    }
}
