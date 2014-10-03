/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.demo.notepad3;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class NoteEdit extends Activity implements OnItemSelectedListener{

    private EditText mTitleText;
    private EditText mDateText;
    private EditText mBodyText;
    private String mSong, mBackground;
    ArrayAdapter<CharSequence> mSong_adapter, mBG_adapter;
    private Long mRowId;
    private Spinner mSongSpinner;
    private Spinner mBGSpinner;
    boolean bSongSpinnerFlag = true;
    boolean bBGSpinnerFlag = true;
    
    private NotesDbAdapter mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_edit);
        setTitle(R.string.edit_note);

        mTitleText = (EditText) findViewById(R.id.title);
        mDateText = (EditText) findViewById(R.id.dateEdit);
        mBodyText = (EditText) findViewById(R.id.body);
        mSongSpinner = (Spinner) findViewById(R.id.songSpinner);
        mBGSpinner = (Spinner) findViewById(R.id.bgSpinner);
        
        mSong_adapter = ArrayAdapter.createFromResource(this,
                R.array.song_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        mSong_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mSongSpinner.setAdapter(mSong_adapter);
        mSongSpinner.setOnItemSelectedListener(this);

        mBG_adapter = ArrayAdapter.createFromResource(this,
                R.array.bg_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        mBG_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mBGSpinner.setAdapter(mBG_adapter);
        mBGSpinner.setOnItemSelectedListener(this);

        Button confirmButton = (Button) findViewById(R.id.confirm);
        
        mDbHelper = new NotesDbAdapter(this);

        mDbHelper.open();

        mRowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(NotesDbAdapter.KEY_ROWID);
        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            mRowId = extras != null ? extras.getLong(NotesDbAdapter.KEY_ROWID)
                                    : null;
        }
        
        populateFields();

        confirmButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Bundle bundle = new Bundle();
                Intent mIntent = new Intent();
                bundle.putString("song",mSong);
                bundle.putString("bg",mBackground);
                mIntent.putExtras(bundle);
                setResult(RESULT_OK, mIntent);
                finish();
            }

        });
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putSerializable(NotesDbAdapter.KEY_ROWID, mRowId);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        populateFields();
    }
    
    private void populateFields() {
        if (mRowId != null) {
            Cursor note = mDbHelper.fetchNote(mRowId);
            startManagingCursor(note);
            mTitleText.setText(note.getString(
                        note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE)));
            mDateText.setText(note.getString(
                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_DATE)));
            mBodyText.setText(note.getString(
                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY)));
            
            mSong = note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_SONG));
            int pos = mSong_adapter.getPosition(mSong);
            mSongSpinner.setSelection(pos);
            
            mBackground = note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BACKGROUND));
            pos = mBG_adapter.getPosition(mBackground);
            mBGSpinner.setSelection(pos);
            
        }
    }
    
    private void saveState() {
        String title = mTitleText.getText().toString();
        String date = mDateText.getText().toString();
        String body = mBodyText.getText().toString();

        if (mRowId == null) {
            long id = mDbHelper.createNote(title, body,date,mSong,mBackground);
            if (id > 0) {
                mRowId = id;
            }
        } else {
            mDbHelper.updateNote(mRowId, title, body,date,mSong,mBackground);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
        // TODO Auto-generated method stub
        switch (parent.getId())
        {
            case R.id.songSpinner:
                mSong = (String) parent.getItemAtPosition(pos);
                if(!bSongSpinnerFlag)
                {
                    Toast.makeText(this, "You choose the song " + mSong, Toast.LENGTH_SHORT).show();
                }
                bSongSpinnerFlag = false;
                break;
            case R.id.bgSpinner:
                mBackground = (String) parent.getItemAtPosition(pos);
                if(!bBGSpinnerFlag)
                {
                    Toast.makeText(this, "You choose the background " + mBackground, Toast.LENGTH_SHORT).show();
                }
                bBGSpinnerFlag = false;
                break;
        }
        
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
        
    }
}
