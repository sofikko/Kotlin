package com.example.kotlin.windows.special

import retrofit2.Retrofit

const val foodTableName = "Food"
const val drinksTableName = "Drink"

const val productFiledPosition = "position"
const val productFiledCost = "cost"
const val productFiledCount = "count"
const val productFiledImageSource = "image"

val productFiled = listOf<Pair<String,String>>(
    Pair(productFiledPosition, "TEXT"),
    Pair(productFiledCost, "TEXT"),
    Pair(productFiledCount, "TEXT"),
    Pair(productFiledImageSource, "TEXT")
)

val retrofit: Retrofit = Retrofit.Builder()
    .baseUrl("https://text-host.ru")
    .build()