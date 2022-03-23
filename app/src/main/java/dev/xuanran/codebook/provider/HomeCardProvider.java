package dev.xuanran.codebook.provider;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.PopupMenu;

import com.chad.library.adapter.base.entity.node.BaseNode;
import com.chad.library.adapter.base.provider.BaseNodeProvider;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

import dev.xuanran.codebook.MainActivity;
import dev.xuanran.codebook.R;
import dev.xuanran.codebook.bean.CardData;
import dev.xuanran.codebook.dao.UserDataDao;
import dev.xuanran.codebook.db.AppDatabase;
import dev.xuanran.codebook.util.AesUtil;
import dev.xuanran.codebook.util.AppExecutors;
import dev.xuanran.codebook.util.ClipboardUtil;

/**
 * Created By XuanRan on 2022/3/19
 */
public class HomeCardProvider extends BaseNodeProvider {
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
        setLayoutContent(baseViewHolder, baseNode);
        Button bn = baseViewHolder.getView(R.id.list_cardView_view);
        bn.setOnClickListener(view -> showContent(view, baseNode));
    }

    /**
     * 设置卡片视图内容
     *
     * @param baseViewHolder ViewHolder
     * @param node           BaseNode 子类
     */
    private void setLayoutContent(BaseViewHolder baseViewHolder, BaseNode node) {
        CardData cardData = (CardData) node;
        baseViewHolder.setText(R.id.list_cardView_title, cardData.getAppName());
        baseViewHolder.setText(R.id.list_cardView_id, "# " + cardData.getCardId());
        baseViewHolder.setText(R.id.list_cardView_createDate, "创建时间：" + new SimpleDateFormat(DATE_FORMAT, Locale.CHINA).format(cardData.getCreateDate()));
    }

    /**
     * 显示卡片的更多菜单
     *
     * @param view 父View
     */
    private void showMoreMenu(View view, CardData data, AlertDialog dialog) {
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.getMenuInflater().inflate(R.menu.dialog_content_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.dialog_content_menu_delete:
                    Snackbar.make(view, "数据删除后不可恢复，是否继续？", 10000).setAction("确定", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            deleteCardData(view, data, dialog);
                        }
                    }).show();
            }
            return true;
        });
        popup.show();
    }

    /**
     * 删除指定卡片
     *
     * @param data 卡片对象
     */
    @SuppressLint("NotifyDataSetChanged")
    private void deleteCardData(View ctx, CardData data, AlertDialog dialog) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            UserDataDao dao = AppDatabase.getInstance(ctx.getContext()).userDataDao();
            dao.deleteData(data);
        });
        Snackbar.make(ctx, "已删除,请手动刷新数据", 3000).show();

    }

    /**
     * 弹出卡片视图对话框
     *
     * @param view 触发视图
     * @param node CardData 对象
     */
    private void showContent(View view, BaseNode node) {
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
        appName.setText(data.getAppName());
        String accountId = AesUtil.decrypt(data.getAccountId(), MainActivity.AES_PASSWORD);
        Objects.requireNonNull(accountID.getEditText()).setText(accountId);
        String dataPassword = AesUtil.decrypt(data.getPassword(), MainActivity.AES_PASSWORD);
        Objects.requireNonNull(password.getEditText()).setText(dataPassword);

        builder.setView(contentView);
        AlertDialog dialog = builder.create();
        if (!dialog.isShowing()) {
            dialog.show();
        }

        more.setOnClickListener(view13 -> showMoreMenu(view13, data, dialog));

        copyAccountID.setOnClickListener(view1 -> {
            ClipboardUtil.setTextToClipboard(view1.getContext(), accountId);
            Snackbar.make(view1, "已复制，请谨防恶意应用读取", 50000).setBackgroundTint(Color.RED).setAction("OK", null).show();
        });
        copyPassword.setOnClickListener(view12 -> {
            ClipboardUtil.setTextToClipboard(view12.getContext(), dataPassword);
            Snackbar.make(view12, "已复制，请谨防恶意应用读取", 50000).setBackgroundTint(Color.RED).setAction("OK", null).show();
        });
    }
}
