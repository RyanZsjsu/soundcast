package com.ahmilio.turtle.soundcast;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SelectMusicActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_music);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_select_music);
        Button btnToMain = (Button) findViewById(R.id.btnToMain);
        btnToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "btnToMain", Toast.LENGTH_SHORT).show();
                Intent Host = new Intent(SelectMusicActivity.this, HostActivity.class);
                startActivity(Host);
            }
        });
        Button btnAddSong = (Button) findViewById(R.id.btnAddSong);
        btnAddSong.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onAdd(view);
            }
        });


    }

    public void onAdd(View view){
        Intent data = new Intent();
        String song = ((EditText)findViewById(R.id.etSong)).getText().toString();
        data.putExtra("song", song);
        setResult(RESULT_OK, data);
        finish();
    }

}
