package com.score.shopz.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Do all database insertions, updated, deletions from here
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class PayzDbSource {
    private static final String TAG = PayzDbSource.class.getName();
    private static Context context;

    public PayzDbSource(Context context) {
        Log.i(TAG, "Init: db source");
        this.context = context;
    }

    public void createPayz(com.score.shopz.pojos.Payz payz) {
        Log.i(TAG, "Create payz with account: " + payz.getAccount() + " amount: " + payz.getAmount());

        SQLiteDatabase db = PayzDbHelper.getInstance(context).getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PayzDbContract.Payz.COLUMN_NAME_ACCOUNT, payz.getAccount());
        values.put(PayzDbContract.Payz.COLUMN_NAME_AMOUNT, payz.getAmount());
        values.put(PayzDbContract.Payz.COLUMN_NAME_TIME, payz.getTime());

        db.insert(PayzDbContract.Payz.TABLE_NAME, null, values);
        db.close();
    }

    public List<com.score.shopz.pojos.Payz> readAllPayz() {
        Log.i(TAG, "Read payz");

        List<com.score.shopz.pojos.Payz> payzList = new ArrayList<com.score.shopz.pojos.Payz>();

        SQLiteDatabase db = PayzDbHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = db.query(PayzDbContract.Payz.TABLE_NAME, null, null, null, null, null, null);

        // user attributes
        String _account;
        String _amount;
        String _time;

        // extract attributes
        while (cursor.moveToNext()) {
            _account = cursor.getString(cursor.getColumnIndex(PayzDbContract.Payz.COLUMN_NAME_ACCOUNT));
            _amount = cursor.getString(cursor.getColumnIndex(PayzDbContract.Payz.COLUMN_NAME_AMOUNT));
            _time = cursor.getString(cursor.getColumnIndex(PayzDbContract.Payz.COLUMN_NAME_TIME));

            payzList.add(new com.score.shopz.pojos.Payz(_account, _amount, _time));
        }

        // clean
        cursor.close();
        db.close();

        Log.d(TAG, "payz count " + payzList.size());

        return payzList;
    }

}
