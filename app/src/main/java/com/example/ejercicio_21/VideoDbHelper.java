package com.example.ejercicio_21;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class VideoDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "video_db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_VIDEOS = "videos";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_VIDEO_PATH = "video_path";


    private static final String DATABASE_CREATE = "CREATE TABLE " + TABLE_VIDEOS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_VIDEO_PATH + " TEXT NOT NULL);";

    public VideoDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VIDEOS);
        onCreate(db);
    }
}
