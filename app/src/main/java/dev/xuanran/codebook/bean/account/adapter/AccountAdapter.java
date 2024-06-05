package dev.xuanran.codebook.bean.account.adapter;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dev.xuanran.codebook.R;
import dev.xuanran.codebook.bean.account.AccountEntity;
import dev.xuanran.codebook.bean.account.model.AccountViewModel;

public class AccountAdapter extends RecyclerView.Adapter<AccountViewHolder> {
    @SuppressLint("SimpleDateFormat")
    public static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private final AccountViewModel accountViewModel;

    private List<AccountEntity> accounts = new ArrayList<>();

    public AccountAdapter(AccountViewModel accountViewModel) {
        this.accountViewModel = accountViewModel;
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.account_item, parent, false);
        return new AccountViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        AccountEntity currentAccount = accounts.get(position);
        holder.title.setText(currentAccount.getAppName());
        holder.id.setText("# " + currentAccount.getId());
        holder.createDate.setText(simpleDateFormat.format(currentAccount.getCreateTime()));

        holder.viewButton.setOnClickListener(v -> {
            showAccountDetailsDialog(v, currentAccount);
        });
    }

    @Override
    public int getItemCount() {
        return accounts.size();
    }

    public void setAccounts(List<AccountEntity> accounts) {
        this.accounts = accounts;
        notifyDataSetChanged();
    }

    private void showAccountDetailsDialog(View view, AccountEntity account) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(view.getContext(), R.style.ThemeOverlayAppMaterialAlertDialog);
        LayoutInflater inflater = LayoutInflater.from(view.getContext());
        View dialogView = inflater.inflate(R.layout.content_view_dialog, null);
        builder.setView(dialogView);

        AppCompatImageView more = dialogView.findViewById(R.id.content_view_dialog_more);
        TextView title = dialogView.findViewById(R.id.content_view_dialog_title);
        TextInputLayout accountIDInputLayout = dialogView.findViewById(R.id.content_view_dialog_accountID);
        TextInputLayout passwordInputLayout = dialogView.findViewById(R.id.content_view_dialog_password);
        Button copyAccountIDButton = dialogView.findViewById(R.id.content_view_dialog_copyAccountID);
        Button copyPasswordButton = dialogView.findViewById(R.id.content_view_dialog_copyPassword);

        title.setText(account.getAppName());
        Objects.requireNonNull(accountIDInputLayout.getEditText()).setText(account.getUsername());
        Objects.requireNonNull(passwordInputLayout.getEditText()).setText(account.getPassword());

        copyAccountIDButton.setOnClickListener(v -> {
            copyToClipboard(view.getContext(), "Account", account.getUsername());
            Snackbar.make(dialogView, R.string.account_copy, 3000).show();
        });

        copyPasswordButton.setOnClickListener(v -> {
            copyToClipboard(view.getContext(), "Password", account.getPassword());
            Snackbar.make(dialogView, R.string.password_copy_tips, 3000).show();
        });

        more.setOnClickListener(view13 -> showMoreMenu(view13, account));


        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void copyToClipboard(Context context, String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
    }

    /**
     * 显示卡片的更多菜单
     *
     * @param view 父View
     */
    private void showMoreMenu(View view, AccountEntity data) {
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.getMenuInflater().inflate(R.menu.dialog_content_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.dialog_content_menu_delete) {
                showDeleteConfirmation(view, data);
            }
            if (item.getItemId() == R.id.dialog_content_menu_remark) {
                showRemark(view, data);
            }
            return true;
        });
        popup.show();
    }

    private void showDeleteConfirmation(View view, AccountEntity account) {
        new MaterialAlertDialogBuilder(view.getContext())
                .setTitle(R.string.confirm_delete)
                .setMessage(R.string.confirm_delete_tips)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    accountViewModel.delete(account);
                    Snackbar.make(view, R.string.deleted, Snackbar.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showRemark(View view, AccountEntity account) {
        new MaterialAlertDialogBuilder(view.getContext())
                .setMessage(account.getRemark())
                .setPositiveButton(R.string.copy, (dialog, which) -> {
                    copyToClipboard(view.getContext(), "remark", account.getRemark());
                    Snackbar.make(view, R.string.copy_success, Snackbar.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

}
