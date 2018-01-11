package ir.newway.databasehelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

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

    public synchronized SQLiteDatabase setupDatabase() {
        if (mDatabase == null)
            mDatabase = getWritableDatabase();
        return mDatabase;
    }

    public String getName() {
        return mName;
    }

    // </Database Instance Creator> //


    public void insertOrReplace(String tableName, ContentValues values, onInsertOrReplaceListener listener) {
        MasterAsyncTask.createNewTask().insertOrReplace(setupDatabase(), tableName, values, listener);
    }

    public void select(String tableName, String[] sqlSelect, String sqlWhere, String[] WhereArgs, String groupBy, String having, String orderBy, onReadListener listener) {
        MasterAsyncTask.createNewTask().select(setupDatabase(), tableName, sqlSelect, sqlWhere, WhereArgs, groupBy, having, orderBy, listener);
        // Cursor cursor = mDatabase.query(ZekrDatabase.TABLE_ZEKR, sqlSelect, "user_id = ?", new String[]{userId}, null, null, null);
        // Cursor cursor = mDatabase.rawQuery("SELECT * FROM zekr", null);
    }

    public void delete(String tableName, String sqlWhere, String[] WhereArgs, onDeleteListener listener) {
        MasterAsyncTask.createNewTask().delete(setupDatabase(), tableName, sqlWhere, WhereArgs, listener);
    }

    public void rawQuery(String rawQuery, String[] queryARGS, onReadListener listener) {
        MasterAsyncTask.createNewTask().rawQuery(setupDatabase(), rawQuery, queryARGS, listener);
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


    // SQL Debug Activity:
    public ArrayList<Cursor> getData(String Query) {
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[]{"mesage"};
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2 = new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);


        try {
            String maxQuery = Query;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);


            //add value to cursor2
            Cursor2.addRow(new Object[]{"Success"});

            alc.set(1, Cursor2);
            if (null != c && c.getCount() > 0) {


                alc.set(0, c);
                c.moveToFirst();

                return alc;
            }
            return alc;
        } catch (SQLException sqlEx) {
            Log.d("printing exception", sqlEx.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[]{"" + sqlEx.getMessage()});
            alc.set(1, Cursor2);
            return alc;
        } catch (Exception ex) {

            Log.d("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[]{"" + ex.getMessage()});
            alc.set(1, Cursor2);
            return alc;
        }


    }

}
