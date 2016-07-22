package com.threeDBJ.comicReader;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;

import android.support.v4.app.FragmentActivity;

import com.threeDBJ.comicReader.ComicReaderApp.ComicState;

public class comicReader extends FragmentActivity {
    ComicState state;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.title);
        state = ((ComicReaderApp)getApplicationContext()).getComicState();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == 1) {
            showDialog("Error", "No saved comics to show.");
        } else if(resultCode == 2) {
            showDialog("Warning", "SD card not readable or could not be found.");
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        state.clearComics();
    }

    public void showDialog(String title, String text) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.text_alt);
        dialog.setTitle(title);
        TextView t = (TextView) dialog.findViewById(R.id.alt_text);
        t.setText(text);
        dialog.setCancelable(true);
        Button done = (Button) dialog.findViewById(R.id.alt_done);
        done.setOnClickListener(makeDialogCancelListener(dialog));
        dialog.show();
    }

    public OnClickListener makeDialogCancelListener(final Dialog d) {
        return new OnClickListener() {
            public void onClick(View v) {
                d.dismiss();
            }
        };
    }
}
