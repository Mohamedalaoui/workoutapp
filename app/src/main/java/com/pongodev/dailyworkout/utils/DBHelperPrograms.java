/*
* Copyright (c) 2015 Pongodev. All Rights Reserved.
*/
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

public class DBHelperPrograms extends SQLiteOpenHelper {



    // application package to store database
    private static String DB_PATH = Utils.ARG_DATABASE_PATH;


    final static String DB_NAME = "db_programs";
    public final static int DB_VERSION = 1;
    public static SQLiteDatabase db;

    private final Context context;

    private final String DAY_ID         = "day_id";

    private final String TABLE_PROGRAMS = "tbl_programs";
    private final String PROGRAM_ID     = "program_id";
    private final String PROGRAM_WORKOUT_ID = "workout_id";
    private final String PROGRAM_NAME   = "name";
    private final String PROGRAM_IMAGE  = "image";
    private final String PROGRAM_TIME   = "time";


    public DBHelperPrograms(Context context) {

        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    // method to create database
    public void createDataBase() throws IOException {

        boolean dbExist = checkDataBase();
        SQLiteDatabase db_Read;


        if (!dbExist) {
            db_Read = this.getReadableDatabase();
            db_Read.close();

            try {
                copyDataBase();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }

    }


    // method to check database
    private boolean checkDataBase() {

        File dbFile = new File(DB_PATH + DB_NAME);

        return dbFile.exists();

    }

    // method to copy database from assets to application package
    private void copyDataBase() throws IOException {

        InputStream myInput = context.getAssets().open(DB_NAME);

        String outFileName = DB_PATH + DB_NAME;

        OutputStream myOutput = new FileOutputStream(outFileName);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        myOutput.flush();
        myOutput.close();
        myInput.close();

    }

    // method to open database
    public void openDataBase() throws SQLException {
        String myPath = DB_PATH + DB_NAME;
        db = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    // method to get day from database
    public ArrayList<ArrayList<Object>> getAllDays() {
        ArrayList<ArrayList<Object>> dataArrays = new ArrayList<>();

        Cursor cursor;
        try {
            String TABLE_DAYS = "tbl_days";
            String DAY_NAME = "day_name";
            cursor = db.query(
                    TABLE_DAYS,
                    new String[]{DAY_ID, DAY_NAME},
                    null, null, null, null, null);

            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                do {
                    ArrayList<Object> dataList = new ArrayList<>();
                    long id = countWorkouts(cursor.getLong(0));

                    dataList.add(cursor.getLong(0));
                    dataList.add(cursor.getString(1));
                    dataList.add(id);

                    dataArrays.add(dataList);
                }

                while (cursor.moveToNext());
            }
            cursor.close();
        } catch (SQLException e) {
            Log.e("DB Error", e.toString());
            e.printStackTrace();
        }

        return dataArrays;
    }

    // method to get number of workout programs on each day
    public int countWorkouts(long id) {
        Cursor dataCount = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PROGRAMS + " WHERE " + DAY_ID + " = " + id, null);
        dataCount.moveToFirst();
        int count = dataCount.getInt(0);
        dataCount.close();
        return count;
    }


    // method to get all workout programs by day from database
    public ArrayList<ArrayList<Object>> getWorkoutListByDay(String selectedID) {
        ArrayList<ArrayList<Object>> dataArrays = new ArrayList<>();

        Cursor cursor;
        try {
            cursor = db.query(
                    TABLE_PROGRAMS,
                    new String[]{PROGRAM_ID, PROGRAM_WORKOUT_ID, PROGRAM_NAME, PROGRAM_IMAGE, PROGRAM_TIME},
                    DAY_ID + " = " + selectedID, null, null, null, null);

            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                do {
                    ArrayList<Object> dataList = new ArrayList<>();
                    dataList.add(cursor.getLong(0));
                    dataList.add(cursor.getLong(1));
                    dataList.add(cursor.getString(2));
                    dataList.add(cursor.getString(3));
                    dataList.add(cursor.getString(4));

                    dataArrays.add(dataList);
                }

                while (cursor.moveToNext());
            }
            cursor.close();
        } catch (SQLException e) {
            Log.e("DB Error", e.toString());
            e.printStackTrace();
        }


        return dataArrays;
    }

    // method to check whether workout program is available or not on day
    public boolean isDataAvailable(int dayID, int workoutID) {
        boolean isAvailable = false;

        Cursor cursor;

        try {
            cursor = db.query(
                    TABLE_PROGRAMS,
                    new String[]{PROGRAM_WORKOUT_ID},
                    DAY_ID + " = " + dayID + " AND " + PROGRAM_WORKOUT_ID + " = " + workoutID,
                    null, null, null, null, null);


            if (cursor.getCount() > 0) {
                isAvailable = true;
            }

            cursor.close();
        } catch (SQLException e) {
            Log.e("DB Error", e.toString());
            e.printStackTrace();
        }

        return isAvailable;
    }

    // method to add workout program to selected day
    public void addData(int workoutID, String name, int dayID, String image, String time, String steps) {
        ContentValues values = new ContentValues();
        values.put(PROGRAM_WORKOUT_ID, workoutID);
        values.put(PROGRAM_NAME, name);
        values.put(DAY_ID, dayID);
        values.put(PROGRAM_IMAGE, image);
        values.put(PROGRAM_TIME, time);
        String PROGRAM_STEPS = "steps";
        values.put(PROGRAM_STEPS, steps);

        try {
            db.insert(TABLE_PROGRAMS, null, values);
        } catch (Exception e) {
            Log.e("DB ERROR", e.toString());
            e.printStackTrace();
        }
    }

    // method to delete workout program from day
    public void deleteData(String programID) {
        // ask the database manager to delete the row of given id
        try {
            db.delete(TABLE_PROGRAMS, PROGRAM_ID + "=" + programID, null);
        } catch (Exception e) {
            Log.e("DB ERROR", e.toString());
            e.printStackTrace();
        }
    }

    // method to get location data from database
    public String getWorkoutId(String programID){

        String workoutId = "0";
        Cursor cursor;

        try{
            cursor = db.query(
                    TABLE_PROGRAMS,
                    new String[] {PROGRAM_WORKOUT_ID},
                    PROGRAM_ID + " = " + programID,
                    null, null, null, null, null);

            if (cursor != null) cursor.moveToFirst();

            workoutId = cursor.getString(0);

            cursor.close();
        }
        catch (SQLException e)
        {
            Log.e("DB ERROR", e.toString());
            e.printStackTrace();
        }

        return workoutId;
    }

    public void checkDBPrograms() {
        /**
         * when this app's installed at the first time, code below will
         * copy database stored in assets to
         * /data/data/com.daily.workout.app/databases/
         */
        try {
            createDataBase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }

        // then, the database will be open to use
        try {
            openDataBase();
        } catch (SQLException sqle) {
            throw sqle;
        }
    }
}
