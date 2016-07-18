package com.score.shopz.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Perform creating tables here
 *
 * @author erangaeb@gmail.com(eranga herath)
 */
public class PayzDbHelper extends SQLiteOpenHelper {

    private static final String TAG = PayzDbHelper.class.getName();

    // we use singleton database
    private static PayzDbHelper payzDbHelper;

    // If you change the database schema, you must increment the database version
    private static final int DATABASE_VERSION = 6;
    private static final String DATABASE_NAME = "PAYZ.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String NUMBER_TYPE = " NUM";

    private static final String SQL_CREATE_PAYZ =
            "CREATE TABLE IF NOT EXISTS " + PayzDbContract.Payz.TABLE_NAME + " (" +
                    PayzDbContract.Payz._ID + " " + " INTEGER PRIMARY KEY AUTOINCREMENT" + ", " +
                    PayzDbContract.Payz.COLUMN_NAME_ACCOUNT + " " + TEXT_TYPE + ", " +
                    PayzDbContract.Payz.COLUMN_NAME_AMOUNT + " " + TEXT_TYPE + " NOT NULL" + ", " +
                    PayzDbContract.Payz.COLUMN_NAME_TIME + " " + TEXT_TYPE + ")";

    private static final String SQL_DELETE_PAYZ =
            "DROP TABLE IF EXISTS " + PayzDbContract.Payz.TABLE_NAME;

    /**
     * Init context
     * Init database
     *
     * @param context application context
     */
    public PayzDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * We are reusing one database instance in all over the app for better memory usage
     *
     * @param context application context
     * @return db helper instance
     */
    synchronized static PayzDbHelper getInstance(Context context) {
        if (payzDbHelper == null) {
            payzDbHelper = new PayzDbHelper(context.getApplicationContext());
        }

        return payzDbHelper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "OnCreate: creating db helper, db version - " + DATABASE_VERSION);
        Log.d(TAG, SQL_CREATE_PAYZ);

        db.execSQL(SQL_CREATE_PAYZ);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // enable foreign key constraint here
        Log.d(TAG, "OnConfigure: Enable foreign key constraint");
        db.setForeignKeyConstraintsEnabled(true);
    }

    /**
     * {@inheritDoc}
     */
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        Log.d(TAG, "OnUpgrade: updating db helper, db version - " + DATABASE_VERSION);
        db.execSQL(SQL_DELETE_PAYZ);

        onCreate(db);
    }

    /**
     * {@inheritDoc}
     */
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
