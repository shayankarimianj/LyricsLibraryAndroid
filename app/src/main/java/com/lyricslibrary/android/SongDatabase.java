package com.lyricslibrary.android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class SongDatabase extends SQLiteOpenHelper {
    private static final String DB_NAME = "lyrics.db";
    private static final int DB_VERSION = 1;

    public SongDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE songs (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT NOT NULL," +
                "lyrics TEXT DEFAULT ''," +
                "dastgah TEXT DEFAULT ''," +
                "gam TEXT DEFAULT ''," +
                "sort_order INTEGER DEFAULT 0)");
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Versioned migrations can be added here without deleting user data.
    }

    public List<Song> allSongs() {
        List<Song> result = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT id,title,lyrics,dastgah,gam,sort_order FROM songs ORDER BY sort_order,id", null)) {
            while (c.moveToNext()) result.add(readSong(c));
        }
        return result;
    }

    public Song getSong(long id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT id,title,lyrics,dastgah,gam,sort_order FROM songs WHERE id=?",
                new String[]{String.valueOf(id)})) {
            return c.moveToFirst() ? readSong(c) : null;
        }
    }

    private Song readSong(Cursor c) {
        return new Song(c.getLong(0), c.getString(1), c.getString(2),
                c.getString(3), c.getString(4), c.getInt(5));
    }

    public long addSong(String title) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("title", title);
        v.put("lyrics", "");
        v.put("dastgah", "");
        v.put("gam", "");
        v.put("sort_order", allSongs().size());
        return db.insertOrThrow("songs", null, v);
    }

    public void updateSong(Song song) {
        ContentValues v = new ContentValues();
        v.put("title", song.title);
        v.put("lyrics", song.lyrics);
        v.put("dastgah", song.dastgah);
        v.put("gam", song.gam);
        getWritableDatabase().update("songs", v, "id=?", new String[]{String.valueOf(song.id)});
    }

    public void deleteSong(long id) {
        getWritableDatabase().delete("songs", "id=?", new String[]{String.valueOf(id)});
        normalizeOrder();
    }

    public void saveOrder(List<Song> ordered) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (int i = 0; i < ordered.size(); i++) {
                ContentValues v = new ContentValues();
                v.put("sort_order", i);
                db.update("songs", v, "id=?", new String[]{String.valueOf(ordered.get(i).id)});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void normalizeOrder() {
        saveOrder(allSongs());
    }
}
