package com.ahmilio.turtle.soundcast;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class HostActivity extends AppCompatActivity {
    private final int REQUEST_CODE = 100;
    private ArrayAdapter<String> queueAdapter;
    private PlayQueue<Song> playQueue;
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
//        playQueue = new PlayQueue<>("Hello SoundCast!, Drop It Like It's Hot, Thomas the Tank Engine Theme, imdabes, Never Gonna Give You Up".split(", "));
//        playQueue.enqueue("Ripped Pants");
//        playQueue.enqueue("My House");
//        playQueue.dequeue();
//        playQueue.enqueue("America, Fuck Yeah!");
//        queueAdapter.add("last song");

        PlayQueue<String> test = new PlayQueue<>("intro cant king superman ridiculous fall bounce affairs want cash fiona".split(" "));

        cache = getCacheDir();
        Log.v("created dir", cache.getPath());

        // simulating file from external storage
        File ext = getDir("external", Context.MODE_PRIVATE);
        Log.v("created dir", ext.getPath());

        playQueue = new PlayQueue<>();

        File testExtFile = null;
        try {
            while (!test.isEmpty()){
                String asset = test.dequeue();
                File cursrc = extFileFromRawAsset(ext,asset,"mp3");
                Log.v("dir to asset "+asset, cursrc.getPath());
                Song cursong = new Song(cursrc.getPath(), cache.getPath(), Song.SRC_LOCAL);
                Log.v("init song "+asset, cursong.getFilename());
                cursong.cache();
                Log.v("cached song"+asset, cursong.getCachedCopy().getPath());
                playQueue.enqueue(cursong);
            }
        } catch (IOException e){
            Log.e("IOException",e.getMessage());
            e.printStackTrace();
        }

        // initializing first song
        nowPlaying = playQueue.dequeue();
        try {
            nowPlaying.cache();
        } catch (IOException e) {
            Log.e("Write error",e.getMessage());
            e.printStackTrace();
        }
        Log.v("cached copy", nowPlaying.getCachedCopy().getPath());

        ArrayList<String> titles = new ArrayList<>();
        for (Song s : playQueue.toArray())
            titles.add(s.getName() + " - " + s.getArtist());

        queueAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, titles);
        lvPlayQueue.setAdapter(queueAdapter);

        FloatingActionButton fabConnect = (FloatingActionButton) findViewById(R.id.fabConnect);
        Button btnAddMusic = (Button) findViewById(R.id.btnAddMusic);
        Switch swtPlay = (Switch) findViewById(R.id.swtPlay);
        Button btnSkip = (Button) findViewById(R.id.btnSkip);

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

        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Song skipped", Toast.LENGTH_SHORT).show();
                nextSong();
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
                nextSong();
            }
        });

//comment
    }

    private File extFileFromRawAsset(File ext, String assetName, String fileExt) throws IOException {
        File test = new File(ext+File.separator+assetName+"."+fileExt);
        Log.v("init "+assetName, test.getPath());
        Log.v(assetName+" deleted", test.delete() ? "yes" : "no");
        if (!test.exists()) try {
            InputStream is = getResources().openRawResource(
                    getResources().getIdentifier(assetName, "raw", getPackageName()));

            int size = is.available();
            Log.v("Size mismatch?", size != test.length() ? "yes" : "no");
            test.createNewFile();
            Log.v(assetName+" input size", ""+size);
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            FileOutputStream fos = new FileOutputStream(test);
            fos.write(buffer);
            fos.close();
        } catch (IOException e) {
            Log.e("Write error",e.getMessage());
            e.printStackTrace();
        }
        Log.v("created "+assetName, test.getPath());
        Log.v(assetName+" size", ""+test.length());
        return test;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK){
            String song = data.getExtras().getString("song");
//            enqueueSong(song);
            Toast.makeText(getApplicationContext(), song+" not really added to queue!", Toast.LENGTH_SHORT).show();
        }
    }

    protected void refreshList(){
        ArrayList<String> titles = new ArrayList<>();
        for (Song s : playQueue.toArray())
            titles.add(s.getName() + " - " + s.getArtist());
        queueAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, titles);
        lvPlayQueue.setAdapter(queueAdapter);
    }

    protected void enqueueSong(Song s){
        playQueue.enqueue(s);
        refreshList();
    }

    protected Song dequeueSong(){
        Song s = playQueue.dequeue();
        refreshList();
        return s;
    }

    protected void nextSong(){
        nowPlaying = playQueue.dequeue();
        mp.reset();
        try {
            if (!nowPlaying.isCached())
                nowPlaying.cache();
            mp.setDataSource(nowPlaying.getCachedCopy().getPath());
            mp.prepare();
        } catch (IOException e) {
            Log.e("mediaplayer error", e.getMessage());
            e.printStackTrace();
        }
        playSong();
        refreshList();
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