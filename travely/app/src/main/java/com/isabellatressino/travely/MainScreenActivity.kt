package com.isabellatressino.travely

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.isabellatressino.travely.databinding.ActivityMainScreenBinding
import com.isabellatressino.travely.fragments.HomeFragment
import com.isabellatressino.travely.fragments.MapFragment
import com.isabellatressino.travely.fragments.ProfileFragment

class MainScreenActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainScreenBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    loadFragment(HomeFragment())
                    true
                }

                R.id.navigation_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }

                R.id.navigation_local -> {
                    loadFragment(MapFragment())
                    true
                }

                else -> false
            }
        }

        loadFragment(HomeFragment())

    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.replace(R.id.fragment_content, fragment)
        transaction.commit()
    }

}