package ru.kondrashen.diplomappv20.presentation.fragments

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.Looper
import android.os.Handler
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Filterable
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.snackbar.Snackbar
import ru.kondrashen.diplomappv20.R
import ru.kondrashen.diplomappv20.databinding.EmployerMainListPageBinding
import ru.kondrashen.diplomappv20.domain.MainPageViewModel
import ru.kondrashen.diplomappv20.presentation.activitys.AuthActivity
import ru.kondrashen.diplomappv20.presentation.adapters.DocumentListAdapter
import ru.kondrashen.diplomappv20.presentation.baseClasses.CatchException
import ru.kondrashen.diplomappv20.presentation.baseClasses.PublicConstants
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocumentInfoWithKnowledge


class MainFragmentEmployer : Fragment(){
    private var _binding: EmployerMainListPageBinding? =null
    private var adapter: DocumentListAdapter? = null
    private var adapter2: DocumentListAdapter? = null
    private var adapter3: DocumentListAdapter? = null
    private var adapter4: DocumentListAdapter? = null
    private var position: Int = 0
    private var position2: Int = 0
    private var text =""
    private var userId: Int? = null
    private var userType: String? = null
    private var docsInfo: MutableList<DocumentInfoWithKnowledge> = mutableListOf()
    private var docsInfo2: MutableList<DocumentInfoWithKnowledge> = mutableListOf()
    private var docsInfo3: MutableList<DocumentInfoWithKnowledge> = mutableListOf()
    private lateinit var dataModel: MainPageViewModel
    private lateinit var knowledgeTouchListener: RecyclerView.OnItemTouchListener
    private lateinit var mostViewedTouchListener: RecyclerView.OnItemTouchListener
    private lateinit var noExpDocumentTouchListener: RecyclerView.OnItemTouchListener
    private lateinit var newDocumentTouchListener: RecyclerView.OnItemTouchListener
    private val handler = Handler(Looper.getMainLooper())
    private val handler2 = Handler(Looper.getMainLooper())
    private val handler3 = Handler(Looper.getMainLooper())
    private val handler4 = Handler(Looper.getMainLooper())
    private val binding get() = _binding!!
    private lateinit var run1: Runnable
    private lateinit var run2: Runnable
    private lateinit var run3: Runnable
    private lateinit var run4: Runnable
    var menuProviderMain = object: MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.menu_main, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when (menuItem.itemId) {
                R.id.action_go_to_account -> {
                    val bundle = Bundle()
                    userId = arguments?.getInt("userId")
                    bundle.putInt("userId", userId?: 0)
                    findNavController().navigate(R.id.action_mainFragment_to_personalSpaceFragment, bundle)
                    true
                }
                R.id.action_settings -> {
                    findNavController().navigate(R.id.action_mainFragment_to_preferenceFragment)
                    true
                }
                R.id.action_go_to_filtration -> {
                    val bundle = Bundle()
                    userId = arguments?.getInt("userId")
                    bundle.putInt("userId", userId?: 0)
                    findNavController().navigate(R.id.action_mainFragmentView_to_filtrationFragment, bundle)
                    true
                }
                R.id.action_logout -> {
                    val intent = AuthActivity.newIntent(requireActivity())
                    if (intent != null) {
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        requireActivity().finish()
                        startActivity(intent)
                    }

                    true
                }
                else -> false
            }
        }
    }
    companion object {
        private const val TAG = "MainPageEmployer"
        private const val delay: Long = 5000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println(TAG)
        dataModel = ViewModelProvider(requireActivity()).get(MainPageViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        _binding = EmployerMainListPageBinding.inflate(inflater, container, false)
        run1 = createRunnable(binding.newDocumentRecyclerView, handler)
        run2 = createRunnable(binding.documentForUserAbilitiesRecyclerView, handler2)
        run3 = createRunnable(binding.mostViewedRecyclerView, handler3)
        run4 = createRunnable(binding.partTimeRecyclerView, handler4)
        updateUI()
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(menuProviderMain, viewLifecycleOwner)
        val snapHelp =PagerSnapHelper()
        val snapHelp2 =PagerSnapHelper()
        val snapHelp3 =PagerSnapHelper()
        val snapHelp4 =PagerSnapHelper()
        snapHelp.attachToRecyclerView(binding.newDocumentRecyclerView)
        snapHelp2.attachToRecyclerView(binding.documentForUserAbilitiesRecyclerView)
        snapHelp3.attachToRecyclerView(binding.mostViewedRecyclerView)

        val params: ViewGroup.MarginLayoutParams = binding.extraBar.extraView.layoutParams as ViewGroup.MarginLayoutParams
        params.setMargins(0,-getStatusBarHeight(),0,4)

        binding.bottomBar.apply {
            homePage.setImageResource(R.drawable.home_dark_svg)
            analysisPage.setOnClickListener {
                val bundle = Bundle()
                userId = arguments?.getInt(PublicConstants.USER_ID)
                bundle.putInt(PublicConstants.USER_ID, userId?: 0)
                if (findNavController().currentDestination?.id == R.id.mainFragmentView) {
                    findNavController().navigate(
                        R.id.action_mainFragmentEmployee_to_analysisPrefFragment,
                        bundle
                    )
                }
            }
            chatPage.setOnClickListener {
                val bundle = Bundle()
                userId = arguments?.getInt("userId")
                bundle.putInt("userId", userId?: 0)
                if (findNavController().currentDestination?.id == R.id.mainFragmentView) {
                    findNavController().navigate(
                        R.id.action_mainFragmentEmployee_to_chatChoseFragment,
                        bundle
                    )
                }
            }
        }
        binding.extraBar.levelBar.viewTreeObserver.addOnGlobalLayoutListener {
            val levelBarHeight = binding.extraBar.levelBar.height
//            println(levelBarHeight.toString() + "result")
            (binding.documentsNewText.layoutParams as ViewGroup.MarginLayoutParams).setMargins(
                0,
                levelBarHeight,
                0,
                0
            )
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
                    filterAdapters(newText , adapter, adapter2, adapter3, adapter4)

                    return true
                }
                override fun onQueryTextChange(newText: String?): Boolean {
                    filterAdapters(newText , adapter, adapter2, adapter3, adapter4)
                    return true
                }
            })
        }

    }

    override fun onPause() {
        super.onPause()
        binding.newDocumentRecyclerView.removeCallbacks(run1)
        binding.documentForUserAbilitiesRecyclerView.removeCallbacks(run2)
        binding.mostViewedRecyclerView.removeCallbacks(run3)
        binding.partTimeRecyclerView.removeCallbacks(run4)
        removeAllRunnable(listOf(run1, run2, run3, run4), listOf(handler, handler2, handler3, handler4))
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.newDocumentRecyclerView.removeOnItemTouchListener(newDocumentTouchListener)
        binding.documentForUserAbilitiesRecyclerView.removeOnItemTouchListener(knowledgeTouchListener)
        binding.mostViewedRecyclerView.removeOnItemTouchListener(mostViewedTouchListener)
//        binding.partTimeRecyclerView.removeOnItemTouchListener(noExpVacancyTouchListener)
        removeAllRunnable(listOf(run1, run2, run3, run4), listOf(handler, handler2, handler3, handler4))

    }

    private fun updateUI() {
        userId = arguments?.getInt("userId")
        userType = arguments?.getString("userType")
        dataModel.apply {

            userId?.let {
                if(activity != null && isAdded) {
                    val bar = Snackbar.make(requireActivity().findViewById(android.R.id.content), text, 3500)
                    val textview =
                        bar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                    textview.gravity = Gravity.CENTER_HORIZONTAL
                    textview.textAlignment = View.TEXT_ALIGNMENT_CENTER
                    textview.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.remove_pressed_svg, 0)
                    if (CatchException.hasInternetCheck(requireContext())) {
                        getCurrentUserDataFromServ(it).observe(requireActivity()) { str ->
                            if (str != "OK") {
                                bar.setText(R.string.dismis_connection)
                                bar.setAnchorView(binding.extraBar.searchBar)
                                bar.show()
                            }
                        }
                        getUserLevelData(it).observe(viewLifecycleOwner){ levelData ->
                            levelData?.let {
                                binding.extraBar.levelCurrentText.text = levelData.number.toString()
                                binding.extraBar.levelNextText.text = levelData.nextNumber.toString()
                                val progressMax = levelData.maxPoints - levelData.minPoints
                                val progressUser = levelData.curPoints - levelData.minPoints
                                val progress =((progressUser.toFloat()/progressMax.toFloat())*100)
                                binding.extraBar.userLevelBar.smoothProgress(progress.toInt())
                            }?: run{
                                binding.extraBar.levelCurrentText.text = "1"
                                binding.extraBar.levelNextText.text = "2"
                                binding.extraBar.progressText.text =
                                    getString(R.string.you_has_no_level_data)
                            }

                        }
                    }
                    else {
                        bar.setText(getText(R.string.no_connection_not_saving))
                        bar.setAnchorView(binding.extraBar.searchBar)
                        bar.show()
                    }
                }
            }

            val type = when(userType){
                "соискатель" -> "vacancy"
                "работодатель" -> "resume"
                else -> "vacancy"
            }
            var mod = "new"
            // Адаптер самых новых документов
            adapter = DocumentListAdapter(
                docsInfo,
                userId,
                type,
                "new",
                findNavController(),
                requireActivity()
            )
            binding.newDocumentRecyclerView.adapter = adapter
            binding.newDocumentRecyclerView.addOnScrollListener(
                createOnScrollListener(
                    binding.newDocumentRecyclerView,
                    adapter
                )
            )
            getMainPageData2FromRoom(type, mod).observe(viewLifecycleOwner){

                var docsInfo = it as MutableList<DocumentInfoWithKnowledge>
                if (findNavController().currentDestination?.id == R.id.mainFragmentView) {
                    when (docsInfo.size) {
                        0 -> binding.progressNew.visibility = ViewGroup.VISIBLE
                        else -> binding.progressNew.visibility = ViewGroup.GONE
                    }
                    adapter?.submitList(docsInfo)

                }
            }
            mod = "all"
            adapter2 = DocumentListAdapter(
                docsInfo2,
                userId,
                type,
                mod,
                findNavController(),
                requireActivity()
            )
            binding.documentForUserAbilitiesRecyclerView.adapter = adapter2
            binding.documentForUserAbilitiesRecyclerView.addOnScrollListener(
                createOnScrollListener(
                    binding.documentForUserAbilitiesRecyclerView,
                    adapter2
                )
            )
            getMainPageData2FromRoom(type, mod).observe(viewLifecycleOwner){
                var docsInfo = it as MutableList<DocumentInfoWithKnowledge>
                if (findNavController().currentDestination?.id == R.id.mainFragmentView) {

                    when (docsInfo.size) {
                        0 -> binding.progressUserAbilities.visibility = ViewGroup.VISIBLE
                        else -> binding.progressUserAbilities.visibility = ViewGroup.GONE
                    }
                    adapter2?.submitList(docsInfo)

                }
            }
            mod = "mostviewed"
            adapter3 = DocumentListAdapter(
                docsInfo3,
                userId,
                type,
                mod,
                findNavController(),
                requireActivity()
            )
            binding.mostViewedRecyclerView.adapter = adapter3
            binding.mostViewedRecyclerView.addOnScrollListener(
                createOnScrollListener(
                    binding.mostViewedRecyclerView,
                    adapter3
                )
            )
            getMainPageData2FromRoom(type, mod).observe(viewLifecycleOwner) {
                var docsInfo = it as MutableList<DocumentInfoWithKnowledge>
                if (findNavController().currentDestination?.id == R.id.mainFragmentView) {

                    when (docsInfo.size) {
                        0 -> binding.progressMostViewed.visibility = ViewGroup.VISIBLE
                        else -> binding.progressMostViewed.visibility = ViewGroup.GONE
                    }
                    adapter3?.submitList(docsInfo)

                }
            }

        }
        removeAllRunnable(null, listOf(handler, handler2, handler3, handler4))
        binding.newDocumentRecyclerView.let { newDocumentRecycle ->
            newDocumentTouchListener = createOnItemTouchListener(handler, handler2, run2,null, null, handler3, run3)
            newDocumentRecycle.addOnItemTouchListener(newDocumentTouchListener)
            newDocumentRecycle.postDelayed(run1, delay)
        }
        binding.documentForUserAbilitiesRecyclerView.let { documentAbilitiesRecycle ->
            knowledgeTouchListener = createOnItemTouchListener(handler2, handler3, run3,null, null, handler, run1)
            documentAbilitiesRecycle.addOnItemTouchListener(knowledgeTouchListener)
            documentAbilitiesRecycle.postDelayed(run2, delay)
        }
        binding.mostViewedRecyclerView.let { mostViewed ->
            mostViewedTouchListener = createOnItemTouchListener(handler, handler2, run2, null, null,handler3, run1)
            mostViewed.addOnItemTouchListener(mostViewedTouchListener)
            mostViewed.postDelayed(run3, delay)
        }
    }
    private fun isOnUserScreen(view: View?):Boolean? {
        val scrollBounds = Rect()
        binding.scrollView.getDrawingRect(scrollBounds)
        val location = IntArray(2)
        view?.getLocationOnScreen(location)
        return view?.getGlobalVisibleRect(scrollBounds)
    }
    private fun filterAdapters(text: String?,vararg adapters: RecyclerView.Adapter<RecyclerView.ViewHolder>?){
        for (adapt in adapters){
            if (adapt is Filterable)
                adapt.filter?.filter(text)
        }
    }
    private fun removeAllRunnable(runners: List<Runnable>?, handlers: List<Handler>){
        for (i in handlers.indices){
            handlers[i].removeCallbacksAndMessages(null)
            runners?.let {
                handlers[i].removeCallbacks(runners[i])
            }

        }
    }

    private fun createOnScrollListener(view: RecyclerView, localadapter: RecyclerView.Adapter<RecyclerView.ViewHolder>?): RecyclerView.OnScrollListener{
        return object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItem = (view.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
                view.post {
                    (view.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
                    localadapter?.notifyItemChanged(visibleItem)
                }
            }
        }
    }
    private fun createOnItemTouchListener(handler1: Handler, handler2: Handler?, run2: Runnable?,  handler3: Handler?, run3: Runnable?, handler4: Handler, run4: Runnable): RecyclerView.OnItemTouchListener{
        return object: RecyclerView.OnItemTouchListener{
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                handler1.removeCallbacksAndMessages(null)
                handler2?.let {
                    it.removeCallbacksAndMessages(null)
                    it.postDelayed(run2!!, delay)
                }
                handler3?.let {
                    it.removeCallbacksAndMessages(null)
                    it.postDelayed(run3!!, delay)
                }
                handler4.removeCallbacksAndMessages(null)
                handler4.postDelayed(run4, delay)
                return false
            }
            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
            }
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
            }
        }
    }
    fun ProgressBar.smoothProgress(percent: Int){
        val animation = ObjectAnimator.ofInt(this, "progress", percent)
        animation.duration = 1300
        animation.interpolator =  AccelerateInterpolator()
        animation.start()
    }
    private fun getStatusBarHeight() : Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else 0
    }
    private fun createRunnable(view: RecyclerView, handler: Handler): Runnable{
        return object : Runnable {
            override fun run() {
                val location = IntArray(2)
                view.getLocationOnScreen(location)
                if( isOnUserScreen(view) == true) {
                    var position =
                        (view.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
                    position = if (position < (view.adapter?.itemCount
                            ?: 0) - 1
                    ) {
                        (view.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition() + 1
                    } else 0
                    view.smoothScrollToPosition(position)

                }
                handler.postDelayed(this, delay)
            }
        }
    }
}
