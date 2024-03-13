package com.palash.posedetectionusingmlkit

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.palash.posedetectionusingmlkit.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityMainBinding
    private val REQUESTCODE_PERMISSIONS = 3
    private val permissions = arrayListOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mBinding.btnSingleMode.setOnClickListener {
            startActivity(Intent(this, SingleImageActivity::class.java))
        }
        if (!isPermissionGranted()) {
            askPendingPermission()
        }
    }

    private fun askPendingPermission() {
        val permissionToAsk = ArrayList<String>()
        for (permission in permissions) {
            if (!isGranted(permission)) {
                permissionToAsk.add(permission)
            }
        }
        if (permissionToAsk.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionToAsk.toTypedArray(),
                REQUESTCODE_PERMISSIONS
            )
        }
    }

    private fun isPermissionGranted(): Boolean {
        for (permission in permissions)
            if (!isGranted(permission)) {
                return false
            }
        return true
    }

    private fun isGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED

    }
}