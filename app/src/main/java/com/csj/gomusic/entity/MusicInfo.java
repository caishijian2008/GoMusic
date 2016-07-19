package com.csj.gomusic.entity;

/**
 * Created by caishijian on 16-4-25.
 */
public class MusicInfo {
    private long id; //音乐id
    private String title; //音乐标题
    private String artist; //艺术家
    private String album; //专辑
    private String displayName; //显示名称
    private String url; //音乐路径
    private long albumId; //专辑id
    private long duration; //时长
    private long size; //文件大小

    public MusicInfo() {
    }

    public MusicInfo(String album, long albumId, String artist, String displayName, long duration, long id, long size, String title, String url) {
        this.album = album;
        this.albumId = albumId;
        this.artist = artist;
        this.displayName = displayName;
        this.duration = duration;
        this.id = id;
        this.size = size;
        this.title = title;
        this.url = url;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "MusicInfo{" +
                "album='" + album + '\'' +
                ", id=" + id +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", displayName='" + displayName + '\'' +
                ", url='" + url + '\'' +
                ", albumId=" + albumId +
                ", duration=" + duration +
                ", size=" + size +
                '}';
    }
}
