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
import fi.metropolia.alkompassi.ar.ArFragment
import fi.metropolia.alkompassi.ui.favorite.FavoriteFragment
import fi.metropolia.alkompassi.ui.maps.MapsFragment
import kotlinx.android.synthetic.main.maps_activity.*
import android.content.Intent
import android.net.Uri
import fi.metropolia.alkompassi.data.TempData


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
        floatingActionButton.hide()
        floatingActionButtonDirections.hide()

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

        floatingActionButton.setOnClickListener {
            val fragManager = supportFragmentManager
            fragManager.beginTransaction().replace(R.id.container, ArFragment()).commitNow()
        }

        floatingActionButtonDirections.setOnClickListener{
            val intent = Intent(android.content.Intent.ACTION_VIEW, Uri.parse(
                    "http://maps.google.com/maps?saddr=${TempData.myLat},${TempData.myLng}&daddr=${TempData.alkoLat},${TempData.alkoLng}"))
            startActivity(intent)
        }
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
