package dev.xuanran.codebook.bean.account;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AccountDao {
    @Insert
    void insert(AccountEntity account);

    @Update
    void update(AccountEntity account);

    @Delete
    void delete(AccountEntity account);

    @Query("SELECT * FROM account_table WHERE id = :id")
    LiveData<AccountEntity> getAccountById(int id);

    @Query("SELECT * FROM account_table WHERE app_name LIKE '%' || :query || '%' OR username LIKE '%' || :query || '%'")
    LiveData<List<AccountEntity>> selectAccountByNameOrAccount(String query);
}