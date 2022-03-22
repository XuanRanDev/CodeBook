package dev.xuanran.codebook.dao;

import android.service.autofill.UserData;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import dev.xuanran.codebook.bean.CardData;

/**
 * Created By XuanRan on 2022/3/22
 */
public interface UserDataDao {
    @Query("select * from UserData")
    List<CardData> getAll();

    @Query("select * from UserData where cardId in (:appNames)")
    List<CardData> getAllByID(String[] appNames);

    @Query("select * from UserData where appName like :appName")
    CardData findUserByName(String appName);

    @Insert
    void insertUser(UserData... userData);

    @Delete
    void deleteUser(UserData user);
}
