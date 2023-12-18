package com.example.kotlin.windows.support

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET

public interface API {
    @GET("/raw/food-62784")
    fun getFood(): Call<ResponseBody>

    @GET("/raw/drinks-629")
    fun getDrinks(): Call<ResponseBody>
}