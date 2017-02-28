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
    public static final int TASK_INSERT_REPLACE = 10;
    public static final int TASK_READ = 20;
    private static List<MasterAsyncTask> mTaskLists;
    private SQLiteDatabase mDatabaseSQLiteIO;
    private SQLiteHelper mDatabaseInstance;
    private SQLiteHelper.onInsertOrReplaceListener[] mInsertListener;
    private ContentValues mValues;
    private String mTableName;
    private String[] mSqlSelect;
    private int mTaskCode;
    private SQLiteHelper.onGetInstanceListener[] mGetInstanceListener;
    private SQLiteHelper.onReadListener[] mReadListener;
    private Cursor mReadCursor;
    private String mSqlWhere;
    private String[] mSqlWehreArgs;
    private String mSqlGroupBy;
    private String mSqlHaving;
    private String mSqlOrderBy;
    private long mInsertedRowId;

    protected void insertOrReplace(SQLiteDatabase database, String tableName, ContentValues values, SQLiteHelper.onInsertOrReplaceListener... listener) {
        mDatabaseSQLiteIO = database;
        mValues = values;
        mInsertListener = listener;
        mTableName = tableName;
        mTaskCode = TASK_INSERT_REPLACE;
        execute();
    }

    protected void select(SQLiteDatabase database, String tableName, String[] sqlSelect, String sqlWhere, String[] WhereArgs, String groupBy, String having, String orderBy, SQLiteHelper.onReadListener... listener) {
        mDatabaseSQLiteIO = database;
        mTableName = tableName;
        mSqlSelect = sqlSelect;
        mSqlWhere = sqlWhere;
        mSqlWehreArgs = WhereArgs;
        mSqlGroupBy = groupBy;
        mSqlHaving = having;
        mSqlOrderBy = orderBy;
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
            case TASK_INSERT_REPLACE:
                mInsertedRowId = mDatabaseSQLiteIO.insertOrThrow(mTableName, null, mValues);
                break;
            case TASK_GET_DATABASE:
                mDatabaseInstance.setupDatabase();
                break;
            case TASK_READ:
                mReadCursor = mDatabaseSQLiteIO.query(mTableName, mSqlSelect, mSqlWhere, mSqlWehreArgs, mSqlGroupBy, mSqlHaving, mSqlOrderBy);
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
            case TASK_INSERT_REPLACE:
                for (SQLiteHelper.onInsertOrReplaceListener listener : mInsertListener)
                    listener.onInsertOrReplace(mInsertedRowId);
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
