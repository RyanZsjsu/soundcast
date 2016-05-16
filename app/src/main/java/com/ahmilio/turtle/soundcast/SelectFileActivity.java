package com.ahmilio.turtle.soundcast;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.io.File;

public class SelectFileActivity extends AppCompatActivity {
    private FileChooser fc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_file);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fc = new FileChooser(this);
        fc.setFileListener(new FileChooser.FileSelectedListener() {
            @Override public void fileSelected(final File file) {
                // do something with the file
            }}).showDialog();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
