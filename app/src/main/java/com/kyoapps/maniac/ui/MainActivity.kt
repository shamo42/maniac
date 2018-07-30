package com.kyoapps.maniac.ui

import android.content.res.Resources
import android.os.Bundle
import android.support.v4.widget.DrawerLayout
import android.support.v4.widget.SlidingPaneLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.util.TypedValue
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.kyoapps.maniac.R
import com.kyoapps.maniac.dagger.components.DaggerActivityComponent
import com.kyoapps.maniac.dagger.modules.ContextModule
import com.kyoapps.maniac.helpers.C_SETTINGS

class MainActivity : AppCompatActivity() {
    private var drawerLayout: DrawerLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val component = DaggerActivityComponent.builder()
                .contextModule(ContextModule(this))
                .build()

        setTheme(if (component.defaultSettings.getBoolean(C_SETTINGS.NIGHT_MODE, true)) R.style.AppCompat_Night else R.style.AppCompat_Day )

        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        val toolbar = findViewById<Toolbar>(R.id.tb_main)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)


        val host: NavHostFragment = supportFragmentManager
                .findFragmentById(R.id.main_host_frag_threads) as NavHostFragment? ?: return

        // Set up SlidingPane fade color
        val typedValue = TypedValue()
        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)
        (findViewById<SlidingPaneLayout>(R.id.pane_main))?.sliderFadeColor = typedValue.data


        // Set up Action Bar
        val navController = host.navController


        navController.addOnNavigatedListener { _, destination ->
            val dest: String = try {
                resources.getResourceName(destination.id)
            } catch (e: Resources.NotFoundException) {
                Integer.toString(destination.id)
            }
            Log.d(TAG, "Navigated to $dest")
        }
    }

   /* private fun setupBottomNavMenu(navController: NavController) {
        findViewById<BottomNavigationView>(R.id.bottom_nav_view)?.let { bottomNavView ->
            NavigationUI.setupWithNavController(bottomNavView, navController)
        }
    }

    private fun setupNavigationMenu(navController: NavController) {
        findViewById<NavigationView>(R.id.nav_view)?.let { navigationView ->
            NavigationUI.setupWithNavController(navigationView, navController)
        }
    }

    private fun setupActionBar(navController: NavController) {
        drawerLayout = findViewById(R.id.drawer_layout)

        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val retValue = super.onCreateOptionsMenu(menu)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        // The NavigationView already has these same navigation items, so we only add
        // navigation items to the menu here if there isn't a NavigationView
        if (navigationView == null) {
            menuInflater.inflate(R.menu.menu_overflow, menu)
            return true
        }
        return retValue
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Have the NavHelper look for an action or destination matching the menu
        // item id and navigate there if found.
        // Otherwise, bubble up to the parent.
        return NavigationUI.onNavDestinationSelected(item,
                Navigation.findNavController(this, R.id.main_host_frag_threads))
                || super.onOptionsItemSelected(item)
    }*/

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(drawerLayout,
                Navigation.findNavController(this, R.id.main_host_frag_threads))
    }


    override fun onBackPressed() {
        val slidingPaneLayout: SlidingPaneLayout? = findViewById(R.id.pane_main)
        if (slidingPaneLayout != null &&!slidingPaneLayout.isOpen && slidingPaneLayout.isSlideable) slidingPaneLayout.openPane()
            else super.onBackPressed()
    }

    companion object {
        const val TAG = "MainActivity"
    }
}
