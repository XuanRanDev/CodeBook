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
    private long createDate;
    @ColumnInfo(name = "updateDate")
    private long updateDate;

    public CardData(String appName, String accountId, String password, long createDate) {
        this.appName = appName;
        this.accountId = accountId;
        this.password = password;
        this.createDate = createDate;
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

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public long getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(long updateDate) {
        this.updateDate = updateDate;
    }

    @Nullable
    @Override
    public List<BaseNode> getChildNode() {
        return null;
    }

    @Override
    public String toString() {
        return "CardData{" +
                "cardId=" + cardId +
                ", appName='" + appName + '\'' +
                ", accountId='" + accountId + '\'' +
                ", password='" + password + '\'' +
                ", createDate=" + createDate +
                ", updateDate=" + updateDate +
                '}';
    }
}
