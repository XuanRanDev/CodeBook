package dev.xuanran.codebook.provider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;

import com.chad.library.adapter.base.entity.node.BaseNode;
import com.chad.library.adapter.base.provider.BaseNodeProvider;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Locale;

import dev.xuanran.codebook.R;
import dev.xuanran.codebook.bean.CardData;

/**
 * Created By XuanRan on 2022/3/19
 */
public class HomeCardProvider extends BaseNodeProvider implements View.OnClickListener {
    private static final String DATE_FORMAT = "yyyy.MM.dd";

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
        setLayoutContent(baseViewHolder,baseNode);
        Button bn = baseViewHolder.getView(R.id.list_cardView_view);
        bn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showContent(view,baseNode);
            }
        });
    }

    /**
     * 设置卡片视图内容
     * @param baseViewHolder ViewHolder
     * @param node BaseNode 子类
     */
    private void setLayoutContent(BaseViewHolder baseViewHolder, BaseNode node) {
        CardData cardData = (CardData) node;
        baseViewHolder.setText(R.id.list_cardView_title,cardData.getCardName());
        baseViewHolder.setText(R.id.list_cardView_id,"# " + cardData.getCardId());
        baseViewHolder.setText(R.id.list_cardView_createDate,new SimpleDateFormat(DATE_FORMAT, Locale.CHINA).format(cardData.getCreateDate()));
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.content_view_dialog_more) {
            showMoreMenu(view);
        }
    }

    /**
     * 显示卡片的更多菜单
     * @param view 父View
     */
    private void showMoreMenu(View view) {
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.getMenuInflater().inflate(R.menu.dialog_content_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                Toast.makeText(view.getContext(), "Click:" + item.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        popup.show();
    }

    /**
     * 弹出卡片视图对话框
     * @param view 触发视图
     * @param node CardData 对象
     */
    private void showContent(View view,BaseNode node) {
        CardData data = (CardData) node;
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(view.getContext(), R.style.ThemeOverlayAppMaterialAlertDialog);
        View contentView = LayoutInflater.from(view.getContext()).inflate(R.layout.content_view_dialog, null);
        // View
        AppCompatImageView more = contentView.findViewById(R.id.content_view_dialog_more);
        TextView appName = contentView.findViewById(R.id.content_view_dialog_title);
        TextInputLayout accountID = contentView.findViewById(R.id.content_view_dialog_accountID);
        TextInputLayout password = contentView.findViewById(R.id.content_view_dialog_password);
        Button copyAccountID = contentView.findViewById(R.id.content_view_dialog_copyAccountID);
        Button copyPassword = contentView.findViewById(R.id.content_view_dialog_copyPassword);

        //setValue
        appName.setText(data.getCardName());
        accountID.getEditText().setText(data.getAccountId());
        password.getEditText().setText(data.getPassword());

        more.setOnClickListener(this);
        builder.setView(contentView);
        builder.show();
    }
}
