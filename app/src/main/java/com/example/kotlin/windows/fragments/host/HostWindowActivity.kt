package com.example.kotlin.windows.fragments.host

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.kotlin.windows.support.BasketClickHelper
import com.example.kotlin.R
import com.example.kotlin.windows.fragments.basket.Basket

class HostWindowActivity : AppCompatActivity(), BasketClickHelper {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_window)
    }

    override fun onClickBasketHost() {
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainerMenu, Basket())
        transaction.addToBackStack(null);
        transaction.commit()
    }
}