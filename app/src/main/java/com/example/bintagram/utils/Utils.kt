package com.example.bintagram.utils

import android.app.ProgressDialog
import android.content.Context
import android.net.Uri
import android.webkit.ValueCallback
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

fun uploadImage(uri: Uri,folderName: String, callback:(String?)->Unit){
    var imageUrl:String?=null
FirebaseStorage.getInstance().getReference(folderName).child(UUID.randomUUID().toString())
    .putFile(uri)
    .addOnSuccessListener {
        it.storage.downloadUrl.addOnSuccessListener{
            imageUrl=it.toString()
            callback(imageUrl)
        }
    }

}
fun uploadVideo(uri: Uri,folderName: String,progressDialog: ProgressDialog,callback:(String?)->Unit){
    var imageUrl:String?=null
    progressDialog.setTitle("Uploading . . .")
    progressDialog.show()

    FirebaseStorage.getInstance().getReference(folderName).child(UUID.randomUUID().toString())
        .putFile(uri)
        .addOnSuccessListener {
            it.storage.downloadUrl.addOnSuccessListener{
                imageUrl=it.toString()
                progressDialog.dismiss()
                callback(imageUrl)
            }
        }.addOnProgressListener {
            val uploadValue:Long=(it.bytesTransferred/it.totalByteCount)*100
            progressDialog.setMessage("Uploaded $uploadValue %")
        }

}