package com.project17.tourbooking.helper.firebase_cloud_helper

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.project17.tourbooking.constant.DEFAULT_AVATAR
import kotlinx.coroutines.tasks.await

object FirebaseCloudHelper {
    private val storage = FirebaseStorage.getInstance()

    suspend fun deleteImageFromUrl(imageUrl: String): Boolean {
        Log.d("DeleteImage", "Attempting to delete image from URL: $imageUrl")
        val storageReference: StorageReference? = storage.getReferenceFromUrl(imageUrl)

        return try {
            if (storageReference != null) {
                Log.d("DeleteImage", "Found storage reference, proceeding to delete.")
                storageReference.delete().await()
                Log.d("DeleteImage", "Image deleted successfully.")
                true
            } else {
                Log.e("DeleteImage", "Storage reference is null. Cannot delete image.")
                false
            }
        } catch (e: Exception) {
            Log.e("DeleteImage", "Error deleting image: ${e.message}", e)
            e.printStackTrace()
            false
        }
    }

    suspend fun uploadImage(imageUri: String, pathString: String): String? {
        val storageRef: StorageReference = storage.reference
        val imageRef = storageRef.child("$pathString/${System.currentTimeMillis()}.jpg")

        return try {
            imageRef.putFile(Uri.parse(imageUri)).await()
            imageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun updateImage(oldImageUrl: String, newImageUri: String, pathString: String): String {
        val storageRef: StorageReference = storage.reference
        val newImageRef = storageRef.child("$pathString/${System.currentTimeMillis()}.jpg")

        return try{
            if(oldImageUrl != newImageUri && newImageUri.isNotEmpty()){
                if(oldImageUrl.isNotEmpty()  && oldImageUrl != DEFAULT_AVATAR){
                    deleteImageFromUrl(oldImageUrl)
                }
                newImageRef.putFile(Uri.parse(newImageUri)).await()
                val url = newImageRef.downloadUrl.await().toString()
                url
            }
            else oldImageUrl

        }
        catch (e: Exception){
            e.printStackTrace()
            ""
        }
    }
}