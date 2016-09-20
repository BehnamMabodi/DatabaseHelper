package ir.newway.databasehelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by goldm on 13/09/2016.
 */
public class SQLiteDBHelper extends SQLiteAssetHelper {

    protected Context mContext;
    protected SQLiteDatabase mDatabase;
    protected String mName;
    protected int mVersion;
    private static List<SQLiteDBHelper> mInstance;


    public interface onInsertTaskListener {
        void onInsertTask();
    }

    public interface onGetInstanceListener {
        void onGetInstanceTask(SQLiteDBHelper database);
    }


    public SQLiteDBHelper(Context context, String name, String storageDirectory, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, storageDirectory, factory, version);
        mContext = context.getApplicationContext();
        mName = name;
        mVersion = version;
    }

    public SQLiteDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context.getApplicationContext();
        mName = name;
        mVersion = version;
    }

    // <Database Instance Creator> //
    public static synchronized void getInstance(Context context, String dbName, int dbVersion, onGetInstanceListener listener) {
        SQLiteDBHelper DBHelper = getInstanceIfExist(dbName);
        if (DBHelper == null) {
            //TODO: Add synchronized block here
            DBHelper = new SQLiteDBHelper(context, dbName, null, dbVersion);
            mInstance.add(DBHelper);
            MasterAsyncTask getInstanceTask = MasterAsyncTask.createNewTask();
            getInstanceTask.setupDatabaseInBackground(DBHelper, listener);
        } else {
            listener.onGetInstanceTask(DBHelper);
        }
    }

    private static SQLiteDBHelper getInstanceIfExist(String dbName) {
        if (mInstance != null) {
            for (SQLiteDBHelper instance : mInstance) {
                if (instance.getName().equals(dbName))
                    return instance;
            }
        } else {
            mInstance = new ArrayList<>();
        }
        return null;
    }

    protected synchronized SQLiteDatabase setupDatabase() {
        mDatabase = getWritableDatabase();
        return mDatabase;
    }

    public String getName() {
        return mName;
    }

    // </Database Instance Creator> //


    public void insert(String tableName, ContentValues values, onInsertTaskListener listener) {
        MasterAsyncTask.createNewTask().insert(mDatabase, tableName, values, listener);
    }

    public Cursor select(String tableName, String[] sqlSelect) {
        // Cursor cursor = mDatabase.query(ZekrDatabase.TABLE_ZEKR, sqlSelect, "user_id = ?", new String[]{userId}, null, null, null);
        Cursor cursor = mDatabase.query(tableName, sqlSelect, null, null, null, null, null);
        // Cursor cursor = mDatabase.rawQuery("SELECT * FROM zekr", null);
        cursor.moveToFirst();
        return cursor;
    }

    public SQLiteDatabase getWriteableDatabase() {
        return mDatabase;
    }


    public static void closeAllTasks() {
        if (mInstance != null)
            for (SQLiteDBHelper instance : mInstance) {
                instance.closeTasks();
            }
    }

    public void closeTasks() {
        MasterAsyncTask.cancelAllTasks();
    }

}
