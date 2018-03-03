package com.kunel.lantimat.kunelradio.adapters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.kunel.lantimat.kunelradio.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by GabdrakhmanovII on 16.08.2017.
 */

public class SlidingImage_Adapter extends PagerAdapter {


    private ArrayList<String> IMAGES;
    private ArrayList<String> videos;
    private LayoutInflater inflater;
    private Context context;
    private Callback callback;
    private View imageLayout;
    private boolean isVideo;
    private boolean isAudioFocusThis = false;
    AudioManager.OnAudioFocusChangeListener listener;
    boolean move = false;

    private SimpleExoPlayer player;
    private SimpleExoPlayerView simpleExoPlayerView;
    private MediaSource mediaSource;
    private int RENDERER_COUNT = 300000;
    private int minBufferMs =    250000;
    private final int BUFFER_SEGMENT_SIZE = 64 * 1024;
    private final int BUFFER_SEGMENT_COUNT = 256;

    ImageView mute;

    public SlidingImage_Adapter(Context context,ArrayList<String> IMAGES) {
        this.context = context;
        this.IMAGES=IMAGES;
        inflater = LayoutInflater.from(context);
    }

    public SlidingImage_Adapter(Context context, ArrayList<String> IMAGES, ArrayList<String> videos,  boolean isVideo) {
        this.context = context;
        this.IMAGES=IMAGES;
        this.videos = videos;
        inflater = LayoutInflater.from(context);
        this.isVideo = isVideo;
    }

    // Internal methods

    private void initPlayer(String url) {

        initNoisyReceiver();


        // 1. Create a default TrackSelector
        Handler mainHandler = new Handler();
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);


        // 2. Create the player
        player =
                ExoPlayerFactory.newSimpleInstance(context, trackSelector);


        // Measures bandwidth during playback. Can be null if not required.
        DefaultBandwidthMeter bandwidthMeter2 = new DefaultBandwidthMeter();
        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context,
                Util.getUserAgent(context, context.getPackageName()), bandwidthMeter2);
        // Produces Extractor instances for parsing the media data.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        // This is the MediaSource representing the media to be played.
        MediaSource videoSource = new ExtractorMediaSource(Uri.parse(url),
                dataSourceFactory, extractorsFactory, null, null);




        simpleExoPlayerView.requestFocus();
        simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);
        //simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT);
        simpleExoPlayerView.setUseController(false);
        simpleExoPlayerView.setClickable(true);
        simpleExoPlayerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        move = true;
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        move = false;
                    case MotionEvent.ACTION_DOWN:
                            Log.d("Adapter", "click");
                            if (!isAudioFocusThis) {
                                isAudioFocusThis = successfullyRetrievedAudioFocus();
                                player.setVolume(1f);
                                mute.setImageResource(R.drawable.ic_volume_high_white_24dp);


                            } else if (isAudioFocusThis) {
                                AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                                audioManager.abandonAudioFocus(listener);
                                player.setVolume(0f);
                                mute.setImageResource(R.drawable.ic_volume_mute_white_24dp);
                                isAudioFocusThis = false;
                            }
                        break;
                }


                return true;
            }
        });
        simpleExoPlayerView.setPlayer(player);


        // Prepare the player with the source.
        player.prepare(videoSource);
        player.setVolume(0f);
        player.setRepeatMode(Player.REPEAT_MODE_ONE);
        player.setPlayWhenReady(true);

        listener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {

            }
        };
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        container.removeView((View) object);
    }

    public void stopPlayer() {
        if(player!=null) {
            player.setVolume(0f);
            player.stop();
            player.release();
            context.unregisterReceiver(mNoisyReceiver);
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            audioManager.abandonAudioFocus(listener);
        }
    }

    private BroadcastReceiver mNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //if( mMediaPlayer != null && mMediaPlayer.isPlaying() ) {
            //mMediaPlayer.pause();
            if( player != null) {
                player.setPlayWhenReady(false);
            }
        }
    };

    private void initNoisyReceiver() {
        //Handles headphones coming unplugged. cannot be done through a manifest receiver
        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        context.registerReceiver(mNoisyReceiver, filter);
    }

    private boolean successfullyRetrievedAudioFocus() {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        int result = audioManager.requestAudioFocus(listener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        return result == AudioManager.AUDIOFOCUS_GAIN;
    }

    @Override
    public int getCount() {
        return IMAGES.size();
    }

    @Override
    public Object instantiateItem(ViewGroup view, int position) {
        imageLayout = inflater.inflate(R.layout.slidingimages_layout, view, false);

        assert imageLayout != null;
        final ImageView imageView = (ImageView) imageLayout.findViewById(R.id.image);
        mute = (ImageView) imageLayout.findViewById(R.id.mute);
        mute.setVisibility(View.INVISIBLE);
        final ProgressBar progressBar = (ProgressBar) imageLayout.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        simpleExoPlayerView = (SimpleExoPlayerView) imageLayout.findViewById(R.id.player_view);
        simpleExoPlayerView.setVisibility(View.INVISIBLE);

        callback = new Callback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onError() {
                progressBar.setVisibility(View.INVISIBLE);

            }
        };


            if(isVideo) {
                initPlayer(videos.get(position));
                player.addListener(new Player.EventListener() {
                    @Override
                    public void onTimelineChanged(Timeline timeline, Object manifest) {

                    }

                    @Override
                    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

                    }

                    @Override
                    public void onLoadingChanged(boolean isLoading) {

                    }

                    @Override
                    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                        if(playbackState == Player.STATE_READY) {
                            simpleExoPlayerView.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.INVISIBLE);
                            mute.setImageResource(R.drawable.ic_volume_mute_white_24dp);
                            mute.setVisibility(View.VISIBLE);
                        }
                        Log.d("PlayerState", "State " + playbackState);
                    }

                    @Override
                    public void onRepeatModeChanged(int repeatMode) {

                    }

                    @Override
                    public void onPlayerError(ExoPlaybackException error) {

                    }

                    @Override
                    public void onPositionDiscontinuity() {

                    }

                    @Override
                    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

                    }
                });
                imageView.setVisibility(View.INVISIBLE);
            }

        progressBar.setVisibility(View.VISIBLE);

        Picasso.with(context).load(IMAGES.get(position)).into(imageView, callback);

        view.addView(imageLayout, 0);

        return imageLayout;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }


}
