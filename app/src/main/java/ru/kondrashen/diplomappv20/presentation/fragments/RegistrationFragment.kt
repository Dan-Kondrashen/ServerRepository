package ru.kondrashen.diplomappv20.presentation.fragments

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.GONE
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.AdapterView.VISIBLE
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import ru.kondrashen.diplomappv20.R
import ru.kondrashen.diplomappv20.databinding.FragmentRegistrationBinding
import ru.kondrashen.diplomappv20.domain.AuthViewModel
import ru.kondrashen.diplomappv20.domain.MainPageViewModel
import ru.kondrashen.diplomappv20.presentation.activitys.MainPageActivity
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.AddUser
import ru.kondrashen.diplomappv20.repository.responces.AuthResponse

class RegistrationFragment: Fragment() {
    private var _binding: FragmentRegistrationBinding? = null
    private lateinit var dataModel: AuthViewModel
    private val dataModel2: MainPageViewModel by viewModels()
    private val binding get() = _binding!!
    private var adapter: ArrayAdapter<String>? = null
    private var rolesName: MutableList<String> = mutableListOf()
    private var result: AuthResponse = AuthResponse("",0, "", "")
    private var text: String =""
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        updateUI()
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.registration.setOnClickListener {
            val bar = Snackbar.make(binding.root, text,Snackbar.LENGTH_LONG)
            val textview = bar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
            textview.gravity =Gravity.CENTER_HORIZONTAL
            textview.textAlignment =View.TEXT_ALIGNMENT_CENTER
            var type = when(binding.userRole.selectedItem.toString()){
                "соискатель"-> "vacancy"
                "работодатель" -> "resume"
                else -> "all"
            }
            if (binding.etPhone.text != null && binding.etPhone.text.toString() != "" && binding.etEmail.text != null && binding.etEmail.text.toString() != "" ) {
                if (binding.etPassword.text.toString() == binding.etPasswordRepeat.text.toString()) {
                    dataModel.clearDatabase()
                    dataModel.getRolesId(binding.userRole.selectedItem.toString())
                        .observe(requireActivity()) {
                            val roleId = it
                            result = dataModel.register(
                                AddUser(
                                    binding.etFname.text.toString(),
                                    binding.etLname.text.toString(),
                                    if (binding.etMname.visibility == VISIBLE) binding.etMname.text.toString() else "",
                                    binding.etPhone.text.toString().toLongOrNull(),
                                    binding.etEmail.text.toString(),
                                    binding.etPassword.text.toString(),
                                    roleId.toLong(),
                                )
                            )
                            binding.result.text = result.status
                            if (result.status == "Пользователь успешно добавлен!") {
                                val intent = MainPageActivity.newIntent(
                                    requireActivity(),
                                    result.id,
                                    binding.userRole.selectedItem.toString()
                                )
                                dataModel.clearDatabase()
                                dataModel2.getRegMainData(result.id, type, 20, 1, "new")
                                startActivity(intent)
                                text = getString(R.string.add_account_success)
                                bar.setText(text)
                                bar.show()
                            }
                        }
                } else {
                    text = getString(R.string.wrong_pass_copy)
                    bar.setText(text)
                    bar.show()
                }
            }
            else{
                text = getString(R.string.not_at_all)
                bar.setText(text)
                bar.show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)
    }

    private fun updateUI(){
        dataModel.getRolesFromRoom().observe(requireActivity()){
            rolesName = it.filter { it != "администратор" } as MutableList<String>
            if (rolesName.isEmpty()) {
                dataModel.getRolesFromServer().observe(requireActivity()){
                    rolesName = it.filter { it != "администратор" }  as MutableList<String>
                    adapter = ArrayAdapter(requireActivity(), R.layout.spiner_dropdown_item, rolesName)
                }
            }
            else {
                adapter = ArrayAdapter(requireActivity(), R.layout.spiner_dropdown_item, rolesName)
            }
            binding.userRole.adapter = adapter
            binding.userRole.onItemSelectedListener = object : OnItemSelectedListener{
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (binding.userRole.selectedItem.toString() == "работодатель"){
                        binding.etMname.visibility = GONE
                        binding.etFname.hint = getString(R.string.userFIO)
                        binding.etLname.hint = getString(R.string.company_name)
                    }
                    else{
                        binding.etMname.visibility = VISIBLE
                        binding.etFname.hint = getString(R.string.fname)
                        binding.etLname.hint = getString(R.string.lname)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }

            }
        }

    }

}