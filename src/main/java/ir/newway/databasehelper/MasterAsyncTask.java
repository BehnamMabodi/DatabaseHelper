package ir.newway.databasehelper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by goldm on 13/09/2016.
 */
public class MasterAsyncTask extends AsyncTask {
    public static final int TASK_GET_DATABASE = 0;
    public static final int TASK_INSERT = 10;
    public static final int TASK_READ = 20;
    private static List<MasterAsyncTask> mTaskLists;
    private SQLiteDatabase mDatabaseSQLiteIO;
    private SQLiteHelper mDatabaseInstance;
    private SQLiteHelper.onInsertListener[] mInsertListener;
    private ContentValues mValues;
    private String mTableName;
    private String[] mSqlSelect;
    private int mTaskCode;
    private SQLiteHelper.onGetInstanceListener[] mGetInstanceListener;
    private SQLiteHelper.onReadListener[] mReadListener;
    private Cursor mReadCursor;

    protected void insert(SQLiteDatabase database, String tableName, ContentValues values, SQLiteHelper.onInsertListener... listener) {
        mDatabaseSQLiteIO = database;
        mValues = values;
        mInsertListener = listener;
        mTableName = tableName;
        mTaskCode = TASK_INSERT;
        execute();
    }

    protected void select(SQLiteDatabase database, String tableName, String[] sqlSelect, SQLiteHelper.onReadListener... listener) {
        mDatabaseSQLiteIO = database;
        mTableName = tableName;
        mSqlSelect = sqlSelect;
        mReadListener = listener;
        mTaskCode = TASK_READ;
        execute();
    }

    protected static MasterAsyncTask createNewTask() {
        if (mTaskLists == null)
            mTaskLists = new ArrayList<>();
        MasterAsyncTask newTask = new MasterAsyncTask();
        mTaskLists.add(newTask);
        return newTask;
    }

    protected static void RemoveTask(MasterAsyncTask task) {
        if (mTaskLists != null)
            mTaskLists.remove(task);
    }

    protected void setupDatabaseInBackground(SQLiteHelper database, SQLiteHelper.onGetInstanceListener... listener) {
        mGetInstanceListener = listener;
        mTaskCode = TASK_GET_DATABASE;
        mDatabaseInstance = database;
        execute();
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        switch (mTaskCode) {
            case TASK_INSERT:
                mDatabaseSQLiteIO.insert(mTableName, null, mValues);
                break;
            case TASK_GET_DATABASE:
                mDatabaseInstance.setupDatabase();
                break;
            case TASK_READ:
                mReadCursor = mDatabaseSQLiteIO.query(mTableName, mSqlSelect, null, null, null, null, null);
        }
/*        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        switch (mTaskCode) {
            case TASK_INSERT:
                for (SQLiteHelper.onInsertListener listener : mInsertListener)
                    listener.onInsert();
                break;
            case TASK_GET_DATABASE:
                for (SQLiteHelper.onGetInstanceListener listener : mGetInstanceListener)
                    listener.onGetInstance(mDatabaseInstance);
                break;
            case TASK_READ:
                for (SQLiteHelper.onReadListener listener : mReadListener)
                    listener.onRead(mReadCursor);

        }
        clearCatch();
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        clearCatch();
    }

    public static void cancelAllTasks() {
        for (MasterAsyncTask task : mTaskLists) {
            task.cancel(false);
        }
        mTaskLists.clear();
    }

    private void clearCatch() {
        mDatabaseInstance = null;
        mDatabaseSQLiteIO = null;
        mGetInstanceListener = null;
        mInsertListener = null;
        mReadListener = null;
        mValues = null;
        mReadCursor = null;
        RemoveTask(this);
    }
}
