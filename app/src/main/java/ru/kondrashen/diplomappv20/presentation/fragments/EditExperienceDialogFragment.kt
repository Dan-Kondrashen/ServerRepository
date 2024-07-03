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
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import ru.kondrashen.diplomappv20.R
import ru.kondrashen.diplomappv20.databinding.AddExperienceDialogFragmentBinding
import ru.kondrashen.diplomappv20.domain.ExtraInfoPageViewModel
import ru.kondrashen.diplomappv20.presentation.baseClasses.ImageFactory
import java.io.File
import java.io.FileOutputStream

class EditExperienceDialogFragment: DialogFragment() {
    private lateinit var extraInfoModel: ExtraInfoPageViewModel
    private var _binding: AddExperienceDialogFragmentBinding? =null
    private val binding get() = _binding!!
    private var suff = ""

    private var choseResult: String =""
    private var experienceAdapter: ArrayAdapter<String>? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = AddExperienceDialogFragmentBinding.inflate(layoutInflater)
        extraInfoModel = ViewModelProvider(requireActivity()).get(ExtraInfoPageViewModel::class.java)
        var text =""

        binding.addFileCheck.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
            TransitionManager.beginDelayedTransition(binding.root as ViewGroup?)
            if (binding.addFileCheck.isChecked) {
                binding.fileInfoView.visibility = View.VISIBLE
                binding.addFileView.visibility = View.VISIBLE
                binding.buttonChoseFile.visibility = View.VISIBLE
            }
            else {
                binding.fileInfoView.visibility = View.GONE
                binding.addFileView.visibility = View.GONE
                binding.buttonChoseFile.visibility = View.GONE
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
            val experienceTime = binding.userExperienceTime.selectedItem.toString()
            val experience = binding.experienceDescInput.text.toString()
            val role = binding.expRoleInput.text.toString()
            val place = binding.expPlaceInput.text.toString()
            val name = binding.fileName.text.toString()
            if (role == "") {
                text = getString(R.string.err_role_not_chosen)
                bar.setText(text)
                bar.show()
            }
            if (binding.addFileCheck.isChecked) {
                if (name == "") {
                    text = getString(R.string.err_empty_name )
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
                        "expTime", experienceTime,
                        "experience", experience,
                        "role", role,
                        "place", place)
                    dismiss()
                }
                else {
                    sendData(
                        "filename",
                        choseResult,
                        "name",
                        name,
                        "suff",
                        suff,
                        "expTime",
                        experienceTime,
                        "experience",
                        experience,
                        "role",
                        role,
                        "place",
                        place
                    )
                    dismiss()
                }
            }
            else {
                sendDataNoFile("filename",
                    "expTime", experienceTime,
                    "experience", experience,
                    "role", role,
                    "place", place)
                dismiss()
            }
        }
        binding.userExperienceTime.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val item = binding.userExperienceTime.selectedItem.toString()

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
                         keyName: String, fileName: String,
                         keySuff: String, suff: String,
                         keyExp: String, experience: String,
                         keyExpTime: String, experienceTime: String,
                         keyRole: String, role: String,
                         keyPlace: String, place: String){
        val bundle = Bundle()
        bundle.putString(keyType, fileUri)
        bundle.putString(keyName, fileName)
        bundle.putString(keySuff, suff)
        bundle.putString(keyExp, experience)
        bundle.putString(keyExpTime, experienceTime)
        bundle.putString(keyRole, role)
        bundle.putString(keyPlace, place)
        setFragmentResult(keyType, bundle)
    }

    private fun sendDataOldFileNewName(keyType: String,
                                       keyName: String, fileName: String,
                                       keyExp: String, experience: String,
                                       keyExpTime: String, experienceTime: String,
                                       keyRole: String, role: String,
                                       keyPlace: String, place: String){
        val bundle = Bundle()
        bundle.putString(keyName, fileName)
        bundle.putString(keyExp, experience)
        bundle.putString(keyExpTime, experienceTime)
        bundle.putString(keyRole, role)
        bundle.putString(keyPlace, place)
        setFragmentResult(keyType, bundle)
    }

    private fun sendDataNoFile(keyType: String, keyExp: String, experience: String,
                               keyExpTime: String, experienceTime: String,
                               keyRole: String, role: String,
                               keyPlace: String, place: String){
        val bundle = Bundle()
        bundle.putString(keyExp, experience)
        bundle.putString(keyExpTime, experienceTime)
        bundle.putString(keyRole, role)
        bundle.putString(keyPlace, place)
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
        val expId = arguments?.getInt("expId")
        extraInfoModel.getExperienceTimeNameList().observe(requireActivity()){ it ->
            val result = it as List<String>
            experienceAdapter = ArrayAdapter(requireActivity(), R.layout.spiner_dropdown_item, result)
            binding.userExperienceTime.adapter = experienceAdapter

            expId?.let {expId ->
                choseResult = "oldfile"

                val userId = arguments?.getInt("userId")
                val mod = arguments?.getString("mod")
                extraInfoModel.getExperienceById(expId).observe(requireActivity()){ expInfo ->
                    experienceAdapter?.let { adapter ->
                        expInfo?.let { experienceInfo ->
                            val expTime = adapter.getPosition(experienceInfo.experienceTime)
                            println(expInfo.experienceTime+ experienceInfo.toString())
                            binding.userExperienceTime.setSelection(expTime)
                        }
                    }
                    expInfo?.documentScanId?.let {
                        binding.addFileCheck.setText(R.string.chose_another_file)
                        activity?.let { active ->
                            ImageFactory.setFileDocPreView(binding.root, it, active)
                        }
                        binding.image1.visibility =View.VISIBLE
                    }
                    expInfo?.let {
                        binding.expPlaceInput.setText(it.place)
                        binding.expRoleInput.setText(it.role)
                        binding.experienceDescInput.setText(it.experience)
                    }
                }
            }

        }


    }
}