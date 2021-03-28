@file:Suppress(
    "DEPRECATION",
    "UNCHECKED_CAST",
    "RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS"
)

package com.furkankrktr.pshare

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.furkankrktr.pshare.adapter.TakipEdilenlerRecyclerAdapter
import com.furkankrktr.pshare.databinding.ActivityTakipcilerBinding
import com.furkankrktr.pshare.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class TakipcilerActivity : AppCompatActivity() {
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var recyclerUserView: RecyclerView
    private lateinit var recyclerUserViewAdapter: TakipEdilenlerRecyclerAdapter
    private lateinit var guncelKullanici: String
    private var fullList = ArrayList<String>()

    private var userList = ArrayList<User>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityTakipcilerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()
        recyclerUserView = binding.takipcilerRecyclerView

        if (auth.currentUser != null) {
            guncelKullanici = auth.currentUser!!.email.toString()
        }

        supportActionBar?.title = "Takipçiler"
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val layoutManager = LinearLayoutManager(this)
        recyclerUserView.layoutManager = layoutManager
        recyclerUserViewAdapter = TakipEdilenlerRecyclerAdapter(userList)
        recyclerUserView.adapter = recyclerUserViewAdapter

        database.collection("Users").whereEqualTo("useremail", guncelKullanici)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Toast.makeText(this, exception.localizedMessage, Toast.LENGTH_SHORT).show()
                } else {
                    if (snapshot != null) {
                        if (!snapshot.isEmpty) {
                            userList.clear()
                            fullList.clear()
                            val documents1 = snapshot.documents
                            for (document1 in documents1) {
                                fullList = document1.get("takipciler") as ArrayList<String>
                                fullList.remove(guncelKullanici)
                                for (user in fullList) {
                                    database.collection("Users").whereEqualTo("useremail", user)
                                        .addSnapshotListener { snapshot1, exception1 ->
                                            if (exception1 != null) {
                                                Toast.makeText(
                                                    this,
                                                    exception1.localizedMessage,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                if (snapshot1 != null) {
                                                    if (!snapshot1.isEmpty) {
                                                        val documents = snapshot1.documents
                                                        for (document in documents) {

                                                            val kullaniciEmail =
                                                                document.get("useremail") as String
                                                            val gorselUrl =
                                                                document.get("profileImage") as String
                                                            val userId =
                                                                document.get("userId") as String
                                                            val indirilenUser =
                                                                User(
                                                                    kullaniciEmail,
                                                                    gorselUrl,
                                                                    userId
                                                                )
                                                            userList.add(indirilenUser)


                                                        }
                                                        recyclerUserViewAdapter.notifyDataSetChanged()

                                                    }
                                                }
                                            }
                                        }
                                }

                            }
                        }
                    }
                }
            }

    }
}