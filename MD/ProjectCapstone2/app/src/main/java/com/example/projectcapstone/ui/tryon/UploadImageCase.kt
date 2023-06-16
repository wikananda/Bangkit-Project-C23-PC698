package com.example.projectcapstone.ui.tryon

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.example.projectcapstone.ResultState
import com.example.projectcapstone.response.TryonResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody

class UploadImageCase(private val uploadImageRepository: ImageRepository) {

    fun uploadImage(
        cloth_file : MultipartBody.Part,
        model_file : MultipartBody.Part
    ) : LiveData<ResultState<TryonResponse>> = liveData {
        emit(ResultState.Loading)
        try{
            val response = uploadImageRepository.uploadImage(cloth_file, model_file)
            val remoteResponse = MutableLiveData<ResultState<TryonResponse>>()
            //remoteResponse.value = ResultState.Success(response)
            //(remoteResponse)
        }
        catch (e : Exception){
            emit(ResultState.Error(e.message.toString()))
        }
    }
}
