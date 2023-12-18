package com.example.kotlin.windows.support

import androidx.fragment.app.FragmentActivity
import java.lang.Exception

interface BasketClickHelper {
    fun onClickBasketHost() {
        throw Exception("Must be override")
    }
    fun onClickBasketClient(activity: FragmentActivity) {
        val communicate = activity as BasketClickHelper
        communicate.onClickBasketHost()
    }
}