package com.example.familytrack

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.textview_to_row
import java.sql.Timestamp

class ChatLogActivity : AppCompatActivity() {

    companion object {
        val TAG = "ChatLog"
    }

    val adapter = GroupAdapter<ViewHolder>()

    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

    //to add value to recyclerview
        recycle_view_chat.adapter = adapter

       // supportActionBar?.title = "Chat Log"

        //put user`s name into action bar from firebase
      //  val username = intent.getStringExtra(NewMessageActivity.User_KEY)
        toUser = intent.getParcelableExtra<User>(NewMessageActivity.User_KEY)
        supportActionBar?.title = toUser?.username

    listenForMessages()


        send_button_chat.setOnClickListener{
            Log.d(TAG, "Attempt to send a message")
            performSendMessage()
        }
    }

    //show the messages from firebase to the recyclerview

    private fun listenForMessages(){
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        ref.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(p0:  DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)
                if (chatMessage != null) { Log.d(TAG, chatMessage.text)

                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser = LatestMessagesActivity.currentUser ?: return
                        adapter.add(ChatToItem(chatMessage.text, currentUser))
                    }  else {
                        adapter.add(ChatFromItem(chatMessage.text, toUser!!))
                    }
                }

                recycle_view_chat.scrollToPosition(adapter.itemCount -1)

            }

            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildChanged(p0: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildMoved(p0: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }

        })

    }


    // send message to firebase
    private fun performSendMessage(){
        val text = edittext_chat.text.toString()

        val fromID = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.User_KEY)
        val toID = user?.uid

        if (fromID == null) return

        // val reference = FirebaseDatabase.getInstance().getReference("/messages").push()
        // recognise which user sends message to who in firebase
        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromID/$toID").push()

        val toReference = FirebaseDatabase.getInstance().getReference("/user-messages/$toID/$fromID").push()

        val chatMessage = ChatMessage(reference.key!!, text, fromID, toID!!, System.currentTimeMillis() / 1000)

        reference.setValue(chatMessage)
                .addOnSuccessListener {
                    Log.d(TAG, "Chat message was successfully saved on Firebase. The ID is ${reference.key}")
                    edittext_chat.text.clear()
                    recycle_view_chat.scrollToPosition(adapter.itemCount - 1)
                }
        toReference.setValue(chatMessage)


        //Bring latest messages for each user to the main messaging activity
        val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromID/$toID")
        latestMessageRef.setValue(chatMessage)

        val latestMessageToRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toID/$fromID")
        latestMessageToRef.setValue(chatMessage)
    }
}

//picasso
//rows of chat messages, from row
class ChatFromItem(val text: String, val user: User): Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textview_from_row.text = text

        //load user image to the chat log activity on left side
        val uri = user.profileImageUrl
        val targetImageView = viewHolder.itemView.imageview_chat_from
        Picasso.get().load(uri).into(targetImageView)
    }
    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}

//rows of chat messages, to
class ChatToItem(val text: String, val user: User): Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textview_to_row.text = text

        //load user image to the chat log activity on right side
        val uri = user.profileImageUrl
        val targetImageView = viewHolder.itemView.imageview_chat_to
        Picasso.get().load(uri).into(targetImageView)

    }


    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}
