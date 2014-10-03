package com.android.demo.notepad3;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class PlayService extends Service implements MediaPlayer.OnPreparedListener{

    private NotificationManager mNM;
    private MediaPlayer mp = new MediaPlayer();
    private boolean isPause = false;
    private boolean PlayFlag = false;
    boolean bPrepeared = false;
    private int mID;

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = 5566;

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        PlayService getService() {
            return PlayService.this;
        }
    }
    
    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

    }
    
    @Override
    public boolean onUnbind(Intent intent) {
        // TODO Auto-generated method stub
        stopService(intent);
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
     // Cancel the persistent notification.
        mNM.cancel(NOTIFICATION);
        //mp.stop();

        mp.release();
        // Tell the user we stopped.
        //Toast.makeText(this, "Service stopped", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        int act = intent.getIntExtra("operation", 2);
        
        if(mp.isPlaying() && act == 0)
        {
            mp.pause();
            PlayFlag = true; 
        }
        else if(PlayFlag && act == 1)
        {
            mp.start();
            PlayFlag = false; 
        }
        
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        setPlaylist(arg0.getIntExtra("songid", 0));
        return mBinder;
    }
    
 // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        //CharSequence text = getText(R.string.local_service_started);
        
     // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, NoteDisplay.class), PendingIntent.FLAG_UPDATE_CURRENT);
        // Set the icon, scrolling text and timestamp
        Notification notification = 
        new Notification.Builder(this)
        .setSmallIcon(R.drawable.icon)
        .setContentIntent(contentIntent)
        .setContentTitle("My Diary")
        .setContentText("now playing..")
        //.setSmallIcon(R.drawable.new_mail)
        //.setLargeIcon(aBitmap)
        .build();

        

        // Set the info for the views that show in the notification panel.
//        notification.setLatestEventInfo(this, getText(R.string.local_service_label),
//                       text, contentIntent);

        // Send the notification.
        mNM.notify(NOTIFICATION, notification);
    }
    
    public void setPlaylist(int id)
    {
        if(id == 0)
        {
            return;
        }
        mID = id;
        mp = MediaPlayer.create(this, id);
        mp.setOnPreparedListener(this);
    }
    
    public void reload(int id)
    {
        if(id == 0)
        {
            return;
        }
        mID = id;
        mp.release();
        mp = MediaPlayer.create(this, id);
        mp.setOnPreparedListener(this);
    }
    
    public void play()
    {

        if (!isPause && !mp.isPlaying())
        {
            
            mp.start();
            // Display a notification about us starting.  We put an icon in the status bar.
            showNotification();
            isPause = false;
        }
        else
        if(bPrepeared)
        {
            mp.start();
            isPause = false;
        }

    }
    
    public void pause()
    {
        if (mp.isPlaying())
        {
            mp.pause();
            isPause = true;
        }
    }
    
    public void stop()
    {
        if (mp.isPlaying() || isPause)
        {
            mp.stop();
            isPause = false;
            mp.release();
            mNM.cancel(NOTIFICATION);
            mp = MediaPlayer.create(this, mID);
        }
    }


    public void foward(int duration) {
        int curr = mp.getCurrentPosition();
        int length = mp.getDuration();
        if(curr + duration*10 > length) //foward faster 10 times
        {
            mp.seekTo(0);
        }
        else
        {
            mp.seekTo(curr+duration*10);
        }
    }

    public void backward(int duration) {
        int curr = mp.getCurrentPosition();
        if(curr - duration*10 < 0) //backward faster 10 times
        {
            mp.seekTo(0);
        }
        else
        {
            mp.seekTo(curr-duration*10);
        }
    }

    @Override
    public void onPrepared(MediaPlayer player) {
        // TODO Auto-generated method stub
        bPrepeared = true;
    }

}
