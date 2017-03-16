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
public class SQLiteHelper extends SQLiteAssetHelper {

    protected SQLiteDatabase mDatabase;
    protected String mName;
    protected int mVersion;
    private static List<SQLiteHelper> mInstance;


    public interface onInsertOrReplaceListener {
        void onInsertOrReplace(long id);
    }

    public interface onDeleteListener {
        void onDelete(int numberOfDeletedRows);
    }

    public interface onGetInstanceListener {
        void onGetInstance(SQLiteHelper database);
    }

    public interface onReadListener {
        void onRead(Cursor cursor);
    }


    public SQLiteHelper(Context context, String name, String storageDirectory, SQLiteDatabase.CursorFactory factory, int version) {
        super(context.getApplicationContext(), name, storageDirectory, factory, version);
        mName = name;
        mVersion = version;
    }

    public SQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context.getApplicationContext(), name, factory, version);
        mName = name;
        mVersion = version;
    }

    // <Database Instance Creator> //
    public static synchronized void getInstance(Context context, String dbName, int dbVersion, onGetInstanceListener listener) {
        SQLiteHelper DBHelper = getInstanceIfExist(dbName);
        if (DBHelper == null) {
            //TODO: Add synchronized block here
            DBHelper = new SQLiteHelper(context, dbName, null, dbVersion);
            mInstance.add(DBHelper);
            MasterAsyncTask getInstanceTask = MasterAsyncTask.createNewTask();
            getInstanceTask.setupDatabaseInBackground(DBHelper, listener);
        } else {
            listener.onGetInstance(DBHelper);
        }
    }

    private static SQLiteHelper getInstanceIfExist(String dbName) {
        if (mInstance != null) {
            for (SQLiteHelper instance : mInstance) {
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


    public void insertOrReplace(String tableName, ContentValues values, onInsertOrReplaceListener listener) {
        MasterAsyncTask.createNewTask().insertOrReplace(mDatabase, tableName, values, listener);
    }

    public void select(String tableName, String[] sqlSelect, String sqlWhere, String[] WhereArgs, String groupBy, String having, String orderBy, onReadListener listener) {
        MasterAsyncTask.createNewTask().select(mDatabase, tableName, sqlSelect, sqlWhere, WhereArgs, groupBy, having, orderBy, listener);
        // Cursor cursor = mDatabase.query(ZekrDatabase.TABLE_ZEKR, sqlSelect, "user_id = ?", new String[]{userId}, null, null, null);
        // Cursor cursor = mDatabase.rawQuery("SELECT * FROM zekr", null);
    }

    public void delete(String tableName, String sqlWhere, String[] WhereArgs, onDeleteListener listener) {
        MasterAsyncTask.createNewTask().delete(mDatabase, tableName, sqlWhere, WhereArgs, listener);
    }

    public SQLiteDatabase getWriteableDatabase() {
        return mDatabase;
    }


    public static void closeAllTasks() {
        if (mInstance != null)
            for (SQLiteHelper instance : mInstance) {
                instance.closeTasks();
            }
    }

    public void closeTasks() {
        MasterAsyncTask.cancelAllTasks();
    }

}
