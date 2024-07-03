package ru.kondrashen.diplomappv20.presentation.fragments
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import ru.kondrashen.diplomappv20.R
import ru.kondrashen.diplomappv20.databinding.AnalysisPagerFragmentBinding
import ru.kondrashen.diplomappv20.domain.ExtraInfoPageViewModel
import ru.kondrashen.diplomappv20.domain.UserAccountControlViewModel
import ru.kondrashen.diplomappv20.presentation.adapters.AnalysisPagerAdapter
import ru.kondrashen.diplomappv20.presentation.baseClasses.PublicConstants


class AnaliticFragment : Fragment() {
    private var _binding: AnalysisPagerFragmentBinding? = null

    private var userId: Int? = null
    private var userType: String? = null
    private var respId: Int? = null
    private var adapter: AnalysisPagerAdapter? = null
    private lateinit var extraInfoModel: ExtraInfoPageViewModel
    private lateinit var userModel: UserAccountControlViewModel
    private var isPrefOpen: Boolean = false
    private val binding get() = _binding!!


    companion object {
        private const val TAG = "AnaliticFragment"
        private const val delay: Long = 5000

    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireActivity().window.setDecorFitsSystemWindows(true)
        } else {
            requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Возвращаем состояние системных окон к исходному при выходе из фрагмента
            requireActivity().window.setDecorFitsSystemWindows(true)
        } else {
            requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    }
//    override fun onResume() {
//        super.onResume()
//        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
//    }
//
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        (getContext() as AppCompatActivity).window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        extraInfoModel = ViewModelProvider(requireActivity()).get(ExtraInfoPageViewModel::class.java)
        userModel = ViewModelProvider(requireActivity()).get(UserAccountControlViewModel::class.java)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        _binding = AnalysisPagerFragmentBinding.inflate(inflater, container, false)
        updateUI()

        val menuHost: MenuHost = requireActivity()
        menuHost.removeMenuProvider(MainFragmentEmployee().menuProviderMain)
        menuHost.addMenuProvider(object: MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_main, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_go_to_account -> {
                        val bundle = Bundle()
                        userId = arguments?.getInt(PublicConstants.USER_ID)
                        bundle.putInt(PublicConstants.USER_ID, userId?: 0)
                        if (userId !=0 && respId !=0)
                            findNavController().navigate(R.id.action_analysisPrefFragment_to_personalSpaceFragment, bundle)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.bottomBar.apply {
            homePage.setOnClickListener {
                val bundle = Bundle()
                userId = arguments?.getInt(PublicConstants.USER_ID)
                bundle.putInt(PublicConstants.USER_ID, userId?: 0)
                bundle.putString(PublicConstants.USER_TYPE, userType?: "соискатель")
                findNavController().navigate(R.id.action_analysisPrefFragment_to_mainFragmentEmployee, bundle)
            }
            chatPage.setOnClickListener {
                val bundle = Bundle()
                userId = arguments?.getInt(PublicConstants.USER_ID)
                bundle.putInt(PublicConstants.USER_ID, userId?: 0)
                bundle.putString(PublicConstants.USER_TYPE, userType)
                findNavController().navigate(R.id.action_analysisPrefFragment_to_chatChoseFragment, bundle)
            }
            analysisPage.setImageResource(R.drawable.graph_dark_svg)

        }
        binding.apply {
            viewPagerForAnalysisGraph.isUserInputEnabled = false;
            mainAnalytic.setOnClickListener {
                it.setBackgroundResource(R.drawable.hidden_list_pressed)
                userAnalytic.setBackgroundResource(R.drawable.hidden_list)
                viewPagerForAnalysisGraph.setCurrentItem(0, true)
            }
            userAnalytic.setOnClickListener{
                it.setBackgroundResource(R.drawable.hidden_list_pressed)
                mainAnalytic.setBackgroundResource(R.drawable.hidden_list)
                viewPagerForAnalysisGraph.setCurrentItem(1, true)
            }
        }

    }

    private fun updateUI() {
        if (isAdded) {
            userId = arguments?.getInt(PublicConstants.USER_ID)
            userId?.let { id ->
                userModel.getUserData(id).observe(viewLifecycleOwner) { user ->
                    user?.roleId?.let {
                        userModel.getRoleNameById(it).observe(viewLifecycleOwner) {
                            userType = it
                            adapter = AnalysisPagerAdapter(
                                requireActivity().supportFragmentManager,
                                lifecycle,
                                id,
                                it)
                            binding.viewPagerForAnalysisGraph.adapter = adapter
                        }
                    }
                }
            }
        }
    }

}