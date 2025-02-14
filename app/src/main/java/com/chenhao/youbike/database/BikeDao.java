package com.chenhao.youbike.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.chenhao.youbike.model.Bike;


import java.util.List;
@Dao
public interface BikeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Bike bike);

    @Query("select * from Bike")
    List<Bike> getAll();
    @Query("select * from Bike WHERE sno = :id")
    Bike findByBike(String id);
    @Delete
    void delete(Bike bike);

}