package com.example.musicplayer;

public class MusicDates {
    private String path;
    private int musicId;
    private String name;
    private String album;
    private String artist;
    private long size;
    private int duration;
    private String uri;

    public String getPath() {
        return path;
    }

    public int getMusicId() {
        return musicId;
    }

    public String getName() {
        return name;
    }

    public String getAlbum() {
        return album;
    }

    public String getArtist() {
        return artist;
    }

    public long getSize() {
        return size;
    }

    public int getDuration() {
        return duration;
    }

    public String getUri() {
        return uri;
    }

    public MusicDates(String path, int musicId, String name, String album, String artist, long size, int duration, String uri) {
        this.path = path;// 路径
        this.musicId = musicId;// 歌曲的id
        this.name = name;// 歌曲名
        this.album = album;// 专辑
        this.artist = artist;// 作者
        this.size = size;// 大小
        this.duration = duration;// 时长(毫秒)
        this.uri = uri;
    }
}
