package com.kunel.lantimat.kunelradio.fragments;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.kunel.lantimat.kunelradio.FullFeedActivity;
import com.kunel.lantimat.kunelradio.Utils.ItemClickSupport;
import com.kunel.lantimat.kunelradio.models.Feed;
import com.kunel.lantimat.kunelradio.KunelRestClient;
import com.kunel.lantimat.kunelradio.R;
import com.kunel.lantimat.kunelradio.adapters.FeedsRecyclerAdapter;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;


/**
 * Created by GabdrakhmanovII on 28.07.2017.
 */

public class FeedFragment extends Fragment {

    RecyclerView recyclerView;
    FeedsRecyclerAdapter feedsRecyclerAdapter;
    ArrayList<Feed> arFeeds;

    ProgressBar progressBar;

    public FeedFragment() {
    }


    public static FeedFragment newInstance() {
        FeedFragment fragment = new FeedFragment();
        return fragment;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        arFeeds= new ArrayList<>();
        feedsRecyclerAdapter = new FeedsRecyclerAdapter(getContext(), arFeeds);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_news, null);

        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        initRecyclerView();

        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        KunelRestClient.get("", null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                new ParseFeeds().execute(bytes);
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {

            }
        });

        return v;
    }

    private void initRecyclerView() {
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1, OrientationHelper.VERTICAL, false));
        //arFeeds.add(new Feed("Супер новость", "12/08/2017", "https://www.w3schools.com/css/trolltunga.jpg", "", ""));
        recyclerView.setAdapter(feedsRecyclerAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);



        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                Intent intent = new Intent(getContext(), FullFeedActivity.class);
                intent.putExtra("url", arFeeds.get(position).getFeedUrl());
                intent.putExtra("title", arFeeds.get(position).getTitle());
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {

        super.onResume();
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }


    public class ParseFeeds extends AsyncTask<byte[], Void, Void> {


        @Override
        protected Void doInBackground(byte[]... params) {
            String str = null;

            try {

                str = new String(params[0], "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            Document doc = Jsoup.parse(str);
            Elements feed = doc.select("article");
            //feed = feed.select("a");
            Elements title = feed.select("a[title]");
            Elements imgUrl = feed.select("[src]");
            Elements url = feed.select("a[title]");
            for (int j = 0; j < feed.size(); j++) {


                arFeeds.add(new Feed(title.get(j).attr("title").replace("Permalink to ", ""), feed.get(j).select("div.excerpt-stats").text(), imgUrl.get(j).attr("src"), url.get(j).attr("href"), ""));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            feedsRecyclerAdapter.notifyDataSetChanged();
            progressBar.setVisibility(View.INVISIBLE);
            super.onPostExecute(aVoid);
        }
    }

}
