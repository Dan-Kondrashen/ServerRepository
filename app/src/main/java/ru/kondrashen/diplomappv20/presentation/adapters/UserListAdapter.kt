package ru.kondrashen.diplomappv20.presentation.adapters

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.content.ContextCompat.getString
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import ru.kondrashen.diplomappv20.R
import ru.kondrashen.diplomappv20.databinding.ListItemChatIntroBinding
import ru.kondrashen.diplomappv20.databinding.ListItemUserBinding
import ru.kondrashen.diplomappv20.presentation.baseClasses.ImageFactory
import ru.kondrashen.diplomappv20.presentation.baseClasses.PublicConstants
import ru.kondrashen.diplomappv20.repository.data_class.User
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.ResponseInfo

class UserListAdapter(users: MutableList<User>, private var userId: Int, private var activity: Activity, navController: NavController): RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    Filterable {
    private var _usersList: MutableList<User> = mutableListOf()
    private var _usersFilterList: MutableList<User> = mutableListOf()
    private val _navController: NavController
    init {
        this._usersList = users
        this._usersFilterList = users
        this._navController = navController
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {

        val binding = ListItemUserBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return UsersViewHolder(binding)
    }


    override fun getItemCount(): Int {
        return _usersList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var holder1 = holder as UsersViewHolder
        val item = _usersList[position]
        holder1.setItem(item)
    }
    inner class UsersViewHolder(private val binding:  ListItemUserBinding): RecyclerView.ViewHolder(binding.root) {
        private lateinit var user: User

        fun setItem(userLoc: User) {
            this.user = userLoc
            if(user.roleId == 2) {
                binding.userFIOInfo.text = "${userLoc.lname} ${userLoc.fname} ${userLoc.mname?: ""}"
                binding.companyInfoView.visibility = GONE
            }
            else {
                binding.companyInfoView.visibility = VISIBLE
                binding.companyInfo.text = user.lname
                binding.userFIOInfo.text = "${userLoc.fname}"
            }
            ImageFactory.setUserIcon(binding.avatar, userLoc.id, activity)
            binding.root.setOnClickListener {
                var bundle = Bundle()
                bundle.putInt(PublicConstants.ADMIN_USER_ID, userId)
                bundle.putInt(PublicConstants.USER_ID, user.id)
                _navController.navigate(R.id.action_mainFragmentView_to_userChoseFragment, bundle)
//                bundle.putInt(PublicConstants.DOC_ID, user.docId)
//                bundle.putInt(PublicConstants.RESP_ID, user.respId)
//                _navController.navigate(R.id.action_chatChoseFragment_to_chatFragment, bundle)

            }
        }
    }

    override fun getFilter(): Filter {
        return  object  : Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val resultOfFiltration = FilterResults()
                if(constraint == null || constraint.length <0){
                    resultOfFiltration.count = _usersFilterList.size
                    resultOfFiltration.values = _usersFilterList
                }else{
                    var str = constraint.toString().lowercase()
                    var resps = mutableListOf<User>()
                    for(item in _usersFilterList){
                        if(item.email?.lowercase()?.contains(str) == true
                            ||item.phone.toString().lowercase().contains(str)
                            ||item.fname.lowercase().contains(str)
                            ||item.lname.lowercase().contains(str)
                            ||item.mname?.lowercase()?.contains(str) == true){
                            resps.add(item)
                        }
                    }
                    resultOfFiltration.count = resps.size
                    resultOfFiltration.values = resps
                }
                return resultOfFiltration
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                _usersList =  results!!.values as MutableList<User>
                notifyDataSetChanged()
            }

        }
    }

}