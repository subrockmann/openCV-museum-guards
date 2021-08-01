package gr.georkouk.theguardaians.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import java.util.concurrent.atomic.AtomicInteger;

import gr.georkouk.theguardaians.Application;


public class MyDB {

    private static MyDB instance;
    private DBHelper dbHelper;
    private SQLiteDatabase ourDatabase;
    private AtomicInteger mOpenCounter = new AtomicInteger();


    public MyDB(Context context) {
        dbHelper = new DBHelper(context);
    }

    public static synchronized void initializeInstance(Context context) {
        if (instance == null) {
            instance = new MyDB(context);
        }
    }

    public static synchronized MyDB getInstance() {
        if (instance == null){
            initializeInstance(Application.getContext());
        }

        return instance;
    }

    public synchronized MyDB open() throws SQLException {
        if(mOpenCounter.incrementAndGet() == 1) {
            ourDatabase = dbHelper.getWritableDatabase();
        }

        return this;
    }

    public synchronized void close() {
        if(mOpenCounter.decrementAndGet() == 0 && isOpened()) {
            dbHelper.close();
        }
    }

    private boolean isOpened(){
        return ourDatabase.isOpen();
    }

    public void runSql(String sql){
        Cursor c = ourDatabase.rawQuery(sql, null);

        if (c != null) {
            c.moveToFirst();
            c.close();
        }
    }

    public Cursor runSqlCursor(String sql){
        return ourDatabase.rawQuery(sql, null);
    }

    public boolean update(ContentValues contentValues, String tableName, String whereClause){
        int result = ourDatabase.update(tableName, contentValues, whereClause, null);

        return result > 0;
    }

    public boolean delete(String tableName, String whereClause){
        int result = ourDatabase.delete(tableName, whereClause, null);

        return result > 0;
    }

    public long insert(ContentValues contentValues, String tableName){
        return ourDatabase.insertWithOnConflict(
                tableName,
                null,
                contentValues,
                SQLiteDatabase.CONFLICT_REPLACE
        );
    }

}
