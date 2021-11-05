package com.example.musicplayer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private OnClickListener listener;

    private List<MusicDates> musicDates;

    public void setMusicDates(List<MusicDates> musicDates) {
        if (this.musicDates == null) {
            this.musicDates = musicDates;
            return;
        }
        if (!this.musicDates.equals(musicDates)) {
            this.musicDates.clear();
            this.musicDates.addAll(musicDates);
        }
    }

    public void setListener(OnClickListener listener) {
        this.listener = listener;
    }

    public MyAdapter(List<MusicDates> musicDates) {
        this.musicDates = musicDates;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.music_item, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.name.setText(musicDates.get(position).getName());
        holder.artist.setText(musicDates.get(position).getArtist());
        holder.duration.setText(getDuration(musicDates.get(position).getDuration()));
        loadingCover(musicDates.get(position).getPath(), holder.cover);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(v, holder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return musicDates == null ? 0 : musicDates.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView name, artist, duration;
        private ImageView cover;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.music_name);
            artist = itemView.findViewById(R.id.music_artist);
            duration = itemView.findViewById(R.id.music_duration);
            cover = itemView.findViewById(R.id.cover);
        }
    }

    public interface OnClickListener {
        void onItemClick(View view, int position);
    }

    public static void loadingCover(String path, ImageView imageView) {//加载歌曲封面
        MediaMetadataRetriever mediaMetadataRetriever=new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(path);
        byte[] picture = mediaMetadataRetriever.getEmbeddedPicture();
        Bitmap bitmap= BitmapFactory.decodeByteArray(picture,0,picture.length);
        imageView.setImageBitmap(bitmap);
    }

    public static String getDuration(int duration) {
        String time = "" ;
        long minute = duration / 60000 ;
        long seconds = duration % 60000 ;
        long second = Math.round((float)seconds/1000) ;
        if( minute < 10 ){
            time += "0" ;
        }
        time += minute+":" ;
        if( second < 10 ){
            time += "0" ;
        }
        time += second ;
        return time ;
    }
}
