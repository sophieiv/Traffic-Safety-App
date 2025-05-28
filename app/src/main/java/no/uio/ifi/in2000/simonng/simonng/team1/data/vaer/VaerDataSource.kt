package no.uio.ifi.in2000.simonng.simonng.team1.data.vaer

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class VaerDataSource {

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .header("User-Agent", "MinVaerApp")
                .build()
            chain.proceed(request)
        }
        .build()

    private val baseUrl = "https://in2000.api.met.no/weatherapi/locationforecast/2.0"

    suspend fun hentVaerData(breddegrad: Double, lengdegrad: Double, hoydeOverHavet: Int? = null): VaerData? {
        return withContext(Dispatchers.IO) {
            try {
                val urlBuilder = StringBuilder("$baseUrl/complete?lat=$breddegrad&lon=$lengdegrad")
                if (hoydeOverHavet != null) {
                    urlBuilder.append("&altitude=$hoydeOverHavet")
                }

                val request = Request.Builder()
                    .url(urlBuilder.toString())
                    .build()

                Log.d("VaerDataSource", "Henter vaerdata fra: $urlBuilder")

                try {
                    val response = client.newCall(request).execute()

                    if (!response.isSuccessful) {
                        Log.e("VaerDataSource", "API-feil: ${response.code}")
                        return@withContext null
                    }

                    val jsonString = response.body?.string()
                    if (jsonString.isNullOrEmpty()) {
                        Log.e("VaerDataSource", "Tomt svar fra API")
                        return@withContext null
                    }

                    parseVaerData(jsonString)

                } catch (e: IOException) {
                    Log.e("VaerDataSource", "Nettverksfeil: ${e.message}", e)

                    try {
                        val testConnection = java.net.URL("https://google.com").openConnection() as java.net.HttpURLConnection
                        testConnection.connectTimeout = 1500
                        testConnection.connect()
                        testConnection.disconnect()
                        Log.d("VaerDataSource", "Internett tilgjengelig men API svarer ikke")
                    } catch (e2: Exception) {
                        Log.e("VaerDataSource", "Ingen internettilkobling: ${e2.message}")
                    }

                    return@withContext null
                }
            } catch (e: Exception) {
                Log.e("VaerDataSource", "Annen feil: ${e.message}", e)
                return@withContext null
            }
        }
    }

    private fun parseVaerData(jsonString: String): VaerData? {
        try {
            val json = JSONObject(jsonString)
            val properties = json.getJSONObject("properties")
            val timeseries = properties.getJSONArray("timeseries")

            if (timeseries.length() == 0) {
                Log.e("VaerDataSource", "Ingen tidsseriedata funnet")
                return null
            }

            val currentTimeData = timeseries.getJSONObject(0)
            val time = currentTimeData.getString("time")
            val data = currentTimeData.getJSONObject("data")

            val instant = data.getJSONObject("instant").getJSONObject("details")
            val currentTemperature = instant.optDouble("air_temperature", 0.0).toFloat()
            Log.d("VaerDataSource", "Nåværende temperatur: $currentTemperature")

            var nedborNesteTime: Float? = null
            var symbolKodeNesteTime: String? = null

            if (data.has("next_1_hours")) {
                val next1h = data.getJSONObject("next_1_hours")
                if (next1h.has("summary")) {
                    symbolKodeNesteTime = next1h.getJSONObject("summary").optString("symbol_code")
                }
                if (next1h.has("details")) {
                    nedborNesteTime =
                        next1h.getJSONObject("details").optDouble("precipitation_amount", 0.0)
                            .toFloat()
                }
            }

            val totalNedbor = kalkuler24timersNedbor(timeseries)
            val minTemperatur = kalkulerMinTemperatur(timeseries, currentTemperature)

            val extendedForecast = tiDagersVarsel(timeseries)

            return VaerData(
                time = time,
                currentTemperature = currentTemperature,
                minTemperature = minTemperatur,
                precipitation1h = nedborNesteTime ?: 0f,
                totalPrecipitation24h = totalNedbor,
                symbolCode1h = symbolKodeNesteTime,
                extendedForecast = extendedForecast
            )
        } catch (e: Exception) {
            Log.e("VaerDataSource", "JSON parsing-feil", e)
            e.printStackTrace()
            return null
        }
    }

    private fun kalkulerMinTemperatur(timeseries: JSONArray, currentTemperature: Float): Float {
        var minTemperatur = currentTemperature

        for (i in 0 until minOf(24, timeseries.length())) {
            try {
                val timeData = timeseries.getJSONObject(i)
                val timeDataObj = timeData.getJSONObject("data")

                val timeInstant = timeDataObj.getJSONObject("instant").getJSONObject("details")
                val temp = timeInstant.optDouble("air_temperature", Double.MAX_VALUE).toFloat()

                if (temp < minTemperatur && temp != Float.MAX_VALUE) {
                    minTemperatur = temp
                }
            } catch (e: Exception) {
                Log.w("VaerDataSource", "Feil ved parsing av temperatur for tidsperiode $i", e)
            }
        }
        return minTemperatur
    }

    private fun kalkuler24timersNedbor(timeseries: JSONArray): Float {
        var totalNedbor = 0.0f

        for (i in 0 until minOf(24, timeseries.length())) {
            try {
                val timeData = timeseries.getJSONObject(i)
                val timeDataObj = timeData.getJSONObject("data")

                if (timeDataObj.has("next_1_hours")) {
                    val next1h = timeDataObj.getJSONObject("next_1_hours")
                    if (next1h.has("details")) {
                        val nedborTime = next1h.getJSONObject("details")
                            .optDouble("precipitation_amount", 0.0).toFloat()
                        totalNedbor += nedborTime
                    }
                } else if (timeDataObj.has("next_6_hours")) {
                    val sixHourData = timeDataObj.getJSONObject("next_6_hours")
                    if (sixHourData.has("details")) {
                        val nedborSeksTimer = sixHourData.getJSONObject("details")
                            .optDouble("precipitation_amount", 0.0).toFloat()
                        totalNedbor += nedborSeksTimer / 6.0f
                    }
                }
            } catch (e: Exception) {
                Log.w("VaerDataSource", "Feil ved parsing av nedbør for tidsperiode $i", e)
            }
        }

        Log.d("VaerDataSource", "Total nedbør for 24 timer: $totalNedbor")
        return totalNedbor
    }

    private fun tiDagersVarsel(timeseries: JSONArray): List<DailyForecast> {
        val dailyForecasts = mutableMapOf<String, MutableList<JSONObject>>()
        val result = mutableListOf<DailyForecast>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val outputDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayNameFormat = SimpleDateFormat("EEEE", Locale("no", "NO"))

        dateFormat.timeZone = TimeZone.getTimeZone("UTC")

        try {
            for (i in 0 until timeseries.length()) {
                val timeData = timeseries.getJSONObject(i)
                val timeString = timeData.getString("time")
                val date = dateFormat.parse(timeString)

                if (date != null) {
                    val dateKey = outputDateFormat.format(date)
                    if (!dailyForecasts.containsKey(dateKey)) {
                        dailyForecasts[dateKey] = mutableListOf()
                    }
                    dailyForecasts[dateKey]?.add(timeData)
                }
            }

            val sortedDates = dailyForecasts.keys.sorted()
            val daysToProcess = sortedDates.take(10)

            for (dateKey in daysToProcess) {
                val timeDataList = dailyForecasts[dateKey] ?: continue
                if (timeDataList.isEmpty()) continue

                var totalTemp = 0f
                var minTemp = Float.MAX_VALUE
                var maxTemp = Float.MIN_VALUE
                var totalPrecipitation = 0f
                val symbolCounts = mutableMapOf<String, Int>()

                for (timeData in timeDataList) {
                    val data = timeData.getJSONObject("data")
                    val instant = data.getJSONObject("instant").getJSONObject("details")

                    val temp = instant.optDouble("air_temperature", 0.0).toFloat()
                    totalTemp += temp
                    minTemp = minOf(minTemp, temp)
                    maxTemp = maxOf(maxTemp, temp)

                    var precipitation: Float
                    if (data.has("next_1_hours")) {
                        val next1h = data.getJSONObject("next_1_hours")
                        if (next1h.has("details")) {
                            precipitation = next1h.getJSONObject("details").optDouble("precipitation_amount", 0.0).toFloat()
                            totalPrecipitation += precipitation
                        }

                        if (next1h.has("summary")) {
                            val symbol = next1h.getJSONObject("summary").optString("symbol_code")
                            symbolCounts[symbol] = (symbolCounts[symbol] ?: 0) + 1
                        }
                    } else if (data.has("next_6_hours")) {
                        val next6h = data.getJSONObject("next_6_hours")
                        if (next6h.has("details")) {
                            precipitation = next6h.getJSONObject("details").optDouble("precipitation_amount", 0.0).toFloat()
                            totalPrecipitation += precipitation
                        }

                        if (next6h.has("summary")) {
                            val symbol = next6h.getJSONObject("summary").optString("symbol_code")
                            symbolCounts[symbol] = (symbolCounts[symbol] ?: 0) + 3
                        }
                    }
                }

                val avgTemp = if (timeDataList.isNotEmpty()) totalTemp / timeDataList.size else 0f
                val dominantSymbol = symbolCounts.entries.maxByOrNull { it.value }?.key

                val date = outputDateFormat.parse(dateKey)
                val dayName = if (date != null) {
                    dayNameFormat.format(date).replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                    }
                } else "Ukjent"

                val dailyForecast = DailyForecast(
                    date = dateKey,
                    dayName = dayName,
                    avgTemperature = avgTemp,
                    minTemperature = if (minTemp != Float.MAX_VALUE) minTemp else avgTemp,
                    maxTemperature = if (maxTemp != Float.MIN_VALUE) maxTemp else avgTemp,
                    totalPrecipitation = totalPrecipitation,
                    symbolCode = dominantSymbol
                )

                result.add(dailyForecast)
            }

        } catch (e: Exception) {
            Log.e("VaerDataSource", "Feil ved generering av 10-dagers vaermelding", e)
        }

        return result
    }
}

data class VaerData(
    val time: String,
    val currentTemperature: Float,
    val minTemperature: Float,
    val precipitation1h: Float,
    val totalPrecipitation24h: Float,
    val symbolCode1h: String?,
    val extendedForecast: List<DailyForecast> = emptyList()
)

data class DailyForecast(
    val date: String,
    val dayName: String,
    val avgTemperature: Float,
    val minTemperature: Float,
    val maxTemperature: Float,
    val totalPrecipitation: Float,
    val symbolCode: String?
)
