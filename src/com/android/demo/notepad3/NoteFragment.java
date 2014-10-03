package com.android.demo.notepad3;

import java.util.HashMap;
import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class NoteFragment extends Fragment implements OnClickListener{

    private TextView mTitleText;
    private TextView mDateText;
    private TypingText mBodyText;
    private String mAnimateText;
    private Long mRowId;
    private Button mEdit, mPlay, mStop, mPrev, mNext;
    private String mSong, mBackground;
    private boolean bPlaying = false;
    private int mSongID, mBGID;
    HashMap<String, Integer> mSongMap, mBGMap;
    
    private NotesDbAdapter mDbHelper;
    private PlayService mBoundService;
    Context mContext;
    private boolean mIsBound = false;
    private Cursor mNotesCursor;
    
    private static final int ACTIVITY_EDIT=1;
    private static final int PREVIOUS=-1;
    private static final int NEXT=1;
    OnFragmentSwitchListener mCallback;
    
    public interface OnFragmentSwitchListener {
        public void onFragmentSelected(Long id, int direction);
    }
    
    @Override  
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.note_fragment, container, false);  
        mTitleText = (TextView) v.findViewById(R.id.titleText);
        mDateText = (TextView) v.findViewById(R.id.dateText);
        mBodyText = (TypingText) v.findViewById(R.id.bodyText);

        mEdit = (Button) v.findViewById(R.id.editButton);
        mEdit.setOnClickListener(this);
        mPlay = (Button) v.findViewById(R.id.playButton);
        mPlay.setOnClickListener(this);
        mStop = (Button) v.findViewById(R.id.stopButton);
        mStop.setOnClickListener(this);
        mPrev = (Button) v.findViewById(R.id.prevButton);
        mPrev.setOnClickListener(this);
        mNext = (Button) v.findViewById(R.id.nextButton);
        mNext.setOnClickListener(this);
        
        populateFields();
        
        //if (mSongID != 0)
        {
            doBindService();
        }
        v.setBackgroundResource(mBGID);
        return v;
    }  
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity().getApplicationContext();
        
        mDbHelper = new NotesDbAdapter(getActivity());

        mDbHelper.open();

        mRowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(NotesDbAdapter.KEY_ROWID);
        if (mRowId == null) {
            Bundle extras = getArguments();
            mRowId = extras != null ? extras.getLong(NotesDbAdapter.KEY_ROWID)
                                    : null;
            }
        mNotesCursor = mDbHelper.fetchAllNotes();
        
        mSongMap = new HashMap<String, Integer>();
        mSongMap.put("none",0);
        mSongMap.put("Song1",R.raw.song1);
        mSongMap.put("Song2",R.raw.song2);
        mSongMap.put("Song3",R.raw.song3);
        
        mBGMap = new HashMap<String, Integer>();
        mBGMap.put("black",0);
        mBGMap.put("Background1",R.drawable.bg1);
        mBGMap.put("Background2",R.drawable.bg2);
        mBGMap.put("Background3",R.drawable.bg3);
        
        mContext.registerReceiver(mBroadcast, new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED));
    }
    
    @Override  
    public void onAttach(Activity activity) {  
        super.onAttach(activity);  
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnFragmentSwitchListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentSelectedListener");
        }
    }
    
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBoundService = ((PlayService.LocalBinder)service).getService();          
//            Toast.makeText(getActivity().getApplicationContext(), "service connected",
//                    Toast.LENGTH_SHORT).show();
        }
        @Override
        public void onServiceDisconnected(ComponentName className) {
            mBoundService = null;
//            Toast.makeText(getActivity().getApplicationContext(), "service disconnected",
//                    Toast.LENGTH_SHORT).show();
        }
    };
    
    public void doBindService() {
        Intent i = new Intent(mContext, PlayService.class);
        i.putExtra("songid", mSongID);
        mContext.bindService(i, mConnection, Context.BIND_AUTO_CREATE );
        mIsBound = true;
    }

    public void doUnbindService() {
        if (mIsBound) 
        {
            // Detach our existing connection.
            mContext.unbindService(mConnection);
            mIsBound = false;
        }
    }
    
    private BroadcastReceiver mBroadcast =  new BroadcastReceiver(){
    
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            try {
                // TELEPHONY MANAGER class object to register one listner
                TelephonyManager tmgr = (TelephonyManager) context
                        .getSystemService(Context.TELEPHONY_SERVICE);

                // Create Listner
                MyPhoneStateListener PhoneListener = new MyPhoneStateListener();

                // Register listener for LISTEN_CALL_STATE
                tmgr.listen(PhoneListener, PhoneStateListener.LISTEN_CALL_STATE);

            } catch (Exception e) {
                Log.e("Phone Receive Error", " " + e);
            }
        }
    };
    
    private class MyPhoneStateListener extends PhoneStateListener {
        //boolean PlayFlag = false; 
        public void onCallStateChanged(int state, String incomingNumber) {
            Log.d("MyPhoneListener", state + "   incoming no:" + incomingNumber);
            if (state == 1) {

                String msg = "New Phone Call Event. Incomming Number : " + incomingNumber;
                Log.i("MyPhoneStateListener", msg);

                Intent i = new Intent(mContext, PlayService.class);
                i.putExtra("operation", 0); // pause
                mContext.startService(i);
            }
            else
            {
                Intent i = new Intent(mContext, PlayService.class);
                i.putExtra("operation", 1); // play
                mContext.startService(i);
            }
        }
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(NotesDbAdapter.KEY_ROWID, mRowId);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        //mContext.startService(new Intent(mContext, PlayService.class));
        if(mBoundService!=null && mIsBound == true)
        {
            mBoundService.stop();
        }
        mBodyText.stop();
        bPlaying = false;
        doUnbindService();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        populateFields();
        getView().setBackgroundResource(mBGID);
    }
    
    private void populateFields() {
        if (mRowId != null) {
            Cursor note = mDbHelper.fetchNote(mRowId);
            //startManagingCursor(note);
            mTitleText.setText(note.getString(
                        note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE)));
            mAnimateText = note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY));
            mBodyText.setText(mAnimateText);
            mDateText.setText(note.getString(
                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_DATE)));
            mSong = note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_SONG));
            mSongID = mSongMap.get(mSong);
            mBackground = note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BACKGROUND));
            mBGID = mBGMap.get(mBackground);
        }
    }


    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        int index;
        Long id;
        switch(v.getId())
        {
            case R.id.editButton:
                Intent i = new Intent(getActivity(), NoteEdit.class);
                i.putExtra(NotesDbAdapter.KEY_ROWID, mRowId);
                startActivityForResult(i, 1);
                if(mBoundService!=null && mIsBound == true)
                {
                    mBoundService.stop();
                }
                mBodyText.stop();
                bPlaying = false;
                break;
            case R.id.playButton:

                if (!bPlaying)
                {
                    if(mBoundService!=null && mSongID != 0 && mIsBound == true)
                    {
                    mBoundService.play();
                    }
                    mBodyText.setCharacterDelay(150);
                    mBodyText.animateText(mAnimateText);
                    bPlaying = true;
                    mPlay.setText("Pause");
                }
                else
                {
                    if(mBoundService!=null && mSongID != 0 && mIsBound == true)
                    {
                    mBoundService.pause();
                    }
                    mBodyText.pause();
                    bPlaying = false;
                    mPlay.setText("Play");
                }
                break;
            case R.id.stopButton:
                if (mBoundService != null && mIsBound == true)
                {
                    mBoundService.stop();
                }
                mBodyText.stop();
                mBodyText.setText(mAnimateText);
                bPlaying = false;
                mPlay.setText("Play");
                break;
            case R.id.nextButton:
                mNotesCursor.moveToFirst();
                index = mNotesCursor.getColumnIndex(NotesDbAdapter.KEY_ROWID);
                while(true){
                id = mNotesCursor.getLong(index);
                    if (id == mRowId)
                    {
                        break;
                    }
                    mNotesCursor.moveToNext();
                }
                if(!mNotesCursor.isLast())
                {
                    mNotesCursor.moveToNext();
                    mCallback.onFragmentSelected(mNotesCursor.getLong(index), NEXT);
                    if (mBoundService != null && mIsBound == true)
                    {
                        mBoundService.stop();
                    }
                    mBodyText.stop();
                    doUnbindService();
                    mIsBound = false;
                }
                break;
            case R.id.prevButton:
                mNotesCursor.moveToFirst();
                index = mNotesCursor.getColumnIndex(NotesDbAdapter.KEY_ROWID);
                while(true){
                id = mNotesCursor.getLong(index);
                    if (id == mRowId)
                    {
                        break;
                    }
                    mNotesCursor.moveToNext();
                }
                if(!mNotesCursor.isFirst())
                {
                    mNotesCursor.moveToPrevious();
                    mCallback.onFragmentSelected(mNotesCursor.getLong(index), PREVIOUS);
                    if (mBoundService != null && mIsBound == true)
                    {
                        mBoundService.stop();
                    }
                    mBodyText.stop();
                    doUnbindService();
                    mIsBound = false;
                }
                break;
        }
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (intent != null) {
            Bundle extras = intent.getExtras();

            switch (requestCode) {
            case ACTIVITY_EDIT:
                String song = extras.getString("song");
                if (song != null) {
                    mSongID = mSongMap.get(song);
                    mBoundService.reload(mSongID);
                }
                String bg = extras.getString("bg");
                if (bg != null) {
                    mBGID = mBGMap.get(bg);
                    getView().setBackgroundResource(mBGID);
                }
                mPlay.setText("Play");
                break;
            }
        }
    }
    
}
