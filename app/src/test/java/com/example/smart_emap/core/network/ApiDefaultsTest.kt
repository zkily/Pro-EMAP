package com.example.smart_emap.core.network

import org.junit.Assert.assertEquals
import org.junit.Test

class ApiDefaultsTest {

    @Test
    fun migrateDevApiUrl_frontendHttps5010_toBackendHttp8010() {
        assertEquals(
            "http://192.168.0.12:8010/",
            ApiDefaults.migrateDevApiUrl("https://192.168.0.12:5010"),
        )
    }

    @Test
    fun migrateDevApiUrl_frontendHttp3005_toBackendHttp8005() {
        assertEquals(
            "http://192.168.0.12:8005/",
            ApiDefaults.migrateDevApiUrl("http://192.168.0.12:3005"),
        )
    }

    @Test
    fun migrateDevApiUrl_httpsBackend8010_onLan_toHttp() {
        assertEquals(
            "http://192.168.0.12:8010/",
            ApiDefaults.migrateDevApiUrl("https://192.168.0.12:8010"),
        )
    }
}
