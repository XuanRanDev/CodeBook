package dev.xuanran.codebook.db;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

import dev.xuanran.codebook.bean.AccountDao;
import dev.xuanran.codebook.bean.AccountEntity;

@Database(entities = {AccountEntity.class}, version = 1)
public abstract class AccountDatabase extends RoomDatabase {
    private static AccountDatabase instance;

    public abstract AccountDao accountDao();

    public static synchronized AccountDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AccountDatabase.class, "account_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
