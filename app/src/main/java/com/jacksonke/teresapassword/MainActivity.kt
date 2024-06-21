package com.jacksonke.teresapassword


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.jacksonke.teresapassword.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Use ToolBar instead of ActionBar in current development
        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)


        // todo
        // https://stackoverflow.com/questions/58730127/view-binding-how-do-i-get-a-binding-for-included-layouts
        // include layout under viewbinding
        binding.content.tabItemTwo.setOnClickListener {
            if (navController.currentDestination!!.id != R.id.FragmentSettings){
                navController.navigate(R.id.action_FirstFragment_to_FragmentSettings)
            }
        }

        binding.content.tabItemOne.setOnClickListener {
            if (navController.currentDestination!!.id != R.id.FirstFragment){
                navController.navigate(R.id.action_FragmentSettings_to_FirstFragment)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

}