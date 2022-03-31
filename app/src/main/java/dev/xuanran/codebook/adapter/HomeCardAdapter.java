package dev.xuanran.codebook.adapter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.PopupMenu;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.entity.node.BaseNode;
import com.chad.library.adapter.base.module.DraggableModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
 * Created By XuanRan on 2022/3/24
 */
public class HomeCardAdapter extends BaseQuickAdapter<CardData, BaseViewHolder> implements Filterable, DraggableModule {
    private static final String DATE_FORMAT = "yyyy.MM.dd";
    private List<CardData> mSourceList = new ArrayList<>();
    private List<CardData> mFilterList = new ArrayList<>();

    public HomeCardAdapter(int layoutResId) {
        super(layoutResId);
    }

    public HomeCardAdapter(int layoutResId, @Nullable List<CardData> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, CardData cardData) {
        setLayoutContent(baseViewHolder, cardData);
        Button bn = baseViewHolder.getView(R.id.list_cardView_view);
        bn.setOnClickListener(view -> showContent(view, cardData));
    }

    @Override
    public void setList(@Nullable Collection<? extends CardData> list) {
        mSourceList = (List<CardData>) list;
        //这里需要初始化filterList
        mFilterList = (List<CardData>) list;
        super.setList(list);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            //执行过滤操作
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    //没有过滤的内容，则使用源数据
                    mFilterList = mSourceList;
                } else {
                    List<CardData> cardData = new ArrayList<>();
                    for (int i = 0; i < mSourceList.size(); i++) {
                        if (mSourceList.get(i).getAppName().contains(charString)){
                            cardData.add(mSourceList.get(i));
                        }
                    }
                    mFilterList = cardData;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = mFilterList;
                return filterResults;
            }
            //把过滤后的值返回出来
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mFilterList = (ArrayList<CardData>) filterResults.values;
                if (mFilterList.size() == 0){
                    setEmptyView(R.layout.loading);
                    setUseEmpty(true);
                }else if (isUseEmpty()){
                    setUseEmpty(false);
                }
                notifyDataSetChanged();
            }
        };
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
        baseViewHolder.setText(R.id.list_cardView_createDate, new SimpleDateFormat(DATE_FORMAT, Locale.CHINA).format(cardData.getCreateDate()));
        baseViewHolder.setText(R.id.list_cardView_tag_text,getTagClass(cardData.getTag()));

    }

    /**
     * 根据TAG值返回具体类型
     * @param tag tag code
     * @return 字符串类的文本
     */
    private String getTagClass(Integer tag) {
        if (tag == null) return null;
        if (tag == 1){ // 银行卡模式
            return getString(R.string.idCard);
        }
        if (tag == 2){
            return getString(R.string.bankCard);
        }
        if (tag == 3){
            return getString(R.string.harvestAddress);
        }
        return "账号和密码";
    }

    private String getString(int resId) {
        return getContext().getResources().getString(resId);
    }

    /**
     * 显示卡片的更多菜单
     *
     * @param view 父View
     */
    private void showMoreMenu(View view, CardData data) {
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.getMenuInflater().inflate(R.menu.dialog_content_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.dialog_content_menu_delete) {
                Snackbar.make(view, "数据删除后不可恢复，是否继续？", 10000).setAction("确定", view1 -> deleteCardData(view1, data)).show();
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
    private void deleteCardData(View ctx, CardData data) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            UserDataDao dao = AppDatabase.getInstance(ctx.getContext()).userDataDao();
            dao.deleteData(data);
            Snackbar.make(ctx, "已删除,将在下次启动时彻底移除", 5000).show();
        });

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

        if (password.getEditText().getText().toString().equals("")){
            password.setVisibility(View.GONE);
            copyPassword.setVisibility(View.GONE);
        }

        if (data.getTag() == 1) {
            accountID.setHint(getString(R.string.cardID));
            copyAccountID.setText(getString(R.string.copyCardID));
        }
        if (data.getTag() == 2 ){
            accountID.setHint(getString(R.string.BankID));
            password.setHint(getString(R.string.BackPassword));
            copyAccountID.setText(getString(R.string.copyBankID));
            copyPassword.setText(getString(R.string.CopyPassword));
        }
        if (data.getTag() == 3) {
            accountID.setHint(getString(R.string.address));
            copyAccountID.setText(getString(R.string.copyAddress));
            copyPassword.setVisibility(View.GONE);
            password.setVisibility(View.GONE);
        }

        more.setOnClickListener(view13 -> showMoreMenu(view13, data));

        copyAccountID.setOnClickListener(view1 -> {
            ClipboardUtil.setTextToClipboard(view1.getContext(), accountId);
            Snackbar.make(view1, "已复制，请谨防恶意应用读取", 50000).setBackgroundTint(Color.RED).setAction("OK", null).show();
        });
        copyPassword.setOnClickListener(view12 -> {
            ClipboardUtil.setTextToClipboard(view12.getContext(), dataPassword);
            Snackbar.make(view12, "已复制，请谨防恶意应用读取", 50000).setBackgroundTint(Color.RED).setAction("OK", null).show();
        });

        // Hide view...
    }


    @Override
    public int getItemCount() {
       return mFilterList.size();
    }
/*
    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }*/
}
