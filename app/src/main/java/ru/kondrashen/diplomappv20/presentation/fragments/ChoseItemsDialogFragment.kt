package ru.kondrashen.diplomappv20.presentation.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import ru.kondrashen.diplomappv20.R
import ru.kondrashen.diplomappv20.databinding.ChoseSpecDialogFragmentBinding
import ru.kondrashen.diplomappv20.domain.ExtraInfoPageViewModel
import ru.kondrashen.diplomappv20.domain.UserAccountControlViewModel
import ru.kondrashen.diplomappv20.presentation.adapters.CustomArchiveRVAdapter
import ru.kondrashen.diplomappv20.presentation.adapters.CustomExperienceRVAdapter
import ru.kondrashen.diplomappv20.presentation.adapters.CustomSpecializationRVAdapter
import ru.kondrashen.diplomappv20.presentation.baseClasses.PublicConstants
import ru.kondrashen.diplomappv20.repository.data_class.Archive
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocDependenceFullInfo
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.ExperienceInfo

class ChoseItemsDialogFragment: DialogFragment() {
    private lateinit var userAccountModel: UserAccountControlViewModel
    private lateinit var extraInfoModel: ExtraInfoPageViewModel
    private var _binding: ChoseSpecDialogFragmentBinding? =null
    private val binding get() = _binding!!
    private var adapterSpec: CustomSpecializationRVAdapter? = null
    private var adapterExp: CustomExperienceRVAdapter? = null
    private var adapterArch: CustomArchiveRVAdapter? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = ChoseSpecDialogFragmentBinding.inflate(layoutInflater)
        extraInfoModel = ViewModelProvider(requireActivity()).get(ExtraInfoPageViewModel::class.java)
        userAccountModel = ViewModelProvider(requireActivity()).get(UserAccountControlViewModel::class.java)
        var text =""

        binding.title.setText(R.string.chose_exp)

        binding.cancelBtn.setOnClickListener{
            dismiss()
        }
        binding.saveBtn.setOnClickListener{
            adapterSpec?.let {
                val bar = Snackbar.make(binding.root, text, Snackbar.LENGTH_LONG)
                val textview = bar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                textview.gravity = Gravity.CENTER_HORIZONTAL
                textview.textAlignment = View.TEXT_ALIGNMENT_CENTER
                val modItemList = it.getCheckedItems()
                when (modItemList.size) {
                    0-> bar.setText(R.string.not_chosed).show()
                    in 1..7-> {
                        sendData("result", "dependenceIds",
                            modItemList as ArrayList<Int>)
                        dismiss()
                    }
                    else -> bar.setText(R.string.so_many_item_7).show()
                }
            }
            adapterExp?.let {
                val bar = Snackbar.make(binding.root, text, Snackbar.LENGTH_LONG)
                val textview = bar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                textview.gravity = Gravity.CENTER_HORIZONTAL
                textview.textAlignment = View.TEXT_ALIGNMENT_CENTER
                val modItemList = it.getCheckedItems()
                when (modItemList.size) {
                    0-> bar.setText(R.string.not_chosed).show()
                    in 1..7-> {
                        sendData("result", "dependenceIds",
                            modItemList as ArrayList<Int>)
                        dismiss()
                    }
                    else -> bar.setText(R.string.so_many_item_7).show()
                }
            }
            adapterArch?.let {
                val bar = Snackbar.make(binding.root, text, Snackbar.LENGTH_LONG)
                val modItemList = it.getCheckedItems()
                val userId = arguments?.getInt(PublicConstants.USER_ID)
                val docId = arguments?.getInt(PublicConstants.DOC_ID)
                when (modItemList.size) {
                    0-> bar.setText(R.string.not_chosed).show()
                    else -> {
                        userId?.let {
                            userAccountModel.postResponse(userId, modItemList, docId!!)
                        }
                        dismiss()
                    }
                }
            }
        }
        updateUIDialog()
        return AlertDialog.Builder(requireActivity())
            .setView(binding.root)
            .create()
    }

    private fun sendData(keyType: String, keySpec: String, dependenceIdsList: ArrayList<Int>){
        val bundle = Bundle()
        bundle.putIntegerArrayList(keySpec, dependenceIdsList)
        setFragmentResult(keyType, bundle)
    }


    private fun updateUIDialog(){
        val userId = arguments?.getInt(PublicConstants.USER_ID)
        val iDs = arguments?.getIntegerArrayList("listOfIndexes")
        val mod = arguments?.getString(PublicConstants.MODE)

        userId?.let {
            when(mod){
                "chose_spec" -> {
                    binding.title.setText(R.string.chose_spec)
                    userAccountModel.getUserDocumentDependenceByIdsRoom(userId, iDs ?: listOf(0), "out")
                        .observe(requireActivity()) {
                            var docDependencies = it as MutableList<DocDependenceFullInfo>
                            if (docDependencies.isNotEmpty()) {
                                adapterSpec = CustomSpecializationRVAdapter(docDependencies)
                                binding.itemChoseRecycle.adapter = adapterSpec
                            }
                            else{
                                binding.adapterProblem.visibility = VISIBLE
                                binding.adapterProblem.text = getText(R.string.no_items_in_adapter_doc)
                                binding.itemChoseRecycle.visibility = GONE
                            }
                        }
                }
                "chose_exp" -> {
                    binding.title.setText(R.string.chose_exp)
                    userAccountModel.getUserExperienceByIdsRoom(userId, iDs ?: listOf(0), "out")
                        .observe(requireActivity()) {
                            var experience = it as MutableList<ExperienceInfo>
                            if (experience.isNotEmpty()) {
                                adapterExp = CustomExperienceRVAdapter(experience)
                                binding.itemChoseRecycle.adapter = adapterExp
                            }
                            else{
                                binding.adapterProblem.visibility = VISIBLE
                                binding.adapterProblem.text = getText(R.string.no_items_in_adapter_doc)
                                binding.itemChoseRecycle.visibility = GONE
                            }

                        }
                }
                "chose_archive" -> {
                    val types = arguments?.getStringArrayList("listOfNames")
                    binding.title.setText(R.string.chose_user_archive)
                    userAccountModel.getUserArchivesRoom(userId).observe(requireActivity()) {
                        var archiveList = it as MutableList<Archive>
                        if (archiveList.isNotEmpty()) {
                            adapterArch = CustomArchiveRVAdapter(archiveList, types ?: listOf())
                            binding.itemChoseRecycle.adapter = adapterArch
                        }
                        else{
                            binding.adapterProblem.visibility = VISIBLE
                            binding.adapterProblem.text = getText(R.string.no_items_in_adapter_arch)
                            binding.itemChoseRecycle.visibility = GONE
                        }

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