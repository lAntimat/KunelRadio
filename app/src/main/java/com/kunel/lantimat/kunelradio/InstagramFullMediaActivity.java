package com.kunel.lantimat.kunelradio;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kunel.lantimat.kunelradio.Utils.InstaViewPager;
import com.kunel.lantimat.kunelradio.adapters.SlidingImage_Adapter;
import com.kunel.lantimat.kunelradio.models.Images;
import com.kunel.lantimat.kunelradio.models.Media;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;
import com.viven.imagezoom.ImageZoomHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;
import me.relex.circleindicator.CircleIndicator;

import static com.kunel.lantimat.kunelradio.R.id.textView;

public class InstagramFullMediaActivity extends AppCompatActivity {

    TextView tvName, tvDescription, tvLike, tvLocation;
    CircleImageView profileImage;
    ImageView imageView;
    String text;
    String likeText;
    String locationText;
    SlidingImage_Adapter slidingAdapter;
    ImageZoomHelper imageZoomHelper;

    private Boolean isVideo = false;
    private static InstaViewPager mPager;
    private static int currentPage = 0;
    private static int NUM_PAGES = 0;
    private ArrayList<String> imagesArray = new ArrayList<String>();
    private ArrayList<String> videousArray = new ArrayList<String>();
    int h, w;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instagram_full_media);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        mPager = (InstaViewPager) findViewById(R.id.pager);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            View decorView = getWindow().getDecorView();
            // Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(uiOptions);
        }

        tvName = (TextView) findViewById(R.id.display_name);
        tvLocation = (TextView) findViewById(R.id.location);
        tvDescription = (TextView) findViewById(R.id.description);
        tvLike = (TextView) findViewById(R.id.tvLike);

        profileImage = (CircleImageView) findViewById(R.id.profile_image);

        imageView = (ImageView) findViewById(R.id.imageView);


        tvName.setText(getIntent().getStringExtra("name"));
        //tvDescription.setText("radiokunel " + getIntent().getStringExtra("description"));

        Picasso.with(this).load(getIntent().getStringExtra("profile_img")).into(profileImage);
        //Picasso.with(this).load(getIntent().getStringExtra("image")).into(imageView);

        //https://www.instagram.com/p/BX05JgLlmla/?__a=1

        loadMedia(getIntent().getStringExtra("code"));


    }

    private void init(boolean isVideo) {

//        imagesArray.addAll(getIntent().getStringArrayListExtra("carousel"));

        //mPager = (InstaViewPager) findViewById(R.id.pager);

        if(isVideo) {
            slidingAdapter = new SlidingImage_Adapter(InstagramFullMediaActivity.this, imagesArray, videousArray, isVideo);
            mPager.setAdapter(slidingAdapter);
        }
        else {
            slidingAdapter = new SlidingImage_Adapter(InstagramFullMediaActivity.this, imagesArray);
            mPager.setAdapter(slidingAdapter);
        }

        CircleIndicator indicator = (CircleIndicator)
                findViewById(R.id.indicator);

        if(imagesArray.size()==1) indicator.setVisibility(View.GONE);
        indicator.setViewPager(mPager);

        final float density = getResources().getDisplayMetrics().density;

        //Set circle indicator radius

        NUM_PAGES = imagesArray.size();

        // Pager listener over indicator
        indicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                currentPage = position;

            }

            @Override
            public void onPageScrolled(int pos, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int pos) {

            }
        });


        imageZoomHelper = new ImageZoomHelper(this);
        //ImageZoomHelper.setViewZoomable(mPager);

    }

    private void setPagerDimension(int h, int w) {
        mPager.getLayoutParams().height = h;
        mPager.getLayoutParams().width = w;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return imageZoomHelper.onDispatchTouchEvent(ev) || super.dispatchTouchEvent(ev);
    }

    private void loadMedia(String code) {
        //https://www.instagram.com/p/BX05JgLlmla/?__a=1


        RequestParams params = new RequestParams();
        params.add("__a", "1");
        InstagramRestClient.get("p/" + code, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    JSONObject obj = response.getJSONObject("graphql").getJSONObject("shortcode_media");

                    text = obj.getJSONObject("edge_media_to_caption").getJSONArray("edges").getJSONObject(0).getJSONObject("node").getString("text");
                    likeText = obj.getJSONObject("edge_media_preview_like").getString("count");
                    if(!obj.isNull("location")) locationText = obj.getJSONObject("location").getString("name");
                    Log.d("InstagramFullActivity", text);

                    if(obj.getString("__typename").equalsIgnoreCase("GraphImage")) imagesArray.add(obj.getString("display_url"));

                    else if(obj.getString("__typename").equalsIgnoreCase("GraphSidecar")) {
                        JSONArray arObj = obj.getJSONObject("edge_sidecar_to_children").getJSONArray("edges");
                        for (int i = 0; i <arObj.length() ; i++) {
                            imagesArray.add(arObj.getJSONObject(i).getJSONObject("node").getString("display_url"));
                        }
                    } else if (obj.getString("__typename").equalsIgnoreCase("GraphVideo")) {
                        isVideo = true;
                        imagesArray.add(obj.getString("display_url"));
                        videousArray.add(obj.getString("video_url"));
                    }
                    h = obj.getJSONObject("dimensions").getInt("height");
                    w = obj.getJSONObject("dimensions").getInt("width");

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //setPagerDimension(h,w);
                init(isVideo);

                String boldText = "radiokunel ";
                SpannableString str = new SpannableString(boldText + text);
                //str.setSpan(new StyleSpan(Typeface.BOLD), 0, boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                str.setSpan(new ForegroundColorSpan(Color.BLACK), 0, boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                tvDescription.setText(str);
                tvLike.setText("Нравится: " + likeText);
                tvLocation.setText(locationText);


            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                Toast.makeText(InstagramFullMediaActivity.this, "Error", Toast.LENGTH_SHORT).show();
                super.onFailure(statusCode, headers, throwable, errorResponse);

            }


        });
    }

    @Override
    protected void onStop() {

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if(slidingAdapter!=null) slidingAdapter.stopPlayer();
        super.onDestroy();
    }
}
