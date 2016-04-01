package record.jaikrishnan.com.lifesaver;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by JAI KRISHNAN on 31-03-2016.
 */
public class DB {

    public static final String KEY_ID = "id";
    public static final String KEY_CONTACTS = "phone";
    private static final String SELECT_SQL = "SELECT * FROM contacts";
    private static final String DATABASE_NAME = "ContactsDB";
    private static final String DATABASE_TABLE = "contacts";
    private static final int DATABASE_VERSION = 1;

    private DBHelper ourHelper;
    private final Context ourContext;
    private SQLiteDatabase ourDatabase;
    private String data;

    public long createEntry(String num1) {
        ContentValues cv = new ContentValues();
        cv.put("phone",num1);
        //cv.put(KEY_CONTACTS, num2);
        return ourDatabase.insert(DATABASE_TABLE,null,cv);
    }

    public void clean(){
        ourDatabase.execSQL("delete from "+ DATABASE_TABLE);
    }

    public String getData() {
        Cursor c = ourDatabase.rawQuery(SELECT_SQL,null);
        c.moveToFirst();
        //int id = c.getColumnIndex(0);
        String result = "";

        for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
            result = result + c.getString(0)+" ";
            //System.out.println(result);
        }
        return result;
    }

    private static class DBHelper extends SQLiteOpenHelper{

        public DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            //db.execSQL("DROP TABLE IF EXISTS "+ DATABASE_TABLE);
            db.execSQL("CREATE TABLE " + DATABASE_TABLE + "(" +
                            KEY_CONTACTS + " TEXT);"
            );

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS "+ DATABASE_TABLE);
            onCreate(db);
        }

    }

    public DB(Context c){
        ourContext = c;
    }

    public DB open(){
        ourHelper = new DBHelper(ourContext);
        ourDatabase = ourHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        ourHelper.close();
    }
}
