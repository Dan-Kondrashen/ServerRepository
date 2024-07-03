package ru.kondrashen.diplomappv20.presentation.fragments
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.snackbar.Snackbar

import ru.kondrashen.diplomappv20.R
import ru.kondrashen.diplomappv20.databinding.DocumentChatFragmentBinding
import ru.kondrashen.diplomappv20.domain.MainPageViewModel
import ru.kondrashen.diplomappv20.presentation.adapters.CommentsListAdapter
import ru.kondrashen.diplomappv20.presentation.baseClasses.PublicConstants
import ru.kondrashen.diplomappv20.presentation.baseClasses.onItemClickInterface
import ru.kondrashen.diplomappv20.repository.data_class.Comment
import java.util.Date


class ChatFragment : Fragment(), onItemClickInterface<Comment> {
    private var _binding: DocumentChatFragmentBinding? = null
    private var adapter: CommentsListAdapter? = null
    private var text = ""
    private var userId: Int? = null
    private var secondUserId: Int? = null
    private var respId: Int? = null
    private var docId: Int? = null
    private lateinit var dataModel: MainPageViewModel
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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        _binding = DocumentChatFragmentBinding.inflate(inflater, container, false)
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
                            findNavController().navigate(R.id.action_chatFragment_to_personalSpaceFragment, bundle)
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
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        }
        // Добавление проверки жизненного цикла фрагментов
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            onBackPressedCallback
        )
        binding.apply {
            (messageRecycle.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            userId = arguments?.getInt(PublicConstants.USER_ID)
            respId = arguments?.getInt(PublicConstants.RESP_ID)
            sendComm.setOnClickListener {

                if (etMessage.text.toString().length > 1){
                    userId?.let {id->
                        val date = Date().toString()
                        adapter?.let {
                            println(date+"data22")
                            val comm =Comment(0, etMessage.text.toString(), "Not confirmed", date, id, respId?:0)
                            dataModel.addCommentToRoom(comm)
                            var size = it.itemCount
                            binding.noMessage.visibility = GONE
                            it.addItemToList(comm)
                            it.notifyItemInserted(size-1)
                            println(date+"data22")
                            dataModel.sendMessageToServ(id, etMessage.text.toString(), respId?:0, date).observe(viewLifecycleOwner){resp->
                                resp.id?.let {commId ->
                                    dataModel.getCommentFromRoom(commId).observe(viewLifecycleOwner){comm->
                                        comm?.let {comms ->
                                            etMessage.setText("")
                                            it.replaceItemToList(comms, if (size > 0)size-1 else 0)
                                            messageRecycle.scrollToPosition(if (size > 0) size else 0)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            etMessage.addTextChangedListener ( object : TextWatcher{
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    sendComm.visibility = if (s?.length!! > 0) View.VISIBLE else View.GONE
                }
                override fun afterTextChanged(s: Editable?) {
                }
            })
        }
    }
    private fun updateUI() {
        userId = arguments?.getInt(PublicConstants.USER_ID)
        secondUserId = arguments?.getInt(PublicConstants.ANOTHER_USER_ID)
        respId = arguments?.getInt(PublicConstants.RESP_ID)
        docId = arguments?.getInt(PublicConstants.DOC_ID)

        (userId != null && respId != null).let {
            println("htcc" + secondUserId + " вариант" + docId)
            dataModel.getDocumentTitleAndUserName(docId!!, secondUserId!!).observe(viewLifecycleOwner){
                println(it)
                binding.documentInfo.text = it?.title
                binding.authorInfo.text = it?.nick
            }
            dataModel.getCommentsFromServ(userId?:0, respId?:0)
            dataModel.getCommentsFromRoom(respId!!).observe(viewLifecycleOwner){
                if(it.isEmpty()){
                    binding.noMessage.visibility = VISIBLE
                }
                else{
                    binding.noMessage.visibility = GONE
                }
                adapter = CommentsListAdapter(
                    it as MutableList<Comment>,
                    userId!!,
                    requireActivity(),
                    dataModel,
                    this
                    )
                binding.messageRecycle.adapter = adapter
                }

            }
        }

    override fun onChildAdapterClickEventMessage(position: Int, text: String): Snackbar {

        val bar = Snackbar.make(binding.root, text, Snackbar.LENGTH_LONG)
        bar.setAnchorView(binding.messageFunctionalView)
        return bar
    }
    override fun onChildClickStartPutEvent(adapterPosition: Int, itemId: Int, mod: String) {
    }
    override fun onTabClickEvent(position: Int, itemId: Int, text: String) {
    }
    override fun onItemInAdapterUsableClickEvent(
        position: Int,
        item: Comment,
        activatorType: String
    ) {
    }

}