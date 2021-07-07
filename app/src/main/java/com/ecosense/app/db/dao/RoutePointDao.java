package com.ecosense.app.db.dao;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import com.ecosense.app.db.models.MetaRouteInfo;
import com.ecosense.app.db.models.RoutePoint;
import com.ecosense.app.db.models.RoutePointStatus;

/**
 * <h1>DAO interface for {@link RoutePoint}</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
@Dao
public interface RoutePointDao {
    @Query("SELECT * FROM route_point ORDER BY timestamp DESC")
    List<RoutePoint> getAllRoutePoints();

    @Query("SELECT * FROM route_point ORDER BY timestamp DESC LIMIT :count")
    List<RoutePoint> getRoutePoints(final int count);

    @Query("SELECT * FROM route_point ORDER BY timestamp DESC LIMIT 1")
    RoutePoint getLastRoutePoint();

    @Query("SELECT * FROM route_point WHERE isUploaded=0")
    List<RoutePoint> getAllUnSyncedRoutePoints();

    @Query("SELECT * FROM route_point WHERE isUploaded=0 LIMIT :count")
    List<RoutePoint> getUnSyncedRoutePoints(final int count);

    @Query("SELECT * FROM route_point WHERE isUploaded=0 LIMIT 1")
    RoutePoint getLastUnSyncedRoutePoint();

    @Query("SELECT * FROM route_point WHERE routeClientId=:routeClientId LIMIT 1")
    RoutePoint getRoutePoint(@NonNull final String routeClientId);

    @Query("SELECT * FROM route_point WHERE isUploaded=0 AND routeClientId=:routeClientId ORDER BY timestamp ASC")
    List<RoutePoint> getAllRoutePoints(@NonNull final String routeClientId);

    @Query("SELECT * FROM route_point WHERE isUploaded=0 AND vehicleId=:vehicleId AND routeName=:routeName ")
    List<RoutePoint> getAllUnSyncedRoutePoints(@NonNull final String vehicleId, @NonNull final String routeName);

    @Query("SELECT * FROM route_point WHERE isUploaded=0 AND vehicleId=:vehicleId AND routeName=:routeName LIMIT :count")
    List<RoutePoint> getUnSyncedRoutePoints(final int count, @NonNull final String vehicleId, @NonNull final String routeName);

    @Query("SELECT * FROM route_point WHERE isUploaded=0 AND vehicleId=:vehicleId AND routeName=:routeName LIMIT 1")
    RoutePoint getLastUnSyncedRoutePoint(@NonNull final String vehicleId, @NonNull final String routeName);

    @Query("SELECT DISTINCT * FROM route_point WHERE isUploaded=0 GROUP BY vehicleId, routeName ORDER BY timestamp ASC")
    List<MetaRouteInfo> getUnSyncedRoutes();

    @Query("SELECT COUNT(id) FROM route_point WHERE isUploaded=1")
    int getAllSyncedRoutePointsCount();

    @Query("SELECT COUNT(id) FROM route_point WHERE isUploaded=0")
    int getAllUnSyncedRoutePointsCount();

    @Query("SELECT COUNT(id) FROM route_point WHERE routeId = :routeId AND isUploaded=0")
    int getAllUnSyncedRoutePointsCount(@NonNull final String routeId);

    @Query("SELECT COUNT(id) FROM route_point")
    int getTotalRoutePointsCount();

    @Query("UPDATE route_point SET isUploaded = 1 WHERE routeClientId=:routeClientId")
    int updateRouteUploadedStatus(@NonNull final String routeClientId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertRoutePoint(@NonNull final RoutePoint routePoint);

    @Update
    int updateRoutePoint(@NonNull final RoutePoint routePoint);

    @Update(entity = RoutePoint.class)
    int updateRoutePointStatus(@NonNull final RoutePointStatus routePointStatus);

    @Update(entity = RoutePoint.class)
    int updateRoutePointsStatus(@NonNull final List<RoutePointStatus> routePointStatusList);

    @Delete
    int deleteRoutePoint(@NonNull final RoutePoint routePoint);

    @Delete
    int deleteRoutePoints(@NonNull final List<RoutePoint> routePoints);

    @Query("DELETE FROM route_point WHERE isUploaded=1")
    int deleteSyncedRoutePoints();
}
