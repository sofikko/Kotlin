package com.example.kotlin.windows.fragments.bar

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

class BarViewModelFactory(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val dataBase: DBHelper
): ViewModelProvider.Factory {
    private val mutex = Mutex()
    private val productMutableLiveData = MutableLiveData<MutableList<Pair<Int, Product>>>(
        mutableListOf()
    )

    private fun connect() {
        dataBase.selectTable(drinksTableName, productFiled)
    }

    private fun getCursor(): Cursor? {
        connect()
        return try {
            dataBase.getAllDataFromCurrentTableByCursor()
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
        dataBase.updateColumnById(id, productFiledCount, value.toString())
    }

    @SuppressLint("Range")
    private fun loadProduct() {
        val cursor = getCursor()
        if (cursor == null || !cursor.moveToFirst()) return

        do {
            productMutableLiveData.value?.plusAssign(
                mutableListOf(Pair(
                    dataBase.getId(cursor),
                    Product(
                        cursor.getString(cursor.getColumnIndex(productFiledPosition)),
                        cursor.getString(cursor.getColumnIndex(productFiledCost)),
                        cursor.getString(cursor.getColumnIndex(productFiledCount)),
                        cursor.getString(cursor.getColumnIndex(productFiledImageSource))
                    )
                ))
            )
            postProductMutableLiveData(
                productMutableLiveData.value!!
            )
        } while (cursor.moveToNext())

        cursor.close()
    }

    private fun postProductMutableLiveData(data:MutableList<Pair<Int, Product>>) {
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

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BarViewModel(
            context = context,
            lifecycleOwner = lifecycleOwner,
            updateCount = ::updateCount,
            loadProduct = ::loadProduct,
            productMutableLiveData = productMutableLiveData,
            postProductMutableLiveData = ::postProductMutableLiveData
        ) as T
    }
}