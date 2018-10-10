package fi.metropolia.alkompassi

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import fi.metropolia.alkompassi.ui.favorite.FavoriteFragment
import fi.metropolia.alkompassi.ui.maps.MapsFragment
import kotlinx.android.synthetic.main.maps_activity.*


class MapsActivity : AppCompatActivity() {

    private lateinit var fragmentPagerAdapter: FragmentPagerAdapter
    private lateinit var mViewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    private lateinit var nearbyFragment: MapsFragment
    private lateinit var favoriteFragment: FavoriteFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.maps_activity)
        supportActionBar?.hide()

        nearbyFragment = MapsFragment.newInstance()
        favoriteFragment = FavoriteFragment.newInstance()

        if ((ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 0)
        }

        fragmentPagerAdapter = MapsFragmentPagerAdapter(
                supportFragmentManager,
                mapOf("nearby" to nearbyFragment, "favorite" to favoriteFragment))

        mViewPager = findViewById(R.id.pager)
        mViewPager.adapter = fragmentPagerAdapter
        tabLayout = findViewById(R.id.tabs)
        tabLayout.setupWithViewPager(mViewPager)

        // Replace the default tab slide animation with fadeOut/fadeIn
        pager.setPageTransformer(true) { page, position ->
            if(position <= -1.0F || position >= 1.0F) {
                page.translationX = page.width * position
                page.alpha = 0.0F
            } else if( position == 0.0F ) {
                page.translationX = page.width * position
                page.alpha = 1.0F
            } else {
                // position is between -1.0F & 0.0F OR 0.0F & 1.0F
                page.translationX = page.width * -position
                page.alpha = 1.0F - Math.abs(position)
            }
        }

        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> nearbyFragment.zoomToLocation()
                    1 -> {
                        favoriteFragment.updateFavorites()
                        favoriteFragment.zoomToLocation()
                    }
                }
            }
        })
    }

    class MapsFragmentPagerAdapter(fragmentManager: FragmentManager?, private val fragments: Map<String, Fragment>) : FragmentPagerAdapter(fragmentManager) {
        override fun getItem(position: Int): Fragment? {
            return when (position) {
                0 -> fragments["nearby"]
                1 -> fragments["favorite"]
                else -> null
            }
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return when (position) {
                0 -> "Nearby"
                1 -> "Favorite"
                else -> null
            }
        }

        override fun getCount(): Int = 2
    }
}
