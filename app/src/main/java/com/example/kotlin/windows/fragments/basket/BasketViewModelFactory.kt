package com.example.kotlin.windows.fragments.basket

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kotlin.windows.data.Product
import com.example.kotlin.windows.database.DBHelper
import com.example.kotlin.windows.special.drinksTableName
import com.example.kotlin.windows.special.foodTableName
import com.example.kotlin.windows.special.makeAlert
import com.example.kotlin.windows.special.productFiled
import com.example.kotlin.windows.special.productFiledCost
import com.example.kotlin.windows.special.productFiledCount
import com.example.kotlin.windows.special.productFiledImageSource
import com.example.kotlin.windows.special.productFiledPosition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext

class BasketViewModelFactory(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val dataBase: DBHelper
): ViewModelProvider.Factory {
    private val mutex = Mutex()
    private val productMutableLiveData = MutableLiveData<MutableMap<Int, Product>>(
        mutableMapOf()
    )

    private fun connect(tableName: String) {
        dataBase.selectTable(tableName, productFiled)
    }

    private fun getSpecificCursor(tableName: String): Cursor? {
        connect(tableName)
        return try {
            dataBase.getDataFromCurrentTableWhereByCursor(productFiledCount, ">", "0")
        }
        catch(e: Exception) {
            makeAlert(
                context = context,
                title = "Что-то пошло не так...",
                message = e.message.toString()
            )
            null
        }
    }

    private fun updateCount(id: Int, value: Int) {
        val table = if (id.toString().substring(0,1).toInt() == 1) foodTableName else drinksTableName
        connect(table)
        dataBase.updateColumnById(
            id.toString().substring(1).toInt(),
            productFiledCount, value.toString()
        )
    }

    private fun clearBasket() {
        for (table in listOf<String>(foodTableName, drinksTableName)) {
            connect(table)
            dataBase.setAllRecordWhere(
                column = productFiledCount,
                operator = ">",
                value = "0",
                columnTarget = productFiledCount,
                newValue = "0"
            )
            postProductMutableLiveData(mutableMapOf())
        }
    }

    @SuppressLint("Range")
    private fun loadProduct() {
        var supportIdPrefix = 0
        for (table in listOf<String>(foodTableName, drinksTableName)) {
            supportIdPrefix++
            val cursor = getSpecificCursor(table)
            if (cursor == null || !cursor.moveToFirst()) continue

            do {
                productMutableLiveData.value?.plusAssign(
                    mutableListOf(
                        Pair(
                            supportIdPrefix * 10 + dataBase.getId(cursor),
                            Product(
                                cursor.getString(cursor.getColumnIndex(productFiledPosition)),
                                cursor.getString(cursor.getColumnIndex(productFiledCost)),
                                cursor.getString(cursor.getColumnIndex(productFiledCount)),
                                cursor.getString(cursor.getColumnIndex(productFiledImageSource))
                            )
                        )
                    )
                )
                postProductMutableLiveData(
                    productMutableLiveData.value!!
                )
            } while (cursor.moveToNext())

            cursor.close()
        }
    }

    private fun postProductMutableLiveData(data:MutableMap<Int, Product>) {
        CoroutineScope(Dispatchers.IO).launch {
            mutex.lock()
            Log.v("SOG", "FUN postProductMutableLiveData -> mutex lock")
            withContext(Dispatchers.Main) {
                productMutableLiveData.postValue(data)
            }
            Log.v("SOG", "FUN postProductMutableLiveData -> mutex unlock")
            mutex.unlock()
        }
    }

    private fun deleteFromProductMutableLiveData(key: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            mutex.lock()
            withContext(Dispatchers.Main) {
                productMutableLiveData.value!!.remove(key)
                postProductMutableLiveData(productMutableLiveData.value!!)
            }
            mutex.unlock()
        }
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BasketViewModel(
            context = context,
            lifecycleOwner = lifecycleOwner,
            updateCount = ::updateCount,
            loadProduct = ::loadProduct,
            productMutableLiveData = productMutableLiveData,
            postProductMutableLiveData = ::postProductMutableLiveData,
            deleteFromProductMutableLiveData = ::deleteFromProductMutableLiveData,
            clearBasket = ::clearBasket
        ) as T
    }
}