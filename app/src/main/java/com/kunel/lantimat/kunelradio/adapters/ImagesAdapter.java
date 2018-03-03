package com.kunel.lantimat.kunelradio.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.kunel.lantimat.kunelradio.R;
import com.kunel.lantimat.kunelradio.Utils.SquareImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import jp.wasabeef.picasso.transformations.BlurTransformation;

/**
 * Created by lAntimat on 03.03.2018.
 */

public class ImagesAdapter extends PagerAdapter {

    Context context;
    ArrayList<String> ar;

    public ImagesAdapter(Context context, ArrayList<String> ar) {
        this.context = context;
        this.ar = ar;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_cover, container,
                false);
        //View view = LayoutInflater.from(getContext()).inflate(R.layout.item_cover,null);
        final SquareImageView imageViewBg = (SquareImageView) view.findViewById(R.id.image_cover);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageView);

        Picasso.with(context)
                .load(ar.get(position))
                .resize(300, 170)
                .transform(new BlurTransformation(context))
                .into(imageViewBg);

        Picasso.with(context).load(ar.get(position))
                .into(imageView);
        //imageView.setImageDrawable(getResources().getDrawable(covers[position]));
        //imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View)object);
    }

    @Override
    public int getCount() {
        return ar.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view == object);
    }
}
