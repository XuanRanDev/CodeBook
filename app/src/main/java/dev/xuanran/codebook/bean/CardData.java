package dev.xuanran.codebook.bean;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.entity.node.BaseNode;

import java.util.List;

/**
 * Created By XuanRan on 2022/3/19
 */
public class CardData extends BaseNode {
    private Integer cardId;
    private String cardName;

    public CardData(Integer cardId, String cardName) {
        this.cardId = cardId;
        this.cardName = cardName;
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

    @Nullable
    @Override
    public List<BaseNode> getChildNode() {
        return null;
    }
}
