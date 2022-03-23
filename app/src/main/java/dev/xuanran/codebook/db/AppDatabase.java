package dev.xuanran.codebook.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import dev.xuanran.codebook.bean.CardData;
import dev.xuanran.codebook.dao.UserDataDao;

@Database(entities = {CardData.class}, version = 1,exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public static final String TAG = AppDatabase.class.getSimpleName();
    public static final Object lock = new Object();
    public static final String DATABASES_NAME = "UserData";
    public static AppDatabase appDatabase;

    public abstract UserDataDao userDataDao();

    public static AppDatabase getInstance(Context ctx) {
        if (appDatabase == null) {
            synchronized (lock) {
                appDatabase = Room.databaseBuilder(ctx, AppDatabase.class, DATABASES_NAME).build();
            }
        }
        return appDatabase;
    }

}
