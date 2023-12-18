package com.example.kotlin.windows.fragments.bar

import com.example.kotlin.windows.database.DBHelper
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.kotlin.R
import com.example.kotlin.windows.data.Product
import com.mikhaellopez.circularimageview.CircularImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Bar.newInstance] factory method to
 * create an instance of this fragment.
 */
class Bar : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val barViewModelFactory: BarViewModelFactory by inject{ parametersOf(requireActivity()) }
    private lateinit var barViewModel: BarViewModel

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
        val view = inflater.inflate(R.layout.fragment_bar, container, false)

        view.findViewById<CircularImageView>(R.id.btn_basket).setOnClickListener {
            findNavController().navigate(R.id.action_bar_to_basket)
        }

        barViewModel = ViewModelProvider(this, barViewModelFactory)
            .get(BarViewModel::class.java)
        barViewModel.productLiveData.observe(this.requireActivity()) {
            update(view, it)
        }
        return view
    }

    @SuppressLint("Range")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    ///////////TABLE////////////// All it's row_element
    //  BIG   |______NAME_______// After image it's table_inner
    // IMAGE  |  COST  | BUTTON // In table_inner (bellow row_name) table_cost_inner
    //////////////////////////////
    private fun update(view: View, mutableList: MutableList<Pair<Int, Product>>) {
        val context = this.context
        val ll = view.findViewById<LinearLayout>(R.id.ll_bar)

        for (indexOfElement in ll.childCount until mutableList.size) {
            val table = barViewModel.createMainTable()

            //////////////////////////////////////////////////////
            val row_element = barViewModel.createRow()

            val image = barViewModel.createImage(indexOfElement)

            row_element.addView(image)

            barViewModel.bindPostImage(image)
            //////////////////////////////////////////////////////

            val table_inner = barViewModel.createTable()
            val table_cost_inner = barViewModel.createTable()
            val row_name = barViewModel.createRow()
            val row_cost = barViewModel.createRow()
            //////////////////////////////////////////////////////

            val textView_name = barViewModel.createUpperText()
            textView_name.text = mutableList[indexOfElement].second.position

            row_name.addView(textView_name)
            //////////////////////////////////////////////////////

            val textView_cost = barViewModel.createLowerText()
            textView_cost.text = "${mutableList[indexOfElement].second.cost}₽"

            row_cost.addView(textView_cost)
            //////////////////////////////////////////////////////

            val button_buy = barViewModel.createButton(indexOfElement)

            row_cost.addView(button_buy)
            //////////////////////////////////////////////////////

            table_inner.addView(row_name)
            table_cost_inner.addView(row_cost)
            table_inner.addView(table_cost_inner)

            row_element.addView(table_inner)

            table.addView(row_element)
            //////////////////////////////////////////////////////

            ll.addView(table)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Bar.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Bar().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}