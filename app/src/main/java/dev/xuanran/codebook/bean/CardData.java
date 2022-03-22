package dev.xuanran.codebook.bean;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.chad.library.adapter.base.entity.node.BaseNode;

import java.util.Date;
import java.util.List;

/**
 * Created By XuanRan on 2022/3/19
 */
@Entity(tableName = "UserData")
public class CardData extends BaseNode {
    @PrimaryKey(autoGenerate = true)
    private Integer cardId;
    @ColumnInfo(name = "appName")
    private String appName;
    @ColumnInfo(name = "accountID")
    private String accountId;
    @ColumnInfo(name = "password")
    private String password;
    @ColumnInfo(name = "createDate")
    private Date createDate;
    @ColumnInfo(name = "updateDate")
    private Date updateDate;

    public CardData(Integer cardId, String appName, String accountId, String password) {
        this.cardId = cardId;
        this.appName = appName;
        this.accountId = accountId;
        this.password = password;
    }

    public Integer getCardId() {
        return cardId;
    }

    public void setCardId(Integer cardId) {
        this.cardId = cardId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    @Nullable
    @Override
    public List<BaseNode> getChildNode() {
        return null;
    }
}
