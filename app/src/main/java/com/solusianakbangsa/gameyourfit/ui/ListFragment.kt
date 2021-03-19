package com.solusianakbangsa.gameyourfit.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.solusianakbangsa.gameyourfit.R
import kotlin.jvm.java

abstract class ListFragment<T>() : Fragment() {
    abstract val layout : Int
    abstract val layoutContentId : Int

    lateinit var contentLayout : LinearLayout
    lateinit var viewModel: ListViewModel<T>
    lateinit var inflater : LayoutInflater

    abstract fun createView(args : T)
    abstract fun loadEntries()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(layout, container, false)
        contentLayout = root.findViewById(layoutContentId)

//        var test : Button = contentLayout.findViewById(R.id.testButton)
//        test.setOnClickListener{
//            loadEntries()
//        }

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ListViewModel::class.java) as ListViewModel<T>

        viewModel.getList().forEach{
            createView(it)
        }
        viewModel.getNewEntry().observe(viewLifecycleOwner, Observer {
            createView(it)
        })
    }
}