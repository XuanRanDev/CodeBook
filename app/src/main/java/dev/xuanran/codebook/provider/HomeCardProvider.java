package dev.xuanran.codebook.provider;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.entity.node.BaseNode;
import com.chad.library.adapter.base.provider.BaseNodeProvider;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import dev.xuanran.codebook.R;
import dev.xuanran.codebook.bean.CardData;

/**
 * Created By XuanRan on 2022/3/19
 */
public class HomeCardProvider extends BaseNodeProvider {
    @Override
    public int getItemViewType() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.list_cardview;
    }

    @Override
    public void convert(@NonNull BaseViewHolder baseViewHolder, BaseNode baseNode) {
        CardData cardData = (CardData) baseNode;
        baseViewHolder.setText(R.id.list_cardview_title, cardData.getCardName());
    }
}
