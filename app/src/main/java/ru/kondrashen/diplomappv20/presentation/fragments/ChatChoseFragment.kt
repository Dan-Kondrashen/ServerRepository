package ru.kondrashen.diplomappv20.presentation.fragments
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.snackbar.Snackbar

import ru.kondrashen.diplomappv20.R
import ru.kondrashen.diplomappv20.databinding.ChoseChatFragmentBinding
import ru.kondrashen.diplomappv20.databinding.DocumentChatFragmentBinding
import ru.kondrashen.diplomappv20.domain.MainPageViewModel
import ru.kondrashen.diplomappv20.domain.UserAccountControlViewModel
import ru.kondrashen.diplomappv20.presentation.adapters.ChatListAdapter
import ru.kondrashen.diplomappv20.presentation.adapters.CommentsListAdapter
import ru.kondrashen.diplomappv20.presentation.baseClasses.PublicConstants
import ru.kondrashen.diplomappv20.presentation.baseClasses.SearchableBase
import ru.kondrashen.diplomappv20.presentation.baseClasses.onItemClickInterface
import ru.kondrashen.diplomappv20.repository.data_class.Comment
import java.util.Date


class ChatChoseFragment : Fragment() {
    private var _binding: ChoseChatFragmentBinding? = null
    private var userId: Int? = null
    private var userType: String? = null
    private var chatListAdapter: ChatListAdapter? = null
    private var docId: Int? = null
    private lateinit var dataModel: MainPageViewModel
    private lateinit var userModel: UserAccountControlViewModel
    private val binding get() = _binding!!

    companion object {
        private const val TAG = "ChatFragment"
    }

    override fun onResume() {
        super.onResume()
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        (getContext() as AppCompatActivity).window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataModel = ViewModelProvider(requireActivity()).get(MainPageViewModel::class.java)
        userModel = ViewModelProvider(requireActivity()).get(UserAccountControlViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        _binding = ChoseChatFragmentBinding.inflate(inflater, container, false)
        updateUI()
        docId = arguments?.getInt(PublicConstants.DOC_ID)
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
                        if (userId !=0)
                            findNavController().navigate(R.id.action_chatChoseFragment_to_personalSpaceFragment, bundle)
                        true
                    }
                    R.id.action_settings -> {
                        findNavController().navigate(R.id.action_chatChoseFragment_to_preferenceFragment)
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
            chatPage.setImageResource(R.drawable.comments_dark_svg)
            analysisPage.setOnClickListener {
                val bundle = Bundle()
                userId = arguments?.getInt(PublicConstants.USER_ID)
                bundle.putInt(PublicConstants.USER_ID, userId?: 0)
                findNavController().navigate(R.id.action_chatChoseFragment_to_analysisPrefFragment, bundle)
            }
        }
        binding.apply {
            filter.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                }

                override fun afterTextChanged(s: Editable?) {
                    TransitionManager.beginDelayedTransition(chatChoseRecycle)
                    SearchableBase().filterAdapters(filter.text.toString(), chatListAdapter)
                }

            })
        }
    }
    private fun updateUI() {
        userId = arguments?.getInt(PublicConstants.USER_ID)
        userId?.let { id ->
            userModel.getUserData(id).observe(viewLifecycleOwner) { user ->
                user?.roleId?.let {
                    userModel.getRoleNameById(it).observe(viewLifecycleOwner) {
                        userType = it
                        binding.bottomBar.homePage.setOnClickListener {
                            it?.let {
                                val bundle = Bundle()
                                userId = arguments?.getInt(PublicConstants.USER_ID)
                                bundle.putInt(PublicConstants.USER_ID, userId ?: 0)
                                bundle.putString(PublicConstants.USER_TYPE, userType ?: "соискатель")
                                findNavController().navigate(
                                    R.id.action_chatChoseFragment_to_mainFragmentEmployee,
                                    bundle
                                )
                            }
                        }
                    }
                }
            }
        }
        docId = arguments?.getInt(PublicConstants.DOC_ID)
        println(docId)
        if (userId != null && docId !in listOf(null, 0)){

        }
        else if (userId != null){
            dataModel.getUserRespWithDocInfoFromServ(userId!!,10).observe(viewLifecycleOwner){
                println(it)
            }
            dataModel.getUserRespFromRoom(userId!!).observe(viewLifecycleOwner){ it->
                if (it.isEmpty())
                    binding.infoText.visibility = VISIBLE
                else
                    binding.infoText.visibility = GONE
//                println(it.map { it.respId.toString() +" "+ it.title })

                chatListAdapter = ChatListAdapter(it as MutableList, userId!!, requireActivity(), findNavController())
                binding.chatChoseRecycle.adapter = chatListAdapter
                if (binding.filter.text.toString().isNotEmpty()){
                    SearchableBase().filterAdapters(binding.filter.text.toString(), chatListAdapter)
                }
            }
        }

    }
}