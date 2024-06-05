package dev.xuanran.codebook.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.io.InputStream;
import java.util.Date;

import dev.xuanran.codebook.BuildConfig;
import dev.xuanran.codebook.R;
import dev.xuanran.codebook.bean.Constants;
import dev.xuanran.codebook.bean.account.AccountEntity;
import dev.xuanran.codebook.bean.account.model.AccountViewModel;
import dev.xuanran.codebook.callback.DialogEditTextCallback;

public class DialogHelper {
    private final Context context;
    private final LayoutInflater inflater;

    public DialogHelper(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    public void showEncryptionTypeDialog(DialogInterface.OnClickListener onFingerprintSelected, DialogInterface.OnClickListener onPasswordSelected) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle(R.string.choose_encryption_method)
                .setMessage(R.string.choose_encryption_method_tips)
                .setCancelable(false)
                .setPositiveButton(R.string.fingerprint, onFingerprintSelected)
                .setNegativeButton(R.string.password, onPasswordSelected)
                .show();
    }

    public void showCancelFingerprintDialog(DialogInterface.OnClickListener reAuthClick,
                                            DialogInterface.OnClickListener exitClick) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle(R.string.fingerprint_title)
                .setMessage(R.string.fingerprint_auth_cancel)
                .setCancelable(false)
                .setPositiveButton(R.string.re_auth, reAuthClick)
                .setNegativeButton(R.string.exit, exitClick)
                .show();
    }

    public void showImportDialog(Uri uri, DialogEditTextCallback callback) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        View dialogView = inflater.inflate(R.layout.dialog_password, null);
        EditText passwordInput = dialogView.findViewById(R.id.password_input);
        builder.setTitle(R.string.import_data);
        builder.setView(dialogView);
        builder.setPositiveButton(R.string.importStr, (dialogInterface, i) -> callback.onEditTextEntered(passwordInput.getText().toString()));
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    public void showVerifyDialog() {
        View dialogView = inflater.inflate(R.layout.dialog_verfiy, null);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setCancelable(false)
                .setView(dialogView)
                .show();
    }


    public void showDonateDialog() {
        View dialogView = inflater.inflate(R.layout.dialog_donate, null);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setView(dialogView)
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("打开支付宝", (dialog, which) -> openAlipay())
                .show();

        ImageView imageView = dialogView.findViewById(R.id.iv_donate_image);
        try {
            InputStream inputStream = context.getAssets().open("donate_image.png");
            Drawable drawable = Drawable.createFromStream(inputStream, null);
            imageView.setImageDrawable(drawable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startPasswordFlow(DialogEditTextCallback callback, DialogInterface.OnClickListener exit) {
        // 弹出密码验证对话框，让用户输入密码
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        View dialogView = inflater.inflate(R.layout.dialog_password_enter, null);
        EditText passwordInput = dialogView.findViewById(R.id.password_input);
        TextView passwordInputTips = dialogView.findViewById(R.id.password_input_tips);
        passwordInputTips.setText(R.string.set_password_tips);
        builder.setTitle(R.string.enter_password);
        builder.setView(dialogView);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            String password = passwordInput.getText().toString();
            callback.onEditTextEntered(password);
        });
        builder.setNegativeButton(R.string.exit, exit);
        builder.show();
    }

    private void openAlipay() {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.eg.android.AlipayGphone");
        if (intent != null) {
            context.startActivity(intent);
        } else {
            Toast.makeText(context, "未安装支付宝应用", Toast.LENGTH_SHORT).show();
        }
    }

    public void showExportDialog(DialogEditTextCallback callback) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        View dialogView = inflater.inflate(R.layout.dialog_password, null);
        EditText passwordInput = dialogView.findViewById(R.id.password_input);
        builder.setTitle(R.string.export_data);
        builder.setView(dialogView);
        builder.setPositiveButton(R.string.export, (dialog, which) -> {
            String exportDataPassword = passwordInput.getText().toString();
            callback.onEditTextEntered(exportDataPassword);
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }


    public void showAddAccountDialog(AccountViewModel accountViewModel) {
        AccountEntity accountEntity = new AccountEntity();
        View dialogView = inflater.inflate(R.layout.add_content_view_dialog, null);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setView(dialogView);

        TextInputLayout appNameInputLayout = dialogView.findViewById(R.id.add_content_view_dialog_appName);
        TextInputLayout accountIDInputLayout = dialogView.findViewById(R.id.add_content_view_dialog_accountID);
        TextInputLayout passwordInputLayout = dialogView.findViewById(R.id.add_content_view_dialog_password);
        ImageView more = dialogView.findViewById(R.id.add_content_view_dialog_more);
        Button cancel = dialogView.findViewById(R.id.add_content_view_dialog_cancel);
        Button save = dialogView.findViewById(R.id.add_content_view_dialog_save);
        AlertDialog dialog = builder.create();

        cancel.setOnClickListener(view -> dialog.cancel());
        save.setOnClickListener(view -> {
            String appName = appNameInputLayout.getEditText().getText().toString();
            String accountID = accountIDInputLayout.getEditText().getText().toString();
            String password = passwordInputLayout.getEditText().getText().toString();
            accountEntity.setAppName(appName);
            accountEntity.setUsername(accountID);
            accountEntity.setPassword(password);
            accountEntity.setCreateTime(new Date());
            accountViewModel.insert(accountEntity);
            dialog.cancel();
        });

        more.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
            popupMenu.getMenuInflater().inflate(R.menu.add_popup_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.dialog_popup_menu_remark) {
                    showRemarkDialog(view, accountEntity);
                }
                return false;
            });
            popupMenu.show();
        });

        dialog.show();
    }

    private void showRemarkDialog(View view, AccountEntity account) {
        View dialogView = inflater.inflate(R.layout.dialog_create_remark, null);
        TextInputLayout edittext = dialogView.findViewById(R.id.dialog_create_remark_edittext);

        new MaterialAlertDialogBuilder(view.getContext())
                .setView(dialogView)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    account.setRemark(edittext.getEditText().getText().toString());
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @SuppressLint("SetTextI18n")
    public void showAboutDialog() {
        View dialogView = inflater.inflate(R.layout.dialog_about, null);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setView(dialogView)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                .show();

        TextView githubLink = dialogView.findViewById(R.id.github_link);
        githubLink.setOnClickListener(v -> {
            String url = context.getString(R.string.github_url);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            context.startActivity(intent);
        });

        TextView buildInfo = dialogView.findViewById(R.id.build_info);

        buildInfo.setText(context.getString(R.string.build_time) + BuildConfig.BUILD_TIME + "\n" +
                context.getString(R.string.git_hash)+ BuildConfig.GIT_HASH);
    }

    /**
     * 显示隐私协议
     *
     * @param okClick     同意回调
     * @param agreeStatus 是否已同意，已同意状态下CheckBox选中不可更改，弹窗禁用
     */
    public void showUserAgreementDialog(View.OnClickListener okClick, boolean agreeStatus) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_user_agreement, null);

        CheckBox checkBox = dialogView.findViewById(R.id.dialog_user_agreement_cb);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        AlertDialog alertDialog = builder.setView(dialogView)
                .setPositiveButton(android.R.string.ok, null)
                .setCancelable(agreeStatus)
                .create();
        alertDialog.show();

        TextView tvUserAgreement = dialogView.findViewById(R.id.tv_user_agreement);
        String text = FileUtils.readAssetTextFile(context, "user_rule.txt");

        if (agreeStatus) {
            String date = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
                    .getString(Constants.KEY_USER_RULE_AGREE_DATE, "");
            text += context.getString(R.string.agree_date) + date;
        }

        tvUserAgreement.setText(text);

        // 延迟设置滚动方法
        tvUserAgreement.post(() -> tvUserAgreement.setMovementMethod(new ScrollingMovementMethod()));

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setEnabled(false);

        checkBox.setOnCheckedChangeListener((compoundButton, b) ->
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setEnabled(b));

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(view -> {
                    okClick.onClick(view);
                    alertDialog.dismiss(); // 关闭弹窗
                });


        if (agreeStatus) {
            checkBox.setChecked(true);
            checkBox.setEnabled(false);
            Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setText(R.string.you_already_agree);
            button.setEnabled(false);
        }
    }


    public void showReAuthenticationDialog(Context context, DialogInterface.OnClickListener onReAuthClicked, DialogInterface.OnClickListener onExitClicked) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.re_auth)
                .setMessage(R.string.re_auth_tips)
                .setCancelable(false)
                .setPositiveButton(R.string.re_auth, onReAuthClicked)
                .setNegativeButton(R.string.exit, onExitClicked)
                .show();
    }
}
