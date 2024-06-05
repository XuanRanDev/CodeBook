package dev.xuanran.codebook.activity;

import static dev.xuanran.codebook.bean.Constants.ENCRYPTION_TYPE_FINGERPRINT;
import static dev.xuanran.codebook.bean.Constants.ENCRYPTION_TYPE_PASSWORD;
import static dev.xuanran.codebook.bean.Constants.FINGERPRINT_AUTH_EXPIRED;
import static dev.xuanran.codebook.bean.Constants.KEY_ENCRYPTION_TYPE;
import static dev.xuanran.codebook.bean.Constants.KEY_SALT;
import static dev.xuanran.codebook.bean.Constants.KEY_USER_RULE_AGREE_DATE;
import static dev.xuanran.codebook.bean.Constants.KEY_USER_RULE_AGREE_STATUS;
import static dev.xuanran.codebook.bean.Constants.KEY_VALIDATE;
import static dev.xuanran.codebook.bean.Constants.PREFS_NAME;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.biometric.BiometricManager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

import dev.xuanran.codebook.R;
import dev.xuanran.codebook.bean.account.adapter.AccountAdapter;
import dev.xuanran.codebook.bean.account.model.AccountViewModel;
import dev.xuanran.codebook.callback.CipherStrategyCallback;
import dev.xuanran.codebook.callback.ExportCallback;
import dev.xuanran.codebook.callback.ImportCallback;
import dev.xuanran.codebook.service.CipherStrategy;
import dev.xuanran.codebook.service.impl.FingerprintCipherStrategy;
import dev.xuanran.codebook.service.impl.PasswordCipherStrategy;
import dev.xuanran.codebook.util.DateUtils;
import dev.xuanran.codebook.util.DialogHelper;
import dev.xuanran.codebook.util.HexUtils;
import dev.xuanran.codebook.util.PasswordUtils;

public class MainActivity extends AppCompatActivity implements CipherStrategyCallback {

    public static byte[] salt;

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
    private String exportDataPassword;
    private DialogHelper dialogHelper;
    private boolean initialized = false;
    public static CipherStrategy cipherStrategy;

    private static final int REQUEST_CODE_IMPORT = 1;
    private static final int REQUEST_CODE_EXPORT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // DialogHelper中包含了很多使用到的弹窗
        dialogHelper = new DialogHelper(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        encryptionType = sharedPreferences.getString(KEY_ENCRYPTION_TYPE, "");
        validateData = sharedPreferences.getString(KEY_VALIDATE, "");
        String saltHex = sharedPreferences.getString(KEY_SALT, "");
        salt = HexUtils.hexToBytes(saltHex);
        boolean userRuleStatus = sharedPreferences.getBoolean(KEY_USER_RULE_AGREE_STATUS, false);

        if (!userRuleStatus) {
            dialogHelper.showUserAgreementDialog(view -> {
                salt = PasswordUtils.generateSalt();
                sharedPreferences.edit()
                        .putString(KEY_SALT, HexUtils.bytesToHex(salt))
                        .putBoolean(KEY_USER_RULE_AGREE_STATUS, true)
                        .putString(KEY_USER_RULE_AGREE_DATE, DateUtils.getNowTime())
                        .apply();
                init();
            }, false);
        }else {
            init();
        }

    }

    private void init() {
        // 如果存储的加密类型是空的代表第一次启动
        if (encryptionType.isEmpty()) {
            showEncryptionTypeDialog();
        } else {
            // 根据选择的加密类型启动不同的处理流程
            startAppropriateFlow();
        }
        // View init
        initView();
    }

    /**
     * 显示选择加密类型的弹窗
     */
    private void showEncryptionTypeDialog() {
        dialogHelper.showEncryptionTypeDialog(
                (dialogInterface, i) -> startFingerprintFlow(),
                (dialogInterface, i) -> startPasswordFlow()
        );
    }

    /**
     * 开始App流程
     */
    private void startAppropriateFlow() {
        if (ENCRYPTION_TYPE_FINGERPRINT.equals(encryptionType)) {
            startFingerprintFlow();
        } else if (ENCRYPTION_TYPE_PASSWORD.equals(encryptionType)) {
            startPasswordFlow();
        }
    }

