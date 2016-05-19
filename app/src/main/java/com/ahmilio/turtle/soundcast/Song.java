package com.ahmilio.turtle.soundcast;

import android.media.MediaMetadataRetriever;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.FileChannel;

public class Song implements Serializable {
    private String name;
    private String src; // where the source is from
    private String cache;
    private File copy;
    private MediaMetadataRetriever meta;
    private int srctype;
    public static final int SRC_LOCAL = 1;
    public static final int SRC_BT = 4;
    public static final int SRC_WIFIP2P = 16;
    public static final int SRC_WLAN = 64;

    // ctor taking in the song's source pathname and cache directory
    public Song(String src, String cache, int srctype){
        this.src = src;
        name = src.substring(src.lastIndexOf(File.separator)+1);
        this.cache = cache;
        this.srctype = srctype;
    }

    public Song(String src, int srctype){
        this(src, null, srctype);
    }

    public Song(String src, String cache){
        this(src, cache, SRC_LOCAL);
    }

    public boolean isCached(){
        return cache != null && /*meta != null &&*/ copy != null;
    }

    public boolean isExternal(){
        switch(srctype){
            case SRC_BT:
            case SRC_WIFIP2P:
            case SRC_WLAN:
                return true;
            case SRC_LOCAL:
                return false;
            default:
                throw new InvalidSourceException("Invalid src code: "+srctype);
        }
    }

    public String getFilename(){
        return name;
    }

    public boolean cache() throws IOException {
        return cache(cache);
    }

    // force cache music file
    public boolean cache(String dir) throws IOException {
        File source, destination = new File(dir+File.separator+name);
        if (!isExternal())
            source = new File(src);
        else
            source = retrieve(src);
        if (!isMusic(source.getPath()))
            return false;

        if (destination.createNewFile())
            copy(source, destination);
        copy = destination;
        cache = destination.getPath().substring(0,src.lastIndexOf(File.separator));
        meta = new MediaMetadataRetriever();
        meta.setDataSource(copy.getPath());
        return true;
    }

    public File getCachedCopy(){ return copy; }

    public File retrieve(String src){
        File load = null;
        switch(srctype){
            case SRC_LOCAL:
                load = new File(src);
                break;
            case SRC_BT:
                // do stuff;
                break;
            case SRC_WIFIP2P:
                // do stuff;
                break;
            case SRC_WLAN:
                // do stuff;
                break;
            default:
                throw new InvalidSourceException("Invalid src code: "+srctype);
        }
        return load;
    }

    public String getName(){
        return meta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
    }

    public String getArtist(){
        return meta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
    }

    public String getDuration(){
        return meta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
    }

    public static boolean isMusic(String path){
        return path.matches(".*\\.(mp3|flac|wav|ogg|mid|m4a|aac|3gp|mkv)$");
    }

    public static void copy(File src, File dest) throws IOException {
        FileInputStream in = new FileInputStream(src);
        FileOutputStream out = new FileOutputStream(dest);

        /* potentially slower method
        byte[] b = new byte[1024];
        int len;
        while ((len = in.read(b)) > 0)
            out.write(b, 0, len);
        */
        FileChannel ichan = in.getChannel();
        FileChannel ochan = out.getChannel();
        ichan.transferTo(0, ichan.size(), ochan);
        in.close();
        out.close();
    }

    private class InvalidSourceException extends RuntimeException {
        public InvalidSourceException(String msg) {
            super(msg);
        }
    }
}

// https://docs.oracle.com/javase/7/docs/api/java/io/File.html
// https://docs.oracle.com/javase/tutorial/essential/io/copy.html
// https://rogerkeays.com/simple-android-file-chooser