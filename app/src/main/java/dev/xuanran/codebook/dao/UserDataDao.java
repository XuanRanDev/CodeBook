package dev.xuanran.codebook.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import dev.xuanran.codebook.bean.CardData;

/**
 * Created By XuanRan on 2022/3/22
 */
@Dao
public interface UserDataDao {
    @Query("select * from UserData")
    List<CardData> getAll();

    @Query("select * from UserData where appName like '%'+:queryCondition+'%'")
    List<CardData> getAllByName(String queryCondition);

    @Query("select * from UserData where appName like :appName")
    CardData findDataByName(String appName);

    @Insert
    void insertData(CardData cardData);

    @Delete
    void deleteData(CardData cardData);
}
