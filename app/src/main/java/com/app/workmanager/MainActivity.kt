package com.app.workmanager

import android.Manifest.permission
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.app.workmanager.databinding.ActivityMainBinding
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit


class MainActivity : BaseActivity<ActivityMainBinding>() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        if (!checkLocationPermission()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(permission.ACCESS_COARSE_LOCATION, permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_CODE
            )
        }
        try {
            if (isWorkScheduled(WorkManager.getInstance().getWorkInfosByTag(TAG).get())) {
                binding.appCompatButtonStart.setText(getString(R.string.button_text_stop))
                binding.message.setText(getString(R.string.message_worker_running))
                binding.logs.setText(getString(R.string.log_for_running))
            } else {
                binding.appCompatButtonStart.setText(getString(R.string.button_text_start))
                binding.message.setText(getString(R.string.message_worker_stopped))
                binding.logs.setText(getString(R.string.log_for_stopped))
            }
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        binding.appCompatButtonStart.setOnClickListener {
            if (binding.appCompatButtonStart.getText()
                    .toString() == getString(R.string.button_text_start)
            ) {
                // START Worker
                val periodicWork: PeriodicWorkRequest =
                    PeriodicWorkRequest.Builder(MyWorker::class.java, 15, TimeUnit.MINUTES)
                        .addTag(TAG)
                        .build()
                WorkManager.getInstance().enqueueUniquePeriodicWork(
                    "Location",
                    ExistingPeriodicWorkPolicy.REPLACE,
                    periodicWork
                )
                Toast.makeText(
                    this@MainActivity,
                    "Location Worker Started : " + periodicWork.id,
                    Toast.LENGTH_SHORT
                ).show()
                binding.appCompatButtonStart.setText(getString(R.string.button_text_stop))
                binding.message.setText(periodicWork.id.toString())
                binding.logs.setText(getString(R.string.log_for_running))
            } else {
                WorkManager.getInstance().cancelAllWorkByTag(TAG)
                binding.appCompatButtonStart.setText(getString(R.string.button_text_start))
                binding.message.setText(getString(R.string.message_worker_stopped))
                binding.logs.setText(getString(R.string.log_for_stopped))
            }
        }


    }

    override fun createBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    private fun isWorkScheduled(workInfos: List<WorkInfo>?): Boolean {
        var running = false
        if (workInfos == null || workInfos.size == 0) return false
        for (workStatus in workInfos) {
            running =
                (workStatus.state == WorkInfo.State.RUNNING) or (workStatus.state === WorkInfo.State.ENQUEUED)
        }
        return running
    }

    /**
     * All about permission
     */
    private fun checkLocationPermission(): Boolean {
        val result3 = ContextCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION)
        val result4 = ContextCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION)
        return result3 == PackageManager.PERMISSION_GRANTED && result4 == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.size > 0) {
                val coarseLocation = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val fineLocation = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (coarseLocation && fineLocation ) Toast.makeText(
                    this,
                    "Permission Granted",
                    Toast.LENGTH_SHORT
                ).show() else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 200
        private const val TAG = "LocationUpdate"
    }


    fun getNotificationPermission() {
        try {
            if (Build.VERSION.SDK_INT > 32) {
                ActivityCompat.requestPermissions(
                    this, arrayOf<String>(permission.POST_NOTIFICATIONS),
                    PERMISSION_REQUEST_CODE
                )
            }
        } catch (e: Exception) {
        }
    }

}

