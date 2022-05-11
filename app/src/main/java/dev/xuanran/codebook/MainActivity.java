package dev.xuanran.codebook;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.yf.verify.callback.FingerprintAuthenticatedCallback;
import com.yf.verify.fingerprint.FingerprintCharacter;
import com.yf.verify.fingerprint.FingerprintCharacterStepBuilder;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import butterknife.BindView;
import butterknife.ButterKnife;
import dev.xuanran.codebook.adapter.HomeCardAdapter;
import dev.xuanran.codebook.bean.CardData;
import dev.xuanran.codebook.dao.UserDataDao;
import dev.xuanran.codebook.db.AppDatabase;
import dev.xuanran.codebook.listener.AppBarStatusChangeListener;
import dev.xuanran.codebook.util.AesUtil;
import dev.xuanran.codebook.util.AppExecutors;
import dev.xuanran.codebook.util.ClipboardUtil;
import dev.xuanran.codebook.util.FileUtil;
import dev.xuanran.codebook.util.SharedUtil;


@SuppressLint("NonConstantResourceId")
public class MainActivity extends AppCompatActivity implements FingerprintAuthenticatedCallback {

    // View
    @BindView(R.id.activity_main_toolbar)
    Toolbar toolbar;
    @BindView(R.id.activity_main_recyclerview)
    RecyclerView recyclerView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.activity_main_floating_action_button)
    FloatingActionButton floatingActionButton;
    @BindView(R.id.activity_main_appbar)
    AppBarLayout appBarLayout;
    @BindView(R.id.activity_main_SwipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.activity_main_collapsingtoolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;

    // Event type
    public static final int EVENT_TYPE_FROM_UPDATE_DATA = 1; // 事件由更新触发
    public static final int EVENT_TYPE_FROM_SET_NEW_DATA = 2; // 事件由设置数据触发

    // AES密码
    public static final String AES_PASSWORD = "abcabcabcabcabca";

    // AppBarLayout 状态 true 展开 / false 关闭
    private boolean appBarStatus = true;
    private HomeCardAdapter homeCardAdapter;
    private SearchView searchView;
    private long firstTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedUtil.init(this);
        if (SharedUtil.getSharedPreferences().getBoolean(SharedUtil.HAS_DATA, false)) {
            if (SharedUtil.useFingerprint()) requestFingerprintVerification();
            if (SharedUtil.usePassword()) RequestPassword();
        } else {
            showRule();
        }

        //ViewBinder
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        // View init
        initAdapter();
        initRecyclerView();
        initFloatButton();
        initAppBar();
        initSystemBarColor();
        initView();
    }

    /**
     * 显示一系列的用户规则
     */
    private void showRule() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(getString(R.string.userRule));
        builder.setMessage(FileUtil.readTxtFromAssetsFile(this, "rule.txt"));
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> showChooseUnlockWay());
        builder.setNegativeButton(getString(R.string.exit), (dialogInterface, i) -> finish());
        builder.setNeutralButton(getString(R.string.wechatPublic), (dialogInterface, i) -> {
            Toast.makeText(MainActivity.this, "已复制微信公众号名称", Toast.LENGTH_SHORT).show();
            ClipboardUtil.setTextToClipboard(MainActivity.this, "XuanRan");
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        TimeCount timeCount = new TimeCount(0, 1000, alertDialog.getButton(DialogInterface.BUTTON_POSITIVE));
        timeCount.start();

    }

    /**
     * 显示应用程序的加密方式对话框
     * 仅限在首次启动时调用
     */
    private void showChooseUnlockWay() {
        final int[] flag = {0};
        String[] items = new String[]{getString(R.string.password), getString(R.string.fingerprint)};
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(getString(R.string.authenticationMethod));
        builder.setCancelable(false);
        builder.setSingleChoiceItems(items, 0, (dialogInterface, i) -> flag[0] = i);
        builder.setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> {
            if (flag[0] == 0) {
                // 设置密码
                SharedUtil.getSharedEdit().putBoolean(SharedUtil.USE_PASSWORD, true).commit();
            } else {
                // 请求指纹
                SharedUtil.getSharedEdit().putBoolean(SharedUtil.USE_FINGERPRINT, true).commit();
            }
            SharedUtil.getSharedEdit().putBoolean(SharedUtil.HAS_DATA, true).commit();
        });
        builder.show();
    }

    /**
     * 请求指纹验证
     */
    private void requestFingerprintVerification() {
        FingerprintCharacter fingerprintAuthenticatedCharacter = FingerprintCharacterStepBuilder
                .newBuilder()
                .setKeystoreAlias("key")
                .setFingerprintCallback(this)
                .build();

        fingerprintAuthenticatedCharacter.show(this);
    }


    private void RequestPassword() {
        View view = LayoutInflater.from(this).inflate(R.layout.verify_password, null);
        TextInputLayout textInputLayout = view.findViewById(R.id.verify_input);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(getString(R.string.verifyPassword));
        builder.setView(view);
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.ok), null);
        builder.setNegativeButton(getString(R.string.exit), null);
        builder.setNeutralButton(getString(R.string.forgetPassword), null);
        AlertDialog alert = builder.create();
        alert.show();
        alert.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view1 -> {

        });

    }

    /**
     * 初始化RecyclerView
     */
    private void initRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(homeCardAdapter);
    }

    /**
     * 初始化布局适配器
     */
    private void initAdapter() {
        homeCardAdapter = new HomeCardAdapter(R.layout.list_cardview);
        homeCardAdapter.setEmptyView(R.layout.empty_data);
        // Read data from the database and pass in Settings to RecyclerView
        homeCardAdapter.setAnimationEnable(true);
        homeCardAdapter.setAnimationWithDefault(BaseQuickAdapter.AnimationType.SlideInBottom);
        homeCardAdapter.setAnimationFirstOnly(false);
        homeCardAdapter.setFooterView(GenerateBottomView());

    }

    /**
     * 生成底部无数据View
     *
     * @return View
     */
    private View GenerateBottomView() {
        TextView textView = new TextView(this);
        textView.setPadding(0, 50, 0, 150);
        textView.setGravity(Gravity.CENTER);
        textView.setText(String.format("————%s————", getResources().getString(R.string.no_more_data)));
        return textView;
    }

    /**
     * 监听AppBar改变更改状态栏背景
     */
    private void initSystemBarColor() {
        appBarLayout.addOnOffsetChangedListener(
                new AppBarStatusChangeListener() {
                    @Override
                    public void onStateChanged(AppBarLayout appBarLayout, State state, int local) {
                        if (state == State.EXPANDED) {
                            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                            floatingActionButton.animate().scaleX(1.0F).scaleY(1.0F).alpha(1.0F)
                                    .setInterpolator(new FastOutSlowInInterpolator()).withLayer().setDuration(500)
                                    .start();
                            appBarStatus = false;
                        } else if (state == State.COLLAPSED) {
                            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                            floatingActionButton.animate().scaleX(0.0F).scaleY(0.0F).alpha(0.0F).setDuration(500).setInterpolator(new FastOutSlowInInterpolator()).withLayer();
                            appBarStatus = true;
                        }
                    }
                });
    }


    /**
     * 设置浮动按钮相关操作
     */
    private void initFloatButton() {
        floatingActionButton.setOnClickListener(view -> {
            AtomicInteger saveModel = new AtomicInteger(); // 不显示标签
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this, R.style.ThemeOverlayAppMaterialAlertDialog);
            View content = LayoutInflater.from(MainActivity.this).inflate(R.layout.add_content_view_dialog, null);
            builder.setView(content);
            AlertDialog dialog = builder.create();
            if (!dialog.isShowing()) dialog.show();

            TextInputLayout appName = content.findViewById(R.id.add_content_view_dialog_appName);
            TextInputLayout accountID = content.findViewById(R.id.add_content_view_dialog_accountID);
            TextInputLayout password = content.findViewById(R.id.add_content_view_dialog_password);
            Button cancel = content.findViewById(R.id.add_content_view_dialog_cancel);
            Button save = content.findViewById(R.id.add_content_view_dialog_save);
            AppCompatImageView more = content.findViewById(R.id.add_content_view_dialog_more);

            cancel.setOnClickListener(view1 -> dialog.dismiss());
            more.setOnClickListener(view12 -> {
                PopupMenu popupMenu = new PopupMenu(view12.getContext(), view12);
                popupMenu.getMenuInflater().inflate(R.menu.add_more_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.add_menu_idCard:
                            appName.setHint(R.string.idCardOwn);
                            accountID.setHint(R.string.pleaseInputIdCard);
                            password.setVisibility(View.GONE);
                            saveModel.set(1);
                            break;
                        case R.id.add_menu_bankCard:
                            appName.setHint(R.string.ownBank);
                            accountID.setHint(R.string.pleaseInputCardNum);
                            password.setVisibility(View.VISIBLE);
                            password.setHint(R.string.pleaseInputCardPassword);
                            saveModel.set(2);
                            break;
                        case R.id.add_menu_harvestAddress:
                            appName.setHint(R.string.harvestAddressName);
                            accountID.setHint(R.string.pleaseInputHarvestAddress);
                            password.setVisibility(View.GONE);
                            saveModel.set(3);
                            break;
                    }
                    return false;
                });
                popupMenu.show();
            });
            save.setOnClickListener(view13 -> {
                String appNameStr = Objects.requireNonNull(appName.getEditText()).getText().toString().trim();
                String accountIDStr = Objects.requireNonNull(accountID.getEditText()).getText().toString().trim();
                String passwordStr = Objects.requireNonNull(password.getEditText()).getText().toString().trim();

                if (saveModel.get() == 0) {
                    if (appNameStr.equals("") && accountIDStr.equals("") && passwordStr.equals("")) {
                        Snackbar.make(view13, getString(R.string.pleaseCheckData), 3000).show();
                        return;
                    }
                } else {
                    if (appNameStr.equals("") && accountIDStr.equals("") && saveModel.get() != 0) {
                        Snackbar.make(view13, getString(R.string.pleaseCheckData), 3000).show();
                        return;
                    }
                }

                String encryptAccountID = AesUtil.encrypt(accountIDStr, AES_PASSWORD);
                String encryptPassword = AesUtil.encrypt(passwordStr, AES_PASSWORD);
                CardData cardData = new CardData(appNameStr, encryptAccountID, encryptPassword, System.currentTimeMillis());
                cardData.setTag(saveModel.get());
                InsertDataToDataBases(cardData, dialog);
            });
        });
    }

    /**
     * 添加新的数据到数据库
     *
     * @param cardData 数据库对象
     */
    private void InsertDataToDataBases(CardData cardData, AlertDialog dialog) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            UserDataDao userDataDao = AppDatabase.getInstance(MainActivity.this).userDataDao();
            userDataDao.insertData(cardData);
            dialog.dismiss();
            getUserDataFromDatabases(EVENT_TYPE_FROM_UPDATE_DATA);
            Snackbar.make(drawerLayout, "已自动更新数据", 3000).show();
        });
    }

    /**
     * 设置Appbar展开时的文字颜色
     */
    private void initAppBar() {
        collapsingToolbarLayout.setExpandedTitleColor(Color.WHITE);
    }

    /**
     * 初始化相关View
     */
    private void initView() {
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,
                drawerLayout,
                toolbar,
                R.string.open,
                R.string.clone);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        swipeRefreshLayout.setOnRefreshListener(() -> {
            getUserDataFromDatabases(EVENT_TYPE_FROM_UPDATE_DATA);
            Snackbar.make(drawerLayout, "刷新完成！", 3000).show();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    /**
     * 从数据库中读取数据
     *
     * @param EventType 事件类型，主要确定是哪个方法调用
     */
    private void getUserDataFromDatabases(int EventType) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            UserDataDao dao = AppDatabase.getInstance(MainActivity.this).userDataDao();
            List<CardData> all = dao.getAll();
            if (all.size() == 0) {
                runOnUiThread(this::showEmptyLayout);
                return;
            }
            if (homeCardAdapter.isUseEmpty()) homeCardAdapter.setUseEmpty(false);
            runOnUiThread(() -> analyticalEventType(all, EventType));
        });
    }

    /**
     * 为Adapter显示空布局，存在Bug
     */
    @SuppressLint("InflateParams")
    private void showEmptyLayout() {
        homeCardAdapter.setUseEmpty(true);
    }


    /**
     * 解析事件类型，负责分发数据
     *
     * @param allData   从数据库中读取到的所有数据
     * @param eventType 事件类型
     */
    private void analyticalEventType(List<CardData> allData, int eventType) {
        if (eventType == EVENT_TYPE_FROM_SET_NEW_DATA) { // 设置新数据
            setDataToAdapter(allData);
        }
        if (eventType == EVENT_TYPE_FROM_UPDATE_DATA) { // 更新已存在数据
            analyticalNewData(allData);
        }
    }

    /**
     * 设置数据到适配器，并通知适配器数据发生改变
     *
     * @param list 需要设置的数据，必须为BaseNode子类
     */
    @SuppressLint("NotifyDataSetChanged")
    private void setDataToAdapter(List<CardData> list) {
        homeCardAdapter.setList(list);
        homeCardAdapter.notifyDataSetChanged();
    }

    /**
     * 警告：仅限数据更新调用
     * 解析从数据库中读取的数据，并查找出新数据
     *
     * @param list 新数据
     */
    private void analyticalNewData(List<CardData> list) {
        // 懒了，直接设置新数据
        setDataToAdapter(list);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem search = menu.findItem(R.id.app_bar_search);
        searchView = (SearchView) search.getActionView();
        searchView.setQueryHint("搜索...");
        searchView.setOnSearchClickListener(view -> appBarLayout.setExpanded(false));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                searchView.onActionViewCollapsed();
                appBarLayout.setExpanded(true, true);
                return false;
            }


            @Override
            public boolean onQueryTextChange(String query) {
                appBarLayout.setExpanded(false);
                filter(query);
                return false;
            }
        });

        ImageView closeView = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        closeView.setOnClickListener(view -> {
            searchView.clearFocus();
            searchView.onActionViewCollapsed();
            appBarLayout.setExpanded(true, true);
        });
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 过滤数据
     *
     * @param query 查询条件
     */
    private synchronized void filter(String query) {
        Filter filter = homeCardAdapter.getFilter();
        filter.filter(query);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return true;
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
                    Snackbar.make(drawerLayout, "再按一次退出", 3000).setBackgroundTint(Color.parseColor("#FFFF8A80")).setAction("OK", view -> finish()).show();
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

    // *****************请求指纹验证API***************************

    /**
     * 指纹验证成功
     */
    @Override
    public void onFingerprintSucceed() {
        Toast.makeText(this, "指纹验证成功", Toast.LENGTH_SHORT).show();
    }

    /**
     * 指纹验证失败
     */
    @Override
    public void onFingerprintFailed() {
        Toast.makeText(this, "指纹验证失败", Toast.LENGTH_SHORT).show();
    }

    /**
     * 取消验证
     */
    @Override
    public void onFingerprintCancel() {
        Toast.makeText(this, "取消指纹验证", Toast.LENGTH_SHORT).show();
    }

    /**
     * 没有录入指纹或者不支持指纹识别
     */
    @Override
    public void onNoEnrolledFingerprints() {
        Toast.makeText(this, "没有录入指纹锁", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNonsupportFingerprint() {
        Toast.makeText(this, "不支持指纹识别", Toast.LENGTH_SHORT).show();
    }


    class TimeCount extends CountDownTimer {

        Button bn;

        public TimeCount(long millisInFuture, long countDownInterval, Button bn) {
            super(millisInFuture, countDownInterval);
            this.bn = bn;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onTick(long l) {
            bn.setClickable(false);
            bn.setText(getString(R.string.ok) + "(" + l / 1000 + ") ");
        }

        @Override
        public void onFinish() {
            bn.setClickable(true);
            bn.setText(getString(R.string.ok));
        }
    }
}