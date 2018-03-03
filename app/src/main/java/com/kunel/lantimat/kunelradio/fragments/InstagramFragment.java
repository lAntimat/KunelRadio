package com.kunel.lantimat.kunelradio.fragments;


import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kunel.lantimat.kunelradio.InstagramFullMediaActivity;
import com.kunel.lantimat.kunelradio.Utils.ItemClickSupport;
import com.kunel.lantimat.kunelradio.adapters.MediaRecyclerAdapter;
import com.kunel.lantimat.kunelradio.models.Feed;
import com.kunel.lantimat.kunelradio.InstagramRestClient;
import com.kunel.lantimat.kunelradio.R;
import com.kunel.lantimat.kunelradio.adapters.FeedsRecyclerAdapter;
import com.kunel.lantimat.kunelradio.models.Images;
import com.kunel.lantimat.kunelradio.models.Media;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Created by GabdrakhmanovII on 28.07.2017.
 */

public class InstagramFragment extends Fragment {

    RecyclerView recyclerView;
    MediaRecyclerAdapter mediaRecyclerAdapter;
    ArrayList<Media> arMedia;
    ArrayList<String> arMediaCarousel;
    LinearLayoutManager mLayoutManager;
    String requestId = "";
    AppBarLayout appBarLayout;
    boolean mIsLoading = false;

    TextView tvPosts, tvFollowers, tvFollowing, display_name, description, website;
    CircleImageView ivProfile;

    ProgressBar progressBar;


    String profile_pic_url = "";
    String full_name = "";
    String biography = "";
    String followed_by = "";
    String follows = "";
    String media = "";
    String external_url = "";


    public InstagramFragment() {
    }


    public static InstagramFragment newInstance() {
        InstagramFragment fragment = new InstagramFragment();
        return fragment;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        arMedia= new ArrayList<>();
        arMediaCarousel= new ArrayList<>();
        mediaRecyclerAdapter = new MediaRecyclerAdapter(getContext(), arMedia);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_instagram, null);

        appBarLayout = (AppBarLayout) v.findViewById(R.id.appBarLayout);

        tvPosts = (TextView) v.findViewById(R.id.tvPosts);
        tvFollowers = (TextView) v.findViewById(R.id.tvFollowers);
        tvFollowing = (TextView) v.findViewById(R.id.tvFollowing);
        display_name = (TextView) v.findViewById(R.id.display_name);
        description = (TextView) v.findViewById(R.id.description);
        website = (TextView) v.findViewById(R.id.website);

        ivProfile = (CircleImageView) v.findViewById(R.id.profile_image);

        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        initRecyclerView();

        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        loadProfileInfo();
        loadMedia(requestId);

        return v;
    }

    private void initRecyclerView() {

        mLayoutManager = new GridLayoutManager(getContext(), 3, OrientationHelper.VERTICAL, false);
        //mLayoutManager = new LinearLayoutManager(getContext(), OrientationHelper.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(mediaRecyclerAdapter);


        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                Intent intent = new Intent(getContext(), InstagramFullMediaActivity.class);
                intent.putExtra("name", full_name);
                intent.putExtra("profile_img", profile_pic_url);
                intent.putExtra("image", arMedia.get(position).getImages().getStandard_resolution());
                intent.putExtra("code", arMedia.get(position).getCode());


                /*if(!arMedia.get(position).getCarousel_media().isEmpty()) {
                    for (int i = 0; i <arMedia.get(position).getCarousel_media().size() ; i++) {
                        arMediaCarousel.add(arMedia.get(position).getCarousel_media().get(i).getStandard_resolution());
                    }
                } else arMediaCarousel.add(arMedia.get(position).getImages().getStandard_resolution());*/
                //intent.putStringArrayListExtra("carousel", arMediaCarousel);
                startActivity(intent);
            }
        });


        RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                {
                    int pastVisiblesItems, visibleItemCount, totalItemCount;

                    if (dy > 0) //check for scroll down
                    {
                        visibleItemCount = mLayoutManager.getChildCount();
                        totalItemCount = mLayoutManager.getItemCount();
                        pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();


                        if (!mIsLoading) {
                        Log.v("...", "visibleItemCount " + visibleItemCount);
                        Log.v("...", "pastVisiblesItems " + pastVisiblesItems);
                        Log.v("...", "totalItemCount" + totalItemCount);
                            if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                                loadMedia(requestId);

                                Log.v("...", "Last Item Wow !");
                                //Do pagination.. i.e. fetch new data
                            }
                        }
                    }
                }
            }
        };

        recyclerView.addOnScrollListener(mScrollListener);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int firstVisiblePosition = mLayoutManager.findFirstCompletelyVisibleItemPosition();
                    if (firstVisiblePosition == 0) {
                        //appBarLayout.setExpanded(true, true);
                    }
                }
            }
        });
    }

    private void loadMedia(String id) {
        mIsLoading = true;
        RequestParams params = new RequestParams();
        params.add("max_id", id);
        Log.d("MaxId", "maxId=" + id);
        InstagramRestClient.get("radiokunel/media", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                String id = "";
                ArrayList<Images> arImages = new ArrayList<Images>();
                try {
                    JSONArray arItems =  response.getJSONArray("items");
                    JSONObject objItem;
                    Images images;
                    for (int i = 0; i <arItems.length() ; i++) {
                        objItem = arItems.getJSONObject(i);
                        id = objItem.get("id").toString();
                        requestId = id;
                        images = new Images(objItem.getJSONObject("images"));
                        String code = objItem.getString("code");
                        /*ArrayList<Images> arImgCarousel = new ArrayList<Images>();
                        if( objItem.has("carousel_media")) {
                            JSONArray jsonArray = objItem.getJSONArray("carousel_media");
                            for (int j = 0; j < jsonArray.length(); j++) {
                                arImgCarousel.add(new Images(jsonArray.getJSONObject(j).getJSONObject("images")));
                            }
                        }*/
                        arMedia.add(new Media(id, code, images));

                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

                mediaRecyclerAdapter.notifyDataSetChanged();
                recyclerView.invalidate();
                mIsLoading = false;
                progressBar.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                super.onFailure(statusCode, headers, throwable, errorResponse);

            }


        });
    }

    private void loadProfileInfo() {
        RequestParams params = new RequestParams();
        params.add("__a", "1");
        InstagramRestClient.get("radiokunel", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);


                JSONObject ob = null;
                try {
                    ob = response.getJSONObject("user");
                    profile_pic_url = ob.getString("profile_pic_url");
                    full_name = ob.getString("full_name");
                    biography = ob.getString("biography");
                    followed_by = ob.getJSONObject("followed_by").getString("count");
                    follows = ob.getJSONObject("follows").getString("count");
                    media = ob.getJSONObject("media").getString("count");
                    external_url = ob.getString("external_url");

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(profile_pic_url!=null) {
                    Picasso.with(getContext()).load(profile_pic_url).into(ivProfile);
                }

                tvPosts.setText(media);
                tvFollowers.setText(followed_by);
                tvFollowing.setText(follows);
                display_name.setText(full_name);
                description.setText(biography);
                website.setText(external_url);
                website.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getContext().startActivity(
                                new Intent(Intent.ACTION_VIEW, Uri.parse(external_url)));
                    }
                });

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                super.onFailure(statusCode, headers, throwable, errorResponse);

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

}
