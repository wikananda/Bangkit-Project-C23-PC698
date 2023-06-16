package com.example.projectcapstone.networking

import com.example.projectcapstone.response.LoginResponse
import com.example.projectcapstone.response.RegisterResponse
import com.example.projectcapstone.response.TryonResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    //masih belum bisa diakses karena API masih bermasalah
    @FormUrlEncoded
    @POST("api/signup")
    suspend fun register(
        @Field("name") name : String,
        @Field("email") email : String,
        @Field("password") password: String,
        @Field("confirmPassword") confirmPassword: String
    ) : Call<RegisterResponse>

    //masih belum bisa diakses karena API masih bermasalah
    @FormUrlEncoded
    @POST("api/signin")
    suspend fun login(
        @Field("email") email : String,
        @Field("password") password: String
    ) : Call<LoginResponse>

    //masih terdapat kendala saat implementasi model dibawah dan diakrenakan baru mendapatkan model h-2 pengumpulan jadi model belum bisa diakses secara maksimal
    @FormUrlEncoded
    @POST("https://flask-model-deployment-4mdnfdw5fa-et.a.run.app")
     fun generateImage(
        @Part cloth_file: MultipartBody.Part,
        @Part model_file: MultipartBody.Part
    ) : Call<TryonResponse>

}