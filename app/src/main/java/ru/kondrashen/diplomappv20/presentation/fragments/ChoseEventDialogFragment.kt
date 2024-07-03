package ru.kondrashen.diplomappv20.presentation.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import ru.kondrashen.diplomappv20.R
import ru.kondrashen.diplomappv20.databinding.ChoseArchiveDialogFragmentBinding
import ru.kondrashen.diplomappv20.domain.ExtraInfoPageViewModel
import ru.kondrashen.diplomappv20.domain.UserAccountControlViewModel
import ru.kondrashen.diplomappv20.presentation.baseClasses.PublicConstants
import java.util.ArrayList

class ChoseEventDialogFragment: DialogFragment() {
    private lateinit var userAccountModel: UserAccountControlViewModel
    private lateinit var extraInfoModel: ExtraInfoPageViewModel
    private var _binding: ChoseArchiveDialogFragmentBinding? =null
    private val binding get() = _binding!!
    companion object{
        const val TAG = "ChoseEventDialogFr"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = ChoseArchiveDialogFragmentBinding.inflate(layoutInflater)
        userAccountModel = ViewModelProvider(requireActivity()).get(UserAccountControlViewModel::class.java)
        var text =""


        binding.cancelBtn.setOnClickListener{
            dismiss()
        }

        updateUIDialog()
        return AlertDialog.Builder(requireActivity())
            .setView(binding.root)
            .create()
    }



    private fun updateUIDialog(){
        val userId = arguments?.getInt(PublicConstants.USER_ID)
        val docId = arguments?.getInt(PublicConstants.DOC_ID)

        if(userId!=null && docId != null){
            binding.apply {
                userAccountModel.getDocResponsesByUserIdFromRoom(userId, docId)
                    .observe(requireActivity()) { responsesFromRoom ->
                        println(responsesFromRoom+"Воттак")
                        responsesFromRoom.find { it == "response" }?.let {
                            makeResponseBtn.setBackgroundResource(R.drawable.hidden_list_pressed)
                            makeResponseBtn.isClickable= false
                        }
                        responsesFromRoom.find { it == "favorite" }?.let {
                            makeFavoriteBtn.setBackgroundResource(R.drawable.hidden_list_pressed)
                            makeFavoriteBtn.isClickable= false
                        }
                        responsesFromRoom.find { it == "dismiss" }?.let {
                            hideItemBtn.setBackgroundResource(R.drawable.hidden_list_pressed)
                            hideItemBtn.isClickable= false
                        }

                    }
                makeResponseBtn.setOnClickListener {
                    userAccountModel.postResponse(userId, listOf("response"), docId)
                        .observe(requireActivity()) { resp ->
                            Log.i(TAG, resp)
                        }
                }
                makeFavoriteBtn.setOnClickListener {
                    userAccountModel.postResponse(userId, listOf("favorite"), docId)
                        .observe(requireActivity()) { resp ->
                            Log.i(TAG, resp)
                        }
                }
                hideItemBtn.setOnClickListener {
                    userAccountModel.postResponse(userId, listOf("dismiss"), docId)
                        .observe(requireActivity()) { resp ->
                            Log.i(TAG, resp)
                        }
                }
                addToArchivesBtn.setOnClickListener {
                    if (isAdded && activity != null)
                        userAccountModel.getDocResponsesByUserIdWithoutNamesFromRoom(userId, docId,
                            listOf("favorite, dismiss, response, view")).observeOneData(activity as AppCompatActivity){
                            val manager = (activity as AppCompatActivity).supportFragmentManager
                            val dialog = ChoseItemsDialogFragment()
                            val bundle = Bundle()
                            bundle.putInt(PublicConstants.DOC_ID, docId)
                            bundle.putStringArrayList("listOfNames", it as ArrayList<String>)
                            bundle.putInt(PublicConstants.USER_ID, userId)
                            bundle.putString(PublicConstants.MODE, "chose_archive")
                            dialog.arguments = bundle
                            dialog.show(manager, "result")
//                            Handler(Looper.getMainLooper()).postDelayed({dismiss()}, 1000)
                        }

                }
            }
        }

    }
    fun <T> LiveData<T>.observeOneData(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        var previousKey: Any? = value?: "NULL"
        observe(lifecycleOwner){
            if(previousKey == "NULL"){
                previousKey = value
                observer.onChanged(value!!)
            }
        }
    }
}