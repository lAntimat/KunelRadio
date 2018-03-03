/*
 *
 *  MIT License
 *
 *  Copyright (c) 2017 Alibaba Group
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 *
 */

package com.kunel.lantimat.kunelradio.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kunel.lantimat.kunelradio.R;
import com.kunel.lantimat.kunelradio.Utils.SquareImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import jp.wasabeef.picasso.transformations.BlurTransformation;

/**
 * Created by mikeafc on 15/11/26.
 */
public class UltraPagerAdapter extends PagerAdapter {
    private boolean isMultiScr;
    private  Context context;
    private ArrayList<String> ar;
    public UltraPagerAdapter(Context context, ArrayList<String> ar, boolean isMultiScr) {
        this.context = context;
        this.isMultiScr = isMultiScr;
        this.ar = ar;
    }

    @Override
    public int getCount() {
        return ar.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cover,null);
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
        View view = LayoutInflater.from(context).inflate(R.layout.item_cover,null);
        container.removeView(view);
    }

    public void updateAr(ArrayList<String> ar) {
        this.ar = ar;
        notifyDataSetChanged();
    }
}
