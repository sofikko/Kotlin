package com.example.kotlin.windows.hello

import android.app.Activity
import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kotlin.windows.data.Product
import com.example.kotlin.windows.database.DBHelper
import com.example.kotlin.windows.special.drinksTableName
import com.example.kotlin.windows.special.foodTableName
import com.example.kotlin.windows.special.productFiled
import com.example.kotlin.windows.special.retrofit
import com.example.kotlin.windows.support.API
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit

class HelloViewModel(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val load: (call: Call<ResponseBody>, callback: (String?)->Unit)->Unit,
    private val parser: (str: String)->List<Product>,
    private val saveProductList: (
        productList: List<Product>,
        tableName: String,
        tableFiled: List<Pair<String, String>>,
        reset: Boolean)->Unit
) : ViewModel() {
    private val countLoadingProcess: Int = 2

    private val loadedDrinksLiveDataMutableCode = MutableLiveData<Int>(0)
    private val loadedFoodLiveDataMutableCode = MutableLiveData<Int>(0)

    private val allLoadedLiveDataMutable = MutableLiveData<Map<String, Int>>(mapOf())
    val allLoadedLiveData: LiveData<Map<String, Int>> = allLoadedLiveDataMutable

    private fun addMap(pair: Pair<String, Int>) {
        allLoadedLiveDataMutable.value =
            (allLoadedLiveDataMutable.value?: mapOf()) + pair
    }

    fun loadProduct() {
        loadedDrinksLiveDataMutableCode.observe(lifecycleOwner) {
            addMap(Pair(drinksTableName, loadedDrinksLiveDataMutableCode.value!!))
        }

        loadedFoodLiveDataMutableCode.observe(lifecycleOwner) {
            addMap(Pair(foodTableName, loadedFoodLiveDataMutableCode.value!!))
        }

        CoroutineScope(Dispatchers.IO).launch {
            val service = retrofit.create(API::class.java)

            async {
               load(service.getDrinks()) {
                   if (it != null) {
                       saveProductList(
                           parser(it),
                           drinksTableName,
                           productFiled,
                           true
                       )
                   }
                   loadedDrinksLiveDataMutableCode.postValue(if (it != null) 1 else -1)
               }
            }
            async {
                load(service.getFood()) {
                    if (it != null) {
                        saveProductList(
                            parser(it),
                            foodTableName,
                            productFiled,
                            true
                        )
                    }
                    loadedFoodLiveDataMutableCode.postValue(if (it != null) 1 else -1)
                }
            }
        }
    }

    fun allLoaded(): Boolean {
        return allLoadedLiveData.value!!.size == countLoadingProcess
    }

}