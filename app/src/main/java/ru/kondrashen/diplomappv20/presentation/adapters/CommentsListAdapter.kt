package ru.kondrashen.diplomappv20.presentation.adapters

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getString
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import ru.kondrashen.diplomappv20.R
import ru.kondrashen.diplomappv20.databinding.ChatAnotherUserListItemBinding
import ru.kondrashen.diplomappv20.databinding.ChatCurentUserListItemBinding
import ru.kondrashen.diplomappv20.domain.MainPageViewModel
import ru.kondrashen.diplomappv20.presentation.baseClasses.UserAnimatorHelper
import ru.kondrashen.diplomappv20.presentation.baseClasses.onItemClickInterface
import ru.kondrashen.diplomappv20.presentation.fragments.ConfirmDialogFragment
import ru.kondrashen.diplomappv20.presentation.fragments.EditArchiveDialogFragment
import ru.kondrashen.diplomappv20.repository.data_class.Comment

class CommentsListAdapter(comments: MutableList<Comment>, private var userId: Int, private var activity: Activity, private var viewModel: MainPageViewModel, private var listener: onItemClickInterface<Comment>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var _comments: MutableList<Comment> = mutableListOf()
    private var lastClickPosition = -1
    private var clickPosition = -1
    init {
        this._comments = comments
    }
    override fun getItemViewType(position: Int): Int {
        if (_comments.get(position).userId == userId)
            return 1
        else{
            return 2
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 1){
            val binding = ChatCurentUserListItemBinding.inflate(LayoutInflater.from(parent.context),
                parent, false)
            return CurUserViewHolder(binding)
        }
        else {
            val binding = ChatAnotherUserListItemBinding.inflate(LayoutInflater.from(parent.context),
                parent, false)
            return AnotherUserViewHolder(binding)
        }
    }


    override fun getItemCount(): Int {
        return _comments.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = _comments[position]

        val holder1: CurUserViewHolder
        val holder2: AnotherUserViewHolder
//        TransitionManager.beginDelayedTransition(holder.itemView as ViewGroup)
        if(holder.itemViewType == 1){
            holder1 = holder as CurUserViewHolder
            holder1.setItem(item)
            if(position != clickPosition) {
                holder1.itemView.findViewById<LinearLayout>(R.id.buttonsView).visibility = View.GONE
            }
            else{
                holder1.itemView.findViewById<LinearLayout>(R.id.buttonsView).let {
                    val animation = AnimationUtils.loadAnimation(activity, R.anim.scale_in_button_group)
                    it.startAnimation(animation)
                }
            }

        }
        else {
            holder2 = holder as AnotherUserViewHolder
            holder2.setItem(item)
            if(position != clickPosition) {
                holder2.itemView.findViewById<ImageView>(R.id.delete_btn).visibility = View.GONE
            }
            else{
                holder2.itemView.findViewById<LinearLayout>(R.id.buttonsView).let {
                    val animation = AnimationUtils.loadAnimation(activity, R.anim.scale_in_button_group)
                    it.startAnimation(animation)
                }
            }
        }




    }
    fun addItemToList(comment: Comment){
        this._comments.add(comment)
    }
    fun replaceItemToList(comment: Comment, position: Int){
        this._comments.add(position,comment)
        notifyItemChanged(position)
    }
    inner class CurUserViewHolder(private val binding: ChatCurentUserListItemBinding): RecyclerView.ViewHolder(binding.root) {
        private lateinit var comment: Comment

        fun setItem(comment1: Comment) {
            this.comment = comment1
            binding.chatInfo.text = comment1.content
            binding.idView.text = comment1.id.toString()
            binding.chatTime.text = comment1.comment_date
            binding.root.setOnClickListener {
                lastClickPosition = clickPosition
                clickPosition = adapterPosition
                binding.buttonsView.visibility = View.VISIBLE
                notifyItemChanged(lastClickPosition)
                notifyItemChanged(clickPosition)
                listener.onTabClickEvent(clickPosition, lastClickPosition, comment1.content)
            }
            binding.updateBtn.setOnClickListener {
                val updateItemIndex = if (adapterPosition != -1)adapterPosition else 0
                val animatorSet = UserAnimatorHelper().createPulsarScaleItemAnimation(binding.updateBtn)
                animatorSet.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator) {
                        super.onAnimationStart(animation)
                        binding.updateBtn.setImageResource(R.drawable.edit_pressed_svg)
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        binding.updateBtn.setImageResource(R.drawable.edit_svg)
                    }
                })
                animatorSet.start()
                val manager = (activity as AppCompatActivity).supportFragmentManager
                val dialog = EditArchiveDialogFragment()
                var bundle = Bundle()
                bundle.putInt("commId", comment1.id)
                bundle.putString("content", comment1.content)
                bundle.putString("itemType", "comment")
                dialog.arguments = bundle
                dialog.show(manager, "result")
                manager.setFragmentResultListener(
                    "content",
                    activity as LifecycleOwner
                ) { requestKey, bundle1 ->
                    val postedContent = bundle1.getString(requestKey).toString()
                    comment1.content = postedContent
                    binding.chatInfo.text = postedContent
                    binding.progressBar.visibility = View.VISIBLE
                    viewModel.putComment(comment1.id, postedContent, userId).observe(activity as LifecycleOwner) {
                        when (it.status) {
                            "success" -> {
                                Snackbar.make(
                                    binding.root,
                                    getString(activity, R.string.success), Snackbar.LENGTH_LONG
                                ).show()
                                viewModel.putCommentToRoom(comment1.id, postedContent,it.commStatus).observe(activity as LifecycleOwner){str->
                                    when(str){
                                        "done" -> {
                                            (binding.root.parent as RecyclerView).scrollToPosition(updateItemIndex-1)
                                            binding.progressBar.visibility = GONE
                                        }
                                        else -> {
                                            (binding.root.parent as RecyclerView).scrollToPosition(
                                                updateItemIndex
                                            )

                                        }
                                    }
                                }
                            }
                            "No connection to server" -> {
                                Snackbar.make(
                                    binding.root,
                                    getString(activity, R.string.no_connection),
                                    Snackbar.LENGTH_LONG
                                ).show()
                                binding.progressBar.visibility = GONE
                                viewModel.putCommentToRoom(comment1.id, postedContent,it.commStatus).observe(activity as LifecycleOwner){str->
                                    when(str){
                                        "done" -> {(binding.root.parent as RecyclerView).scrollToPosition(updateItemIndex-1)
                                            Handler(Looper.getMainLooper()).post {
                                                (binding.root.parent as RecyclerView).scrollToPosition(
                                                    updateItemIndex
                                                )
                                            }
                                        }
                                        else ->
                                            Handler(Looper.getMainLooper()).post {
                                                (binding.root.parent as RecyclerView).scrollToPosition(
                                                    updateItemIndex
                                                )
                                            }
                                    }
                                }
                            }
                            else -> {
                                Snackbar.make(binding.root, it.status, Snackbar.LENGTH_LONG).show()
                                binding.progressBar.visibility = GONE
                            }
                        }
                    }
                    replaceItemToList(comment1, updateItemIndex)

                }

            }
            binding.deleteBtn.setOnClickListener {
                val manager = (activity as AppCompatActivity).supportFragmentManager
                val dialog = ConfirmDialogFragment()
                dialog.show(manager, "result")
                manager.setFragmentResultListener(
                    "status",
                    activity as AppCompatActivity
                ) { requestKey, bundle ->
                    val postedString = bundle.getString(requestKey).toString()
                    if (postedString == "success") {
                        val removedItemIndex = adapterPosition
                        viewModel.deleteCommentRequest(
                            binding.idView.text.toString().toInt(),
                            userId
                        ).observe(activity as LifecycleOwner) {
                            if (it == "success") {

                                _comments.removeAt(removedItemIndex)
                                notifyItemRemoved(removedItemIndex)
                                Handler(Looper.getMainLooper()).post {
                                    (binding.root.parent as RecyclerView).scrollToPosition(
                                        removedItemIndex - 1
                                    )
                                }
                                println(removedItemIndex.toString()+"проба")
                                val snackbar = listener.onChildAdapterClickEventMessage(
                                    adapterPosition,
                                    getString(activity, R.string.success)
                                )
                                snackbar.show()
                            } else if (it == "No connection to server") {
                                val snackbar =
                                    listener.onChildAdapterClickEventMessage(
                                        adapterPosition,
                                        getString(activity, R.string.no_connection)
                                    )
                                snackbar.show()
                            }
                        }

                    }
                }
                val animatorSet = UserAnimatorHelper().createPulsarScaleItemAnimation(binding.deleteBtn)
                animatorSet.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator) {
                        super.onAnimationStart(animation)
                        binding.deleteBtn.setImageResource(R.drawable.delete_pressed_svg)
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        binding.deleteBtn.setImageResource(R.drawable.delete_svg)
                    }
                })
                animatorSet.start()
            }
        }
    }
    inner class AnotherUserViewHolder(private val binding: ChatAnotherUserListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private lateinit var comment: Comment

        fun setItem(comment1: Comment) {
            this.comment = comment1
//                binding.textView.text =comment1.content
            binding.chatInfo.text = comment1.content
            binding.chatTime.text = comment1.comment_date
            binding.idView.text =comment1.id.toString()
            binding.root.setOnClickListener {
                lastClickPosition = clickPosition
                clickPosition = adapterPosition
                binding.deleteBtn.visibility = View.VISIBLE
                notifyItemChanged(lastClickPosition)
                notifyItemChanged(clickPosition)
                listener.onTabClickEvent(clickPosition, lastClickPosition, comment1.content)
            }
            binding.deleteBtn.setOnClickListener {
                val manager = (activity as AppCompatActivity).supportFragmentManager
                val dialog = ConfirmDialogFragment()
                dialog.show(manager, "result")
                manager.setFragmentResultListener(
                    "status",
                    activity as AppCompatActivity
                ) { requestKey, bundle ->
                    val postedString = bundle.getString(requestKey).toString()
                    if (postedString == "success") {
//                            viewModel.deleteArchiveRequest(
//                                binding.chatInfo.text.toString().toInt(),
//                                userId
//                            ).observe(activity as LifecycleOwner) {
//                                if (it == "success") {
//                                    val removedItemIndex = adapterPosition
//                                    _comments.removeAt(removedItemIndex)
//                                    notifyItemRemoved(removedItemIndex)
//                                    val snackbar = listener.onChildAdapterClickEventMessage(
//                                        adapterPosition,
//                                        getString(activity, R.string.success)
//                                    )
//                                    snackbar.show()
//                                } else if (it == "No connection to server") {
//                                    val snackbar =
//                                        listener.onChildAdapterClickEventMessage(
//                                            adapterPosition,
//                                            getString(activity, R.string.no_connection)
//                                        )
//                                    snackbar.show()
//                                }
//                            }

                    }
                }
                val animatorSet =
                    UserAnimatorHelper().createPulsarScaleItemAnimation(binding.deleteBtn)
                animatorSet.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator) {
                        super.onAnimationStart(animation)
                        binding.deleteBtn.setImageResource(R.drawable.delete_pressed_svg)
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        binding.deleteBtn.setImageResource(R.drawable.delete_svg)
                    }
                })
                animatorSet.start()
            }
        }
    }
}