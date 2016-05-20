package com.ahmilio.turtle.soundcast;

import android.content.Context;
import android.content.Intent;
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
    private static final int REQUEST_CODE = 100;
    private static final int SONGS_TO_CACHE = 4;
    private static final String TAG = "HostActivity";
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
        Log.i(TAG, "onCreate: created cache directory: "+cache.getPath());

        // simulating file from external storage
        File ext = getDir("external", Context.MODE_PRIVATE);
        Log.i(TAG, "onCreate: created simulated external directory: "+ext.getPath());

        playQueue = new PlayQueue<>();

        File testExtFile = null;
        try {
            while (!test.isEmpty()){
                String asset = test.dequeue();
                File cursrc = extFileFromRawAsset(ext,asset,"mp3");
                Log.i(TAG, "dir to asset \""+asset+"\": "+cursrc.getPath());
                Song cursong = new Song(cursrc.getPath(), cache.getPath(), Song.SRC_LOCAL);
                Log.i(TAG, "created song \""+asset+"\": "+cursong.getFilename());
                playQueue.enqueue(cursong);
            }
        } catch (IOException e){
            Log.e(TAG, "onCreate: IOException: "+e.getMessage());
            e.printStackTrace();
        }

        // initializing first song
        nowPlaying = playQueue.dequeue();
        try {
            nowPlaying.cache();
        } catch (IOException e) {
            Log.e(TAG, "onCreate: IOException: "+e.getMessage());
            e.printStackTrace();
        }
        Log.i(TAG, "onCreate: nowPlaying cached into directory: "+nowPlaying.getCachedCopy().getPath());

        ArrayList<String> titles = new ArrayList<>();
        for (Song s : playQueue.toArray())
            titles.add(s.getFilename());

        queueAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, titles);
        lvPlayQueue.setAdapter(queueAdapter);

        FloatingActionButton fabConnect = (FloatingActionButton) findViewById(R.id.fabConnect);
        Button btnAddMusic = (Button) findViewById(R.id.btnAddMusic);
        Switch swtPlay = (Switch) findViewById(R.id.swtPlay);
        Button btnSkip = (Button) findViewById(R.id.btnSkip);

        // initializing player
        mp = new MediaPlayer();
//        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            cacheNext(SONGS_TO_CACHE);
            mp.setDataSource(nowPlaying.getCachedCopy().getPath());
            mp.prepare();
            refreshList();
        } catch (IOException e) {
            Log.e(TAG,"onCreate: MediaPlayer error: "+e.getMessage());
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
                nextSong();
            }
        });

        // makeshift song veto feature
        lvPlayQueue.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {

                Log.d(TAG, "onCreate: lvPlayQueue: long-clicked pos: " + pos);
                vetoSong(pos);
                Log.d(TAG, "onCreate: lvPlayQueue: song "+pos+" removed");

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

    private void cacheNext(int firstnsongs) throws IOException {
        if (playQueue.isEmpty())
            return;
        for (Song s : playQueue.peek(firstnsongs))
            if (!s.isCached()) {
                if (s.cache())
                    Log.i(TAG, "bufferNext: cached "+s.getFilename()+": "+s.getCachedCopy().getPath());
                else
                    Log.i(TAG, "bufferNext: "+s.getFilename()+" was already cached");
            }
    }

    private void toast(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private File extFileFromRawAsset(File ext, String assetName, String fileExt) throws IOException {
        // initialize file
        File test = new File(ext+File.separator+assetName+"."+fileExt);
        Log.i(TAG, "extFileFromRawAsset: initialized "+assetName+"."+fileExt+": "+test.getPath());

        // open raw asset
        InputStream is = getResources().openRawResource(
                getResources().getIdentifier(assetName, "raw", getPackageName()));
        int size = is.available();
        boolean write = true;

        // test to see if overwrite is needed
        if (test.exists()){
            boolean mismatch = size != test.length();
            Log.w(TAG, "extFileFromRawAsset: File already exists. Checking size mismatch...");
            if (write = mismatch) {
                Log.w(TAG, "extFileFromRawAsset: Size mismatch. Overwriting " + assetName+"."+fileExt);
                test.delete();
            }
        }
        else {
            Log.v(TAG, "extFileFromRawAsset: Writing "+assetName+"."+fileExt);
        }
        if (write) {
            test.createNewFile();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            FileOutputStream fos = new FileOutputStream(test);
            fos.write(buffer);
            fos.close();
            Log.v(TAG, "extFileFromRawAsset: "+assetName+"."+fileExt+" written: "+test.getPath());
            Log.v(TAG, "extFileFromRawAsset: "+assetName+"."+fileExt+" size: "+test.length()+" bytes");
        }
        else
            is.close();


        return test;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK){
            String song = data.getExtras().getString("song");
//            enqueueSong(song);
            toast(song+" not really added to queue!");
        }
    }

    protected void refreshList(){
        ArrayList<String> titles = new ArrayList<>();
        if (!playQueue.isEmpty())
            for (Song s : playQueue.toArray())
                titles.add(s.isCached() ? (s.getName() + " - " + s.getArtist()) : s.getFilename());

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
        boolean wasPlaying = mp.isPlaying();
        mp.reset();
        if (!playQueue.isEmpty()) {
            nowPlaying = playQueue.dequeue();
            toast("Song skipped");
        }
        else {
            nowPlaying = null;
            toast("No songs in queue!");
            Log.w(TAG, "nextSong: Next song unavailable: empty queue");
            refreshList();
            return;
        }
        try {
            if (!nowPlaying.isCached())
                nowPlaying.cache();
            cacheNext(SONGS_TO_CACHE);
            mp.setDataSource(nowPlaying.getCachedCopy().getPath());
            mp.prepare();
        } catch (IOException e) {
            Log.e(TAG, "nextSong: MediaPlayer error: "+ e.getMessage());
            e.printStackTrace();
        }
        if (wasPlaying)
            playSong();
        refreshList();
    }

    protected void playSong(){
        if (playQueue.isEmpty() && nowPlaying == null) {
            toast("No songs in queue!");
            Log.w(TAG, "playSong: song cannot be played because queue is out of songs");
            return;
        }
        else if (nowPlaying == null) {
            nextSong();
            return;
        }
        else if (!mp.isPlaying())
            toast("Now playing: "+nowPlaying.getName()+" - "+nowPlaying.getArtist());
        mp.start();
    }

    protected void vetoSong(int pos){
        Song rem = playQueue.remove(pos);
        if (rem.isCached()){
            toast(rem.getName()+" - "+rem.getArtist()+" was vetoed!");
            Log.i(TAG, "vetoSong: song removed: "+rem.getFilename());
            rem.removeCachedData();
            Log.i(TAG, "vetoSong: "+rem.getFilename()+": cached data removed");
        }
        else {
            toast(rem.getFilename()+" was vetoed!");
            Log.i(TAG, "vetoSong: song removed: "+rem.getFilename());
        }
        refreshList();
    }

    protected void onDestroy(){
        super.onDestroy();
        mp.release();
//        mp.reset();
    }

    protected void pauseSong(){
        if (mp.isPlaying())
            mp.pause();
    }
}