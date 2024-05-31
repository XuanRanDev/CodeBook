package dev.xuanran.codebook;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputLayout;

import dev.xuanran.codebook.adapter.AccountAdapter;
import dev.xuanran.codebook.bean.AccountEntity;
import dev.xuanran.codebook.model.AccountViewModel;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private AppBarLayout appBarLayout;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private Toolbar toolbar;
    private FloatingActionButton fab;
    private RecyclerView recyclerView;
    private AccountViewModel accountViewModel;

    private AccountAdapter adapter;


    private SearchView searchView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);


        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        appBarLayout = findViewById(R.id.app_bar_layout);
        collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        toolbar = findViewById(R.id.toolbar);
        fab = findViewById(R.id.fab);
        recyclerView = findViewById(R.id.recycler_view);

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    // Handle home action
                } else if (id == R.id.nav_settings) {
                    // Handle settings action
                } else if (id == R.id.nav_about) {
                    // Handle about action
                }
                drawerLayout.closeDrawer(navigationView);
                return true;
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AccountAdapter();
        recyclerView.setAdapter(adapter);

        accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);
        // Update RecyclerView
        accountViewModel.getAllAccounts().observe(this, adapter::setAccounts);

        fab.setOnClickListener(view -> {
            // Handle FAB click action
            showAddAccountDialog();
        });

        appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                // Collapsed
                fab.hide();
            } else if (verticalOffset == 0) {
                // Expanded
                fab.show();
            } else {
                // Somewhere in between
            }
        });
    }

    private void showAddAccountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_content_view_dialog, null);
        builder.setView(dialogView);

        // Find views in the dialog layout
        TextInputLayout appNameInputLayout = dialogView.findViewById(R.id.add_content_view_dialog_appName);
        TextInputLayout accountIDInputLayout = dialogView.findViewById(R.id.add_content_view_dialog_accountID);
        TextInputLayout passwordInputLayout = dialogView.findViewById(R.id.add_content_view_dialog_password);

        builder.setPositiveButton("Save", (dialogInterface, i) -> {
            // Save data to the database or perform other actions
            String appName = appNameInputLayout.getEditText().getText().toString();
            String accountID = accountIDInputLayout.getEditText().getText().toString();
            String password = passwordInputLayout.getEditText().getText().toString();

            AccountEntity accountEntity = new AccountEntity();
            accountEntity.setName(appName);
            accountEntity.setAccount(accountID);
            accountEntity.setPassword(password);
            accountViewModel.insert(accountEntity);

            dialogInterface.dismiss();
            // Perform action with the data
            // For example, add the data to a list or save it to a database
        });
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("搜索...");
        searchView.setOnSearchClickListener(view -> appBarLayout.setExpanded(false));

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
        adapter.filter(newText);
        return false;
    }
}
