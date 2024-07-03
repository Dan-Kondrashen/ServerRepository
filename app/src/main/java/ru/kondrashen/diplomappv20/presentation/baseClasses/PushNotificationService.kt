package ru.kondrashen.diplomappv20.presentation.baseClasses

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import ru.kondrashen.diplomappv20.repository.data_class.Comment
import ru.kondrashen.diplomappv20.repository.db.WorkSearcherDB
import ru.kondrashen.diplomappv20.repository.repositories.CommentsRepository
import java.lang.NullPointerException

class PushNotificationService: FirebaseMessagingService() {
    val pref =  if (application != null )application.getSharedPreferences("AuthPref", Context.MODE_PRIVATE) else null
    val editor = pref?.edit()
    private val token = pref?.getString("token", null)

    companion object{
        var TAG="PUSH"
    }
    private val commDAO by lazy {
        WorkSearcherDB.getDatabase(applicationContext).commentDao()
    }

    private val userDAO by lazy {
        WorkSearcherDB.getDatabase(applicationContext).authDao()
    }

    private val commRep by lazy {
        CommentsRepository(commDAO, userDAO)
    }




    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i(TAG, token)
        pref?.let {
            Log.d(TAG, "Зашло в цикл")
            var userId = it.getString("userId", null)
            Log.d(TAG, "Зашло в цикл с пользователем $userId")
            if (userId != null)
                commRep.saveFBToken(userId.toInt(),token,userId.toInt())
        }

    }
//    fun generateNotification(title: String, content: String){
//        val intent  = Intent(this, AuthActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//
//        val pendingIntent = PendingIntent.getActivities(this, 0, intent,
//            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)
//
//    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.i(TAG, "From: ${remoteMessage.getNotification()?.getBody()}")

        // Check if message contains a data payload.
        if(remoteMessage.data["messType"] == "newComm"){
            saveMess(remoteMessage)
        }

        Log.i(TAG, "Message data payload: ${remoteMessage.data}")

            // Check if data needs to be processed by long running job


        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.i(TAG, "Message Notification Body: ${it.body}")
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    private fun saveMess(remoteMessage: RemoteMessage) {

        remoteMessage.data.let {
            try {
                val comm = Comment(
                    id = it["id"]!!.toInt(),
                    comment_date = it["comment_date"].toString(),
                    content = it["content"].toString(),
                    status = it["status"].toString(),
                    userId = it["userId"]!!.toInt(),
                    respId = it["respId"]!!.toInt()

                )

                commRep.postCommentToRoom(comm)
                Log.i(TAG, "add comm success ${comm.id}")
            }
            catch (e: NullPointerException){
                e.printStackTrace()
            }
        }
    }
}