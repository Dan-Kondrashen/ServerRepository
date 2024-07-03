package ru.kondrashen.diplomappv20.presentation.fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.transition.TransitionManager
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import org.checkerframework.checker.units.qual.A
import ru.kondrashen.diplomappv20.R
import ru.kondrashen.diplomappv20.databinding.AddSpecializationDialogFragmentBinding
import ru.kondrashen.diplomappv20.domain.ExtraInfoPageViewModel
import ru.kondrashen.diplomappv20.presentation.baseClasses.ImageFactory
import ru.kondrashen.diplomappv20.repository.data_class.Education
import java.io.File
import java.io.FileOutputStream

class EditSpecializationDialogFragment: DialogFragment() {
    private lateinit var extraInfoModel: ExtraInfoPageViewModel
    private var _binding: AddSpecializationDialogFragmentBinding? =null
    private val binding get() = _binding!!
    private var suff = ""
    private var choseResult: String =""
    private var dependEducation: Education? = null
    private var adapterSpec: ArrayAdapter<String>? = null
    private var adapterEdu: ArrayAdapter<String>? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = AddSpecializationDialogFragmentBinding.inflate(layoutInflater)
        extraInfoModel = ViewModelProvider(requireActivity()).get(ExtraInfoPageViewModel::class.java)
        var text =""

