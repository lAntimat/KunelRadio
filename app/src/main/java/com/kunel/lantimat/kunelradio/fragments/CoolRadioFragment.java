package com.kunel.lantimat.kunelradio.fragments;


import android.content.ComponentName;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.graphics.Palette;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.kunel.lantimat.kunelradio.BackgroundAudioService;
import com.kunel.lantimat.kunelradio.KunelRestClient;
import com.kunel.lantimat.kunelradio.R;
import com.kunel.lantimat.kunelradio.Utils.SquareImageView;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.Header;
import dyanamitechetan.vusikview.VusikView;
import jp.wasabeef.picasso.transformations.BlurTransformation;
import me.crosswall.lib.coverflow.CoverFlow;
import me.crosswall.lib.coverflow.core.PagerContainer;


/**
 * Created by GabdrakhmanovII on 28.07.2017.
 */

public class CoolRadioFragment extends Fragment {

    private ImageButton mPlayPauseToggleButton, muteButton, stopButton;
    SliderLayout mDemoSlider;
    VusikView vusikView;
    private View v;
    private ViewPager pager;
    public static int[] covers = {R.drawable.bg_radio, R.drawable.bg_radio, R.drawable.bg_radio, R.drawable.bg_radio, R.drawable.bg_radio, R.drawable.bg_radio};
    public static String[] song = {"Make War", "Shadow", "Black Parade","Make War", "Shadow", "Black Parade" };

    private static final String TAG = "RadioFragment";

    private static final long PROGRESS_UPDATE_INTERNAL = 1000;
    private static final long PROGRESS_UPDATE_INITIAL_INTERVAL = 100;

    private static final int STATE_PAUSED = 0;
    private static final int STATE_PLAYING = 1;

    private int mCurrentState;


    private MediaBrowserCompat mMediaBrowserCompat;
    private MediaControllerCompat mMediaControllerCompat;

    HashMap<String,String> url_maps = new HashMap<String, String>();
    ArrayList<String> arImages = new ArrayList<>();

    RelativeLayout rootView;
    ImageView imageView;

    PagerIndicator pagerIndicator;
    ProgressBar progressBarPlay;

    ProgressBar seekBar;

    TextView tvNowDuration;

