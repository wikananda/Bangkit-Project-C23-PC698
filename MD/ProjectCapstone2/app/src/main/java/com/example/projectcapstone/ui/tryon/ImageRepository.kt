package com.example.projectcapstone.ui.tryon

import com.example.projectcapstone.networking.ApiService
import com.example.projectcapstone.response.TryonResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call

class ImageRepository(private val apiService: ApiService) {

    suspend fun uploadImage(
        cloth_file: MultipartBody.Part,
        model_file: MultipartBody.Part
    ): Call<TryonResponse> {
        return withContext(Dispatchers.IO) {
            apiService.generateImage(cloth_file, model_file)
        }
    }
}