        binding.addFileCheck.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
            TransitionManager.beginDelayedTransition(binding.root as ViewGroup?)
            if (binding.addFileCheck.isChecked) {
                binding.fileInfoView.visibility = View.VISIBLE
                binding.addFileView.visibility = View.VISIBLE
            }
            else {
                binding.fileInfoView.visibility = View.GONE
                binding.addFileView.visibility = View.GONE
            }
        }
        binding.buttonChoseFile.setOnClickListener {
            val allowTypes = arrayOf("image/*", "application/pdf")
            val intent = Intent()
                .setType("*/*")
                .putExtra(Intent.EXTRA_MIME_TYPES, allowTypes)
                .setAction(Intent.ACTION_GET_CONTENT)
            startChoseFileLauncher.launch(intent)
        }
        binding.cancelBtn.setOnClickListener{
            dismiss()
        }
        binding.saveBtn.setOnClickListener{
            val bar = Snackbar.make(binding.root, text, Snackbar.LENGTH_LONG)
            val textview = bar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
            textview.gravity = Gravity.CENTER_HORIZONTAL
            textview.textAlignment =View.TEXT_ALIGNMENT_CENTER
            val specialization = binding.userSpecialization.selectedItem.toString()
            val education = binding.userEducation.selectedItem?.toString()?: ""
            val name = binding.fileName.text.toString()
            if (binding.addFileCheck.isChecked) {
                if (name == "") {
                    text = getString(R.string.empty_file_name)
                    bar.setText(text)
                    bar.show()
                }
                else if (choseResult in listOf("", null)){
                    bar.setText(R.string.forgot_a_file)
                    bar.show()

                }
                else if (choseResult =="oldfile"){
                    sendDataOldFileNewName("filename",
                        "name", name,
                        "specialization", specialization,
                        "education", education)
                    dismiss()
                }
                else {
                    sendData(
                        "filename", choseResult,
                        "suff", suff,
                        "specialization", specialization,
                        "education", education,
                        "name", name
                    )
                    dismiss()
                }
            }
            else {
                sendDataNoFile("filename",
                    "specialization", specialization,
                    "education", education)
                dismiss()
            }
        }
        binding.userSpecialization.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val item = binding.userSpecialization.selectedItem.toString()
                if (isAdded && activity != null) {
                    extraInfoModel.getEducationNamesBySpecName(item).observe(this@EditSpecializationDialogFragment) {
                        val result = it
                        adapterEdu =
                            ArrayAdapter(activity as AppCompatActivity, R.layout.spiner_dropdown_item, result)
                        binding.userEducation.adapter = adapterEdu
                        val eduPos = adapterEdu!!.getPosition(dependEducation?.name)
                        binding.userEducation.setSelection(eduPos)
                    }
                }
            }


            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }
        updateUIDialog()
        return AlertDialog.Builder(requireActivity())

            .setView(binding.root)
            .create()
    }
    private fun sendData(keyType: String, fileUri: String,
                         keySuff: String, suff: String,
                         keySpec: String, specialization: String,
                         keyEdu: String, education: String,
                         keyName: String, name: String){
        val bundle = Bundle()
        bundle.putString(keyType, fileUri)
        bundle.putString(keySuff, suff)
        bundle.putString(keySpec, specialization)
        bundle.putString(keyEdu, education)
        bundle.putString(keyName, name)
        setFragmentResult(keyType, bundle)
    }

    private fun sendDataOldFileNewName(keyType: String,
                                       keySpec: String, specialization: String,
                                       keyEdu: String, education: String,
                                       keyName: String, name: String){
        val bundle = Bundle()
        bundle.putString(keyName, name)
        bundle.putString(keySpec, specialization)
        bundle.putString(keyEdu, education)
        setFragmentResult(keyType, bundle)
    }

    private fun sendDataNoFile(keyType: String, keySpec: String, specialization: String, keyEdu: String, education: String){
        val bundle = Bundle()
        bundle.putString(keySpec, specialization)
        bundle.putString(keyEdu, education)
        setFragmentResult(keyType, bundle)
    }

    private val startChoseFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == Activity.RESULT_OK) {
            val data = it.data?.data
            choseResult = data.toString()
            val contentResolver = requireContext().contentResolver
            val file2 = File.createTempFile("Image", suff)
            var outPut = FileOutputStream(file2)
            val inputStream = contentResolver.openInputStream(data!!)
            val mime = contentResolver.getType(data)
            binding.hintText.visibility = View.VISIBLE
            if (mime!!.contains("image")){
                suff =".jpeg"
                binding.image1.setImageBitmap(BitmapFactory.decodeStream(inputStream))
                binding.image1.visibility = View.VISIBLE
                binding.pdf1.visibility  = View.GONE
            }
            else {
                suff = ".pdf"
                binding.pdf1.fromUri(data)
                    .defaultPage(0)
                    .load()
                binding.image1.visibility = View.GONE
                binding.pdf1.visibility = View.VISIBLE
            }
            inputStream.use { input ->
                outPut.use { out ->
                    input!!.copyTo(out)
                }
            }


        }

    }
    private fun updateUIDialog(){
        val dependId = arguments?.getInt("dependId")
        if (isAdded) {
            extraInfoModel.getSpecializationNames().observe(requireActivity()) {
                val result = it
                context?.let { contextL ->
                    adapterSpec = ArrayAdapter(contextL, R.layout.spiner_dropdown_item, result)
                    binding.userSpecialization.adapter = adapterSpec
                }
                dependId?.let { dependId ->
                    val userId = arguments?.getInt("userId")
                    val mod = arguments?.getString("mod")
                    if (isAdded) {
                        extraInfoModel.getDependenceById(dependId)
                            .observe(this) { dependInfo ->
                                dependInfo.docDependence.documentsScanId?.let {
                                    binding.addFileCheck.setText(R.string.chose_another_file)
                                    binding.image1.visibility = View.VISIBLE
                                    activity?.let { act->
                                        ImageFactory.setFileDocPreView(
                                            binding.root,
                                            it ,
                                            act as AppCompatActivity
                                        )
                                    }

                                }
                                adapterSpec?.let { adapter ->
                                    val specializationPos =
                                        adapter.getPosition(dependInfo.specializations.name)
                                    binding.userSpecialization.setSelection(specializationPos)
                                    adapterEdu = ArrayAdapter(
                                        activity as AppCompatActivity,
                                        R.layout.spiner_dropdown_item,
                                        listOf(dependInfo.educations?.name)
                                    )
                                    binding.userEducation.adapter = adapterEdu
                                    dependEducation = dependInfo.educations

                                }
                            }
                    }
                }
            }
        }

    }
}