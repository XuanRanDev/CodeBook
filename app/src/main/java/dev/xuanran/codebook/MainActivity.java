package dev.xuanran.codebook;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import dev.xuanran.codebook.adapter.AccountAdapter;
import dev.xuanran.codebook.bean.AccountEntity;
import dev.xuanran.codebook.callback.ExportCallback;
import dev.xuanran.codebook.callback.ImportCallback;
import dev.xuanran.codebook.fragment.FingerprintDialog;
import dev.xuanran.codebook.model.AccountViewModel;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener,
        View.OnClickListener {



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

    private static final String PREFS_NAME = "config";
    private static final String KEY_ENCRYPTION_TYPE = "encryption_type";
    private static final String ENCRYPTION_TYPE_FINGERPRINT = "fingerprint";
    private static final String ENCRYPTION_TYPE_PASSWORD = "password";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        encryptionType = sharedPreferences.getString(KEY_ENCRYPTION_TYPE, "");

        if (encryptionType.isEmpty()) {
            showEncryptionTypeDialog();
        } else {
            startAppropriateFlow();
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        initView();

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
            drawerLayout.closeDrawer(navigationView);
            return true;
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AccountAdapter();
        recyclerView.setAdapter(adapter);

        accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);
        accountViewModel.getAllAccounts().observe(this, adapter::setAccounts);

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

    private void showEncryptionTypeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择加密方式");
        builder.setItems(new CharSequence[]{"指纹验证", "密码验证"}, (dialog, which) -> {
            switch (which) {
                case 0:
                    encryptionType = ENCRYPTION_TYPE_FINGERPRINT;
                    break;
                case 1:
                    encryptionType = ENCRYPTION_TYPE_PASSWORD;
                    break;
            }
            sharedPreferences.edit().putString(KEY_ENCRYPTION_TYPE, encryptionType).apply();
            startAppropriateFlow();
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void startAppropriateFlow() {
        if (ENCRYPTION_TYPE_FINGERPRINT.equals(encryptionType)) {
            // 启动指纹验证流程
            startFingerprintFlow();
        } else if (ENCRYPTION_TYPE_PASSWORD.equals(encryptionType)) {
            // 启动密码验证流程
            startPasswordFlow();
        }
    }

    private void startFingerprintFlow() {
        FingerprintManager fingerprintManager = (FingerprintManager) getSystemService(Context.FINGERPRINT_SERVICE);
        if (fingerprintManager != null && fingerprintManager.isHardwareDetected()) {
                FingerprintDialog fingerprintDialog = new FingerprintDialog();
                fingerprintDialog.show(getSupportFragmentManager(), "fingerprint_dialog");
                fingerprintDialog.setFingerprintAuthenticationCallback(new FingerprintDialog.FingerprintAuthenticationCallback() {
                    @Override
                    public void onFingerprintAuthenticationSucceeded() {
                        // 指纹验证成功，使用系统生成的 AES 加密密钥进行数据加密和解密
                        // 这里可以调用相应的加密/解密方法
                        // 示例：String encryptedData = AESUtils.encrypt(data, generatedKey);
                    }

                    @Override
                    public void onFingerprintAuthenticationFailed() {
                        // 指纹验证失败，提示用户选择其他加密方式或退出应用
                        Toast.makeText(MainActivity.this, "指纹验证失败，请选择其他加密方式", Toast.LENGTH_SHORT).show();
                        startPasswordFlow();
                    }
                });

        } else {
            // 设备不支持指纹识别，提示用户选择其他加密方式或退出应用
             Toast.makeText(MainActivity.this, "设备不支持指纹识别，请选择其他加密方式", Toast.LENGTH_SHORT).show();
             startPasswordFlow();
        }
    }


    private void startPasswordFlow() {
        // 弹出密码验证对话框，让用户输入密码
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("密码验证");
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);
        builder.setPositiveButton("确定", (dialog, which) -> {
            String password = input.getText().toString();
            // 使用用户输入的密码生成 AES 加密密钥进行数据加密和解密
            // 这里可以调用相应的加密/解密方法
            // 示例：String encryptedData = AESUtils.encrypt(data, generatedKey);
        });
        builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());
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
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_content_view_dialog, null);
        builder.setView(dialogView);
        TextInputLayout appNameInputLayout = dialogView.findViewById(R.id.add_content_view_dialog_appName);
        TextInputLayout accountIDInputLayout = dialogView.findViewById(R.id.add_content_view_dialog_accountID);
        TextInputLayout passwordInputLayout = dialogView.findViewById(R.id.add_content_view_dialog_password);
        Button cancel = dialogView.findViewById(R.id.add_content_view_dialog_cancel);
        Button save = dialogView.findViewById(R.id.add_content_view_dialog_save);
        AlertDialog dialog = builder.create();
        cancel.setOnClickListener(view -> dialog.cancel());
        save.setOnClickListener(view -> {
            String appName = appNameInputLayout.getEditText().getText().toString();
            String accountID = accountIDInputLayout.getEditText().getText().toString();
            String password = passwordInputLayout.getEditText().getText().toString();
            AccountEntity accountEntity = new AccountEntity();
            accountEntity.setAppName(appName);
            accountEntity.setUsername(accountID);
            accountEntity.setPassword(password);
            accountEntity.setCreateTime(new Date());
            accountViewModel.insert(accountEntity);
            dialog.cancel();
        });
        dialog.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("搜索...");
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
}
