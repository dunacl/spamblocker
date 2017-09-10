package cl.hint.spamblocker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLiteDB {

    public static final String KEY_ID = "id";
    public static final String KEY_PHONENUMBER = "phonenumber";
    public static final  String KEY_REASON = "reason";

    private static final String TAG = "DB";
    private static final String DATABASE_NAME = "spamblocker.db";

    private static final String DATABASE_TABLE = "blacklist";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE =
            "create table blacklist (id text primary key, phonenumber text not null, reason text not null);";

    private final Context context;
    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    public SQLiteDB(Context ctx) {

        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {

            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(DATABASE_CREATE);
                Log.d("DB", "Database Created");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }
    }

    //---open SQLite DB---
    public SQLiteDB open() throws SQLException {
        Log.d(TAG, "Access function open");
        db = DBHelper.getWritableDatabase();
        return this;
    }

    //---close SQLite DB---
    public void close() {

        DBHelper.close();
    }

    //---insert data into SQLite DB---
    public long insert(int id, String phonenumber, String reason) {
        if(!db.isOpen()) {
            Log.d(TAG, "Database Closed en insert");
        }

        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ID, String.valueOf(id));
        initialValues.put(KEY_PHONENUMBER, phonenumber);
        initialValues.put(KEY_REASON, reason);
        /*
        Log.d("DB", String.valueOf(id));
        Log.d("DB", phonenumber);
        Log.d("DB", reason);
        */
        return db.insert(DATABASE_TABLE, null, initialValues);
    }

    //--Select data from DB--
    public Cursor select() {
        //Log.d(TAG, "Access function deleteAll");
        if(!db.isOpen()) {
            Log.d(TAG, "Func select - DB CLOSED");
        }

        return db.rawQuery("SELECT "+KEY_ID+", "+ KEY_PHONENUMBER+", "+KEY_REASON+" FROM "+DATABASE_TABLE, null);
    }


    public void deleteAll() {
        //Log.d(TAG, "Access function deleteAll");
        if(!db.isOpen()) {
            Log.d(TAG, "Database Closed - deleteAll");
        }
        db.delete(DATABASE_TABLE, null, null);
    }

}