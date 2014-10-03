package com.android.demo.notepad3;

import com.android.demo.notepad3.NoteFragment.OnFragmentSwitchListener;

import android.os.Bundle;
import android.app.FragmentTransaction;
import android.app.Activity;
import android.content.Intent;

public class NoteDisplay extends Activity implements OnFragmentSwitchListener{
    
    FragmentTransaction mFT;
    private static final int PREVIOUS=-1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_display);
        setTitle(R.string.display_note);
        
        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            NoteFragment fragment = new NoteFragment();

            // In case this activity was started with special instructions from
            // Intent, pass the Intent's extras to the fragment as arguments
            fragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            mFT = getFragmentManager().beginTransaction();
            //ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
            mFT.add(R.id.fragment_container, fragment, "Frag2Tag").commit();
        }
    }
    @Override
    public void onFragmentSelected(Long id, int direction) {
        // TODO Auto-generated method stub
        NoteFragment fragment = new NoteFragment();
        Intent i = getIntent();
        i.putExtra(NotesDbAdapter.KEY_ROWID, id);
        fragment.setArguments(i.getExtras());
        mFT = getFragmentManager().beginTransaction();
        if(direction == PREVIOUS)
        {
            mFT.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
        }
        else
        {
            mFT.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
        }
        mFT.replace(R.id.fragment_container, fragment);
        mFT.commit();
    }
}
