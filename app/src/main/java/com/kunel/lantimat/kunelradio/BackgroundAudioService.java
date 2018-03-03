package com.kunel.lantimat.kunelradio;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Util;
import com.kunel.lantimat.kunelradio.Utils.NotificationDismissedReceiver;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class BackgroundAudioService extends MediaBrowserServiceCompat implements AudioManager.OnAudioFocusChangeListener{

    public static final String COMMAND_EXAMPLE = "command_example";
    public static final String MUTE_COMMAND = "mute";
    public static final String UNMUTE_COMMAND = "unmute";
    public static final String METADA_UPDATE = "metadate_update";
    public static final String BUFFERED_POSITION = "buffered_position";
    public static final String VOLUME = "volume";
    public static String RADIO_URL = "http://live.kunelradio.ru:8000/128.mp3";
    //public static String RADIO_URL = "https://jt.zf.fm/2/bf/rick_astley_-_never_gonna_give_you_up_(zvukoff.ru).mp3";

    private static final long PROGRESS_UPDATE_INTERNAL = 1000;
    private static final long PROGRESS_UPDATE_INITIAL_INTERVAL = 100;

    Bitmap playingBitmap;
    // make sure to set Target as strong reference
    private Target loadtarget;

    private SimpleExoPlayer mMediaPlayer;

    MediaSource mediaSource;

    private final Runnable mUpdateProgressTask = new Runnable() {
        @Override
        public void run() {
            updateMediaSessionMetadata();
        }
    };

    private final ScheduledExecutorService mExecutorService =
            Executors.newSingleThreadScheduledExecutor();

    private ScheduledFuture<?> mScheduleFuture;
    private PlaybackStateCompat mLastPlaybackState;
    private final Handler mHandler = new Handler();

    private MediaSessionCompat mMediaSessionCompat;

    private BroadcastReceiver mNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //if( mMediaPlayer != null && mMediaPlayer.isPlaying() ) {
                //mMediaPlayer.pause();
            if( mMediaPlayer != null) {
                mMediaSessionCallback.onPause();
            }
        }
    };

    private MediaSessionCompat.Callback mMediaSessionCallback = new MediaSessionCompat.Callback() {

        @Override
        public void onPlay() {
            super.onPlay();
            if( !successfullyRetrievedAudioFocus() ) {
                return;
            }

            mMediaSessionCompat.setActive(true);
            setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);

            showPlayingNotification();
            //mMediaPlayer.play();
            mMediaPlayer.setPlayWhenReady(true);
            startService(new Intent(getApplicationContext(), BackgroundAudioService.class));
            scheduleSeekbarUpdate();
        }

        @Override
        public void onPause() {
            super.onPause();

            mMediaPlayer.setPlayWhenReady(false);
            setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);
            showPausedNotification();
        }

        @Override
        public void onStop() {
            stopForeground(false);
            stopSeekbarUpdate();
            mMediaPlayer.stop();
            mMediaPlayer.release();
            setMediaPlaybackState(PlaybackStateCompat.STATE_STOPPED);
            updateMediaSessionMetadata();
            NotificationManagerCompat.from(getApplicationContext()).cancel(1);
            stopSelf();
            super.onStop();
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            super.onPlayFromMediaId(mediaId, extras);

            try {
                AssetFileDescriptor afd = getResources().openRawResourceFd(Integer.valueOf(mediaId));
                if( afd == null ) {
                    return;
                }

                try {
                    //mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());

                } catch( IllegalStateException e ) {
                    mMediaPlayer.release();
                    //initMediaPlayer();
                    //mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                }

                afd.close();
                initMediaSessionMetadata();

            } catch (IOException e) {
                return;
            }


                //mMediaPlayer.prepare();

            //Work with extras here if you want
        }

        @Override
        public void onPlayFromUri(Uri uri, Bundle extras) {


                try {
                    //mMediaPlayer.setDataSource(String.valueOf(uri));
                    initMediaPlayer();

                } catch( IllegalStateException e ) {
                    mMediaPlayer.release();
                    initMediaPlayer();
                    //mMediaPlayer.setDataSource(String.valueOf(uri));

                }

                initMediaSessionMetadata();
                //mMediaPlayer.prepare();
                onPlay();


            //Work with extras here if you want
            super.onPlayFromUri(uri, extras);
        }



        @Override
        public void onCommand(String command, Bundle extras, ResultReceiver cb) {
            if( COMMAND_EXAMPLE.equalsIgnoreCase(command) ) {
                //Custom command here
            } else if(METADA_UPDATE.equalsIgnoreCase(command)) {
                updateMediaSessionMetadata();
            } else if(VOLUME.equalsIgnoreCase(command)) {
                float volume = extras.getFloat(VOLUME);
                mMediaPlayer.setVolume(volume);
                Log.d("RadioFragment", "volume command");
            }

            Log.d("RadioFragment", "onCommand");
            super.onCommand(command, extras, cb);

        }



        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        playingBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.kunel_logo2);

        registerStopSelf(); //регистриция ресивеа для Swipe To Close Intent
        //initMediaPlayer();
        initMediaSession();
        initNoisyReceiver();
        loadBitmap("http://www.lifemusic.su/pictures/10005b.jpg");
    }

    private void initNoisyReceiver() {
        //Handles headphones coming unplugged. cannot be done through a manifest receiver
        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(mNoisyReceiver, filter);
    }

    private void initMediaPlayer() {
        // 1. Create a default TrackSelector
        Handler mainHandler = new Handler();
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);

        //mMediaPlayer = HybridMediaPlayer.getInstance(getApplicationContext());
        mMediaPlayer = ExoPlayerFactory.newSimpleInstance(getApplicationContext(), trackSelector);
        //mMediaPlayer.setWakeMode (getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        bandwidthMeter = new DefaultBandwidthMeter();
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "mediaPlayerSample"), (TransferListener<? super DataSource>) bandwidthMeter);
        DefaultExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        MediaSource mediaSource = new ExtractorMediaSource(Uri.parse(RADIO_URL), dataSourceFactory, extractorsFactory, null, null);
        mMediaPlayer.prepare(mediaSource);

        mMediaPlayer.addListener(new Player.EventListener() {
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

            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Toast.makeText(getApplicationContext(), "Ошибка " + error.toString(), Toast.LENGTH_LONG).show();
                mMediaSessionCallback.onStop();
            }

            @Override
            public void onPositionDiscontinuity() {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }
        });

    }



    private void showPlayingNotification() {
        NotificationCompat.Builder builder = MediaStyleHelper.from(BackgroundAudioService.this, mMediaSessionCompat);
        if( builder == null ) {
            return;
        }

        Intent resultIntent = new Intent(this, MainActivity.class);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        builder.addAction(new NotificationCompat.Action(R.drawable.ic_pause_white_24dp, "Pause", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY_PAUSE)));
        builder.setStyle(new NotificationCompat.MediaStyle().setShowActionsInCompactView(0).setMediaSession(mMediaSessionCompat.getSessionToken()));
        builder.setSmallIcon(R.drawable.butterfly_48);
        builder.setContentIntent(resultPendingIntent);
        builder.setDeleteIntent(createOnDismissedIntent(getApplicationContext(), 1));
        NotificationManagerCompat.from(BackgroundAudioService.this).notify(1, builder.build());
        startForeground(1, builder.build());
    }

    private void showPausedNotification() {
        NotificationCompat.Builder builder = MediaStyleHelper.from(this, mMediaSessionCompat);
        if( builder == null ) {
            return;
        }

        builder.addAction(new NotificationCompat.Action(R.drawable.ic_play_white_24dp, "Play", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY_PAUSE)));
        builder.setStyle(new NotificationCompat.MediaStyle().setShowActionsInCompactView(0).setMediaSession(mMediaSessionCompat.getSessionToken()));
        builder.setSmallIcon(R.drawable.butterfly_48);
        builder.setDeleteIntent(createOnDismissedIntent(getApplicationContext(), 1));
        NotificationManagerCompat.from(this).notify(1, builder.build());
        stopForeground(false);
    }

    private PendingIntent createOnDismissedIntent(Context context, int notificationId) {
        Intent intent = new Intent(context, NotificationDismissedReceiver.class);
        intent.putExtra("deleteIntent", notificationId);

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(context.getApplicationContext(),
                        notificationId, intent, 0);
        return pendingIntent;
    }

    private BroadcastReceiver stopSelf = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mMediaSessionCallback.onStop();
        }
    };

    private void registerStopSelf() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter("stopSelf");
        registerReceiver(stopSelf, filter);
    }

    private void initMediaSession() {
        ComponentName mediaButtonReceiver = new ComponentName(getApplicationContext(), MediaButtonReceiver.class);
        mMediaSessionCompat = new MediaSessionCompat(getApplicationContext(), "Tag", mediaButtonReceiver, null);

        mMediaSessionCompat.setCallback(mMediaSessionCallback);
        mMediaSessionCompat.setFlags( MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS );

        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setClass(this, MediaButtonReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
        mMediaSessionCompat.setMediaButtonReceiver(pendingIntent);

        setSessionToken(mMediaSessionCompat.getSessionToken());
    }

    private void setMediaPlaybackState(int state) {
        PlaybackStateCompat.Builder playbackstateBuilder = new PlaybackStateCompat.Builder();
        if( state == PlaybackStateCompat.STATE_PLAYING ) {
            playbackstateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PAUSE);
        } else {
            playbackstateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PLAY);
        }
        playbackstateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0);
        mMediaSessionCompat.setPlaybackState(playbackstateBuilder.build());
    }

    private void initMediaSessionMetadata() {
        MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();
        //Notification icon in card
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, playingBitmap);
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, playingBitmap);

        //lock screen icon for pre lollipop
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, playingBitmap);
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, "Кунел радиосы");
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, "Кунелле яшик");
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, 1);
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, 1);
        if(mMediaPlayer!=null) metadataBuilder.putLong(VOLUME, (long) mMediaPlayer.getVolume());

        mMediaSessionCompat.setMetadata(metadataBuilder.build());
    }
    private void updateMediaSessionMetadata() {
        //Log.d("Service", "updateMediaSessionMetadata" + mMediaPlayer.getCurrentPosition());
        MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();
        //Notification icon in card
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, playingBitmap);
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, playingBitmap);

        //lock screen icon for pre lollipop
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, playingBitmap);
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, "Кунел радиосы");
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, "Кунелле яшик");
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, 1);
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, 1);
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mMediaPlayer.getCurrentPosition());
        metadataBuilder.putLong(BUFFERED_POSITION, mMediaPlayer.getBufferedPosition());
        metadataBuilder.putLong(VOLUME, (long) mMediaPlayer.getVolume());

        mMediaSessionCompat.setMetadata(metadataBuilder.build());
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

    private boolean successfullyRetrievedAudioFocus() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        int result = audioManager.requestAudioFocus(this,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        return result == AudioManager.AUDIOFOCUS_GAIN;
    }


    //Not important for general audio service, required for class
    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        if(TextUtils.equals(clientPackageName, getPackageName())) {
            return new BrowserRoot(getString(R.string.app_name), null);
        }

        return null;
    }

    //Not important for general audio service, required for class
    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(null);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch( focusChange ) {
            case AudioManager.AUDIOFOCUS_LOSS: {

                if(mMediaPlayer.getPlayWhenReady() ) {
                mMediaSessionCallback.onPause();
                }
                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: {

                mMediaSessionCallback.onPause();

                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: {
                if( mMediaPlayer != null ) {
                    mMediaPlayer.setVolume(0.3f);
                }
                break;
            }
            case AudioManager.AUDIOFOCUS_GAIN: {
                if( mMediaPlayer != null ) {
                    if( !mMediaPlayer.getPlayWhenReady() ) {
                        mMediaSessionCallback.onPlay();
                    }
                    mMediaPlayer.setVolume(1.0f);
                }
                break;
            }
        }
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mMediaSessionCompat, intent);
        return super.onStartCommand(intent, flags, startId);
    }

    public void loadBitmap(String url) {

        if (loadtarget == null) loadtarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                // do something with the Bitmap
                //playingBitmap = bitmap;
                initMediaSessionMetadata();
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };

        Picasso.with(this).load(url).into(loadtarget);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.abandonAudioFocus(this);
        unregisterReceiver(mNoisyReceiver);
        mMediaSessionCompat.release();
        NotificationManagerCompat.from(this).cancel(1);
        stopSelf();
    }
}
