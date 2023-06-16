package com.example.projectcapstone.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectcapstone.ClothesModel
import com.example.projectcapstone.adapter.ImageAdapter
import com.example.projectcapstone.R
import com.example.projectcapstone.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private lateinit var adapter: ImageAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var clotheArrayList: ArrayList<ClothesModel>
    lateinit var image: Array<Int>
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataInitialize()
        val layoutManager = GridLayoutManager(context,2, LinearLayoutManager.VERTICAL, false)
        recyclerView = view.findViewById(R.id.recycleview)
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)
        adapter = ImageAdapter(clotheArrayList)
        recyclerView.adapter = adapter
    }

    // data dummy untuk ditampilkan pada fragment home
    private fun dataInitialize(){
        clotheArrayList = arrayListOf<ClothesModel>()

        image = arrayOf(
            R.drawable.baju1,
            R.drawable.baju2,
            R.drawable.baju3,
            R.drawable.baju4,
            R.drawable.baju5,
            R.drawable.baju6,
            R.drawable.baju7,
            R.drawable.baju8,
            R.drawable.baju9,
            R.drawable.baju10,
            R.drawable.baju11,
            R.drawable.baju12
        )
        for (i in image.indices){
            val clothes = ClothesModel(image[i])
            clotheArrayList.add(clothes)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
/*
    private var _binding: FragmentHomeBinding? = null
    private lateinit var rvImage: RecyclerView
    private val list = ArrayList<ImageModel>()


    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
       /* val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
*/
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        rvImage.findViewById<RecyclerView>(R.id.recycleview)
        rvImage.layoutManager = GridLayoutManager(context,2)
        val imageAdapter = ImageAdapter(list)
        rvImage.adapter = imageAdapter
        return view



    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // list.addAll(DataDummy.listData)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
         super.onViewCreated(view, savedInstanceState)


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }*/
}