package dev.xuanran.codebook.bean;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.entity.node.BaseNode;

import java.util.Date;
import java.util.List;

/**
 * Created By XuanRan on 2022/3/19
 */
public class CardData extends BaseNode {
    private Integer cardId;
    private String cardName;
    private String accountId;
    private String password;
    private Date createDate;
    private Date updateDate;

    public CardData(Integer cardId, String cardName, String accountId, String password) {
        this.cardId = cardId;
        this.cardName = cardName;
        this.accountId = accountId;
        this.password = password;
    }

    public Integer getCardId() {
        return cardId;
    }

    public void setCardId(Integer cardId) {
        this.cardId = cardId;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
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
