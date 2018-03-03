package com.kunel.lantimat.kunelradio.fragments;


import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.kunel.lantimat.kunelradio.BackgroundAudioService;
import com.kunel.lantimat.kunelradio.KunelRestClient;
import com.kunel.lantimat.kunelradio.MainActivity;
import com.kunel.lantimat.kunelradio.R;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.Header;
import dyanamitechetan.vusikview.VusikView;
import jp.wasabeef.blurry.Blurry;


/**
 * Created by GabdrakhmanovII on 28.07.2017.
 */

public class RadioFragment extends Fragment {

    private ImageButton mPlayPauseToggleButton, m30SecondBack;
    SliderLayout mDemoSlider;
    VusikView vusikView;

    private static final String TAG = "RadioFragment";

    private static final long PROGRESS_UPDATE_INTERNAL = 1000;
    private static final long PROGRESS_UPDATE_INITIAL_INTERVAL = 100;

    private static final int STATE_PAUSED = 0;
    private static final int STATE_PLAYING = 1;

    private int mCurrentState;


    private MediaBrowserCompat mMediaBrowserCompat;
    private MediaControllerCompat mMediaControllerCompat;

    HashMap<String,String> url_maps = new HashMap<String, String>();

    RelativeLayout rootView;
    ImageView imageView;

    PagerIndicator pagerIndicator;
    ProgressBar progressBarPlay;

    SeekBar seekBar;

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
            }
        }
    };

    public RadioFragment() {
    }


    public static RadioFragment newInstance() {
        RadioFragment fragment = new RadioFragment();
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //important! set your user agent to prevent getting banned from the osm servers

        mMediaBrowserCompat = new MediaBrowserCompat(getContext(), new ComponentName((getActivity()), BackgroundAudioService.class),
                mMediaBrowserCompatConnectionCallback, getActivity().getIntent().getExtras());
        mMediaBrowserCompat.connect();

        parseSliderImage();



    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_radio, null);

        mPlayPauseToggleButton = (ImageButton) v.findViewById(R.id.imageButton);
        m30SecondBack = (ImageButton) v.findViewById(R.id.ibtn30SecondBack);
        mDemoSlider  = (SliderLayout) v.findViewById(R.id.slider);
        vusikView = (VusikView) v.findViewById(R.id.vusikView);
        rootView = (RelativeLayout) v.findViewById(R.id.root_view);
        imageView = (ImageView) v.findViewById(R.id.imageView);
        Blurry.with(getContext()).from(BitmapFactory.decodeResource(getResources(),R.drawable.kunel_logo2)).into(imageView);

        pagerIndicator = (PagerIndicator) v.findViewById(R.id.custom_indicator);

        tvNowDuration = (TextView) v.findViewById(R.id.tvNowDur);
        progressBarPlay = (ProgressBar) v.findViewById(R.id.progressBarRadio);
        progressBarPlay.setVisibility(View.INVISIBLE);

        seekBar = (SeekBar) v.findViewById(R.id.seekBar);

        mPlayPauseToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if( mCurrentState == PlaybackStateCompat.STATE_PAUSED) {
                    mMediaControllerCompat.getTransportControls().play();

                    vusikView.resumeNotesFall();
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

        m30SecondBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaControllerCompat.getTransportControls().seekTo(mMediaControllerCompat.getMetadata().getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
            }
        });

        initSlider();

        return v;
    }

    private void parseSliderImage() {
        KunelRestClient.get("", null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {



                new AddImagesToSlider().execute(bytes);



            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {

            }
        });

        /*HashMap<String,String> url_maps = new HashMap<String, String>();
        url_maps.put("Hannibal", "http://static2.hypable.com/wp-content/uploads/2013/12/hannibal-season-2-release-date.jpg");
        url_maps.put("Big Bang Theory", "http://tvfiles.alphacoders.com/100/hdclearart-10.png");
        url_maps.put("House of Cards", "http://cdn3.nflximg.net/images/3093/2043093.jpg");
        url_maps.put("Game of Thrones", "http://images.boomsbeat.com/data/images/full/19640/game-of-thrones-season-4-jpg.jpg");*/



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
            mPlayPauseToggleButton.setImageResource(R.drawable.ic_play_white_36dp);
            Log.d(TAG, "STATE_NULL");
            return;
        }

        mLastPlaybackState = state;
        mCurrentState = state.getState();
        switch (state.getState()) {
            case PlaybackStateCompat.STATE_PLAYING:
                mPlayPauseToggleButton.setImageResource(R.drawable.ic_pause_white_36dp);
                progressBarPlay.setVisibility(View.INVISIBLE);
                tvNowDuration.setVisibility(View.VISIBLE);
                //scheduleSeekbarUpdate();
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                mPlayPauseToggleButton.setImageResource(R.drawable.ic_play_white_36dp);
                break;
            case PlaybackStateCompat.STATE_NONE:
                Log.d(TAG, "STATE_NONE");
                break;
            case PlaybackStateCompat.STATE_STOPPED:
                mPlayPauseToggleButton.setImageResource(R.drawable.ic_play_white_36dp);
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
        mDemoSlider.startAutoCycle();
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
        mDemoSlider.stopAutoCycle();
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


    public class AddImagesToSlider extends AsyncTask<byte[], Void, Void> {



        @Override
        protected Void doInBackground(byte[]... params) {
            String str = null;
            try {

                str = new String(params[0], "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            Document doc = Jsoup.parse(str);
            Elements sliderImages = doc.select("img.slider-5807");
            url_maps.clear();
            for (int j = 0; j < sliderImages.size(); j++) {
                url_maps.put("Изображение " + j, sliderImages.get(j).attr("src"));
            }
            try {
                SharedPreferences.Editor editor;
                editor = getActivity().getSharedPreferences("slider", 0).edit();
                for (Map.Entry entry : url_maps.entrySet())
                    editor.putString(entry.getKey().toString(), entry.getValue().toString());
                editor.apply();
            } catch (Exception e) {}
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

}
