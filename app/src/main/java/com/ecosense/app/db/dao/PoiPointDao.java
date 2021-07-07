package com.ecosense.app.db.dao;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import com.ecosense.app.db.models.PoiPoint;

/**
 * <h1>DAO interface for {@link PoiPoint}</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
@Dao
public interface PoiPointDao {
    @Query("SELECT * FROM poi_point WHERE routeId=:routeId ORDER BY timestamp DESC LIMIT 1")
    PoiPoint getLastPoint(@NonNull final String routeId);

    @Query("SELECT * FROM poi_point WHERE routeId=:routeId")
    List<PoiPoint> getAllPoints(@NonNull final String routeId);

    @Query("SELECT COUNT(id) FROM poi_point WHERE routeId=:routeId")
    int getCount(@NonNull final String routeId);

    @Query("DELETE FROM poi_point WHERE id=:id ")
    int deletePoint(final long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertPoint(@NonNull final PoiPoint poiPoint);

    @Update
    int updatePoint(@NonNull final PoiPoint poiPoint);

    @Delete
    int deletePoint(@NonNull final PoiPoint poiPoint);

    @Delete
    int deletePoints(@NonNull final List<PoiPoint> poiPoints);
}
