package com.kunel.lantimat.kunelradio.Utils;

/**
 * Created by GabdrakhmanovII on 16.08.2017.
 */

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.kunel.lantimat.kunelradio.BackgroundAudioService;


public class NotificationDismissedReceiver extends BroadcastReceiver {

    Context context;

    @Override
    public void onReceive(final Context context, Intent intent) {
        int notificationId = intent.getExtras().getInt("deleteIntent");
      /* Your code to handle the event here */
        this.context = context;
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("stopSelf");
        context.sendBroadcast(broadcastIntent);
        Log.d("Notification", "Received");

    }
}
