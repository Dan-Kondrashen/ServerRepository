package ru.kondrashen.diplomappv20.presentation.adapters

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getString
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import ru.kondrashen.diplomappv20.R
import ru.kondrashen.diplomappv20.databinding.TabListItemBinding
import ru.kondrashen.diplomappv20.domain.UserAccountControlViewModel
import ru.kondrashen.diplomappv20.presentation.baseClasses.UserAnimatorHelper
import ru.kondrashen.diplomappv20.presentation.baseClasses.onItemClickInterface
import ru.kondrashen.diplomappv20.presentation.fragments.ConfirmDialogFragment
import ru.kondrashen.diplomappv20.presentation.fragments.EditArchiveDialogFragment
import ru.kondrashen.diplomappv20.repository.data_class.Archive
import ru.kondrashen.diplomappv20.repository.data_class.User

class LinearHorizontalArrayAdapter(archives: MutableList<Archive>,private var userId: Int, private var activity: Activity, private var viewModel: UserAccountControlViewModel, private var listener: onItemClickInterface<User>): RecyclerView.Adapter<LinearHorizontalArrayAdapter.TabItemViewHolder>() {
    private var _archives: MutableList<Archive> = mutableListOf()
    private var archiveStringArr = activity.resources.getStringArray(R.array.archive_start)
    private var lastClickPosition = -1
    private var clickPosition = -1
    init {
        for (i in archiveStringArr){
            var result =i.split(" ")
            _archives.add(Archive(0, result[0], result[1], userId))
        }
        this._archives += archives
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabItemViewHolder {
        val binding = TabListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false)
        return TabItemViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return _archives.size
    }

    override fun onBindViewHolder(holder: TabItemViewHolder, position: Int) {
        val item = _archives[position]
        holder.setTabItem(item)
        if(position == clickPosition){
            holder.itemView.setBackgroundResource(R.drawable.hidden_list_pressed)
            if (item.id == 0) {
                holder.itemView.findViewById<ImageButton>(R.id.delete_btn).visibility = View.GONE
            }
        }
        else {
            holder.itemView.setBackgroundResource(R.drawable.hidden_list)
            holder.itemView.findViewById<ImageButton>(R.id.delete_btn).visibility = View.GONE

        }
    }
    fun addItemToList(archive: Archive){
        this._archives.add(archive)
    }
    inner class TabItemViewHolder(private val binding: TabListItemBinding): RecyclerView.ViewHolder(binding.root), View.OnLongClickListener {
        private lateinit var archive: Archive

        fun setTabItem(arch: Archive){
            this.archive = arch
            binding.textView.text =arch.name
            binding.idView.text =arch.id.toString()
            binding.root.setOnClickListener {
                lastClickPosition = clickPosition
                clickPosition = adapterPosition
                binding.deleteBtn.visibility = View.VISIBLE
                notifyItemChanged(lastClickPosition)
                notifyItemChanged(clickPosition)
                listener.onTabClickEvent(clickPosition, lastClickPosition, arch.searchableWord)
            }
            binding.root.setOnLongClickListener(this)
            binding.deleteBtn.setOnClickListener {
                val manager =(activity as AppCompatActivity).supportFragmentManager
                val dialog = ConfirmDialogFragment()
                dialog.show(manager, "result")
                manager.setFragmentResultListener("status", activity as AppCompatActivity) { requestKey, bundle ->
                    val postedString = bundle.getString(requestKey).toString()
                    if (postedString == "success"){
                        viewModel.deleteArchiveRequest(binding.idView.text.toString().toInt(), userId).observe(activity as LifecycleOwner) {
                            if (it == "success") {
                                val removedItemIndex = adapterPosition
                                _archives.removeAt(removedItemIndex)
                                notifyItemRemoved(removedItemIndex)
                                val snackbar = listener.onChildAdapterClickEventMessage(adapterPosition, getString(activity, R.string.success))
                                snackbar.show()
                            }
                            else if(it =="No connection to server") {
                                val snackbar =
                                    listener.onChildAdapterClickEventMessage(adapterPosition, getString(activity, R.string.no_connection))
                                snackbar.show()
                            }
                        }

                    }
                }
                val animatorSet = UserAnimatorHelper().createPulsarScaleItemAnimation(binding.deleteBtn)
                animatorSet.addListener(object: AnimatorListenerAdapter(){
                    override fun onAnimationStart(animation: Animator) {
                        super.onAnimationStart(animation)
                        binding.deleteBtn.setBackgroundResource(R.drawable.delete_pressed_svg)
                    }
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        binding.deleteBtn.setBackgroundResource(R.drawable.delete_svg)
                    }
                })
                animatorSet.start()
            }
        }

        override fun onLongClick(v: View?): Boolean {
            clickPosition = adapterPosition
            if (archive.userId !=0) {
                val manager = (activity as AppCompatActivity).supportFragmentManager
                val dialog = EditArchiveDialogFragment()
                var bundle = Bundle()
                bundle.putInt("archId", archive.id)
                bundle.putString("itemType", "archive")
                dialog.arguments = bundle
                dialog.show(manager, "result")
                manager.setFragmentResultListener(
                    "name",
                    activity as LifecycleOwner
                ) { requestKey, bundle1 ->
                    val postedName = bundle1.getString(requestKey).toString()
                    viewModel.putArchive(userId, postedName, archive.id)
                        .observe(activity as LifecycleOwner) {
                            if (it.contains("success")) {
                                Snackbar.make(
                                    binding.root,
                                    getString(activity, R.string.success), Snackbar.LENGTH_LONG
                                ).show()
                                notifyItemChanged(adapterPosition)
                            } else if (it == "No connection to server")
                                Snackbar.make(
                                    binding.root,
                                    getString(activity, R.string.no_connection),
                                    Snackbar.LENGTH_LONG
                                ).show()
                            else
                                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                        }
                }
            }
            return true
        }
    }
}