package com.example.musicplayer;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.IOException;


public class MainActivity extends AppCompatActivity implements MyAdapter.OnClickListener {

    private MyViewModel viewModel;
    private RecyclerView recyclerView;
    private MyAdapter adapter;
    private int currentMusicId = -1;//当前播放的音乐id
    private MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(MyViewModel.class);
        recyclerView = findViewById(R.id.recyclerView);
        adapter = new MyAdapter(viewModel.getMusicDates().getValue());
        adapter.setListener(this);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        viewModel.getMusicDates().observe(this, musicDates -> {
            adapter.setMusicDates(musicDates);
            adapter.notifyDataSetChanged();
        });

         ActivityResultLauncher<String> requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        viewModel.loadMusics();
                        viewModel.getMusicDates();
                    } else {
                        Toast.makeText(MainActivity.this, "无权限, 无法显示音乐列表", Toast.LENGTH_LONG).show();
                    }
                });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            viewModel.loadMusics();
            viewModel.getMusicDates();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                Toast.makeText(MainActivity.this, "无权限, 无法显示音乐列表", Toast.LENGTH_LONG).show();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        Uri uri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, String.valueOf(viewModel.getMusicDates().getValue().get(position).getMusicId()));
        if (player == null) {//第一次点击播放
            player = new MediaPlayer();
            try {
                player.setDataSource(this, uri);
                player.prepare();
                player.start();
                currentMusicId = viewModel.getMusicDates().getValue().get(position).getMusicId();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {//切歌或暂停
            if (currentMusicId == viewModel.getMusicDates().getValue().get(position).getMusicId()) {//再点一次暂停
                if (player.isPlaying()) {
                    player.pause();
                } else {
                    player.start();
                }
            } else {//切歌
                try {
                    player.stop();
                    player.release();
                    player = new MediaPlayer();
                    player.setDataSource(this, uri);
                    player.prepare();
                    player.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

    }
}