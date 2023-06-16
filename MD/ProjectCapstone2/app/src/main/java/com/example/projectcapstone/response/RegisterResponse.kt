package com.example.projectcapstone.response

import com.google.gson.annotations.SerializedName

data class RegisterResponse(
    @field:SerializedName("data")
    val error: String,

    @field:SerializedName("message")
    val message: String
)
