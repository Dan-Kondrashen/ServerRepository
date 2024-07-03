package ru.kondrashen.diplomappv20.presentation.adapters

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.content.ContextCompat.getString
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import ru.kondrashen.diplomappv20.R
import ru.kondrashen.diplomappv20.databinding.ListItemChatIntroBinding
import ru.kondrashen.diplomappv20.presentation.baseClasses.ImageFactory
import ru.kondrashen.diplomappv20.presentation.baseClasses.PublicConstants
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocumentInfoWithKnowledge
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.ResponseInfo

class ChatListAdapter(respInfoList: MutableList<ResponseInfo>, private var userId: Int, private var activity: Activity, navController: NavController): RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    Filterable {
    private var _respInfoList: MutableList<ResponseInfo> = mutableListOf()
    private var _respInfoFilterList: MutableList<ResponseInfo> = mutableListOf()
    private val _navController: NavController
    init {
        this._respInfoList = respInfoList
        this._respInfoFilterList = respInfoList
        this._navController = navController
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatChoseViewHolder {

        val binding = ListItemChatIntroBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return ChatChoseViewHolder(binding)
    }


    override fun getItemCount(): Int {
        return _respInfoList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var holder1 = holder as ChatChoseViewHolder
        val item = _respInfoList[position]
        holder1.setItem(item)
    }
    inner class ChatChoseViewHolder(private val binding:  ListItemChatIntroBinding): RecyclerView.ViewHolder(binding.root) {
        private lateinit var responseInfo: ResponseInfo

        fun setItem(responseInfo1: ResponseInfo) {
            this.responseInfo = responseInfo1
            binding.titleInfo.text = responseInfo1.title
            binding.respId.text = responseInfo1.respId.toString()
            binding.userNameText.text = responseInfo1.userFIO
            if (responseInfo.commUserId == userId)
                binding.lastContentInfo.text = "${getString(activity, R.string.your_comm)} ${responseInfo1.commContent}"
            else if (responseInfo.commContent != null)
                binding.lastContentInfo.text =  "${getString(activity, R.string.another_user_comm)} ${responseInfo1.commContent}"
            else
                binding.lastContentInfo.visibility = GONE
            ImageFactory.setUserIcon(binding.avatar, responseInfo1.userId, activity)
            binding.root.setOnClickListener {
                var bundle = Bundle()
                bundle.putInt(PublicConstants.USER_ID, userId)
                bundle.putInt(PublicConstants.ANOTHER_USER_ID, responseInfo.userId)
                bundle.putInt(PublicConstants.DOC_ID, responseInfo.docId)
                bundle.putInt(PublicConstants.RESP_ID, responseInfo.respId)
                _navController.navigate(R.id.action_chatChoseFragment_to_chatFragment, bundle)

            }
        }
    }

    override fun getFilter(): Filter {
        return  object  : Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val resultOfFiltration = FilterResults()
                if(constraint == null || constraint.length <0){
                    resultOfFiltration.count = _respInfoFilterList.size
                    resultOfFiltration.values = _respInfoFilterList
                }else{
                    var str = constraint.toString().lowercase()
                    var resps = mutableListOf<ResponseInfo>()
                    for(item in _respInfoFilterList){
                        if(item.title.lowercase().contains(str)||item.commContent.toString().lowercase().contains(str)||item.userFIO.lowercase().contains(str)){
                            resps.add(item)
                        }
                    }
                    resultOfFiltration.count = resps.size
                    resultOfFiltration.values = resps
                }
                return resultOfFiltration
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                _respInfoList =  results!!.values as MutableList<ResponseInfo>
                notifyDataSetChanged()
            }

        }
    }

}