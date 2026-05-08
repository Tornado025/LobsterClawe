---
name: ingredient-scraper
description: >
  Scrapes grocery ingredient prices from Indian quick-commerce platforms (Blinkit,
  Zepto, Swiggy Instamart) using a locally hosted SearXNG instance. Use this skill
  whenever the user wants to fetch, parse, or display ingredient prices, build a
  SearxngClient, wire up a price fallback in GroceryViewModel, or work with SearXNG
  search results in the LobsterClawe Android project. Also trigger when the user
  mentions "price scraping", "grocery prices", "SearXNG fallback", or "ingredient
  fetch" — even if they don't mention SearXNG explicitly.
---

# Ingredient Scraper Skill

Fetches real-time grocery prices for ingredients by querying a self-hosted SearXNG
instance, then maps results to known Indian quick-commerce stores.

---

## Environment

| Key | Value |
|---|---|
| SearXNG host (emulator) | `http://10.0.2.2:8888` |
| SearXNG host (device) | your machine's LAN IP, port `8888` |
| Docker image | `searxng/searxng` |
| BuildConfig key | `SEARXNG_BASE_URL` |
| Network security | cleartext to `10.0.2.2` must be allowed — see **Network Security** section |

---

## Step 1 — `local.properties`

```
SEARXNG_BASE_URL=http://10.0.2.2:8888
```

---

## Step 2 — `app/build.gradle.kts`

Inside `defaultConfig {}`, alongside the other `buildConfigField` lines:

```kotlin
buildConfigField("String", "SEARXNG_BASE_URL", "\"${props["SEARXNG_BASE_URL"]}\"")
```

---

## Step 3 — `SearxngClient.kt`

Create at `com.lobsterclawe.network.SearxngClient`.

```kotlin
package com.lobsterclawe.network

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.lobsterclawe.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

class SearxngClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val baseUrl = BuildConfig.SEARXNG_BASE_URL

    /** Known store URL fragments → display name */
    private val storeMap = mapOf(
        "blinkit"  to "Blinkit",
        "zepto"    to "Zepto",
        "swiggy"   to "Instamart"
    )

    /**
     * Query SearXNG for [ingredient] and return a list of [StorePrice].
     * Results with no recognisable store or no price are still included
     * with price_inr = 0 so the UI can show "unavailable".
     */
    suspend fun fetchPrices(ingredient: String): List<StorePrice> =
        withContext(Dispatchers.IO) {
            val query = "$ingredient price instamart blinkit zepto"
            val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
            val url = "$baseUrl/search?q=$encodedQuery&categories=shopping&format=json"

            val request = Request.Builder().url(url).build()

            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful)
                        throw IOException("SearXNG returned ${response.code}")

                    val body = response.body?.string()
                        ?: throw IOException("Empty SearXNG response")

                    val parsed = gson.fromJson(body, SearxngResponse::class.java)
                    mapResults(parsed.results ?: emptyList(), ingredient)
                }
            } catch (e: ConnectException) {
                throw IOException("Cannot reach SearXNG at $baseUrl — is Docker running?", e)
            } catch (e: SocketTimeoutException) {
                throw IOException("SearXNG timed out for '$ingredient'", e)
            }
        }

    private fun mapResults(
        results: List<SearxngResult>,
        ingredient: String
    ): List<StorePrice> = results.mapNotNull { result ->
        val store = storeMap.entries
            .firstOrNull { (key, _) -> result.url?.contains(key, ignoreCase = true) == true }
            ?.value ?: return@mapNotNull null   // skip unrecognised stores

        StorePrice(
            store      = store,
            ingredient = ingredient,
            priceInr   = result.price ?: 0.0,
            available  = result.price != null
        )
    }

    // ── Raw JSON models ──────────────────────────────────────────────────────

    private data class SearxngResponse(
        val results: List<SearxngResult>?
    )

    private data class SearxngResult(
        val title:  String?,
        val url:    String?,
        val price:  Double?,       // present for shopping results, null otherwise
        val engine: String?
    )
}
```

---

## Step 4 — `GroceryViewModel.kt` — fallback logic

```kotlin
viewModelScope.launch {
    _uiState.value = UiState.Loading
    try {
        // Primary: OpenClaw
        val prices = openClawClient.fetchPrices(ingredients)
        _uiState.value = UiState.Success(prices)
    } catch (primaryError: Exception) {
        // Fallback: SearXNG
        try {
            val prices = ingredients.flatMap { ingredient ->
                searxngClient.fetchPrices(ingredient)
            }
            _uiState.value = UiState.Success(prices)
        } catch (fallbackError: Exception) {
            _uiState.value = UiState.Error(
                "Could not fetch prices: both OpenClaw and SearXNG unavailable."
            )
        }
    }
}
```

Inject `SearxngClient` the same way `OpenClawClient` is injected in your ViewModel.

---

## Step 5 — Network Security (cleartext HTTP)

`res/xml/network_security_config.xml` must allow cleartext to `10.0.2.2`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="false">10.0.2.2</domain>
    </domain-config>
</network-security-config>
```

`AndroidManifest.xml` — inside `<application>`:

```xml
android:networkSecurityConfig="@xml/network_security_config"
```

---

## Store Matching Rules

| URL contains | Maps to |
|---|---|
| `blinkit` | Blinkit |
| `zepto` | Zepto |
| `swiggy` | Instamart |
| anything else | skipped |

To add more stores, extend the `storeMap` in `SearxngClient`.

---

## SearXNG Docker — quick reference

```bash
# Start
docker run -d -p 8888:8080 --name searxng searxng/searxng

# Verify
curl "http://localhost:8888/search?q=onion+price+blinkit&categories=shopping&format=json"

# Stop
docker stop searxng
```

Enable the `shopping` category in SearXNG settings if results are empty —
it is disabled by default in some builds.

---

## Error States to Handle

| Condition | Message |
|---|---|
| Docker not running | "Cannot reach SearXNG at ... — is Docker running?" |
| SearXNG timeout | "SearXNG timed out for '...'" |
| Both sources fail | "Could not fetch prices: both OpenClaw and SearXNG unavailable." |
| Price field absent | Show item with `available = false`, price `₹0` |
