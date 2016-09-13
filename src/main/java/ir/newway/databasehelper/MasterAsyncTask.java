package ir.newway.databasehelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

/**
 * Created by goldm on 13/09/2016.
 */
public class MasterAsyncTask extends AsyncTask {
    public static final int TASK_GET_DATABASE = 0;
    public static final int TASK_INSERT = 10;
    private SQLiteDatabase mDatabaseSQLiteIO;
    private SQLiteDBHelper mDatabaseInstance;
    private Context mContext;
    private SQLiteDBHelper.onInsertTaskListener[] mInsertListener;
    private ContentValues mValues;
    private String mTableName;
    private int mTaskCode;
    private SQLiteDBHelper.onGetInstanceListener[] mGetInstanceListener;

    protected void insert(SQLiteDatabase database, String tableName, ContentValues values, SQLiteDBHelper.onInsertTaskListener... listener) {
        mDatabaseSQLiteIO = database;
        mValues = values;
        mInsertListener = listener;
        mTableName = tableName;
        mTaskCode = TASK_INSERT;
        execute();
    }

    protected void setupDatabaseInBackground(Context context, SQLiteDBHelper database, SQLiteDBHelper.onGetInstanceListener... listener) {
        mContext = context.getApplicationContext();
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
                for (SQLiteDBHelper.onInsertTaskListener listener : mInsertListener)
                    listener.onTaskDone(this);
                break;
            case TASK_GET_DATABASE:
                for (SQLiteDBHelper.onGetInstanceListener listener : mGetInstanceListener)
                    listener.onTaskDone(this, mDatabaseInstance);
        }
        clearCatch();
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        clearCatch();
    }

    private void clearCatch() {
        mDatabaseInstance = null;
        mDatabaseSQLiteIO = null;
        mGetInstanceListener = null;
        mInsertListener = null;
        mValues = null;
        mContext = null;
    }
}
