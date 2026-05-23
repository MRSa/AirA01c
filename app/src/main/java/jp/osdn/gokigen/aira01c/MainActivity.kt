package jp.osdn.gokigen.aira01c

import android.Manifest.permission
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import jp.osdn.gokigen.aira01c.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity()
{
    private lateinit var binding: ActivityMainBinding

    // 権限リクエスト用のランチャーをメンバ変数として定義
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // 結果の判定
        if (allPermissionsGranted()) {
            setupNavigation()
        } else {
            // 拒否された場合
            Log.v(TAG, "----- onRequestPermissionsResult() : false")
            Toast.makeText(this, R.string.permission_not_granted, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- 権限チェックとリクエストの実行
        if (allPermissionsGranted()) {
            setupNavigation()
        } else {
            requestPermissionLauncher.launch(getRequiredPermissions())
        }
    }

    private fun setupNavigation() {
        // NavController の取得（FragmentContainerView を使用している前提）
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_tips)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)
    }

    private fun allPermissionsGranted(): Boolean {
        return getRequiredPermissions().all {
            ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    // 実行中のデバイスのOSバージョンに必要な権限だけを抽出する
    private fun getRequiredPermissions(): Array<String> {
        val permissions = mutableListOf(
            permission.INTERNET,
            permission.VIBRATE,
            permission.ACCESS_NETWORK_STATE,
            permission.ACCESS_WIFI_STATE,
            permission.ACCESS_FINE_LOCATION,
            permission.ACCESS_COARSE_LOCATION
        )

        // ----- OSバージョンに応じた必要なパーミッションの追加
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        {
            // ---- Android 10 (SDK: 29)
            permissions.add(permission.ACCESS_MEDIA_LOCATION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        {
            // ---- Android 12 (SDK: 31)
            permissions.add(permission.BLUETOOTH_SCAN)
            permissions.add(permission.BLUETOOTH_CONNECT)
        } else {
            permissions.add(permission.BLUETOOTH)
            permissions.add(permission.BLUETOOTH_ADMIN)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            // ---- Android 13 (SDK: 33)
            permissions.add(permission.NEARBY_WIFI_DEVICES)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN)
        {
            // ---- Android 17 (SDK: 37)
            permissions.add(permission.ACCESS_LOCAL_NETWORK)
        }
        return permissions.toTypedArray()
    }

    companion object
    {
        private val TAG = MainActivity::class.java.simpleName
    }
}
