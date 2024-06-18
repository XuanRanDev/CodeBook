package dev.xuanran.codebook.bean.account;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "account_table")
public class AccountEntity {
    @PrimaryKey(autoGenerate = true)
    private Long id;

    @ColumnInfo(name = "app_name")
    private String appName;

    @ColumnInfo(name = "username")
    private String username;
    private String password;

    private String remark;

    @ColumnInfo(name = "create_time")
    private Date createTime;

    public AccountEntity() {
    }

    public AccountEntity(String appName, String username, String password, String remark, Date createTime) {
        this.appName = appName;
        this.username = username;
        this.password = password;
        this.remark = remark;
        this.createTime = createTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return appName + "," + username + "," + password + "," + remark + "," + createTime.getTime();
    }

    public static AccountEntity fromString(String data) {
        String[] fields = data.split(",");
        AccountEntity account = new AccountEntity();
        account.setAppName(fields[0]);
        account.setUsername(fields[1]);
        account.setPassword(fields[2]);
        account.setRemark(fields[3]);
        account.setCreateTime(new Date(Long.parseLong(fields[4])));
        return account;
    }
}