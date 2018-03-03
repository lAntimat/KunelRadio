package com.kunel.lantimat.kunelradio.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kunel.lantimat.kunelradio.R;
import com.kunel.lantimat.kunelradio.models.Media;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;

/**
 * Created by GabdrakhmanovII on 31.07.2017.
 */

public class MediaRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private ArrayList<Media> mList;
    Context context;
    OkHttpClient okHttpClient;

    public MediaRecyclerAdapter(Context context, ArrayList<Media> itemList) {
        this.context = context;
        this.mList = itemList;
        //Picasso picasso = Picasso.with(context);
        //picasso.setIndicatorsEnabled(true);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_instagram_image_item, parent, false);
        return new ItemViewHolder(view);
    }
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

        final String imgUrl = mList.get(position).getImages().getLow_resolution();

        Picasso.with(context)
                .load(imgUrl)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(((ItemViewHolder) holder).mImageView, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        // Try again online if cache failed
                        Picasso.with(context)
                                .load(imgUrl)
                                .into(((ItemViewHolder) holder).mImageView);
                    }
                });

        //((ItemViewHolder) holder).imageView.setImageResource(););
        //((ItemViewHolder) holder).mDesc.setText(mList.get(position).getDesc());

    }
    @Override
    public int getItemCount() {
        if (mList == null)
            return 0;
        return mList.size();
    }
    @Override
    public int getItemViewType(int position) {
        if (mList != null) {
            //ControlItemsModel object = mList.get(position);
            //if (object != null) {
            // return object.getType();
            //  }
        }
        return 0;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {

        private ImageView mImageView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.imageView);
        }
    }
}
