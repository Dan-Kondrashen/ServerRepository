package ru.kondrashen.diplomappv20.presentation.fragments
import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar

import ru.kondrashen.diplomappv20.R
import ru.kondrashen.diplomappv20.databinding.AdminMainPageBinding
import ru.kondrashen.diplomappv20.domain.MainPageViewModel
import ru.kondrashen.diplomappv20.presentation.adapters.UserListAdapter
import ru.kondrashen.diplomappv20.presentation.baseClasses.PublicConstants
import ru.kondrashen.diplomappv20.presentation.baseClasses.onItemClickInterface
import ru.kondrashen.diplomappv20.repository.data_class.User
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.CallUserListPref


class MainAdminFragment : Fragment(), onItemClickInterface<User> {
    private var _binding: AdminMainPageBinding? = null
    private var adapter: UserListAdapter? = null
    private var userTypeAdapter: ArrayAdapter<String>? = null
    private var userStatusAdapter: ArrayAdapter<String>? = null
    private var userInfoTypeAdapter: ArrayAdapter<String>? = null
    private var text = ""
    private var userId: Int? = null
    private var respId: Int? = null
    private var isPrefOpen: Boolean = false
    private var isExtraPrefOpen: Boolean = false
    private var isUserTypeOpen: Boolean = false
    private var isUserStatusOpen: Boolean = false
    private var isUserInfoTypeOpen: Boolean = false
    private var docId: Int? = null
    private lateinit var dataModel: MainPageViewModel
    private val binding get() = _binding!!
    var menuProviderMain = object: MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.menu_admin, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when (menuItem.itemId) {

                R.id.action_settings -> {
                    findNavController().navigate(R.id.action_mainFragment_to_preferenceFragment)
                    true
                }
                else -> false
            }
        }
    }
    companion object {
        private const val TAG = "AdminMainFragment"
    }

    

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataModel = ViewModelProvider(requireActivity()).get(MainPageViewModel::class.java)
        userId = arguments?.getInt(PublicConstants.USER_ID)
        userId?.let {
            dataModel.getMainUsersListDataServ(
                it,
                CallUserListPref("all", null, null, null, null, null)
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        _binding = AdminMainPageBinding.inflate(inflater, container, false)
        updateUI()

        val menuHost: MenuHost = requireActivity()
        menuHost.removeMenuProvider(MainFragmentEmployee().menuProviderMain)
        menuHost.addMenuProvider(object: MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_main, menu)
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_settings -> {
                        val bundle = Bundle()
                        userId = arguments?.getInt(PublicConstants.USER_ID)
                        bundle.putInt(PublicConstants.USER_ID, userId?: 0)
                        if (userId !=0 && respId !=0)
                            findNavController().navigate(R.id.action_mainFragment_to_preferenceFragment, bundle)
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
        userId = arguments?.getInt(PublicConstants.USER_ID)
        val userTypeArray = requireContext().resources.getStringArray(R.array.pref_user_type)
        val userStatusArray = requireContext().resources.getStringArray(R.array.pref_users_status)
        val userInfoTypeArray = requireContext().resources.getStringArray(R.array.pref_user_info_type)
        userTypeAdapter = ArrayAdapter(requireActivity(), R.layout.spiner_dropdown_item, userTypeArray)
        userStatusAdapter = ArrayAdapter(requireActivity(), R.layout.spiner_dropdown_item, userStatusArray)
        userInfoTypeAdapter = ArrayAdapter(requireActivity(), R.layout.spiner_dropdown_item, userInfoTypeArray)
        binding.usersPref.apply {
            userTypeSelector.adapter = userTypeAdapter
            userStatusSelector.adapter = userStatusAdapter
            userInfoTypeSelector.adapter = userInfoTypeAdapter
            etNumUsers.transformationMethod = null
            addPref.setOnClickListener {
                isExtraPrefOpen = !isExtraPrefOpen
                TransitionManager.beginDelayedTransition(binding.root)
                if (isExtraPrefOpen){
                    extraPref.visibility = VISIBLE
                } else {
                    extraPref.visibility = GONE
                }
            }
            callUsersPreference.setOnClickListener {
                isPrefOpen = !isPrefOpen
                TransitionManager.beginDelayedTransition(binding.root)
                if (isPrefOpen) {
                    usersPrefBlock.visibility = VISIBLE
                    callUsersPreference.setBackgroundResource(R.drawable.open_list_with_arrow)
                    callUsersPreference.text = getString(R.string.hide_parameters)
                } else {
                    usersPrefBlock.visibility = GONE
                    callUsersPreference.setBackgroundResource(R.drawable.hidden_list_with_arrow)
                    callUsersPreference.text = getString(R.string.show_parameters)
                }
            }
            userStatusQuestion.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    TransitionManager.beginDelayedTransition(binding.root)
                    userStatusView.visibility = VISIBLE

                } else {
                    TransitionManager.beginDelayedTransition(binding.root)
                    userStatusView.visibility = GONE
                }
            }
            userNumQuestion.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    TransitionManager.beginDelayedTransition(binding.root)
                    userNumberView.visibility = VISIBLE
                } else {
                    TransitionManager.beginDelayedTransition(binding.root)
                    userNumberView.visibility = GONE
                }
            }
            userInfoQuestion.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    TransitionManager.beginDelayedTransition(binding.root)
                    userInfoViewBlock.visibility = VISIBLE
                } else {
                    TransitionManager.beginDelayedTransition(binding.root)
                    userInfoViewBlock.visibility = GONE
                }
            }
            callUsers.setOnClickListener {
                var number: Int? = null
                var status: String? = null
                var mod = "all"
                var roleId: List<Int>? = null
                var infoTypeMod: String? = null
                var infoResult: String? = null
                userId?.let {
                    when(userTypeArray.indexOf(userTypeSelector.selectedItem.toString())){
                        0 -> roleId = listOf(1,2)
                        1 -> roleId = listOf(1)
                        2 -> roleId = listOf(2)
                    }
                    if (addPref.isChecked){
                        mod = "mod"
                        if (userStatusQuestion.isChecked){
                            val item = userStatusSelector.selectedItem.toString()
                            when(userStatusArray.indexOf(item)){
                                0 -> status = "confirmed"
                                1 -> status = "not confirmed"
                            }
                        }
                        if (userNumQuestion.isChecked){
                            val num = etNumUsers.text.toString()
                            if ( num != "0" && num != "")
                                number = num.toInt()
                            else {
                                val bar = Snackbar.make(
                                    binding.root as ViewGroup,
                                    getText(R.string.not_right_number),
                                    Snackbar.LENGTH_LONG
                                )
                                bar.show()
                            }
                        }
                        if (userInfoQuestion.isChecked){
                            var infoType = userInfoTypeSelector.selectedItem.toString()
                            when(userInfoTypeArray.indexOf(infoType)){
                                0 -> infoTypeMod = "email"
                                1 -> infoTypeMod = "phone"
                            }
                            infoResult = etUserInfo.text.toString()
                        }
                    }

                    dataModel.getMainUsersListDataServ(
                        it,
                        CallUserListPref(mod, infoResult, roleId, status, number, infoTypeMod)
                    )

                    dataModel.getMainUsersListDataRoom(it, 3, CallUserListPref(mod, infoResult, roleId, status, number, infoTypeMod))
                        .observe(viewLifecycleOwner){ usersList ->
                            adapter = UserListAdapter(usersList as MutableList<User>, it, requireActivity(), findNavController())
                            binding.userRecyclerView.adapter = adapter
                        }
                }
            }
        }
    }
    private fun updateUI() {
        userId = arguments?.getInt(PublicConstants.USER_ID)
        userId?.let {
            dataModel.getMainUsersListDataRoom(it, 3, CallUserListPref("all", null, null ,null, null, null))
                .observe(viewLifecycleOwner){ usersList ->
                    adapter = UserListAdapter(usersList as MutableList<User>, it, requireActivity(), findNavController())
                    binding.userRecyclerView.adapter = adapter
                }
        }
    }
    override fun onChildAdapterClickEventMessage(position: Int, text: String): Snackbar {

        val bar = Snackbar.make(binding.root, text, Snackbar.LENGTH_LONG)
        return bar
    }
    override fun onChildClickStartPutEvent(adapterPosition: Int, itemId: Int, mod: String) {
    }
    override fun onTabClickEvent(position: Int, itemId: Int, text: String) {
    }



    override fun onItemInAdapterUsableClickEvent(
        position: Int,
        item: User,
        activatorType: String
    ) {
    }
}