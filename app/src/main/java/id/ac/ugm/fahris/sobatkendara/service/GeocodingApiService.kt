package id.ac.ugm.fahris.sobatkendara.service

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingApiService {
    @GET("geocode/json")
    suspend fun getAddress(
        @Query("latlng") latlng: String,
        @Query("key") apiKey: String
    ): GeocodingResponse

    companion object {
        private const val BASE_URL = "https://maps.googleapis.com/maps/api/"

        fun create(): GeocodingApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GeocodingApiService::class.java)
        }
    }
}

// Response Data Classes
data class GeocodingResponse(
    val results: List<Result>,
    val status: String
)

data class Result(
    val formatted_address: String
)