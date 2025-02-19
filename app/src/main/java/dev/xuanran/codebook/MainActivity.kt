package dev.xuanran.codebook

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import dev.xuanran.codebook.databinding.ActivityMainBinding
import dev.xuanran.codebook.ui.fragment.AppListFragment
import dev.xuanran.codebook.ui.fragment.TotpListFragment
import dev.xuanran.codebook.ui.interfaces.FabClickListener
import dev.xuanran.codebook.ui.viewmodel.SortOrder

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private var currentSearchView: SearchView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置Toolbar
        setSupportActionBar(binding.toolbar)

        // 获取 NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // 设置AppBarConfiguration
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.navigation_passwords, R.id.navigation_totp)
        )
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        // 设置底部导航
        val navView: BottomNavigationView = binding.navView
        navView.setupWithNavController(navController)

        // 设置FAB点击事件
        binding.fabAdd.setOnClickListener {
            // 获取当前Fragment
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val currentFragment = navHostFragment.childFragmentManager.fragments.firstOrNull()
            
            // 如果当前Fragment实现了FabClickListener接口，则调用其onFabClick方法
            (currentFragment as? FabClickListener)?.onFabClick()
        }

        // 监听导航变化来更新FAB的显示状态
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // 根据目的地ID来决定是否显示FAB
            binding.fabAdd.visibility = when (destination.id) {
                R.id.navigation_passwords, R.id.navigation_totp -> View.VISIBLE
                else -> View.GONE
            }
        }

        // 添加这行：设置导航监听
        setupNavigation()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_app_bar, menu)
        
        // 从SharedPreferences读取当前排序方式并设置选中状态
        val prefs = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val sortOrder = prefs.getString("sort_order", SortOrder.NAME.name)
        
        // 设置对应的菜单项选中
        when (sortOrder) {
            SortOrder.NAME.name -> menu.findItem(R.id.sort_name).isChecked = true
            SortOrder.TIME.name -> menu.findItem(R.id.sort_time).isChecked = true
        }

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        currentSearchView = searchView
        
        // 配置SearchView
        searchView.apply {
            queryHint = "搜索应用或账号"
            
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    performSearch(query)
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    performSearch(newText)
                    return true
                }
            })
        }

        // 监听搜索框展开/折叠状态
        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                performSearch("") // 清空搜索时恢复原始列表
                return true
            }
        })
        
        return true
    }

    private fun performSearch(query: String?) {
        // 获取当前Fragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val currentFragment = navHostFragment.childFragmentManager.fragments.firstOrNull()

        // 根据当前页面执行对应的搜索
        when (currentFragment) {
            is AppListFragment -> {
                currentFragment.viewModel.searchApps(query ?: "")
            }
            is TotpListFragment -> {
                currentFragment.viewModel.searchTotps(query ?: "")
            }
        }
    }

    // 在导航发生变化时清空搜索
    private fun setupNavigation() {
        navController.addOnDestinationChangedListener { _, _, _ ->
            currentSearchView?.let { searchView ->
                if (!searchView.isIconified) {
                    searchView.isIconified = true
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sort_name -> {
                applySortOrder(SortOrder.NAME)
                item.isChecked = true
                true
            }
            R.id.sort_time -> {
                applySortOrder(SortOrder.TIME)
                item.isChecked = true
                true
            }
            R.id.action_settings -> {
                // TODO: 跳转到设置页面
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getCurrentFragment(): Fragment? {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHostFragment.childFragmentManager.fragments.firstOrNull()
    }

    private fun applySortOrder(order: SortOrder) {
        when (val currentFragment = getCurrentFragment()) {
            is AppListFragment -> currentFragment.viewModel.setSortOrder(order)
            is TotpListFragment -> currentFragment.viewModel.setSortOrder(order)
        }
    }
}