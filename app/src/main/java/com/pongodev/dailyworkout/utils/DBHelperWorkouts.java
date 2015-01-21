package com.pongodev.dailyworkout.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by keong on 12/23/2014.
 */
public class DBHelperWorkouts extends SQLiteOpenHelper {
    // path of database when app installed on device
    // if you change your package name, make sure to also change this package name
    private static String DB_PATH = "/data/data/com.pongodev.dailyworkout/databases/";

    // create database name and version
    private final static String DB_NAME = "db_workouts";
    public final static int DB_VERSION = 1;
    public static SQLiteDatabase db;
    private final Context context;

    private final String TABLE_CATEGORIES   = "tbl_categories";
    private final String TABLE_IMAGES       = "tbl_images";
    private final String TABLE_WORKOUTS     = "tbl_workouts";
    private final String CATEGORY_ID        = "category_id";
    private final String CATEGORY_NAME      = "category_name";
    private final String CATEGORY_IMAGE     = "category_image";
    private final String IMAGES_ID          = "image_id";
    private final String IMAGES_WORKOUT_ID  = "workout_id";
    private final String IMAGES_FILE        = "image";
    private final String WORKOUTS_ID        = "workout_id";
    private final String WORKOUTS_NAME      = "name";
    private final String WORKOUTS_CATEGORY_ID= "category_id";
    private final String WORKOUTS_IMAGE     = "image";
    private final String WORKOUTS_TIME      = "time";
    private final String WORKOUTS_STEPS     = "steps";

    public DBHelperWorkouts(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    // method to create database
    public void createDataBase() throws IOException {

        boolean dbExist = checkDataBase();
        SQLiteDatabase db_Read = null;

        // if database exist delete database and copy the new one
        if(dbExist){
            // do nothing - database already exist
        }else{
            db_Read = this.getReadableDatabase();
            db_Read.close();

            try {
                copyDataBase();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }

    }

    // method to check database on path
    private boolean checkDataBase(){
        File dbFile = new File(DB_PATH + DB_NAME);
        return dbFile.exists();
    }

    // method to copy database from app to db path
    private void copyDataBase() throws IOException{

        InputStream myInput = context.getAssets().open(DB_NAME);

        String outFileName = DB_PATH + DB_NAME;

        OutputStream myOutput = new FileOutputStream(outFileName);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0){
            myOutput.write(buffer, 0, length);
        }

        myOutput.flush();
        myOutput.close();
        myInput.close();

    }

    // method to open database and read it
    public void openDataBase() throws SQLException {
        String myPath = DB_PATH + DB_NAME;
        db = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    // close database after it is used
    @Override
    public void close() {
        db.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {}

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}


    // method to get category data from database
    public ArrayList<ArrayList<Object>> getAllCategories(){
        ArrayList<ArrayList<Object>> dataArrays = new ArrayList<ArrayList<Object>>();

        Cursor cursor = null;

        try{
            cursor = db.query(
                    TABLE_CATEGORIES,
                    new String[]{CATEGORY_ID, CATEGORY_NAME, CATEGORY_IMAGE},
                    null, null, null, null, null);

            cursor.moveToFirst();
            if (!cursor.isAfterLast()){
                do{
                    ArrayList<Object> dataList = new ArrayList<Object>();
                    long id = countWorkouts(cursor.getLong(0));

                    dataList.add(cursor.getLong(0));
                    dataList.add(cursor.getString(1));
                    dataList.add(cursor.getString(2));
                    dataList.add(id);

                    dataArrays.add(dataList);
                }

                while (cursor.moveToNext());
            }
            cursor.close();
        }catch (SQLException e){
            Log.e("DB Error", e.toString());
            e.printStackTrace();
        }

        return dataArrays;
    }


    public int countWorkouts(long id) {
        Cursor dataCount = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_WORKOUTS +" WHERE "+CATEGORY_ID +" = "+id , null);
        dataCount.moveToFirst();
        int count = dataCount.getInt(0);
        dataCount.close();
        return count;
    }

    // method to get all location data from database
    public ArrayList<ArrayList<Object>> getWorkoutListByCategory(String selectedID){
        ArrayList<ArrayList<Object>> dataArrays = new ArrayList<ArrayList<Object>>();

        Cursor cursor = null;

        try{
            cursor = db.query(
                    TABLE_WORKOUTS,
                    new String[]{WORKOUTS_ID, WORKOUTS_NAME, WORKOUTS_IMAGE, WORKOUTS_TIME, WORKOUTS_STEPS, CATEGORY_ID},
                    CATEGORY_ID +" = "+selectedID, null, null, null, null);

            cursor.moveToFirst();
            if (!cursor.isAfterLast()){
                do{
                    ArrayList<Object> dataList = new ArrayList<Object>();

                    dataList.add(cursor.getLong(0));
                    dataList.add(cursor.getString(1));
                    dataList.add(cursor.getString(2));
                    dataList.add(cursor.getString(3));
                    dataList.add(cursor.getString(4));
                    dataList.add(cursor.getString(5));
                    dataArrays.add(dataList);
                }

                while (cursor.moveToNext());
            }
            cursor.close();
        }catch (SQLException e){
            Log.e("DB Error", e.toString());
            e.printStackTrace();
        }


        return dataArrays;
    }

    // method to get location data from database
    public ArrayList<Object> getDetail(String selectedID){

        ArrayList<Object> rowArray = new ArrayList<Object>();
        Cursor cursor;

        try{
            cursor = db.query(
                    TABLE_WORKOUTS,
                    new String[] {WORKOUTS_ID, WORKOUTS_NAME, WORKOUTS_IMAGE, WORKOUTS_TIME, WORKOUTS_STEPS},
                    WORKOUTS_ID + " = " + selectedID,
                    null, null, null, null, null);

            cursor.moveToFirst();

            if (!cursor.isAfterLast()){
                do{
                    rowArray.add(cursor.getLong(0));
                    rowArray.add(cursor.getString(1));
                    rowArray.add(cursor.getString(2));
                    rowArray.add(cursor.getString(3));
                    rowArray.add(cursor.getString(4));
                }
                while (cursor.moveToNext());
            }

            cursor.close();
        }
        catch (SQLException e)
        {
            Log.e("DB ERROR", e.toString());
            e.printStackTrace();
        }

        return rowArray;
    }


    // method to get all location data from database
    public ArrayList<ArrayList<Object>> getImages(String workoutID){
        ArrayList<ArrayList<Object>> dataArrays = new ArrayList<ArrayList<Object>>();

        Cursor cursor = null;

        try{
            cursor = db.query(
                    TABLE_IMAGES,
                    new String[]{WORKOUTS_IMAGE},
                    WORKOUTS_ID +" = "+workoutID, null, null, null, null);

            cursor.moveToFirst();
            if (!cursor.isAfterLast()){
                do{
                    ArrayList<Object> dataList = new ArrayList<Object>();

                    dataList.add(cursor.getString(0));

                    dataArrays.add(dataList);
                }

                while (cursor.moveToNext());
            }
            cursor.close();
        }catch (SQLException e){
            Log.e("DB Error", e.toString());
            e.printStackTrace();
        }


        return dataArrays;
    }

    public void checkDBWorkouts(){
        /**
         * when this app's installed at the first time, code below will
         * copy database stored in assets to
         * /data/data/com.daily.workout.app/databases/
         */
        try {
        createDataBase();
        }catch(IOException ioe){
            throw new Error("Unable to create database");
        }

        // then, the database will be open to use
        try{
            openDataBase();
        }catch(SQLException sqle){
            throw sqle;
        }
    }

}