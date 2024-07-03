package ru.kondrashen.diplomappv20.presentation.fragments

import android.os.Bundle
import android.os.Looper
import android.os.Handler
import android.view.*
import android.view.View.OnTouchListener
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior.OnScrollStateChangedListener
import ru.kondrashen.diplomappv20.R
import ru.kondrashen.diplomappv20.databinding.UnregMainListPageBinding
import ru.kondrashen.diplomappv20.domain.MainPageViewModel
import ru.kondrashen.diplomappv20.presentation.adapters.DocumentListAdapter
import ru.kondrashen.diplomappv20.presentation.adapters.DocumentListAdapter2
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocumentInfo


class MainFragmentUnReg : Fragment(){
    private var _binding: UnregMainListPageBinding? =null
    private var adapter: DocumentListAdapter2? = null
    private var adapter2: DocumentListAdapter2? = null
    private var position: Int = 0
    private var position2: Int = 0
    private var loadDataFinished = false
    private var userTouchFirstRV = false
    private var userTouchSecondRV = false
    private var searchText = ""
    private var docsInfo: MutableList<DocumentInfo> = mutableListOf()
    private lateinit var dataModel: MainPageViewModel
    private lateinit var vacancyTouchListener: RecyclerView.OnItemTouchListener
    private lateinit var resumeTouchListener: RecyclerView.OnItemTouchListener
    private val binding get() = _binding!!

    companion object {
        private const val TAG = "MainPageUnReg"
        private const val LOAD_DATA = "loadDataFinished"
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(LOAD_DATA,loadDataFinished)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataModel = ViewModelProvider(requireActivity()).get(MainPageViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        _binding = UnregMainListPageBinding.inflate(inflater, container, false)
        updateUI()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val snapHelp =PagerSnapHelper()
        val snapHelp2 =PagerSnapHelper()
        val handler = Handler(Looper.getMainLooper())
        val handler2 = Handler(Looper.getMainLooper())
        snapHelp.attachToRecyclerView(binding.documentResumeRecyclerView)
        snapHelp2.attachToRecyclerView(binding.documentForUserAbilitiesRecyclerView)
        val run1 = object : Runnable {
            override fun run() {
                position =
                    (binding.documentResumeRecyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
                position = if (position < (binding.documentResumeRecyclerView.adapter?.itemCount ?: 0) - 1) {
                    (binding.documentResumeRecyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition() + 1
                } else 0
                binding.documentResumeRecyclerView.smoothScrollToPosition(position)
                handler.postDelayed(this, 3000)
            }
        }
        val run2 = object: Runnable {
            override fun run() {
                position2 = (binding.documentForUserAbilitiesRecyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
                position2 = if (position2 < (binding.documentForUserAbilitiesRecyclerView.adapter?.itemCount ?: 0) - 1) {
                    (binding.documentForUserAbilitiesRecyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition() + 1
                } else 0
                binding.documentForUserAbilitiesRecyclerView.smoothScrollToPosition(position2)
                handler2.postDelayed(this, 3000)

            }
        }
        binding.documentResumeRecyclerView.let { resumeRecycle ->
            resumeRecycle.post(run1)
            resumeRecycle.addOnItemTouchListener(object: RecyclerView.OnItemTouchListener{
                override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                    handler.removeCallbacksAndMessages(null)
                    handler2.removeCallbacksAndMessages(null)
                    handler2.post(run2)
                    return false
                }
                override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
                }
                override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
                }
            })
        }
        binding.documentForUserAbilitiesRecyclerView.let { vacancyRecycle ->
            vacancyRecycle.post(run2)
            vacancyRecycle.addOnItemTouchListener(object: RecyclerView.OnItemTouchListener{
                override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                    handler2.removeCallbacksAndMessages(null)
                    handler.removeCallbacksAndMessages(null)
                    handler.post(run1)
                    return false
                }
                override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
                }
                override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
                }

            })
        }

        binding.extraBar.apply {
            searchBar.setOnSearchClickListener{
                val animation = AnimationUtils.loadAnimation(context, R.anim.scale_in_searchview)
                searchBar.startAnimation(animation)
            }

            searchBar.setOnQueryTextFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    val animation = AnimationUtils.loadAnimation(context, R.anim.scale_out_searchview)
                    animation.setAnimationListener(object: Animation.AnimationListener{
                        override fun onAnimationStart(animation: Animation?) {
                        }

                        override fun onAnimationEnd(animation: Animation?) {
                            searchBar.clearAnimation()
                            searchBar.isIconified = true
                        }

                        override fun onAnimationRepeat(animation: Animation?) {

                        }

                    })
                    if (!searchBar.isIconified and (searchBar.query.toString() == ""))
                        searchBar.startAnimation(animation)
                }
            }
            val btnClose = searchBar.rootView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
            btnClose.setOnClickListener{
                val animation = AnimationUtils.loadAnimation(context, R.anim.scale_out_searchview)
                animation.setAnimationListener(object: Animation.AnimationListener{
                    override fun onAnimationStart(animation: Animation?) {
                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        searchBar.clearAnimation()
                        searchBar.isIconified = true
                    }

                    override fun onAnimationRepeat(animation: Animation?) {
                    }

                })
                if (!searchBar.isIconified)
                    searchBar.startAnimation(animation)
            }
            searchBar.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(newText: String?): Boolean {
                    adapter?.filter?.filter(newText)
                    adapter2?.filter?.filter(newText)
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    adapter?.filter?.filter(newText)
                    adapter2?.filter?.filter(newText)
                    return true
                }

            })
        }
    }
    private fun updateUI() {
        val userId = arguments?.getInt("id")
        val appbar = requireActivity().findViewById<MaterialToolbar>(R.id.toolbar)
        val toolbarHeight = ViewCompat.getMinimumHeight(appbar)
        dataModel.apply {
            getMainPageDataFromRoom("resume","new").observe(requireActivity()){
                docsInfo = it as MutableList<DocumentInfo>
                adapter = DocumentListAdapter2(docsInfo, "resume", "all", findNavController(), requireActivity())
                binding.documentResumeRecyclerView.adapter = adapter
            }
            getMainPageDataFromRoom("vacancy", "all").observe(requireActivity()){
                docsInfo = it as MutableList<DocumentInfo>
                adapter2 = DocumentListAdapter2(docsInfo, "vacancy", "all", findNavController(), requireActivity())
                binding.documentForUserAbilitiesRecyclerView.adapter = adapter2
            }
        }

    }

}
