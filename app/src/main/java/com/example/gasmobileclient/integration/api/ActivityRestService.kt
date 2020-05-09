package com.example.gasmobileclient.integration.api

import com.example.gasmobileclient.integration.dto.ActivityDto
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path


interface ActivityRestService {
    @POST("/rest/open/activity/google-id/{id}")
    fun save(@Path("id") googleId: String, @Body dto: ActivityDto) : Call<Void>
}