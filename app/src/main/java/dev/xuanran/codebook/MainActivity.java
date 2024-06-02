package dev.xuanran.codebook;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import dev.xuanran.codebook.bean.account.AccountEntity;
import dev.xuanran.codebook.bean.account.adapter.AccountAdapter;
import dev.xuanran.codebook.bean.account.model.AccountViewModel;
import dev.xuanran.codebook.callback.CipherStrategyCallback;
import dev.xuanran.codebook.callback.ExportCallback;
import dev.xuanran.codebook.callback.ImportCallback;
import dev.xuanran.codebook.service.CipherStrategy;
import dev.xuanran.codebook.service.impl.FingerprintCipherStrategy;
import dev.xuanran.codebook.service.impl.PasswordCipherStrategy;
import dev.xuanran.codebook.util.CipherHelper;
import dev.xuanran.codebook.util.FileUtils;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener,
        View.OnClickListener, CipherStrategyCallback {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private AppBarLayout appBarLayout;
    private FloatingActionButton fab;
    private RecyclerView recyclerView;
    private AccountViewModel accountViewModel;
    private AccountAdapter adapter;
    private SearchView searchView;

    private SharedPreferences sharedPreferences;
    private String encryptionType;

    private String validateData;
    /**
     * CipherStrategy 对象，用于进行加密/解密操作
     */
    public static CipherStrategy cipherStrategy;

    private boolean appBarStatus = true;

    private long firstTime;
    private static final String PREFS_NAME = "pass_config";
    private static final String KEY_ENCRYPTION_TYPE = "encryption_type";
    public static final String KEY_VALIDATE = "validate_key";
    private static final String ENCRYPTION_TYPE_FINGERPRINT = "fingerprint";
    private static final String ENCRYPTION_TYPE_PASSWORD = "password";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        encryptionType = sharedPreferences.getString(KEY_ENCRYPTION_TYPE, "");
        validateData = sharedPreferences.getString(KEY_VALIDATE, "");

        if (encryptionType.isEmpty()) {
            // 如果没有设置加密方式，则弹出对话框让用户选择加密方式
            showEncryptionTypeDialog();
        } else {
            startAppropriateFlow();
        }
        initView();
    }

    /**
     * 显示验证弹窗
     */
    private void showVerifyDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_verfiy, null);
        MaterialAlertDialogBuilder verifyDialogBuild = new MaterialAlertDialogBuilder(this);
        verifyDialogBuild.setCancelable(false);
        verifyDialogBuild.setView(dialogView);
        AlertDialog verifyDialog = verifyDialogBuild.create();
        verifyDialog.show();
    }

    /**
     * 弹出加密方式选择对话框
     */
    private void showEncryptionTypeDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.choose_encryption_method);
        builder.setCancelable(false);
        builder.setMessage(R.string.choose_encryption_method_tips);
        builder.setPositiveButton(R.string.fingerprint, (dialogInterface, i) -> {
            startFingerprintFlow();
        });
        builder.setNegativeButton(R.string.password, (dialogInterface, i) -> {
            startPasswordFlow();
        });
        builder.setCancelable(false);
        builder.show();
    }


    /**
     * 根据加密方式启动相应的流程
     */
    private void startAppropriateFlow() {
        if (ENCRYPTION_TYPE_FINGERPRINT.equals(encryptionType)) {
            // 启动指纹验证流程
            startFingerprintFlow();
        } else if (ENCRYPTION_TYPE_PASSWORD.equals(encryptionType)) {
            // 启动密码验证流程
            startPasswordFlow();
        }
    }

    /**
     * 启动指纹验证流程
     */
    private void startFingerprintFlow() {
        BiometricManager biometricManager = BiometricManager.from(this);
        if (biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
            authenticateWithFingerprint();
            return;
        }
        Toast.makeText(this, R.string.fingerprint_not_supported, Toast.LENGTH_SHORT).show();
        showEncryptionTypeDialog();
    }

    /**
     * 使用指纹验证进行身份验证
     */
    private void authenticateWithFingerprint() {
        BiometricPrompt biometricPrompt = new BiometricPrompt(this,
                ContextCompat.getMainExecutor(this),
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        Toast.makeText(getApplicationContext(), "指纹认证错误: " + errString, Toast.LENGTH_SHORT).show();
                        startAppropriateFlow();
                    }

                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        CipherHelper.generateSecretKey();
                        Toast.makeText(getApplicationContext(), "指纹认证成功", Toast.LENGTH_SHORT).show();
                        onCipherStrategyCreated(new FingerprintCipherStrategy(), ENCRYPTION_TYPE_FINGERPRINT);
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(getApplicationContext(), "指纹认证失败", Toast.LENGTH_SHORT).show();
                        startAppropriateFlow();
                    }
                });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.fingerprint_title))
                .setSubtitle(getString(R.string.fingerprint_subtitle))
                .setNegativeButtonText(getString(R.string.cancel))
                .build();

        biometricPrompt.authenticate(promptInfo);
    }


    private void startPasswordFlow() {
        // 弹出密码验证对话框，让用户输入密码
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_password_enter, null);
        EditText passwordInput = dialogView.findViewById(R.id.password_input);
        TextView passwordInputTips = dialogView.findViewById(R.id.password_input_tips);
        passwordInputTips.setText(R.string.set_password_tips);
        builder.setTitle(R.string.enter_password);
        builder.setView(dialogView);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            String password = passwordInput.getText().toString();
            onCipherStrategyCreated(new PasswordCipherStrategy(password), ENCRYPTION_TYPE_PASSWORD);
        });
        builder.setNegativeButton(R.string.exit, (dialogInterface, i) -> finish());
        builder.show();
    }

    private void initView() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        appBarLayout = findViewById(R.id.app_bar_layout);
        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        Toolbar toolbar = findViewById(R.id.toolbar);
        fab = findViewById(R.id.fab);
        recyclerView = findViewById(R.id.recycler_view);

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);

        toggle.syncState();
    }


    private void showDonateDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_donate, null);
        // 设置对话框视图
        builder.setView(dialogView);
        // 设置取消按钮
        builder.setNegativeButton("取消", (dialog, which) -> {
            // 取消按钮的点击事件
            dialog.dismiss();
        });
        // 设置打开支付宝按钮
        builder.setPositiveButton("打开支付宝", (dialog, which) -> {
            // 打开支付宝的逻辑，可以使用Intent打开支付宝
            // 示例代码，实际上您需要根据支付宝的具体情况进行处理
            Intent intent = getPackageManager().getLaunchIntentForPackage("com.eg.android.AlipayGphone");
            if (intent != null) {
                startActivity(intent);
                return;
            }
            // 支付宝未安装的处理逻辑
            Toast.makeText(this, "未安装支付宝应用", Toast.LENGTH_SHORT).show();
        });

        // 创建对话框并显示
        AlertDialog dialog = builder.create();
        dialog.show();

        // 设置对话框文本提示和图片
        ImageView imageView = dialogView.findViewById(R.id.iv_donate_image);
        try {
            InputStream inputStream = getAssets().open("donate_image.png");
            Drawable drawable = Drawable.createFromStream(inputStream, null);
            imageView.setImageDrawable(drawable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void showExportDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_password, null);
        EditText passwordInput = dialogView.findViewById(R.id.password_input);
        builder.setTitle(R.string.export_data);
        builder.setView(dialogView);
        builder.setPositiveButton(R.string.export, (dialog, which) -> {
            String password = passwordInput.getText().toString();
            accountViewModel.exportData(password, new ExportCallback() {
                @Override
                public void onSuccess(File file) {
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, R.string.export_success, Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onError(Exception e) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, R.string.export_error, Toast.LENGTH_SHORT).show());
                }
            });
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private void selectFileForImport() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_file)), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            showImportDialog(uri);
        }
    }

    private void showImportDialog(Uri uri) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_password, null);
        EditText passwordInput = dialogView.findViewById(R.id.password_input);
        builder.setTitle(R.string.import_data);
        builder.setView(dialogView);
        builder.setPositiveButton(R.string.importStr, (dialog, which) -> {
            String password = passwordInput.getText().toString();
            accountViewModel.importData(password, uri, new ImportCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, R.string.import_success, Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onError(Exception e) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, R.string.import_error, Toast.LENGTH_SHORT).show());
                }
            });
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private void showAddAccountDialog() {
        AccountEntity accountEntity = new AccountEntity();

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_content_view_dialog, null);
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
        more.setOnClickListener(view ->{
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
        LayoutInflater inflater = getLayoutInflater();
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setOnSearchClickListener(view -> appBarLayout.setExpanded(false));
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        searchView.clearFocus();
        searchView.onActionViewCollapsed();
        appBarLayout.setExpanded(true, true);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        appBarLayout.setExpanded(false);
        accountViewModel.searchAccounts(newText).observe(this, accounts -> {
            adapter.setAccounts(accounts);
        });
        return false;
    }

    @Override
    public void onClick(View view) {

    }

    /**
     * 验证方法
     *
     * @param cipherStrategy 选择的策略模式
     * @param encryption     策略模式类型
     */
    @Override
    public void onCipherStrategyCreated(CipherStrategy cipherStrategy, String encryption) {

        MainActivity.cipherStrategy = cipherStrategy;
        if (encryptionType.isEmpty()) {
            sharedPreferences.edit()
                    .putString(KEY_VALIDATE, cipherStrategy.encryptData("123456"))
                    .putString(KEY_ENCRYPTION_TYPE, encryption)
                    .apply();
        }
        boolean completeVerification = encryptionType.isEmpty();
        // 测试解密数据
        if (!encryptionType.isEmpty()) {
            try {
                cipherStrategy.validate(validateData);
                completeVerification = true;
            } catch (Exception e) {
                startAppropriateFlow();
                showTips(getString(R.string.password_error));
            }
        }
        if (completeVerification) {
            initData();
        }
    }

    public void showTips(String message) {
        Snackbar.make(drawerLayout, message, 3000).show();
    }

    private void showAboutDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_about, null);
        builder.setView(dialogView)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss());

        TextView githubLink = dialogView.findViewById(R.id.github_link);
        githubLink.setOnClickListener(v -> {
            String url = getString(R.string.github_url);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });

        builder.create().show();
    }

    private void showUserAgreementDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_user_agreement, null);
        builder.setView(dialogView)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                .setCancelable(false); // 用户必须阅读协议才能继续

        TextView tvUserAgreement = dialogView.findViewById(R.id.tv_user_agreement);
        tvUserAgreement.setMovementMethod(new ScrollingMovementMethod()); // 使TextView可滚动
        tvUserAgreement.setText(FileUtils.readAssetTextFile(this, "user_rule.txt"));
        builder.create().show();
    }



    /**
     * 处理按钮返回事件，这段方法写的比较难以理解，可能存在未知的bug
     *
     * @param keyCode 由哪个按钮触发
     * @param event   按钮事件
     * @return other
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            searchView.clearFocus();
            searchView.onActionViewCollapsed();
            if (!appBarStatus) {
                appBarLayout.setExpanded(true, true);
                long secondTime = System.currentTimeMillis();
                if (secondTime - firstTime > 3000) {
                    firstTime = secondTime;
                    Snackbar.make(drawerLayout, R.string.exit_tips, 3000).setBackgroundTint(Color.parseColor("#FFFF8A80")).setAction("OK", view -> finish()).show();
                    return true;
                } else {
                    finish();
                }
            } else {
                appBarLayout.setExpanded(true, true);
                return true;
            }

        }

        return super.onKeyUp(keyCode, event);

    }

    private void initData() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_data_export) {
                showExportDialog();
            }
            if (id == R.id.menu_data_import) {
                selectFileForImport();
            }
            if (id == R.id.donate) {
                showDonateDialog();
            }
            if (id == R.id.about) {
                showAboutDialog();
            }
            if (id == R.id.userRule) {
                showUserAgreementDialog();

            }
            drawerLayout.closeDrawer(navigationView);
            return true;
        });

        accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AccountAdapter(accountViewModel);
        recyclerView.setAdapter(adapter);

        accountViewModel.getAllAccounts().observe(this, new Observer<List<AccountEntity>>() {
            @Override
            public void onChanged(List<AccountEntity> accountEntities) {
                adapter.setAccounts(accountEntities);
            }
        });

        fab.setOnClickListener(view -> {
            showAddAccountDialog();
        });

        appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                fab.hide();
            } else if (verticalOffset == 0) {
                fab.show();
            }
        });
    }
}
