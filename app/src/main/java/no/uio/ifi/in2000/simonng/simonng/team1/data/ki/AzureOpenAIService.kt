package no.uio.ifi.in2000.simonng.simonng.team1.data.ki


import android.util.Log

import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType

import okhttp3.OkHttpClient

import okhttp3.Request

import okhttp3.RequestBody.Companion.toRequestBody

import org.json.JSONArray

import org.json.JSONObject

import java.io.IOException

import java.util.concurrent.TimeUnit


class AzureOkHttpClient {

    companion object {

        private const val TAG = "AzureOkHttpClient"

        private const val ENDPOINT = "https://uio-mn-ifi-in2000-swe1.openai.azure.com"

        private const val DEPLOYMENT_ID = "gpt-35-turbo"

        private const val API_VERSION = "2023-07-01-preview" // Bruk en stabil API-versjon

        private const val API_KEY = "" //Legg nøkkel her

    }


    private val httpClient = OkHttpClient.Builder()

        .connectTimeout(45, TimeUnit.SECONDS)

        .readTimeout(45, TimeUnit.SECONDS)

        .writeTimeout(45, TimeUnit.SECONDS)

        .build()


    suspend fun askAssistant(prompt: String): String = withContext(Dispatchers.IO) {

        if (API_KEY.isBlank()) {

            Log.e(TAG, "API-nøkkel for Azure OpenAI er ikke satt i local.properties/BuildConfig!")

            return@withContext "Feil: API-nøkkel mangler."

        }


        try {

            val url = "$ENDPOINT/openai/deployments/$DEPLOYMENT_ID/chat/completions?api-version=$API_VERSION"


            // Bygger JSON-body

            val jsonBody = JSONObject().apply {

                put("temperature", 0.7)

                put("max_tokens", 150) // Tips trenger sjelden mer

                put("messages", JSONArray().put(

                    JSONObject().put("role", "user").put("content", prompt)

                ))
            }


            val mediaType = "application/json; charset=utf-8".toMediaType()

            val requestBody = jsonBody.toString().toRequestBody(mediaType)


            val request = Request.Builder()

                .url(url)

                .post(requestBody)

                .addHeader("api-key", API_KEY)

                .build()


            Log.d(TAG, "Sender forespørsel til Azure...")

            val response = httpClient.newCall(request).execute()


            if (!response.isSuccessful) {

                val errorBody = response.body?.string() ?: "Ukjent feil"

                Log.e(TAG, "Feil fra Azure API (${response.code}): $errorBody")

                return@withContext "Feil fra AI (${response.code})" // Gi litt info tilbake

            }


            val responseBody = response.body?.string()

            if (responseBody.isNullOrBlank()) {

                Log.e(TAG, "Tomt svar fra Azure API")

                return@withContext "Feil: Tomt svar fra AI"

            }


            // Tryggere JSON-parsing

            val responseJson = JSONObject(responseBody)

            val choices = responseJson.optJSONArray("choices")

            if (choices == null || choices.length() == 0) {

                Log.e(TAG, "Ingen 'choices' i svaret: $responseBody")

                return@withContext "Feil: Uventet format fra AI"

            }

            val message = choices.optJSONObject(0)?.optJSONObject("message")

            val reply = message?.optString("content")


            if (reply.isNullOrBlank()) {

                Log.e(TAG, "Fant ikke gyldig 'content' i svaret: $responseBody")

                return@withContext "Feil: Uventet innhold fra AI"

            }


            Log.d(TAG, "Svar mottatt fra Azure AI.")

            return@withContext reply.trim()

        } catch (e: IOException) {
            Log.e(TAG, "Nettverksfeil under Azure-kall: ${e.message}", e)
            return@withContext "Feil: Nettverksproblem mot AI"
        } catch (e: Exception) { // Fanger andre exceptions
            Log.e(TAG, "Generell feil under Azure-kall: ${e.message}", e)
            return@withContext "Feil: Kunne ikke kontakte AI (${e.javaClass.simpleName})"

        }

    }

}