package com.example.musicplayer;

import android.media.MediaPlayer;
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
        holder.duration.setText(musicDates.get(position).getDuration() + "");
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
            name = itemView.findViewById(R.id.name);
            artist = itemView.findViewById(R.id.artist);
            duration = itemView.findViewById(R.id.duration);
            cover = itemView.findViewById(R.id.cover);
        }
    }

    public interface OnClickListener {
        void onItemClick(View view, int position);
    }
}
