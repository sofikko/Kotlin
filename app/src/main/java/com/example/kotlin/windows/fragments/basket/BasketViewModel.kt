package com.example.kotlin.windows.fragments.basket

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kotlin.R
import com.example.kotlin.windows.data.Product
import com.example.kotlin.windows.special.makeAlert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BasketViewModel(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val updateCount: (Int, Int)->Unit,
    private val loadProduct: ()->Unit,
    private val productMutableLiveData: MutableLiveData<MutableMap<Int, Product>>,
    private val postProductMutableLiveData: (MutableMap<Int, Product>)->Unit,
    private val deleteFromProductMutableLiveData: (Int)->Unit,
    private val clearBasket: ()->Unit
): ViewModel() {
    val productLiveData: LiveData<MutableMap<Int, Product>> = productMutableLiveData
    //////////////////////////////////////////////////////

    init {
        CoroutineScope(Dispatchers.IO).launch {
            loadProduct()
        }
    }

    private fun pay() {
        makeAlert(
            context = context,
            title = "Спсибо за заказ!",
            message = "Скоро с вами свяжется менеджер для уточнения деталей заказа.",
            buttonsList = listOf(
                Pair("Хорошо", {})
            )
        )
        clearBasket()
    }
    //////////////////////////////////////////////////////

    fun bindPayButton(button: Button, elementsLayout: LinearLayout) {
        button.setOnClickListener {
            elementsLayout.removeAllViews()
            pay()
        }
    }

    fun bindRezultFiled(text: TextView) {
        productLiveData.observe(lifecycleOwner) {
            if (it == null) {
                text.text = "0"
                return@observe
            }

            var sum = 0.0
            for (element in it)
                sum += element.value.cost.toDouble() * element.value.count.toDouble()

            text.text = sum.toString()
        }
    }
    //////////////////////////////////////////////////////

    fun createText(): TextView {
        return TextView(context)
    }

    fun createUpperText(): TextView {
        return bindUpperText(createText())
    }

    private fun bindUpperText(text: TextView): TextView {
        text.setPadding(20, 0, 20, 10)
        text.maxWidth = context.resources.getDimension(R.dimen.text_w).toInt()
        text.textSize = 18f
        text.setTextColor(context.resources.getColor(R.color.black))
        return text
    }

    fun createLowerText(): TextView {
        return bindLowerText(createText())
    }

    private fun bindLowerText(text: TextView): TextView {
        text.setPadding(20, 10, 20, 10)
        text.maxWidth = context.resources.getDimension(R.dimen.text_w).toInt() / 5 - 15
        text.minWidth = context.resources.getDimension(R.dimen.text_w).toInt() / 7 - 15
        text.textSize = 18f
        text.setTextColor(context.resources.getColor(R.color.black))
        return text
    }

    fun createInnerText(): TextView {
        return bindInnerText(createText())
    }

    private fun bindInnerText(text: TextView): TextView {
        text.setPadding(20, 10, 10, 10)
        text.textSize = 18f
        text.setTextColor(context.resources.getColor(R.color.black))
        return text
    }
    //////////////////////////////////////////////////////

    fun createTable(): TableLayout {
        return TableLayout(context)
    }

    fun createMainTable(): TableLayout {
        return bindMainTable(createTable())
    }

    private fun bindMainTable(table: TableLayout): TableLayout {
        table.setPadding(20, 0, 20, 40)
        table.minimumHeight = 200
        table.gravity = Gravity.CENTER_VERTICAL
        return table
    }
    //////////////////////////////////////////////////////

    fun createRow(): TableRow {
        return TableRow(context)
    }
    //////////////////////////////////////////////////////

    fun createButton(characters: String): Button {
        return bindButton(Button(context), characters)
    }

    private fun bindButton(button: Button, characters: String): Button {
        button.setBackgroundColor(context.resources.getColor(R.color.btn_push_background_color))
        button.setText(characters)
        button.setTextColor(context.resources.getColor(R.color.text_background_color))
        button.textSize = 18f
        button.gravity = Gravity.CENTER
        return button
    }

    fun postBindButton(button: Button): Button {
        button.layoutParams.width = 100
        button.layoutParams.height = 100
        return button
    }

    fun bindButtonOnClick(
        button: Button,
        key: Int,
        action: Int,
        elementsLayout: LinearLayout,
        association: View,
    ): Button {
        button.setOnClickListener {
            productMutableLiveData.value!![key]!!.count = (
                    productMutableLiveData.value!![key]!!.count.toInt() + action
                    ).toString()
            postProductMutableLiveData(productMutableLiveData.value!!)
            updateCount(key, productLiveData.value!![key]!!.count.toInt())
            if (productLiveData.value!![key]!!.count.toInt() == 0) {
                elementsLayout.removeView(association)
                deleteFromProductMutableLiveData(key)
            }
        }
        return button
    }
}