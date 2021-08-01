package gr.georkouk.theguardaians.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;


public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "theGuardAIans.db";
    public static final int DATABASE_VERSION = 1;

    public DBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String messageTableSql =
                "Create table mqttMessage ( " +
                    " mqttMessage integer default '0', " +
                    " topic text default '', " +
                    " roomNumber text default '', " +
                    " cameraId text default '', " +
                    " objectId text default '', " +
                    " objectName text default '', " +
                    " status text default '', " +
                    " timestamp text default '', " +
                    " filename text default '', " +
                    " seen integer default '0', " +
                    " PRIMARY KEY(mqttMessage autoincrement) " +
                " )";

        sqLiteDatabase.execSQL(messageTableSql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

}
