package com.project.foret.ui.main.search

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.foret.ui.main.MainActivity
import com.project.foret.R
import com.project.foret.adapter.ForetAdapter
import com.project.foret.repository.ForetRepository
import com.project.foret.ui.main.foret.CreateForetActivity
import com.project.foret.util.Resource
import com.project.foret.util.snackbar

class SearchFragment : Fragment(R.layout.fragment_search) {

    lateinit var viewModel: SearchViewModel
    lateinit var foretAdapter: ForetAdapter
    lateinit var rvForetRank: RecyclerView
    lateinit var ivCreateForet: ImageView

    val TAG = "SearchFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // initialize
        // ViewModel, Repository
        val foretRepository = ForetRepository()
        val viewModelProviderFactory = SearchViewModelProviderFactory(foretRepository)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory).get(SearchViewModel::class.java)

        viewModel.getForetsByPage(0, 5)

        setFindViewById(view)
        setToolbar()
        setUpRecyclerView()
        setOnClickListener()
        setForetData()
    }

    private fun setForetData() {
        viewModel.forets.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    // hideProgressBar()
                    response.data?.let { foretResponse ->
                        foretAdapter.differ.submitList(foretResponse.forets)
                    }
                }
                is Resource.Error -> {
                    // hideProgressBar()
                    response.message?.let { message ->
                        Log.e(TAG, "An error occured $message")
                    }
                }
                is Resource.Loading -> {
                    // showProgressBar()
                }
            }
        })
    }

    private fun setUpRecyclerView() {
        foretAdapter = ForetAdapter()

        rvForetRank.apply {
            adapter = foretAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }

    private fun setFindViewById(view: View) {
        ivCreateForet = view.findViewById(R.id.ivCreateForet)
        rvForetRank = view.findViewById(R.id.rvForetRank)
    }

    private fun setOnClickListener() {
        ivCreateForet.setOnClickListener {
            val intent = Intent(context, CreateForetActivity::class.java)
            intent.putExtra("memberId", (activity as MainActivity).member?.id)
            startActivityForResult(intent, 0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            when (resultCode) {
                AppCompatActivity.RESULT_OK -> {
                    val bundle = bundleOf("foretId" to data?.getLongExtra("foretId", 0L))
                    view?.findNavController()
                        ?.navigate(
                            R.id.action_searchFragment_to_foretFragment,
                            bundle
                        )
                }
                AppCompatActivity.RESULT_CANCELED -> {
                    (activity as MainActivity).mainLayoutRoot.snackbar("포레 생성 취소!")
                }
            }
        }
    }

    private fun setToolbar() {
        setHasOptionsMenu(true)
        val mContext = (activity as MainActivity)
        mContext.toolbar.setBackgroundColor(
            ContextCompat.getColor(mContext, R.color.white)
        )
        mContext.ivToolbar.setImageDrawable(
            ContextCompat.getDrawable(mContext, R.drawable.foret_logo)
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_anonymous, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.drawer_menu) {
            (activity as MainActivity).drawerLayout.openDrawer(GravityCompat.END)
        }
        return super.onOptionsItemSelected(item)
    }
}