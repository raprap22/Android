package com.example.fooedtra

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

data class PredictResponse(

    @field:SerializedName("linkArtikel")
    val linkArtikel: String,

    @field:SerializedName("restaurants")
    val restaurants: List<RestaurantsItem>,

    @field:SerializedName("deskripsi")
    val deskripsi: String,

    @field:SerializedName("asalProvinsi")
    val asalProvinsi: String,

    @field:SerializedName("keyword")
    val keyword: String,

    @field:SerializedName("namaMakanan")
    val namaMakanan: String
)

data class RestaurantsItem(

    @field:SerializedName("distance")
    val distance: Double? = null,

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("geometry")
    val geometry: Geometry? = null
)

data class Location(

    @field:SerializedName("lng")
    val lng: Double? = null,

    @field:SerializedName("lat")
    val lat: Double? = null
)

data class Geometry(

    @field:SerializedName("location")
    val location: Location? = null
)


interface ApiService {
    @Multipart
    @POST("predict")
    fun predictPost(
        @Header("Authorization") token: String ,
        @Part ("image") image: String,
        @Part("lat") lat: Double,
        @Part("lng") lng: Double
    ): Call<PredictResponse>
}

class ApiConfig {
    fun getApiService(): ApiService {
        val loggingInterceptor =
            HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://backend-foedtra-feeetnd5kq-et.a.run.app/")
//            .baseUrl("https://foedtra-backend-test.herokuapp.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
        return retrofit.create(ApiService::class.java)
    }
}