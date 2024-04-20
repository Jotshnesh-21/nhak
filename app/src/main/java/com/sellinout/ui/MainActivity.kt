package com.sellinout.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.pixplicity.easyprefs.library.Prefs
import com.sellinout.R
import com.sellinout.base.BaseActivity
import com.sellinout.databinding.ContainMainBinding
import com.sellinout.databinding.MainActivityBinding
import com.sellinout.ui.login.ActivityLogin
import com.sellinout.ui.productscan.ActivityAddItemCamera
import com.sellinout.ui.sellinout.ActivitySellInOutList
import com.sellinout.ui.stocksummary.StockSummaryFragment
import com.sellinout.utils.Const
import com.sellinout.utils.FileDownloader
import com.sellinout.utils.SharePrefsKey
import com.sellinout.utils.encodeImageToBase
import com.sellinout.utils.navigateClearStack
import com.sellinout.utils.openPdfUsingIntent
import com.sellinout.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.IOException
import java.util.concurrent.Executors


@AndroidEntryPoint
class MainActivity : BaseActivity(R.layout.main_activity) {
    private lateinit var binding: MainActivityBinding
    lateinit var bindingContainMain: ContainMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        bindingContainMain = binding.includeContainMain
        setContentView(binding.root)
        drawerInit()
        setOnClickListener()
        openFragment(ActivitySellInOutList().newInstance())
    }

    private fun drawerInit() {

        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            null,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.txtProfileName.text = SharePrefsKey.getUserData().PrintName
    }

    private fun setOnClickListener() {
        binding.txtSellInOutHistory.setOnClickListener {
            toggleDrawer()
            this.showToast("Coming Soon")
        }
        binding.txtSellOutHistory.setOnClickListener {
            toggleDrawer()
            this.showToast("Coming Soon")
        }
        binding.txtSellInHistory.setOnClickListener {
            toggleDrawer()
            // STOCK SUMMARY
            openFragment(StockSummaryFragment().newInstance())

        }
        binding.txtAddressAndPrintName.setOnClickListener {
            toggleDrawer()
            this.showToast("Coming Soon")
        }
        bindingContainMain.imgBackButton.setOnClickListener {
            toggleDrawer()
        }

        binding.btnLogout.setOnClickListener {
            toggleDrawer()
            showLogoutDlg {
                Prefs.clear()
                navigateClearStack<ActivityLogin>()
            }
        }
    }


    private fun toggleDrawer() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    fun openFragment(fragment: Fragment) {
        val backStateName: String = fragment.javaClass.name
        val fragmentPopped: Boolean = supportFragmentManager.popBackStackImmediate(backStateName, 0)
        if (!fragmentPopped) { //fragment not in back stack, create it.
            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.content_frame, fragment)
            ft.addToBackStack(backStateName)
            ft.commit()
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 1) {
            finish()
        } else {
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            -1 -> {
                val thumbnail = MediaStore.Images.Media.getBitmap(contentResolver, Const.imageUri)
                Const.imageBase = encodeImageToBase(thumbnail)
//                val strPath = Const.imageUri?.let { getPath(it, this) }
//                Const.vFilename = strPath.toString()

                startActivity(
                    Intent(
                        this, ActivityAddItemCamera::class.java
                    )
                )
            }

            else -> {
                Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }

}