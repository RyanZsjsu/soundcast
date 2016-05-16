package com.ahmilio.turtle.soundcast;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;


public class HostActivity extends AppCompatActivity {
    private final int REQUEST_CODE = 100;
    private ArrayAdapter<String> queueAdapter;
    private PlayQueue<String> playQueue;
    private ListView lvPlayQueue;
    private MediaPlayer mp;
    //whoa
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setContentView(R.layout.activity_host);

        lvPlayQueue = (ListView) findViewById(R.id.lvPlayQueue);
        playQueue = new PlayQueue<>("song1 song2 song3 song4 song5 song6 song7 song8 song9 song10 song11 song12 song13 song14 song15".split("[ ]+"));
        queueAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, playQueue.toArray());
        lvPlayQueue.setAdapter(queueAdapter);
        queueAdapter.add("last song");

        FloatingActionButton fabConnect = (FloatingActionButton) findViewById(R.id.fabConnect);
        Button btnAddMusic = (Button) findViewById(R.id.btnAddMusic);
        Switch swtPlay = (Switch) findViewById(R.id.swtPlay);

        mp = MediaPlayer.create(this, R.raw.ruby);
//        Toast.makeText(getApplicationContext(), "Now playing: Ruby - Warren Malone", Toast.LENGTH_SHORT).show();

        fabConnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Snackbar.make(view, "Not yet implemented", Snackbar.LENGTH_SHORT).show();
            }
        });

        btnAddMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "btnAddMusic", Toast.LENGTH_SHORT).show();
                Intent selectMusic = new Intent(HostActivity.this, SelectMusicActivity.class);
                startActivityForResult(selectMusic, REQUEST_CODE);
            }
        });

        swtPlay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    playSong();
                else
                    pauseSong();
            }
        });

        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playSong();
            }
        });

//comment
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK){
            String song = data.getExtras().getString("song");
            enqueueSong(song);
            Toast.makeText(getApplicationContext(), song+" added to queue!", Toast.LENGTH_SHORT).show();
        }
    }

    protected void enqueueSong(String song){
        playQueue.enqueue(song);
        queueAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, playQueue.toArray());
        lvPlayQueue.setAdapter(queueAdapter);
    }

    protected String dequeueSong(){
        String song = playQueue.dequeue();
        queueAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, playQueue.toArray());
        lvPlayQueue.setAdapter(queueAdapter);
        return song;
    }

    protected void playSong(){
//        play with dequeue later
        if (!mp.isPlaying()) {
            Toast.makeText(getApplicationContext(), "Now playing: Ruby - Warren Malone", Toast.LENGTH_SHORT).show();
        }
        mp.start();
    }

    protected void onDestroy(){
        super.onDestroy();
        mp.reset();
    }

    protected void pauseSong(){
        mp.pause();
    }
}