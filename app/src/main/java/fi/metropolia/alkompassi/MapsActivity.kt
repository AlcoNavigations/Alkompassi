package fi.metropolia.alkompassi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.navigation.fragment.NavHostFragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import fi.metropolia.alkompassi.ui.favorite.FavoriteFragment
import fi.metropolia.alkompassi.ui.maps.MapsFragment


class MapsActivity : AppCompatActivity(){

    private lateinit var fragmentPagerAdapter: FragmentPagerAdapter
    private lateinit var mViewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    private lateinit var nearbyFragment: MapsFragment
    private lateinit var favoriteFragment: FavoriteFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.maps_activity)
        nearbyFragment = MapsFragment.newInstance()
        favoriteFragment = FavoriteFragment.newInstance()

        fragmentPagerAdapter = MapsFragmentPagerAdapter(
                supportFragmentManager,
                mapOf("nearby" to nearbyFragment, "favorite" to favoriteFragment))
        mViewPager = findViewById(R.id.pager)
        mViewPager.adapter = fragmentPagerAdapter
        tabLayout = findViewById(R.id.tabs)
        tabLayout.setupWithViewPager(mViewPager)

        val finalHost = NavHostFragment.create(R.navigation.nav_graph)
        supportFragmentManager.beginTransaction()
                .replace(R.id.pager, finalHost)
                .setPrimaryNavigationFragment(finalHost) // this is the equivalent to app:defaultNavHost="true"
                .commit()

        val navController = NavHostFragment.findNavController(finalHost)

    }

    class MapsFragmentPagerAdapter(fragmentManager: FragmentManager?, val fragments: Map<String, Fragment>) : FragmentPagerAdapter(fragmentManager) {
        override fun getItem(position: Int): Fragment? {
            return when(position) {
                0 -> fragments["nearby"]
                1 -> fragments["favorite"]
                else -> null
            }
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return when(position) {
                0 -> "Nearby"
                1 -> "Favorite"
                else -> null
            }
        }

        override fun getCount(): Int  = 2

    }


}
