package com.ahmilio.turtle.soundcast;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class HostActivity extends AppCompatActivity {
    private final int REQUEST_CODE = 100;
    private ArrayAdapter<String> queueAdapter;
    private PlayQueue<String> playQueue;
    private ListView lvPlayQueue;
    private MediaPlayer mp;
    private File cache;
    private Song nowPlaying;
    //whoa
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setContentView(R.layout.activity_host);

        lvPlayQueue = (ListView) findViewById(R.id.lvPlayQueue);
        playQueue = new PlayQueue<>("Hello SoundCast!, Drop It Like It's Hot, Thomas the Tank Engine Theme, imdabes, Never Gonna Give You Up".split(", "));
        playQueue.enqueue("Ripped Pants");
        playQueue.enqueue("My House");
        playQueue.dequeue();
        playQueue.enqueue("America, Fuck Yeah!");
        queueAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, playQueue.toArray());
        lvPlayQueue.setAdapter(queueAdapter);
        queueAdapter.add("last song");

        // simulating file from external storage
        Log.v("pwd", getApplicationInfo().dataDir);
        cache = getCacheDir();
        Log.v("created dir", cache.getPath());
        File ext = getDir("external", Context.MODE_PRIVATE);
        Log.v("created dir", ext.getPath());
        File testExtFile = new File(cache+File.separator+"ruby.mp3");
        Log.v("init testExtFile", testExtFile.getPath());
        Log.v("ruby deleted", testExtFile.delete() ? "yes" : "no");
        if (!testExtFile.exists()) try {
            testExtFile.createNewFile();
            InputStream is = getResources().openRawResource(
                    getResources().getIdentifier("ruby", "raw", getPackageName()));

            int size = is.available();
            Log.v("ruby input size", ""+size);
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            FileOutputStream fos = new FileOutputStream(testExtFile);
            fos.write(buffer);
            fos.close();
        } catch (IOException e) {
            Log.e("Write error",e.getMessage());
            e.printStackTrace();
        }
        Log.v("created testExtFile", testExtFile.getPath());
        Log.v("testExtFile size", ""+testExtFile.length());
        String source = testExtFile.getPath();
        Log.v("trying source", source);

        // initializing first song
        nowPlaying = new Song(source, cache.getPath(), Song.SRC_LOCAL);
        try {
            nowPlaying.cache();
        } catch (IOException e) {
            Log.e("Write error",e.getMessage());
            e.printStackTrace();
        }
        Log.v("cached copy", nowPlaying.getCachedCopy().getPath());

        FloatingActionButton fabConnect = (FloatingActionButton) findViewById(R.id.fabConnect);
        Button btnAddMusic = (Button) findViewById(R.id.btnAddMusic);
        Switch swtPlay = (Switch) findViewById(R.id.swtPlay);

        // initializing player
        mp = new MediaPlayer();
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mp.setDataSource(nowPlaying.getCachedCopy().getPath());
            mp.prepare();
        } catch (IOException e) {
            Log.e("mediaplayer error", e.getMessage());
            e.printStackTrace();
        }

//        Toast.makeText(getApplicationContext(), "Now playing: Ruby - Warren Malone", Toast.LENGTH_SHORT).show();


        fabConnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Bluetooth page", Toast.LENGTH_SHORT).show();
                Intent bluetooth = new Intent(HostActivity.this, BlueToothConnectActivity.class);
                startActivity(bluetooth);
            }
        });

        btnAddMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(getApplicationContext(), "btnAddMusic", Toast.LENGTH_SHORT).show();
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

        // makeshift song veto feature
        lvPlayQueue.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {

                Log.v("long clicked","pos: " + pos);
                vetoSong(pos);
                Log.v("song removed","song: " + pos);

                return true;
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

    protected void refreshList(){
        queueAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, playQueue.toArray());
        lvPlayQueue.setAdapter(queueAdapter);
    }

    protected void enqueueSong(String song){
        playQueue.enqueue(song);
        refreshList();
    }

    protected String dequeueSong(){
        String song = playQueue.dequeue();
        refreshList();
        return song;
    }

    protected void playSong(){
//        play with dequeue later
        if (!mp.isPlaying()) {
            Toast.makeText(getApplicationContext(), "Now playing: "+nowPlaying.getName()+" - "+nowPlaying.getArtist(), Toast.LENGTH_SHORT).show();
        }
        mp.start();
    }

    protected void vetoSong(int pos){
        playQueue.remove(pos);
        refreshList();
    }

    protected void onDestroy(){
        super.onDestroy();
        mp.release();
//        mp.reset();
    }

    protected void pauseSong(){
        mp.pause();
    }
}