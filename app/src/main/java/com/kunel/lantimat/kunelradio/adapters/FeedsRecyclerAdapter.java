package com.kunel.lantimat.kunelradio.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kunel.lantimat.kunelradio.models.Feed;
import com.kunel.lantimat.kunelradio.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by GabdrakhmanovII on 31.07.2017.
 */

public class FeedsRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {



    private ArrayList<Feed> mList;
    private Context context;

    public FeedsRecyclerAdapter(Context context, ArrayList<Feed> itemList) {
        this.context = context;
        this.mList = itemList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_feeds, parent, false);
        return new ItemViewHolder(view);
    }
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        ((ItemViewHolder) holder).mTitle.setText(mList.get(position).getTitle());
        ((ItemViewHolder) holder).mData.setText(mList.get(position).getDate());

        Picasso.with(context).load(mList.get(position).getImgUrl()).into(((ItemViewHolder) holder).mImageView);




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
        private TextView mTitle;
        private TextView mData;
        private TextView mViews;
        private ImageView mImageView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            mTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            mData = (TextView) itemView.findViewById(R.id.tvDate);
            mImageView = (ImageView) itemView.findViewById(R.id.ivFeedImage);

        }
    }
}
