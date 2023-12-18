package com.example.kotlin.windows.koin

import android.app.Activity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.example.kotlin.windows.database.DBHelper
import com.example.kotlin.windows.fragments.bar.BarViewModelFactory
import com.example.kotlin.windows.fragments.basket.BasketViewModelFactory
import com.example.kotlin.windows.fragments.kitchen.KitchenViewModelFactory
import com.example.kotlin.windows.hello.HelloViewModelFactory
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val moduleApp = module {
    single {
        DBHelper(androidContext())
    }

    factory {(activity: Activity) ->
        HelloViewModelFactory(activity, activity as LifecycleOwner, get())
    }

    factory {(activity: Activity) ->
        KitchenViewModelFactory(activity, activity as LifecycleOwner, get())
    }

    factory {(activity: Activity) ->
        BarViewModelFactory(activity, activity as LifecycleOwner, get())
    }

    factory { (activity: Activity) ->
        BasketViewModelFactory(activity, activity as LifecycleOwner, get())
    }
}