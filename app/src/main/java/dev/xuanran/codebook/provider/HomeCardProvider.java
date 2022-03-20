package dev.xuanran.codebook.provider;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.PopupMenu;

import com.chad.library.adapter.base.entity.node.BaseNode;
import com.chad.library.adapter.base.provider.BaseNodeProvider;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import dev.xuanran.codebook.R;
import dev.xuanran.codebook.bean.CardData;

/**
 * Created By XuanRan on 2022/3/19
 */
public class HomeCardProvider extends BaseNodeProvider implements View.OnClickListener {
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
        Button button = baseViewHolder.getView(R.id.list_cardview_button);

        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.content_view_dialog_more) {
            showMoreMenu(view);
        }

        if (view.getId() == R.id.list_cardview_button) {
            showContent(view);
        }
    }

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

    private void showContent(View view) {

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(view.getContext(), R.style.ThemeOverlayAppMaterialAlertDialog);
        View contentView = LayoutInflater.from(view.getContext()).inflate(R.layout.content_view_dialog, null);
        AppCompatImageView more = contentView.findViewById(R.id.content_view_dialog_more);
        more.setOnClickListener(this);
        builder.setView(contentView);
        builder.show();
    }
}
