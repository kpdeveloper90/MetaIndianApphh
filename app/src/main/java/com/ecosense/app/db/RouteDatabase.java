package com.ecosense.app.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.ecosense.app.db.dao.PoiPointDao;
import com.ecosense.app.db.dao.RoutePointDao;
import com.ecosense.app.db.models.DateConverter;
import com.ecosense.app.db.models.PoiPoint;
import com.ecosense.app.db.models.RoutePoint;

/**
 * <h1>Room DB provider for the route_db</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
@Database(entities = {RoutePoint.class, PoiPoint.class}, exportSchema = false, version = 2)
@TypeConverters({DateConverter.class})
public abstract class RouteDatabase extends RoomDatabase {
    public abstract RoutePointDao routePointDao();
    public abstract PoiPointDao poiPointDao();

    private static final String DB_NAME = "route_dbf";
    private static volatile RouteDatabase instance = null;

    public static RouteDatabase getInstance(@NonNull final Context context) {
        if (instance == null) {
            synchronized (RouteDatabase.class) {
                if (instance == null)
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                            RouteDatabase.class,
                            DB_NAME)
                            .fallbackToDestructiveMigration()
                            .build();
            }
        }
        return instance;
    }
}