    private final Runnable mUpdateProgressTask = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };

    private final ScheduledExecutorService mExecutorService =
            Executors.newSingleThreadScheduledExecutor();

    private ScheduledFuture<?> mScheduleFuture;
    private PlaybackStateCompat mLastPlaybackState;
    private final Handler mHandler = new Handler();


    private MediaBrowserCompat.ConnectionCallback mMediaBrowserCompatConnectionCallback = new MediaBrowserCompat.ConnectionCallback() {

        @Override
        public void onConnected() {
            super.onConnected();

                try {
                    mMediaControllerCompat = new MediaControllerCompat(getActivity().getApplicationContext(), mMediaBrowserCompat.getSessionToken());
                    mMediaControllerCompat.registerCallback(mMediaControllerCompatCallback);
                    MediaControllerCompat.setMediaController(getActivity(), mMediaControllerCompat);
                    //MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().playFromMediaId(String.valueOf(R.raw.warner_tautz_off_broadway), null);
                    //MediaControllerCompat.getMediaController(getActivity()).getTransportControls().playFromUri(Uri.parse("http://live.kunelradio.ru:8000/128.mp3"), null);
                     Log.d(TAG, "OnConnected");

                    PlaybackStateCompat state = mMediaControllerCompat.getPlaybackState();
                    updatePlaybackState(state);
                    updateDuration(mMediaControllerCompat.getMetadata());
                } catch (RemoteException e) {

                }

            //PlaybackStateCompat state = mMediaControllerCompat.getPlaybackState();
            //updatePlaybackState(state);
        }
    };

    private MediaControllerCompat.Callback mMediaControllerCompatCallback = new MediaControllerCompat.Callback() {

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            updatePlaybackState(state);


            if( state == null ) {
                return;
            }

            Log.d(TAG, "PlayBackStateChange");
            mCurrentState = state.getState();

            /*switch( state.getState() ) {
                case PlaybackStateCompat.STATE_PLAYING: {
                    mCurrentState = STATE_PLAYING;
                    break;
                }
                case PlaybackStateCompat.STATE_PAUSED: {
                    mCurrentState = STATE_PAUSED;
                    break;
                }
            }*/

        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            if (metadata != null) {
                //updateMediaDescription(metadata.getDescription());
                updateDuration(metadata);
                //updateMuteButton();
            }
        }
    };

    public CoolRadioFragment() {
    }


    public static CoolRadioFragment newInstance() {
        CoolRadioFragment fragment = new CoolRadioFragment();
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //important! set your user agent to prevent getting banned from the osm servers

        mMediaBrowserCompat = new MediaBrowserCompat(getContext(), new ComponentName((getActivity()), BackgroundAudioService.class),
                mMediaBrowserCompatConnectionCallback, getActivity().getIntent().getExtras());
        mMediaBrowserCompat.connect();

        //parseSliderImage();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.fragment_cool_radio, null);

        mPlayPauseToggleButton = (ImageButton) v.findViewById(R.id.playPauseBtn);
        muteButton = (ImageButton) v.findViewById(R.id.imageButton4);
        stopButton = (ImageButton) v.findViewById(R.id.imageButton6);
        //Blurry.with(getContext()).from(BitmapFactory.decodeResource(getResources(),R.drawable.kunel_logo2)).into(imageView);

        //pagerIndicator = (PagerIndicator) v.findViewById(R.id.custom_indicator);

        tvNowDuration = (TextView) v.findViewById(R.id.tv_current_time);
        progressBarPlay = (ProgressBar) v.findViewById(R.id.progressBar);
        progressBarPlay.setVisibility(View.INVISIBLE);

        seekBar = (ProgressBar) v.findViewById(R.id.progressBarRadio);

        mPlayPauseToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if( mCurrentState == PlaybackStateCompat.STATE_PAUSED) {
                    mMediaControllerCompat.getTransportControls().play();

//                    vusikView.resumeNotesFall();
                    //MediaControllerCompat.getMediaController(getActivity()).getTransportControls().playFromUri(Uri.parse("http://live.kunelradio.ru:8000/128.mp3"), null);
                } else if( mCurrentState == PlaybackStateCompat.STATE_PLAYING ) {
                    mMediaControllerCompat.getTransportControls().pause();
                    //vusikView.pauseNotesFall();
                    } else {
                    //mMediaControllerCompat.getTransportControls().play();
                    //vusikView.start();
                    mMediaControllerCompat.getTransportControls().playFromUri(Uri.parse("http://live.kunelradio.ru:8000/128.mp3"), null);
                    progressBarPlay.setVisibility(View.VISIBLE);

                }
            }
        });

        muteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateMuteButton();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              mMediaControllerCompat.getTransportControls().stop();
            }
        });

        initPager(v);
        parseSliderImage();
        return v;
    }

    private void initPager(View v) {
        PagerContainer container = (PagerContainer) v.findViewById(R.id.pager_container);
        pager = container.getViewPager();
        pager.setAdapter(new MyPagerAdapter());
        pager.setClipChildren(false);
        //
        pager.setOffscreenPageLimit(15);

        boolean showTransformer = getActivity().getIntent().getBooleanExtra("showTransformer",true);


        if(showTransformer){

            new CoverFlow.Builder()
                    .with(pager)
                    .scale(0.3f)
                    .pagerMargin(getResources().getDimensionPixelSize(R.dimen.pager_margin))
                    .spaceSize(0f)
                    .build();

        }else{
            pager.setPageMargin(30);
        }

        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //tv_song.setText(song[position]);
                RelativeLayout relativeLayout = (RelativeLayout) pager.getAdapter().instantiateItem(pager, 0);
                ViewCompat.setElevation(relativeLayout.getRootView(), 8.0f);
                Picasso.with(getContext()).load(arImages.get(position)).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        Bitmap bitmap1 = Bitmap.createBitmap(bitmap);
                        Palette palette = Palette.from(bitmap1).generate();
                        setStatusBar(palette);
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void parseSliderImage() {
        loadViewPagerImagesFromCash();
        KunelRestClient.get("", null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {


                addImageToViewPager(bytes);
                //new AddImagesToSlider().execute(bytes);



            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {

            }
        });
    }

    private void loadViewPagerImagesFromCash() {
        SharedPreferences prefs = getActivity().getSharedPreferences("slider", 0);
        if(prefs.getAll()!=null) {
            for (Map.Entry entry : prefs.getAll().entrySet())
                url_maps.put(entry.getKey().toString(), entry.getValue().toString());
        }
        arImages.clear();
        for(String name : url_maps.keySet()){
            arImages.add(url_maps.get(name));
        }
        pager.getAdapter().notifyDataSetChanged();
    }

    private void addImageToViewPager(byte[] bytes) {
        String str = null;
        try {

            str = new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Document doc = Jsoup.parse(str);
        Elements sliderImages = doc.select("img.slider-5807");
        url_maps.clear();
        arImages.clear();
        for (int j = 0; j < sliderImages.size(); j++) {
            url_maps.put("Изображение " + j, sliderImages.get(j).attr("src"));
            arImages.add(sliderImages.get(j).attr("src"));
        }
        try {
            SharedPreferences.Editor editor;
            editor = getActivity().getSharedPreferences("slider", 0).edit();
            for (Map.Entry entry : url_maps.entrySet())
                editor.putString(entry.getKey().toString(), entry.getValue().toString());
            editor.apply();
        } catch (Exception e) {}

        //initPager(v);
        pager.getAdapter().notifyDataSetChanged();
    }

    private void initSlider() {


        SharedPreferences prefs = getActivity().getSharedPreferences("slider", 0);
        if(prefs.getAll()!=null) {
            for (Map.Entry entry : prefs.getAll().entrySet())
                url_maps.put(entry.getKey().toString(), entry.getValue().toString());
        }


        for(String name : url_maps.keySet()){
           //TextSliderView textSliderView = new TextSliderView(getContext());
            // initialize a SliderLayout

            DefaultSliderView defaultSliderView = new DefaultSliderView(getContext());
            defaultSliderView.image(url_maps.get(name))
                    .setScaleType(BaseSliderView.ScaleType.Fit)
                    .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                        @Override
                        public void onSliderClick(BaseSliderView slider) {

                        }
                    });

            //add your extra information
            //textSliderView.bundle(new Bundle());
            //textSliderView.getBundle()
                    //.putString("extra",name);

            mDemoSlider.addSlider(defaultSliderView);
        }

        mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Fade);
        //mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mDemoSlider.setCustomIndicator(pagerIndicator);
        //mDemoSlider.setCustomAnimation(new DescriptionAnimation());
        mDemoSlider.setDuration(10000);
        mDemoSlider.addOnPageChangeListener(new ViewPagerEx.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //Blurry.with(getContext()).radius(25).sampling(2).onto((ViewGroup) rootView);
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void updatePlaybackState(PlaybackStateCompat state) {
        if (state == null) {
            mPlayPauseToggleButton.setImageResource(R.drawable.ic_play_white_24dp);
            Log.d(TAG, "STATE_NULL");
            return;
        }

        mLastPlaybackState = state;
        mCurrentState = state.getState();
        switch (state.getState()) {
            case PlaybackStateCompat.STATE_PLAYING:
                mPlayPauseToggleButton.setImageResource(R.drawable.ic_pause_white_24dp);
                progressBarPlay.setVisibility(View.INVISIBLE);
                tvNowDuration.setVisibility(View.VISIBLE);
                //scheduleSeekbarUpdate();
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                mPlayPauseToggleButton.setImageResource(R.drawable.ic_play_white_24dp);
                break;
            case PlaybackStateCompat.STATE_NONE:
                Log.d(TAG, "STATE_NONE");
                break;
            case PlaybackStateCompat.STATE_STOPPED:
                mPlayPauseToggleButton.setImageResource(R.drawable.ic_play_white_24dp);
                tvNowDuration.setVisibility(View.INVISIBLE);
                Log.d(TAG, "STATE_STOPPED");
                break;
            case PlaybackStateCompat.STATE_BUFFERING:
                progressBarPlay.setVisibility(View.VISIBLE);
                break;
            default:
                //Timber.d(TAG, "updatePlaybackState called with unhandled state", state.getState());
        }
    }

    private void updateMuteButton() {
        Bundle bundle = new Bundle();
        long nowVolume = mMediaControllerCompat.getMetadata().getLong(BackgroundAudioService.VOLUME);
        float volumeToSend = 0f;
        if(nowVolume>0) {
            volumeToSend = 0f;
            muteButton.setImageResource(R.drawable.ic_volume_high_white_18dp);
        } else {
            volumeToSend = 1f;
            muteButton.setImageResource(R.drawable.ic_volume_mute_white_18dp);
        }
        bundle.putFloat(BackgroundAudioService.VOLUME, volumeToSend);
        mMediaControllerCompat.sendCommand(BackgroundAudioService.VOLUME, bundle, new ResultReceiver(new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                return false;
            }
        })));

        Log.d(TAG, "updateMuteButton vol=" + volumeToSend);
    }

    private void scheduleSeekbarUpdate() {
        stopSeekbarUpdate();
        if (!mExecutorService.isShutdown()) {
            mScheduleFuture = mExecutorService.scheduleAtFixedRate(
                    new Runnable() {
                        @Override
                        public void run() {
                            mHandler.post(mUpdateProgressTask);
                        }
                    }, PROGRESS_UPDATE_INITIAL_INTERVAL,
                    PROGRESS_UPDATE_INTERNAL, TimeUnit.MILLISECONDS);
        }
    }

    private void stopSeekbarUpdate() {
        if (mScheduleFuture != null) {
            mScheduleFuture.cancel(false);
        }
    }

    private void updateProgress() {
        if (mLastPlaybackState == null) {
            return;
        }
        long currentPosition = mLastPlaybackState.getPosition();
        if (mLastPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
            // Calculate the elapsed time between the last position update and now and unless
            // paused, we can assume (delta * speed) + current position is approximately the
            // latest position. This ensure that we do not repeatedly call the getPlaybackState()
            // on MediaControllerCompat.
            long timeDelta = SystemClock.elapsedRealtime() -
                    mLastPlaybackState.getLastPositionUpdateTime();
            currentPosition += (int) timeDelta * mLastPlaybackState.getPlaybackSpeed();
            //Log.d("RadioFragment", "Current position" + currentPosition);
        }
        seekBar.setProgress((int) currentPosition);
    }

    private void updateDuration(MediaMetadataCompat metadata) {
        if (metadata == null) {
            return;
        }
        Bundle args = new Bundle();
        args.putInt("doctor_id", 0);
        mMediaControllerCompat.getTransportControls().sendCustomAction(BackgroundAudioService.COMMAND_EXAMPLE, args);
        //LogHelper.d(TAG, "updateDuration called ");
        int duration = (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        int bufferedDuration = (int) metadata.getLong(BackgroundAudioService.BUFFERED_POSITION);
        seekBar.setMax(1000);
        seekBar.setProgress(duration/1000);
        seekBar.setSecondaryProgress(bufferedDuration/1000);
        tvNowDuration.setText(DateUtils.formatElapsedTime(duration/1000));
        //Log.d("RadioFragment", "Buffered position" + bufferedDuration);
    }

    @Override
    public void onResume() {
//        mDemoSlider.startAutoCycle();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        /*try {
            if (MediaControllerCompat.getMediaController(getActivity()).getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                MediaControllerCompat.getMediaController(getActivity()).getTransportControls().pause();
            }
        } catch (Exception e) {}*/

        Log.d(TAG, "onDestroy");
        //mMediaBrowserCompat.disconnect();
        super.onDestroy();
    }

    @Override
    public void onStop() {
//        mDemoSlider.stopAutoCycle();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView");
        /*if (MediaControllerCompat.getMediaController(getActivity()) != null) {
            MediaControllerCompat.getMediaController(getActivity()).unregisterCallback(mMediaControllerCompatCallback);
        }*/
        mMediaBrowserCompat.disconnect();
        super.onDestroyView();
    }

    private class MyPagerAdapter extends PagerAdapter {

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            View view = LayoutInflater.from(getContext()).inflate(R.layout.item_cover,null);
            final SquareImageView imageViewBg = (SquareImageView) view.findViewById(R.id.image_cover);
            ImageView imageView = (ImageView) view.findViewById(R.id.imageView);

            Picasso.with(getContext())
                    .load(arImages.get(position))
                    .transform(new BlurTransformation(getContext()))
                    .into(imageViewBg);

            Picasso.with(getContext()).load(arImages.get(position)).into(imageView);
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
            return arImages.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return (view == object);
        }
    }
    public void setStatusBar(Palette palette){
        Window window = getActivity().getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Palette.Swatch vibrant = palette.getDominantSwatch();
            if (vibrant != null) {
                window.setStatusBarColor(vibrant.getRgb());
            }

        }
    }


    public Bitmap drawableToBitmap(int id) {
        Bitmap bitmap = null;
        Drawable drawable = getResources().getDrawable(id);
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

}