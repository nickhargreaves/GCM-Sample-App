package com.infinitedimensions.samples.gcm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.infinitedimensions.samples.gcm.models.Message;

import java.util.ArrayList;
import java.util.List;


public class DBHandler {

    //messages
    public static final String TABLE_MESSAGES= "messages";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_BOOK = "book";
    public static final String COLUMN_MESSAGE = "message";
    public static final String COLUMN_USER = "user";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_IS_MINE = "is_mine";


    private static final String CREATE_MESSAGES_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_MESSAGES + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
            + COLUMN_MESSAGE + " TEXT,"
            + COLUMN_USER + " TEXT,"
            + COLUMN_IS_MINE + " TEXT"
            + "); ";
    private static final String DATABASE_NAME = "";
    private static final int DATABASE_VERSION = 0;

    private SQLiteDatabase db;

    protected static final String PASSWORD_SECRET = "";
    private Context context;

    public DBHandler(Context ctx) {
        this.context = ctx;
        db = ctx.openOrCreateDatabase(DATABASE_NAME, 0, null);

        // Create tables if they don't exist
        db.execSQL(CREATE_MESSAGES_TABLE);

        db.setVersion(DATABASE_VERSION);
    }


    public void addMessage(Message message) {

        ContentValues values = new ContentValues();

        values.put(COLUMN_MESSAGE, message.getMessage());
        values.put(COLUMN_USER, message.getUser());
        values.put(COLUMN_IS_MINE, message.getIsMine());

        db.insert(TABLE_MESSAGES, null, values);

    }
    public List<Message> getMessages() {
        List<Message> messagesList = new ArrayList<Message>();
        // Select All Query
        //String selectQuery = "SELECT * FROM " + TABLE_MESSAGES + " WHERE " + COLUMN_USER + " ='" + user_id + "'";
        String selectQuery = "SELECT * FROM " + TABLE_MESSAGES;

        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Message content = new Message();

                content.setMessage((cursor.getString(1)));
                content.setUser((cursor.getString(2)));
                content.setIsMine((cursor.getString(3)));

                messagesList.add(content);
            } while (cursor.moveToNext());
        }

        // return messages as a list
        return messagesList;
    }

}
