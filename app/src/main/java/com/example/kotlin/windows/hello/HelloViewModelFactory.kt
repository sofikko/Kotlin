package com.example.kotlin.windows.hello

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kotlin.windows.data.Product
import com.example.kotlin.windows.database.DBHelper
import com.example.kotlin.windows.special.productFiled
import com.example.kotlin.windows.special.productFiledCost
import com.example.kotlin.windows.special.productFiledCount
import com.example.kotlin.windows.special.productFiledImageSource
import com.example.kotlin.windows.special.productFiledPosition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HelloViewModelFactory(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val dataBase: DBHelper
) : ViewModelProvider.Factory {
    private val mutex = Mutex()


    private fun load(call: Call<ResponseBody>, callback: (String?)->Unit) {
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    callback(response.body()?.string())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.message?.let { Log.e("MyT", it) }
                callback(null)
            }
        })
    }

    private fun parser(str: String) : List<Product> {
        val mutableList = mutableListOf<Product>()

        var items =  str
            .replace("\n","")
            .replace("[", "")
            .replace("\"", "")
            .replace(",", "")
            .split("]]")
            .toList()

        items = items.subList(0, items.size - 1)

        for (productString in items) {
            val productItems = productString.split("]").toList()
            mutableList.add(
                Product(
                productItems[0],
                productItems[1],
                productItems[2],
                productItems[3]
            )
            )
        }

        return mutableList
    }

    private fun saveProductList(
        productList: List<Product>,
        tableName: String,
        tableFiled: List<Pair<String, String>>,
        reset: Boolean = false
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            mutex.lock()
            Log.v("SOG", "FUN saveProductList -> mutex lock")
            try {
                dataBase.selectTable(tableName, tableFiled)

                if (reset)
                    dataBase.clearSelectedTable()

                for (product in productList)
                    dataBase.addDataToCurrentTable(
                        getWritableProduct(product)
                    )
            } finally {
                Log.v("SOG", "FUN saveProductList -> mutex unlock")
                mutex.unlock()
            }
        }
    }

    private fun getWritableProduct(product: Product): List<Pair<String,String>> {
        return listOf(
            Pair(productFiledPosition, product.position),
            Pair(productFiledCost, product.cost),
            Pair(productFiledCount, product.count),
            Pair(productFiledImageSource, product.imageSrc)
        )
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HelloViewModel(
            context = context,
            lifecycleOwner = lifecycleOwner,
            load = ::load,
            parser = ::parser,
            saveProductList = ::saveProductList
        ) as T
    }
}