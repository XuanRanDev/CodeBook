package dev.xuanran.codebook

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import dev.xuanran.codebook.databinding.ActivityMainBinding
import dev.xuanran.codebook.ui.interfaces.FabClickListener

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置Toolbar
        setSupportActionBar(binding.toolbar)

        // 获取 NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

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
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_app_bar, menu)
        
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // TODO: 实现搜索逻辑
                return true
            }
        })
        
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sort_name -> {
                // TODO: 实现按名称排序
                item.isChecked = true
                true
            }
            R.id.sort_time -> {
                // TODO: 实现按时间排序
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
}