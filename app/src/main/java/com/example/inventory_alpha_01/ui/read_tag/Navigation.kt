package com.example.inventory_alpha_01.ui.read_tag

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.inventory_alpha_01.R
import com.example.inventory_alpha_01.databinding.ActivityNavigationBinding
import com.google.android.material.bottomappbar.BottomAppBar
import com.ismaeldivita.chipnavigation.ChipNavigationBar
import kotlinx.android.synthetic.main.activity_read_tag.view.*

class Navigation : AppCompatActivity() {
    lateinit var bind: ActivityNavigationBinding
    lateinit var navController: NavController
    lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityNavigationBinding.inflate(layoutInflater)
        val view = bind.root
        setContentView(view)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.findNavController()


        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.home, R.id.inbound, R.id.outbound, R.id.search),
            bind.container
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        //bind.menu.bottom_navigation.setupWithNavController(navController)

//        bind.menu.setOnItemSelectedListener { id ->
//            when(id){
//                R.id.home -> {
//
//                }
//            }
//        }

        savedInstanceState ?: run {
            bind.menu.showBadge(R.id.home)
            bind.menu.showBadge(R.id.search, 2)
        }


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.navigation_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }

    override fun onNavigateUp(): Boolean {
        return navController.navigateUp()
    }
}