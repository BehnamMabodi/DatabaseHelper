package ir.newway.databasehelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by goldm on 13/09/2016.
 */
public class SQLiteDBHelper extends SQLiteAssetHelper {

    protected SQLiteDatabase mDatabase;
    private static SQLiteDBHelper mInstance;
    protected Context mContext;
    private List<MasterAsyncTask> mTaskLists;

    public interface onInsertTaskListener {
        void onTaskDone(MasterAsyncTask task);
    }

    public interface onGetInstanceListener {
        void onTaskDone(MasterAsyncTask task, SQLiteDBHelper database);
    }


    public SQLiteDBHelper(Context context, String name, String storageDirectory, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, storageDirectory, factory, version);
        mContext = context.getApplicationContext();
        mTaskLists = new ArrayList<>();
    }

    public SQLiteDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context.getApplicationContext();
        mTaskLists = new ArrayList<>();
    }

    protected synchronized SQLiteDatabase setupDatabase() {
        mDatabase = getWritableDatabase();
        return mDatabase;
    }

    protected static synchronized void setupDatabaseInBackground(Context context, onGetInstanceListener listener) {
        MasterAsyncTask task = mInstance.createNewTask();
        task.setupDatabaseInBackground(context, mInstance, listener, new onGetInstanceListener() {
            @Override
            public void onTaskDone(MasterAsyncTask task, SQLiteDBHelper database) {
                mInstance.onTaskDone(task);
            }
        });
    }

    protected void setInstance(SQLiteDBHelper instance) {
        mInstance = instance;
    }


    protected void insert(String tableName, ContentValues values, onInsertTaskListener listener) {
        createNewTask().insert(mDatabase, tableName, values, listener, new onInsertTaskListener() {
            @Override
            public void onTaskDone(MasterAsyncTask task) {
                mInstance.onTaskDone(task);
            }
        });
    }

    protected MasterAsyncTask createNewTask() {
        MasterAsyncTask newTask = new MasterAsyncTask();
        mTaskLists.add(newTask);
        return newTask;
    }

    protected void onTaskDone(MasterAsyncTask task) {
        mTaskLists.remove(task);
    }

    public void closeTasks() {
        for (MasterAsyncTask task : mTaskLists) {
            task.cancel(false);
        }
        mTaskLists.clear();
    }

}
