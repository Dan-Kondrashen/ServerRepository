package ru.kondrashen.diplomappv20.presentation.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import ru.kondrashen.diplomappv20.R
import ru.kondrashen.diplomappv20.databinding.EditUserDialogFragmentBinding
import ru.kondrashen.diplomappv20.domain.UserAccountControlViewModel
import ru.kondrashen.diplomappv20.presentation.baseClasses.PublicConstants

class EditUserDialogFragment: DialogFragment() {
    private lateinit var userInfoModel: UserAccountControlViewModel
    private var statusAdapter: ArrayAdapter<String>? = null
    private var status ="not confirmed"
    private var _binding: EditUserDialogFragmentBinding? =null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = EditUserDialogFragmentBinding.inflate(layoutInflater)
        userInfoModel = ViewModelProvider(requireActivity()).get(UserAccountControlViewModel::class.java)
        var text =""


        binding.cancelBtn.setOnClickListener{
            dismiss()
        }
        binding.saveBtn.setOnClickListener{
            val bar = Snackbar.make(binding.root, text, Snackbar.LENGTH_LONG)
            val textview = bar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
            textview.gravity = Gravity.CENTER_HORIZONTAL
            textview.textAlignment =View.TEXT_ALIGNMENT_CENTER
            val fname = binding.userFnameInput.text.toString()
            val lname = binding.userLnameInput.text.toString()
            val mname = binding.userMnameInput.text.toString()
            val phone = binding.userPhoneInput.text.toString().toLongOrNull()
            val email = binding.userEmailInput.text.toString()
            if (email == "" || phone == null) {
                text = getString(R.string.err_user_auth_data)
                bar.setText(text)
                bar.show()
            }
            else if(fname == "" || lname == ""){
                text = getString(R.string.err_user_personal_data)
                bar.setText(text)
                bar.show()
            }
            else{

                sendData(
                    "operationStatus",
                    "success",
                    "fname",
                    fname,
                    "lname",
                    lname,
                    "mname",
                    mname,
                    "email",
                    email,
                    "phone",
                    phone,
                    "status",
                    status
                )
                dismiss()
            }


        }
        updateUIDialog()
        return AlertDialog.Builder(requireActivity())
            .setView(binding.root)
            .create()
    }


    private fun sendData(keyType: String, type: String,
                         keyFname: String, fname: String,
                         keyLname: String, lname: String,
                         keyMname: String, mname: String,
                         keyEmail: String, email: String,
                         keyPhone: String, phone: Long,
                         keyStatus: String, status: String?){
        val bundle = Bundle()
        bundle.putString(keyType, type)
        bundle.putString(keyFname, fname)
        bundle.putString(keyLname, lname)
        bundle.putString(keyMname, mname)
        bundle.putString(keyEmail, email)
        bundle.putString(keyStatus, status)
        bundle.putLong(keyPhone, phone)
        setFragmentResult(keyType, bundle)
    }

    private fun updateUIDialog(){
        val authUserId = arguments?.getInt(PublicConstants.ADMIN_USER_ID)
        val userId = arguments?.getInt(PublicConstants.USER_ID)
        val expStatusArray = requireContext().resources.getStringArray(R.array.pref_user_status)
        statusAdapter = ArrayAdapter(requireActivity(), R.layout.spiner_dropdown_item, expStatusArray)
        binding.userStatusSelector.adapter = statusAdapter
        binding.userStatusSelector.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                status = when(position){
                    0 -> "confirmed"
                    else -> "not confirmed"
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        userId?.let {uId ->
            activity?.let {active ->
                userInfoModel.getUserData(uId).observe(active) { user ->
                    binding.userEmailInput.setText(user.email)
                    binding.userPhoneInput.setText(user.phone.toString())
                    binding.userFnameInput.setText(user.fname)
                    binding.userLnameInput.setText(user.lname)
                    binding.userMnameInput.setText(user.mname)
                    when(user.status){
                        "confirmed" -> binding.userStatusSelector.setSelection(0)
                        "not confirmed" -> binding.userStatusSelector.setSelection(1)
                        else -> binding.userStatusSelector.setSelection(1)
                    }
                }
            }

        }
    }
}