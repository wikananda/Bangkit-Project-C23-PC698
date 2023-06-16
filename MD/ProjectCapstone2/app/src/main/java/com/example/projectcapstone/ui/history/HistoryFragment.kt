package com.example.projectcapstone.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectcapstone.ClothesModel
import com.example.projectcapstone.R
import com.example.projectcapstone.ResultModel
import com.example.projectcapstone.adapter.ImageAdapter
import com.example.projectcapstone.adapter.ResultAdapter
import com.example.projectcapstone.databinding.FragmentHistoryBinding
import com.example.projectcapstone.databinding.FragmentHomeBinding

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private lateinit var adapter: ResultAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var resultArrayList: ArrayList<ResultModel>
    lateinit var dummyResult: Array<Int>
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataInitialize()
        val layoutManager = GridLayoutManager(context,2, LinearLayoutManager.VERTICAL, false)
        recyclerView = view.findViewById(R.id.rv_history)
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)
        adapter = ResultAdapter(resultArrayList)
        recyclerView.adapter = adapter
    }

    // data dummy untuk ditampilkan pada fragment history
    private fun dataInitialize(){
        resultArrayList = arrayListOf<ResultModel>()

        dummyResult = arrayOf(
            R.drawable.orang1,
            R.drawable.orang2,
            R.drawable.orang3,
            R.drawable.orang4,
            R.drawable.orang5,
            R.drawable.orang6,
            R.drawable.orang7,
            R.drawable.orang8
        )
        for (i in dummyResult.indices){
            val result = ResultModel(dummyResult[i])
            resultArrayList.add(result)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}