package dev.xuanran.codebook;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.os.Bundle;

import com.chad.library.adapter.base.entity.node.BaseNode;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

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



    HomeCardAdapter homeCardAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);;
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        homeCardAdapter = new HomeCardAdapter();
        homeCardAdapter.setList(getEntity());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(homeCardAdapter);


        initView();
    }

    private void initView() {
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,
                drawerLayout,
                toolbar,
                R.string.open,
                R.string.clone);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    private Collection<? extends BaseNode> getEntity() {
        List<BaseNode> list=new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            list.add(new CardData(i,"第" + i + "项"));
        }
        return list;
    }
}