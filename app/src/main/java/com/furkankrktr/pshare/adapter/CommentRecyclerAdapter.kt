@file:Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.furkankrktr.pshare.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.furkankrktr.pshare.GorselActivity
import com.furkankrktr.pshare.R
import com.furkankrktr.pshare.RepliesActivity
import com.furkankrktr.pshare.databinding.RecyclerCommentBinding
import com.furkankrktr.pshare.model.Comment
import com.furkankrktr.pshare.service.glide
import com.furkankrktr.pshare.service.glider
import com.furkankrktr.pshare.service.placeHolderYap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CommentRecyclerAdapter(private val commentList: ArrayList<Comment>) :
    RecyclerView.Adapter<CommentRecyclerAdapter.CommentHolder>() {

    private lateinit var database: FirebaseFirestore
    lateinit var auth: FirebaseAuth
    private lateinit var guncelKullanici: String

    class CommentHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = RecyclerCommentBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.recycler_comment, parent, false)
        return CommentHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CommentHolder, position: Int) {

        with(holder) {
            database = FirebaseFirestore.getInstance()
            auth = FirebaseAuth.getInstance()
            if (auth.currentUser != null) {
                guncelKullanici = auth.currentUser!!.email.toString()
            }
            if (commentList[position].commentAttach == "") {
                binding.commentImageView.visibility = View.GONE
            } else {
                binding.commentImageView.visibility = View.VISIBLE
                binding.commentImageView.glide(
                    commentList[position].commentAttach,
                    placeHolderYap(holder.itemView.context)
                )
            }
            database.collection("Users")
                .whereEqualTo("useremail", commentList[position].kullaniciEmail)
                .addSnapshotListener { snapshot, exception ->
                    when {
                        exception != null -> {

                        }
                        else -> {
                            if (snapshot != null) {
                                if (!snapshot.isEmpty) {
                                    val documents = snapshot.documents
                                    for (document in documents) {
                                        binding.commentEmail.text =
                                            document.get("username") as String
                                        val profile = document.get("profileImage") as String
                                        binding.profileImageComment.glider(
                                            profile,
                                            placeHolderYap(holder.itemView.context)
                                        )
                                    }
                                } else {
                                    binding.commentEmail.text =
                                        commentList[position].kullaniciEmail
                                }
                            } else {
                                binding.commentEmail.text =
                                    commentList[position].kullaniciEmail
                            }
                        }
                    }
                }
            binding.comment.text = commentList[position].kullaniciComment

            if (commentList[position].kullaniciEmail == guncelKullanici) {
                TransitionManager.beginDelayedTransition(binding.commentContainer)

                binding.deleteYorumButton.visibility = View.VISIBLE
            } else {
                TransitionManager.beginDelayedTransition(binding.commentContainer)

                binding.deleteYorumButton.visibility = View.GONE
            }

            binding.commentImageView.setOnClickListener {
                val intent = Intent(holder.itemView.context, GorselActivity::class.java)
                intent.putExtra("resim", commentList[position].commentAttach)
                holder.itemView.context.startActivity(intent)
            }

            binding.deleteYorumButton.setOnClickListener {

                val alert = AlertDialog.Builder(holder.itemView.context)

                alert.setTitle("Yorum Sil")
                alert.setMessage("Yorumu Silmek İstediğinize Emin misiniz?")
                alert.setNegativeButton(
                    "İptal Et"
                ) { _, _ ->
                    Toast.makeText(
                        holder.itemView.context,
                        "İşlem iptal edildi",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                alert.setPositiveButton(
                    "SİL"
                ) { _, _ ->

                    val yorumsRef = database.collection("Yorumlar")
                    val queryYorum =
                        yorumsRef.whereEqualTo("commentId", commentList[position].commentId)
                    queryYorum.get().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            for (document in task.result) {
                                yorumsRef.document(document.id).delete()
                                Toast.makeText(
                                    holder.itemView.context,
                                    "Yorum Silindi",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            Toast.makeText(holder.itemView.context, "Hata", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }

                }.show()


            }
            binding.replyYorumButton.setOnClickListener {
                replyGit(holder, position)
            }
            binding.replyCount.setOnClickListener {
                replyGit(holder, position)
            }


            database.collection("Yanıtlar")
                .whereEqualTo("selectedComment", commentList[position].commentId)
                .addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        println(exception.localizedMessage)
                    } else {
                        if (snapshot != null) {
                            if (!snapshot.isEmpty) {
                                val documents = snapshot.documents
                                binding.replyCount.text = "${documents.size} Yanıt"

                            } else {
                                binding.replyCount.text = "0 Yanıt"
                            }
                        }


                    }

                }
        }
    }

    private fun replyGit(holder: CommentHolder, position: Int) {
        val intent = Intent(holder.itemView.context, RepliesActivity::class.java)
        intent.putExtra("selectedComment", commentList[position].commentId)
        intent.putExtra("selectedCommentEmail", commentList[position].kullaniciEmail)
        intent.putExtra("selectedCommentText", commentList[position].kullaniciComment)
        intent.putExtra("selectedCommentImage", commentList[position].commentAttach)
        intent.putExtra("selectedCommentUID", commentList[position].kullaniciUID)
        holder.itemView.context.startActivity(intent)
    }

    override fun getItemCount(): Int {
        return commentList.size
    }
}