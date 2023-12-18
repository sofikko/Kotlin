package com.example.kotlin.windows.fragments.basket

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import com.example.kotlin.R
import com.example.kotlin.windows.data.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Basket.newInstance] factory method to
 * create an instance of this fragment.
 */
class Basket : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val basketViewModelFactory: BasketViewModelFactory by inject{ parametersOf(requireActivity()) }
    private lateinit var basketViewModel: BasketViewModel
    private val mutex = Mutex()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_basket, container, false)
        basketViewModel = ViewModelProvider(this, basketViewModelFactory)
            .get(BasketViewModel::class.java)
        basketViewModel.productLiveData.observe(this.requireActivity()) {
            CoroutineScope(Dispatchers.IO).launch {
                mutex.lock()
                withContext(Dispatchers.Main) {
                    update(view, it)
                }
                mutex.unlock()
            }
        }
        basketViewModel.bindPayButton(
            view.findViewById<Button>(R.id.btn_basket_pay),
            view.findViewById<LinearLayout>(R.id.ll_basket)
        )
        basketViewModel.bindRezultFiled(
            view.findViewById<TextView>(R.id.basket_rez_sum)
        )
        return view
    }

    @SuppressLint("Range", "UseCompatLoadingForDrawables", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun update(view: View, mutableMap: MutableMap<Int, Product>) {
        val ll = view.findViewById<LinearLayout>(R.id.ll_basket)
        ll.removeAllViews()
        for (item in mutableMap) {
            val table = basketViewModel.createMainTable()
            //////////////////////////////////////////////////////

            val row_name = basketViewModel.createRow()

            val textView_name = basketViewModel.createUpperText()
            textView_name.text = item.value.position

            row_name.addView(textView_name)

            table.addView(row_name)
            //////////////////////////////////////////////////////

            val inner_table = basketViewModel.createTable()

            val row_cost = basketViewModel.createRow()
            //////////////////////////////////////////////////////

            val textView_cost = basketViewModel.createLowerText()
            textView_cost.text = item.value.cost
            row_cost.addView(textView_cost)
            //////////////////////////////////////////////////////

            val spec1 = basketViewModel.createInnerText()
            spec1.text = " * "
            row_cost.addView(spec1)
            //////////////////////////////////////////////////////

            val btn_minus = basketViewModel.createButton("-")
            row_cost.addView(btn_minus)
            basketViewModel.postBindButton(btn_minus)
            //////////////////////////////////////////////////////

            val cur_count = basketViewModel.createInnerText()
            basketViewModel.productLiveData.observe(requireActivity()) {
                if (it == null || it[item.key] == null) {
                    cur_count.text = "0"
                    return@observe
                }
                cur_count.text = it[item.key]!!.count
            }
            row_cost.addView(cur_count)
            //////////////////////////////////////////////////////

            val btn_plus = basketViewModel.createButton("+")
            row_cost.addView(btn_plus)
            basketViewModel.postBindButton(btn_plus)
            //////////////////////////////////////////////////////

            val spec2 = basketViewModel.createInnerText()
            spec2.text = " = "
            row_cost.addView(spec2)
            //////////////////////////////////////////////////////

            val cur_rez = basketViewModel.createInnerText()
            cur_rez.maxWidth = resources.getDimension(R.dimen.text_w).toInt() / 6 - 15
            cur_rez.minWidth = resources.getDimension(R.dimen.text_w).toInt() / 6 - 15
            basketViewModel.productLiveData.observe(requireActivity()) {
                if (it == null || it[item.key] == null) {
                    cur_rez.text = "0"
                    return@observe
                }
                cur_rez.text = (it[item.key]!!.count.toDouble() * it[item.key]!!.cost.toDouble()).toString()
            }

            row_cost.addView(cur_rez)
            //////////////////////////////////////////////////////

            val spec3 = basketViewModel.createInnerText()
            spec3.text = "₽"
            row_cost.addView(spec3)
            //////////////////////////////////////////////////////

            inner_table.addView(row_cost)

            table.addView(inner_table)

            ll.addView(table)
            //////////////////////////////////////////////////////

            basketViewModel.bindButtonOnClick(btn_minus, item.key, -1, ll, table)
            basketViewModel.bindButtonOnClick(btn_plus, item.key, +1, ll, table)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Basket.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Basket().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}