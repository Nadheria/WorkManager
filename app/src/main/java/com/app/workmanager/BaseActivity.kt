package com.app.workmanager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {
    private var _binding: VB? = null
    protected val binding: VB
        get() = _binding ?: error("View binding is not initialized")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = createBinding()
        setContentView(binding.root)
        setupViews()


    }

    abstract fun createBinding(): VB

    open fun setupViews() {



    }



    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}