package com.chenhao.youbike.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.chenhao.youbike.model.Bike;

@Database(entities = {Bike.class},version = 1,exportSchema = false)
public abstract class BikeDatabase extends RoomDatabase {
    public abstract BikeDao bikeDao();
    private static BikeDatabase instance = null;
    public static BikeDatabase getInstance(Context context){
        if(instance == null){
            instance = Room.databaseBuilder(context,BikeDatabase.class,"bike.db").build();
        }
        return instance;
    }


}
