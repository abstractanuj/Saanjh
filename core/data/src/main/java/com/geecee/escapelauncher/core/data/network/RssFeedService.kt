package com.geecee.escapelauncher.core.data.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface RssFeedService {
    @GET
    suspend fun fetchFeed(@Url url: String): Response<ResponseBody>
}
