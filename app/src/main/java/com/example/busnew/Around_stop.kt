package com.example.busnew

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.google.gson.JsonArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.GzipSource
import okio.buffer
import java.io.IOException
import java.util.concurrent.TimeUnit

object Around_stop {
    suspend fun main(): String {
        val tokenUrl = "https://tdx.transportdata.tw/auth/realms/TDXConnect/protocol/openid-connect/token"
        val tdxUrl = "https://tdx.transportdata.tw/api/basic/v2/Bus/Station/City/Taoyuan?%24filter=StationPosition%2FPositionLat%20eq%2025.0139&%24top=30&%24format=JSON"
        val clientId = "11026349-b9820ce1-cd51-4721" //your clientId
        val clientSecret = "c02bf37f-9945-4fcd-bb6d-8a4a2769716c" //your clientSecret

        val objectMapper = ObjectMapper()
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val tokenInfo = withContext(Dispatchers.IO) { getAccessToken(tokenUrl, clientId, clientSecret) }
        val tokenElem: JsonNode = objectMapper.readTree(tokenInfo)
        val accessToken: String = tokenElem.get("access_token").asText()
        return withContext(Dispatchers.IO) { getStopString(tdxUrl, accessToken) }
    }

    @Throws(IOException::class)
    private fun getAccessToken(tokenUrl: String, clientId: String, clientSecret: String): String {
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val formBody = FormBody.Builder()
            .add("grant_type", "client_credentials")
            .add("client_id", clientId)
            .add("client_secret", clientSecret)
            .build()

        val request = Request.Builder()
            .url(tokenUrl)
            .post(formBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            return response.body?.string() ?: throw IOException("Empty response body")
        }
    }

    @Throws(IOException::class)
    private fun getStopString(url: String, accessToken: String): String {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("Accept-Encoding", "gzip")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            val responseBody = response.body ?: throw IOException("Empty response body")

            val jsonString = if ("gzip".equals(response.header("Content-Encoding"), ignoreCase = true)) {
                // Decompress gzip data
                responseBody.source().use { source ->
                    GzipSource(source).buffer().use { gzipBuffer ->
                        gzipBuffer.readUtf8()
                    }
                }
            } else {
                responseBody.string()
            }

            // Parse JSON and extract "Zh_tw" fields
            val gson = Gson()
            val jsonArray = gson.fromJson(jsonString, JsonArray::class.java)
            val zhTwSet = mutableSetOf<String>()
            val zhTwList = mutableListOf<String>()

            for (jsonElement in jsonArray) {
                val jsonObject = jsonElement.asJsonObject
                val stationNameZhTw = jsonObject.getAsJsonObject("StationName").get("Zh_tw").asString
                // We track stations in a separate set to ensure they are unique
                if (zhTwSet.add("Station: $stationNameZhTw")) {
                    zhTwList.add("Station: $stationNameZhTw")
                }

                val stopsArray = jsonObject.getAsJsonArray("Stops")
                for (stopElement in stopsArray) {
                    val stopObject = stopElement.asJsonObject
                    val stopNameZhTw = stopObject.getAsJsonObject("StopName").get("Zh_tw").asString
                    // Add stop names to the list if they are not duplicates
                    if (zhTwSet.add("Stop: $stopNameZhTw")) {
                        zhTwList.add("Stop: $stopNameZhTw")
                    }

                    val routeNameZhTw = stopObject.getAsJsonObject("RouteName").get("Zh_tw").asString
                    // Add route names to the list if they are not duplicates
                    if (zhTwSet.add("Route: $routeNameZhTw")) {
                        zhTwList.add("Route: $routeNameZhTw")
                    }
                }
            }

            // Filter out station information from the display list
            val filteredZhTwList = zhTwList.filterNot { it.startsWith("Station:") }

            return filteredZhTwList.joinToString(separator = "\n\n")
        }
    }

}
