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
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.ref.WeakReference;


public class MainActivity extends AppCompatActivity implements MyAdapter.OnClickListener {

    private MyViewModel viewModel;
    private RecyclerView recyclerView;
    private MyAdapter adapter;
    private int currentMusicId = -1, position = -1;//当前播放的音乐id和list index
    private MediaPlayer player;
    private TextView name, duration, currentTime, musicArtist;
    private ProgressBar progressBar;
    private ImageView playState;
    private MyHandler handler;
    private Thread playThread;
    private PlayerThread mPlayerThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
        handler = new MyHandler(new WeakReference<>(this));
        name = findViewById(R.id.music_name);
        duration = findViewById(R.id.music_duration);
        currentTime = findViewById(R.id.current_time);
        musicArtist = findViewById(R.id.music_artist);
        progressBar = findViewById(R.id.progressBar);
        playState = findViewById(R.id.play_state);
        playState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player == null) {//未播放任何音乐
                    if (viewModel.getMusicDates().getValue() != null && viewModel.getMusicDates().getValue().size() != 0) {
                        Uri uri = Uri.parse(viewModel.getMusicDates().getValue().get(0).getUri());//播放第一首
                        player = new MediaPlayer();
                        try {
                            player.setDataSource(MainActivity.this, uri);
                            player.prepare();
                            startPlay();
                            updateControlUi(0);
                        } catch (IOException e) {
                            e.printStackTrace();
                            player.release();
                        }
                    }
                } else {
                    if (player.isPlaying()) {
                        pause();
                    } else {
                        startPlay();
                    }
                }
            }
        });

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

    private void updateControlUi(int position) {
        this.position = position;
        currentMusicId = viewModel.getMusicDates().getValue().get(position).getMusicId();
        name.setText(viewModel.getMusicDates().getValue().get(position).getName());
        duration.setText(MyAdapter.getDuration(viewModel.getMusicDates().getValue().get(position).getDuration()));
        musicArtist.setText(viewModel.getMusicDates().getValue().get(position).getArtist());
        progressBar.setMax(viewModel.getMusicDates().getValue().get(position).getDuration());
        MyAdapter.loadingCover(viewModel.getMusicDates().getValue().get(position).getPath(), playState);
    }

    @Override
    public void onItemClick(View view, int position) {
        Uri uri = Uri.parse(viewModel.getMusicDates().getValue().get(position).getUri());
        if (player == null) {//第一次点击播放
            player = new MediaPlayer();
            try {
                player.setDataSource(this, uri);
                player.prepare();
                startPlay();
                updateControlUi(position);
            } catch (IOException e) {
                e.printStackTrace();
                player.release();
            }
        } else {//切歌或暂停
            if (currentMusicId == viewModel.getMusicDates().getValue().get(position).getMusicId()) {//再点一次暂停
                if (player.isPlaying()) {
                    pause();
                } else {
                    startPlay();
                }
            } else {//切歌
                try {
                    updatePlayer(position);
                    startPlay();
                    updateControlUi(position);
                } catch (IOException e) {
                    e.printStackTrace();
                    player.release();
                }
            }
        }
    }

    private void updatePlayer(int position) throws IOException {
        Uri uri = Uri.parse(viewModel.getMusicDates().getValue().get(position).getUri());
        player.reset();
        player.setDataSource(this, uri);
        player.prepare();
    }

    private void pause() {
        player.pause();
        mPlayerThread.setPlay(false);
    }

    private void startPlay() {
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (position < viewModel.getMusicDates().getValue().size() -1) {
                    position ++;

                } else {
                    position = 0;
                }
                updateControlUi(position);
                try {
                    updatePlayer(position);
                    startPlay();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        player.start();
        if (playThread == null) {
            mPlayerThread = new PlayerThread(new WeakReference<>(this));
            playThread = new Thread(mPlayerThread);
            playThread.start();
        } else {
            mPlayerThread.setPlay(true);
        }
    }

    private static class MyHandler extends Handler {
        public MyHandler(WeakReference<MainActivity> mWeakReference) {
            this.mWeakReference = mWeakReference;
        }

        private WeakReference<MainActivity> mWeakReference;
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    MainActivity activity = mWeakReference.get();
                    if (activity != null) {
                        MediaPlayer player = activity.player;
                        activity.progressBar.setProgress(player.getCurrentPosition());
                        activity.currentTime.setText(MyAdapter.getDuration(player.getCurrentPosition()));
                    }
                    break;
            }
        }
    }

    private static class PlayerThread implements Runnable {
        WeakReference<MainActivity> mainActivityWeakReference;
        boolean isPlay = true;

        public synchronized void setPlay(boolean play) {
            isPlay = play;
            if (isPlay) {
                notifyAll();
            }
        }

        public PlayerThread(WeakReference<MainActivity> mainActivityWeakReference) {
            this.mainActivityWeakReference = mainActivityWeakReference;
        }

        @Override
        public void run() {
            MainActivity activity = mainActivityWeakReference.get();
            while (true) {
                try {
                    synchronized (this) {
                        if(!isPlay) {
                            wait();
                        }
                    }
                    Thread.sleep(1000);
                    activity.handler.sendEmptyMessage(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}