package com.android.demo.notepad3;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.TextView;
import android.widget.TextView.BufferType;

public class TypingText extends TextView {

    private CharSequence mText;
    private int mIndex;
    private long mDelay = 500; //Default 500ms delay
    private boolean bPausing = false;


    public TypingText(Context context) {
        super(context);
    }
    
    public TypingText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    private Handler mHandler = new Handler();
    private Runnable characterAdder = new Runnable() {
        @Override
        public void run() {
            if(mIndex <= mText.length()) {
                setText(mText.subSequence(0, mIndex++));
                mHandler.postDelayed(characterAdder, mDelay);
            }
        }
    };

    public void animateText(CharSequence text) {
        if(!bPausing)
        {
            mText = text;
            mIndex = 0;

            setText("");
            mHandler.removeCallbacks(characterAdder);
            mHandler.postDelayed(characterAdder, mDelay);
        }
        else
        {
            mHandler.postDelayed(characterAdder, mDelay);
        }
    }

    public void setCharacterDelay(long millis) {
        mDelay = millis;
    }
    
    public void stop() {
        mHandler.removeCallbacks(characterAdder);
        bPausing = false;
    }
    
    public void pause() {
        mHandler.removeCallbacks(characterAdder);
        bPausing = true;
    }

}
