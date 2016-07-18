package com.score.shopz.db;

import android.provider.BaseColumns;

/**
 * Keep database table attributes
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class PayzDbContract {

    /* Inner class that defines pyaz table contents */
    public static abstract class Payz implements BaseColumns {
        public static final String TABLE_NAME = "payz";
        public static final String COLUMN_NAME_ACCOUNT = "account";
        public static final String COLUMN_NAME_AMOUNT = "amount";
        public static final String COLUMN_NAME_TIME = "time";
    }

    /* Inner class that defines metadata table contents */
    public static abstract class MetaData implements BaseColumns {
        public static final String TABLE_NAME = "metadata";
        public static final String COLUMN_NAME_DATA = "data_name";
        public static final String COLUMN_NAME_VALUE = "value";
    }

}