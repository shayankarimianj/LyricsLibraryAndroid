package com.lyricslibrary.android;

public class Song {
    public long id;
    public String title;
    public String lyrics;
    public String dastgah;
    public String gam;
    public int sortOrder;

    public Song(long id, String title, String lyrics, String dastgah, String gam, int sortOrder) {
        this.id = id;
        this.title = title == null ? "" : title;
        this.lyrics = lyrics == null ? "" : lyrics;
        this.dastgah = dastgah == null ? "" : dastgah;
        this.gam = gam == null ? "" : gam;
        this.sortOrder = sortOrder;
    }
}
