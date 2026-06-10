package com.example.smart_emap.core.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.smart_emap.core.network.ApiDefaults
import com.example.smart_emap.data.model.UserDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "smart_emap_session")

data class RememberedCredentials(
    val rememberMe: Boolean,
    val username: String,
    val password: String,
)

class SessionStore(private val context: Context) {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val userAdapter = moshi.adapter(UserDto::class.java)

    private val tokenKey = stringPreferencesKey("token")
    private val userKey = stringPreferencesKey("user_json")
    private val apiBaseUrlKey = stringPreferencesKey("api_base_url")
    private val rememberMeKey = booleanPreferencesKey("remember_me")
    private val rememberUsernameKey = stringPreferencesKey("remember_username")
    private val rememberPasswordKey = stringPreferencesKey("remember_password")

    val tokenFlow: Flow<String?> = context.dataStore.data.map { it[tokenKey] }

    val userFlow: Flow<UserDto?> = context.dataStore.data.map { prefs ->
        val json = prefs[userKey] ?: return@map null
        runCatching { userAdapter.fromJson(json) }.getOrNull()
    }

    val apiBaseUrlFlow: Flow<String?> = context.dataStore.data.map { it[apiBaseUrlKey] }

    suspend fun getToken(): String? = context.dataStore.data.first()[tokenKey]

    suspend fun getUser(): UserDto? {
        val json = context.dataStore.data.first()[userKey] ?: return null
        return runCatching { userAdapter.fromJson(json) }.getOrNull()
    }

    /** 返回用户保存的 API 地址（不做端口迁移），登录页与 ApiClient 均以此为准。 */
    suspend fun getApiBaseUrl(defaultUrl: String): String {
        val raw = context.dataStore.data.first()[apiBaseUrlKey]?.trim().orEmpty()
        return if (raw.isBlank()) {
            ApiDefaults.ensureTrailingSlash(defaultUrl)
        } else {
            ApiDefaults.ensureTrailingSlash(raw)
        }
    }

    suspend fun getRememberedCredentials(): RememberedCredentials {
        val prefs = context.dataStore.data.first()
        val remember = prefs[rememberMeKey] == true
        return RememberedCredentials(
            rememberMe = remember,
            username = if (remember) prefs[rememberUsernameKey].orEmpty() else "",
            password = if (remember) prefs[rememberPasswordKey].orEmpty() else "",
        )
    }

    suspend fun saveSession(token: String, user: UserDto) {
        context.dataStore.edit { prefs ->
            prefs[tokenKey] = token
            prefs[userKey] = userAdapter.toJson(user)
        }
    }

    suspend fun saveApiBaseUrl(url: String) {
        context.dataStore.edit { prefs ->
            prefs[apiBaseUrlKey] = ApiDefaults.ensureTrailingSlash(url)
        }
    }

    suspend fun saveRememberMe(remember: Boolean, username: String, password: String) {
        context.dataStore.edit { prefs ->
            prefs[rememberMeKey] = remember
            if (remember) {
                prefs[rememberUsernameKey] = username
                prefs[rememberPasswordKey] = password
            } else {
                prefs.remove(rememberUsernameKey)
                prefs.remove(rememberPasswordKey)
            }
        }
    }

    suspend fun clear() {
        context.dataStore.edit { prefs ->
            prefs.remove(tokenKey)
            prefs.remove(userKey)
        }
    }
}
