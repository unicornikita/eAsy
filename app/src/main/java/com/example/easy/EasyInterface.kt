package com.example.easy

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


interface EasyInterface {
    @POST("setClass")
    fun setClass(@Body razred: String?): Call<String?>?
    @GET("allClasses")
    fun allClasses(): Call<List<String>>
    @GET("danes")
    fun danes(): Call<List<vsebina>>
}