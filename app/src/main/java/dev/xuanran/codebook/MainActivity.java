package dev.xuanran.codebook;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.animation.ScaleInAnimation;
import com.chad.library.adapter.base.entity.node.BaseNode;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dev.xuanran.codebook.adapter.HomeCardAdapter;
import dev.xuanran.codebook.bean.CardData;


@SuppressLint("NonConstantResourceId")
public class MainActivity extends AppCompatActivity {

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

    List<CardData> list = new ArrayList<>();

    HomeCardAdapter homeCardAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        setSupportActionBar(toolbar);


        homeCardAdapter = new HomeCardAdapter();
        homeCardAdapter.setList(getEntity());
        homeCardAdapter.setAnimationEnable(true);
        homeCardAdapter.setAdapterAnimation(new ScaleInAnimation());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(homeCardAdapter);

        initFloatButton();
        initAppBar();
        //initSystemBarColor();
        initView();
    }


    private void initSystemBarColor() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    private void initFloatButton() {
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this);
                View content = LayoutInflater.from(MainActivity.this).inflate(R.layout.add_content_view_dialog, null);
                builder.setView(content);
                AlertDialog dialog = builder.show();

                TextInputLayout appName = content.findViewById(R.id.add_content_view_dialog_appName);
                TextInputLayout accountID = content.findViewById(R.id.add_content_view_dialog_accountID);
                TextInputLayout password = content.findViewById(R.id.add_content_view_dialog_password);
                Button cancel = content.findViewById(R.id.add_content_view_dialog_cancel);
                Button save = content.findViewById(R.id.add_content_view_dialog_save);
                AppCompatImageView more = content.findViewById(R.id.add_content_view_dialog_more);

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                more.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showAddPopupMenu(view);
                    }
                });


            }
        });
    }

    private void showAddPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.add_more_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Snackbar.make(drawerLayout, "Click:" + item.getTitle(), 3000).show();
                return false;
            }
        });
        popupMenu.show();
    }


    private void initAppBar() {
        collapsingToolbarLayout.setExpandedTitleColor(Color.WHITE);
    }

    private void alertWarning(String info) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.error);
        builder.setMessage(info);
        builder.setCancelable(false);
        builder.setPositiveButton("OK", null);
        builder.show();
    }


    private void initView() {
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,
                drawerLayout,
                toolbar,
                R.string.open,
                R.string.clone);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        runDataLoadingLayoutAnimation(recyclerView);
                        swipeRefreshLayout.setRefreshing(false);
                        Snackbar.make(drawerLayout, "刷新完成！", 2000).show();
                    }
                }, 1000);
            }
        });
    }

    private Collection<? extends BaseNode> getEntity() {
        for (int i = 0; i < 30; i++) {
            list.add(new CardData(i, "第" + i + "项"));
        }
        return list;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem search = menu.findItem(R.id.app_bar_search);
        SearchView searchView = (SearchView) search.getActionView();
        searchView.setQueryHint("搜索...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                appBarLayout.setExpanded(false);
                filter(query);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filter(String query) {
        try {
            for (int i = 0; i < homeCardAdapter.getData().size(); i++) {
                CardData cardData = (CardData) homeCardAdapter.getData().get(i);
                if (!cardData.getCardName().contains(query)) {
                    runDataRefreshLayoutAnimation(recyclerView);
//                    homeCardAdapter.removeAt(i);
                }
            }
            homeCardAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            alertWarning(e.getMessage());
        }
    }

    private void runDataLoadingLayoutAnimation(final RecyclerView recyclerView) {
        final Context context = recyclerView.getContext();
        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down);
        recyclerView.setLayoutAnimation(controller);
        recyclerView.scheduleLayoutAnimation();
    }

    private void runDataRefreshLayoutAnimation(final RecyclerView recyclerView) {
        final Context context = recyclerView.getContext();
        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down);
        recyclerView.setLayoutAnimation(controller);
        recyclerView.scheduleLayoutAnimation();
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return true;
    }
}