    /**
     * 开始指纹认证流程
     */
    private void startFingerprintFlow() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            showTips(R.string.fingerprint_require_R);
            showEncryptionTypeDialog();
            return;
        }

        BiometricManager biometricManager = BiometricManager.from(this);
        if (biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
            boolean needGenKey = this.encryptionType.isEmpty();
            cipherStrategy = new FingerprintCipherStrategy(this, needGenKey, this::handleCipherStrategyCreation);
        } else {
            showTips(R.string.fingerprint_not_supported);
            showEncryptionTypeDialog();
        }
    }


    private void handleCipherStrategyCreation(boolean success, int code, String msg) {
        if (success) {
            scheduleReAuthentication(this);
            onCipherStrategyCreated(cipherStrategy, ENCRYPTION_TYPE_FINGERPRINT);
        } else {
            showTips(msg);
            startAppropriateFlow();
        }
    }

    /**
     * 开始密码验证流程
     */
    private void startPasswordFlow() {
        dialogHelper.startPasswordFlow(text -> onCipherStrategyCreated(new PasswordCipherStrategy(text), ENCRYPTION_TYPE_PASSWORD), (dialogInterface, i) -> finish());
    }

    /**
     * View init
     */
    private void initView() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        appBarLayout = findViewById(R.id.app_bar_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        fab = findViewById(R.id.fab);
        recyclerView = findViewById(R.id.recycler_view);

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        setupFabBehavior();
    }

    /**
     * 设置Fab事件与AppBarLayout在展开和折叠时Fab的状态处理
     */
    private void setupFabBehavior() {
        fab.setOnClickListener(view -> dialogHelper.showAddAccountDialog(accountViewModel));
        appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                fab.hide();
            } else if (verticalOffset == 0) {
                fab.show();
            }
        });
    }

    private void selectFileForImport() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_file)), REQUEST_CODE_IMPORT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null || data.getData() == null) return;
        Uri uri = data.getData();
        if (requestCode == REQUEST_CODE_IMPORT && resultCode == RESULT_OK) {
            dialogHelper.showImportDialog(uri, password -> accountViewModel.importData(password, uri, new ImportCallback() {
                @Override
                public void onSuccess() {
                    showToast(R.string.import_success);
                }

                @Override
                public void onError(Exception e) {
                    showToast(R.string.import_error);
                }
            }));
        } else if (requestCode == REQUEST_CODE_EXPORT && resultCode == RESULT_OK) {
            accountViewModel.exportData(exportDataPassword, uri, new ExportCallback() {
                @Override
                public void onSuccess(File file) {
                    showToast(R.string.export_success);
                }

                @Override
                public void onError(Exception e) {
                    showToast(R.string.export_error);
                }
            });
        }
    }

    /**
     * 在指纹方式下由于SecretKey会过期，如果App仍在运行当中则启动定时任务在到时间之后请求重新认证
     * @param context Activity
     */
    private void scheduleReAuthentication(Context context) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> dialogHelper.showReAuthenticationDialog(
                        context,
                        (dialogInterface, i) -> startFingerprintFlow(),
                        (dialogInterface, i) -> finish()),
                (FINGERPRINT_AUTH_EXPIRED - 3) * 1000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);

        searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setOnSearchClickListener(view -> appBarLayout.setExpanded(false));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                clearSearchFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                appBarLayout.setExpanded(false);
                accountViewModel.searchAccounts(newText).observe(MainActivity.this, accounts -> adapter.setAccounts(accounts));
                return false;
            }
        });
        return true;
    }


    /**
     * 加密策略处理以及数据解密尝试
     * @param cipherStrategy 加密策略实例
     * @param encryption 选择的加密方式
     */
    @Override
    public void onCipherStrategyCreated(CipherStrategy cipherStrategy, String encryption) {
        // 条件二主要解决第一次密码错误第二次密码正确的场景
        if (MainActivity.cipherStrategy == null || encryption.equalsIgnoreCase(ENCRYPTION_TYPE_PASSWORD)) {
            MainActivity.cipherStrategy = cipherStrategy;
        }
        // 如果条件成立代表App首次启动
        if (encryptionType.isEmpty()) {
            // 加密一个测试数据，方便后续测试密码是否能够正常解密数据
            String encryptedValue = cipherStrategy.encryptData("123456");
            sharedPreferences.edit()
                    .putString(KEY_VALIDATE, encryptedValue)
                    .putString(KEY_ENCRYPTION_TYPE, encryption)
                    .apply();
            validateData = encryptedValue;
            encryptionType = encryption;
        }

        // 此方法可能会频繁调用(指纹场景)，如果已初始化的情况下后续的方法不再调用
        if (!initialized) {
            try {
                cipherStrategy.validate(validateData);
            } catch (Exception e) {
                startAppropriateFlow();
                showTips(getString(R.string.password_error));
                return;
            }
            initialized = true;
            initData();
        }
    }

    public void showTips(String message) {
        Snackbar.make(drawerLayout, message, Snackbar.LENGTH_LONG).show();
    }

    private void showTips(int messageId) {
        showTips(getString(messageId));
    }

    private void clearSearchFocus() {
        searchView.clearFocus();
        searchView.onActionViewCollapsed();
        appBarLayout.setExpanded(true, true);
    }

    private void initData() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_data_export) {
                dialogHelper.showExportDialog(password -> {
                    exportDataPassword = password;
                    Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("text/plain");
                    // 获取本地化的日期和时间格式
                    DateFormat dateTimeFormatter = DateFormat.getDateTimeInstance();
                    intent.putExtra(Intent.EXTRA_TITLE, String.format("codebook_backup_%s.txt", DateUtils.getNowTime()));
                    startActivityForResult(intent, REQUEST_CODE_EXPORT);
                });
            } else if (itemId == R.id.menu_data_import) {
                selectFileForImport();
            } else if (itemId == R.id.donate) {
                dialogHelper.showDonateDialog();
            } else if (itemId == R.id.about) {
                dialogHelper.showAboutDialog();
            } else if (itemId == R.id.userRule) {
                dialogHelper.showUserAgreementDialog(null, true);
            }

            drawerLayout.closeDrawer(navigationView);
            return true;
        });

        accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AccountAdapter(accountViewModel);
        recyclerView.setAdapter(adapter);

        accountViewModel.getAllAccounts().observe(this, accountEntities -> adapter.setAccounts(accountEntities));
    }

    private void showToast(int messageId) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, messageId, Toast.LENGTH_SHORT).show());
    }
}
