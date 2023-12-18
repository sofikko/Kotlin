package com.example.kotlin.windows.fragments.kitchen

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.kotlin.R
import com.example.kotlin.windows.data.Product
import com.example.kotlin.windows.special.productFiledCount
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
 * Use the [Kitchen.newInstance] factory method to
 * create an instance of this fragment.
 */
class Kitchen : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val kitchenViewModelFactory: KitchenViewModelFactory by inject{ parametersOf(requireActivity()) }
    private lateinit var kitchenViewModel: KitchenViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @SuppressLint("Range", "UseCompatLoadingForDrawables", "ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_kitchen, container, false)

        kitchenViewModel = ViewModelProvider(this, kitchenViewModelFactory)
            .get(KitchenViewModel::class.java)

        view.findViewById<CircularImageView>(R.id.btn_basket).setOnClickListener {
            findNavController().navigate(R.id.action_kitchen_to_basket)
        }
        kitchenViewModel.productLiveData.observe(this.requireActivity()) {
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
        val ll = view.findViewById<LinearLayout>(R.id.ll_kitchen)

        for (indexOfElement in ll.childCount until mutableList.size) {
            val table = kitchenViewModel.createMainTable()

            //////////////////////////////////////////////////////
            val row_element = kitchenViewModel.createRow()

            val image = kitchenViewModel.createImage(indexOfElement)

            row_element.addView(image)

            kitchenViewModel.bindPostImage(image)
            //////////////////////////////////////////////////////

            val table_inner = kitchenViewModel.createTable()
            val table_cost_inner = kitchenViewModel.createTable()
            val row_name = kitchenViewModel.createRow()
            val row_cost = kitchenViewModel.createRow()
            //////////////////////////////////////////////////////

            val textView_name = kitchenViewModel.createUpperText()
            textView_name.text = mutableList[indexOfElement].second.position

            row_name.addView(textView_name)
            //////////////////////////////////////////////////////

            val textView_cost = kitchenViewModel.createLowerText()
            textView_cost.text = "${mutableList[indexOfElement].second.cost}₽"

            row_cost.addView(textView_cost)
            //////////////////////////////////////////////////////

            val button_buy = kitchenViewModel.createButton(indexOfElement)

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
         * @return A new instance of fragment Kitchen.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Kitchen().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}