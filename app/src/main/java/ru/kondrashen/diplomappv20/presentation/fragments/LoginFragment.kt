package ru.kondrashen.diplomappv20.presentation.fragments

// Импорт библитиотек для работы фрагмента
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import ru.kondrashen.diplomappv20.R
import ru.kondrashen.diplomappv20.domain.AuthViewModel
import ru.kondrashen.diplomappv20.databinding.FragmentLoginBinding
import ru.kondrashen.diplomappv20.domain.MainPageViewModel
import ru.kondrashen.diplomappv20.presentation.activitys.MainPageActivity
import ru.kondrashen.diplomappv20.presentation.baseClasses.PublicConstants
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.UserLog
import ru.kondrashen.diplomappv20.repository.responces.AuthResponse

// Класс фрагмента, реализующий основную логику авторизации пользователя
class LoginFragment(private val testContext: Context? = null) : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private lateinit var dataModel: AuthViewModel
    private val dataModel2: MainPageViewModel by viewModels()
    private var adapter: ArrayAdapter<String>? = null
    private var rolesName: MutableList<String> = mutableListOf()
    private var result: AuthResponse = AuthResponse("",0, "", "")
    private val fragmentContext: Context?
        get() = testContext ?: requireContext()
    companion object{
        private const val LOG_TAG = "loginFragment"
    }
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        updateUI()
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Кнопка отправки запроса на авторизацию
        binding.login.setOnClickListener {
            login(binding.userRole.selectedItem.toString(), binding.etEmail.text.toString(), binding.etPassword.text.toString())
        }
        binding.losePass.setOnClickListener{
//            dataModel.clearDatabase()
//            binding.result.text = dataModel2.getUnRegMainData("all", 20)
            var type = when(binding.userRole.selectedItem.toString()){
                "соискатель"-> "vacancy"
                "работодатель" -> "resume"
                else -> "all"
            }
            val intent = MainPageActivity.newIntent(requireActivity(), 1, binding.userRole.selectedItem.toString())
            println("Проверка ${binding.userRole.selectedItem}")
            startActivity(intent)
            Toast.makeText(fragmentContext, "Успешно авторизовано!", Toast.LENGTH_SHORT).show()

//            dataModel2.getRegMainData(1,type, 20, "userSkill")
        }
        binding.joinAsGuest.setOnClickListener{
            val intent = MainPageActivity.newIntentGuest(requireActivity(), binding.userRole.selectedItem.toString())
            startActivity(intent)
            Toast.makeText(fragmentContext, "Вы вошли в режиме ограниченной функциональности! Вы можете только просматривать вакансии или резюме других пользователей, но данные о том что вы их просматривали не сохранятся!", Toast.LENGTH_LONG).show()
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun logValidator(email: String, password: String): Boolean{
        return (email != "" && email != "null" && password !="" && password !="null")
    }
    private fun login(role: String, email: String, password: String){
        var type = when(role){
            "соискатель"-> "vacancy"
            "работодатель" -> "resume"
            else -> "vacancy"
        }

        dataModel.getRolesId(role)
            .observe(requireActivity()) {
                val roleId = it
                println(roleId)
                roleId?.let {

                    if (logValidator(email, password)) {

                        result = dataModel.login(
                            UserLog(
                                email,
                                password,
                                roleId
                            )
                        )
                        println("Вот: $result")
                        binding.result.text = result.status
                        if (result.status == "Вы успешно вошли в систему!") {
                            Log.i(LOG_TAG, "login success")
                            dataModel.clearDatabase()
                            dataModel2.getRegMainData(result.id, type, 20, 1, "new")

                            val intent = MainPageActivity.newIntent(
                                requireActivity(),
                                result.id,
                                role
                            )
                            startActivity(intent)
                            Toast.makeText(
                                fragmentContext,
                                "Успешно авторизовано!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else
                            Log.e(LOG_TAG, "login failed")
                    } else {
                        Toast.makeText(
                            fragmentContext,
                            "Сначала нужно внести аутентификационные данные",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }
    fun testLogin(){
        login("соискатель", "dankonad@yandex.com", "123")
    }
    private fun updateUI(){
        dataModel.getRolesFromRoom().observe(requireActivity()){
            rolesName = it as MutableList<String>
            if (rolesName.isEmpty()) {
                dataModel.getRolesFromServer().observe(requireActivity()){
                    rolesName = it as MutableList<String>
                    adapter = ArrayAdapter(requireActivity(), R.layout.spiner_dropdown_item, rolesName)
                }
            }
            else {
                adapter = ArrayAdapter(requireActivity(), R.layout.spiner_dropdown_item, rolesName)
            }
            binding.userRole.adapter = adapter
        }

    }
}

//var type = when(binding.userRole.selectedItem.toString()){
//    "соискатель"-> "vacancy"
//    "работодатель" -> "resume"
//    else -> "vacancy"
//}
//
//dataModel.getRolesId(binding.userRole.selectedItem.toString())
//.observe(requireActivity()) {
//    val roleId = it
//    println(roleId)
//    if (binding.etEmail.text.toString() != "" && binding.etEmail.text.toString() != "null"
//        && binding.etPassword.text.toString() !="" && binding.etPassword.text.toString() !="null") {
//        result = dataModel.login(
//            UserLog(
//                binding.etEmail.text.toString(),
//                binding.etPassword.text.toString(),
//                roleId
//            )
//        )
//        println("Вот: $result")
//        binding.result.text = result.status
//        if (result.status == "Вы успешно вошли в систему!") {
//            dataModel.clearDatabase()
//            dataModel2.getRegMainData(result.id, type, 20, 1, "new")
//
//            val intent = MainPageActivity.newIntent(
//                requireActivity(),
//                result.id,
//                binding.userRole.selectedItem.toString()
//            )
//            startActivity(intent)
//            Toast.makeText(
//                requireContext(),
//                "Успешно авторизовано!",
//                Toast.LENGTH_SHORT
//            ).show()
//        }
//    }
//    else{
//        Toast.makeText(
//            requireContext(),
//            "Сначала нужно внести аутентификационные данные",
//            Toast.LENGTH_SHORT
//        ).show()
//    }
//
//}