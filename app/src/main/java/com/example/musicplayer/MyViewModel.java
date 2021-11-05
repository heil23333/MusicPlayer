package com.example.musicplayer;

import android.Manifest;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

public class MyViewModel extends AndroidViewModel {

    private MutableLiveData<List<MusicDates>> musicDates;
    private Application application;

    public MyViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
        musicDates = new MutableLiveData<>();
    }

    public LiveData<List<MusicDates>> getMusicDates() {
        return musicDates;
    }

    public void loadMusics() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                List list = getAllMusicDates(application);
                musicDates.postValue(list);
            }
        });
        thread.start();
    }

    //获取手机中的所有音乐
    public static ArrayList<MusicDates> getAllMusicDates(Context context) {
        ArrayList<MusicDates> list = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {//没权限返回空列表
            return list;
        }

        ContentResolver mContentResolver;
        mContentResolver = context.getContentResolver();
        Cursor c = null;
        try {
            c = mContentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                    MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

            MusicDates musicDates = null;
            while (c.moveToNext()) {

                String path = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));// 路径
                int musicId = c.getInt(c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));// 歌曲的id
                String name = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)); // 歌曲名
                String album = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)); // 专辑
                String artist = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)); // 作者
                long size = c.getLong(c.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));// 大小
                int duration = c.getInt(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));// 时长
                musicDates = new MusicDates(path, musicId, name, album, artist, size, duration);
                list.add(musicDates);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return list;
    }
}
