package com.example.kotlin.windows.fragments.bar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kotlin.R
import com.example.kotlin.windows.data.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class BarViewModel(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val updateCount: (Int, Int)->Unit,
    private val loadProduct: ()->Unit,
    private val productMutableLiveData: LiveData<MutableList<Pair<Int, Product>>>,
    private val postProductMutableLiveData: (MutableList<Pair<Int, Product>>)->Unit
): ViewModel() {
    val productLiveData: LiveData<MutableList<Pair<Int, Product>>> = productMutableLiveData
    private val canBePushed = MutableLiveData<MutableMap<Int, Boolean>>(mutableMapOf())
    //////////////////////////////////////////////////////

    init {
        CoroutineScope(Dispatchers.IO).launch {
            loadProduct()
        }
    }

    private fun addOrRewriteCanBePushed(key: Int, value: Boolean) {
        canBePushed.value?.plusAssign(Pair(key, value))
        canBePushed.postValue(canBePushed.value)
    }
    //////////////////////////////////////////////////////

    fun createMainTable(): TableLayout {
        return bindMainTableParam(TableLayout(context))
    }

    private fun bindMainTableParam(table: TableLayout): TableLayout {
        table.background = context.resources.getDrawable(R.drawable.menu_frame)
        table.setPadding(20, 0, 20, 40)
        table.minimumHeight = 400
        table.gravity = Gravity.CENTER_VERTICAL
        return table
    }

    fun createTable(): TableLayout {
        return TableLayout(context)
    }
    //////////////////////////////////////////////////////

    fun createRow(): TableRow {
        return TableRow(context)
    }
    //////////////////////////////////////////////////////

    fun createImage(index: Int): ImageView {
        return bindImage(ImageView(context), index)
    }

    private fun bindImage(image: ImageView, index: Int): ImageView {
        image.setImageDrawable(context.resources.getDrawable(R.drawable.loading))
        addOrRewriteCanBePushed(productLiveData.value!![index].first, false)

        CoroutineScope(Dispatchers.IO).launch {
            var bitmap: Bitmap? = null
            try {
                val url = URL(productLiveData.value!![index].second.imageSrc)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()

                val input: InputStream = connection.inputStream
                bitmap = BitmapFactory.decodeStream(input)
            } catch (e: Exception) {
                Log.e("SOG", e.message.toString())
            }

            withContext(Dispatchers.Main) {
                if (bitmap != null) {
                    image.setImageBitmap(bitmap)
                    addOrRewriteCanBePushed(productLiveData.value!![index].first, true)
                }
                else
                    image.setImageDrawable(context.resources.getDrawable(R.drawable.sold))
            }
        }
        image.visibility = View.VISIBLE
        return image
    }

    fun bindPostImage(image: ImageView): ImageView {
        image.setPadding(10, 20, 10, 20)
        image.layoutParams.width = 330
        image.layoutParams.height = 330
        return image
    }
    //////////////////////////////////////////////////////

    fun createUpperText(): TextView {
        return bindUpperText(TextView(context))
    }

    fun createLowerText(): TextView {
        return bindLowerText(TextView(context))
    }

    private fun bindUpperText(text: TextView): TextView {
        text.setPadding(20, 0, 20, 10)
        text.maxWidth = context.resources.getDimension(R.dimen.text_w).toInt() / 2
        text.textSize = 18f
        text.setTextColor(context.resources.getColor(R.color.black))
        return text
    }

    private fun bindLowerText(text: TextView): TextView {
        text.setPadding(20, 10, 20, 10)
        text.maxWidth = context.resources.getDimension(R.dimen.text_w).toInt() / 6
        text.minWidth = context.resources.getDimension(R.dimen.text_w).toInt() / 6
        text.textSize = 18f
        text.setTextColor(context.resources.getColor(R.color.black))
        return text
    }
    //////////////////////////////////////////////////////

    fun createButton(index: Int): Button {
        return bindButton(Button(context), index)
    }

    private fun bindButton(button: Button, index: Int): Button {
        button.setPadding(10, 10, 10, 10)
        button.maxWidth = context.resources.getDimension(R.dimen.text_w).toInt() / 2
        button.textSize = 12f
        button.setTextColor(context.resources.getColor(R.color.black))
        productLiveData.observe(lifecycleOwner) {
            button.text = "Добавить в корзину (" + productLiveData.value!![index].second.count + ")"
        }

        canBePushed.observe(lifecycleOwner) {
            if (it == null)
                return@observe

            button.isClickable = it[productLiveData.value!![index].first] == true
            if (button.isClickable) {
                if (productLiveData.value!![index].second.count.toInt() == 0)
                    button.setBackgroundColor(context.resources.getColor(R.color.btn_background_color))
                else
                    button.setBackgroundColor(context.resources.getColor(R.color.btn_push_background_color))
            }
            else {
                button.setBackgroundColor(context.resources.getColor(R.color.btn_disable))
            }
        }

        button.setOnTouchListener { v, event ->
            if (!button.isClickable)
                return@setOnTouchListener(false)
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    button.setTextColor(context.resources.getColor(R.color.white))
                    button.setBackgroundColor(context.resources.getColor(R.color.btn_push_background_color))
                    productMutableLiveData.value!![index].second.count = (productMutableLiveData.value!![index].second.count.toInt() + 1).toString()
                    postProductMutableLiveData(productMutableLiveData.value!!)
                    updateCount(productLiveData.value!![index].first, productLiveData.value!![index].second.count.toInt())
                    true
                }

                MotionEvent.ACTION_UP -> {
                    button.setTextColor(context.resources.getColor(R.color.black))
                    button.setBackgroundColor(context.resources.getColor(R.color.btn_push_background_color))
                    true
                }

                else -> false
            }
        }
        return button
    }
}