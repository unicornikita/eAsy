package com.example.easy

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


interface EasyInterface {
    @GET("/allClasses")
    fun allClasses(): Call<List<String>>
    @GET("/danes/{razred}")
    fun danes(@Path("razred") string: String): Call<List<vsebina>>
    @GET("/izbranDan/{razred}/{dan}")
    fun izbranDan(@Path("razred") razred: String, @Path("dan") dan: Int): Call<List<vsebina>>
}