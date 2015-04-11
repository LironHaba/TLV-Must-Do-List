package com.shenkar.mobile.linitudo.db;

import android.provider.BaseColumns;

/**
 * Created by Tuvit 026490060, Liron 037093788 & Nir 312470160 on 4/4/2015.
 */

public class TaskContract {
    public static final String DB_NAME = "com.shenkar.mobile.linitudo.db.tasks";
    public static final int DB_VERSION = 3;
    public static final String TABLE = "tasks";

    public class Columns {
        public static final String TASK = "task";
        public static final String DATE = "date";
        public static final String LOCATION = "location";
        public static final String _ID = BaseColumns._ID;
    }
}