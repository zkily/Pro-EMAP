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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "smart_emap_session")

data class RememberedCredentials(
    val rememberMe: Boolean,
    val username: String,
)

class SessionStore(private val context: Context) {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val userAdapter = moshi.adapter(UserDto::class.java)

    private val tokenKey = stringPreferencesKey("token")
    private val userKey = stringPreferencesKey("user_json")
    private val apiBaseUrlKey = stringPreferencesKey("api_base_url")
    private val rememberMeKey = booleanPreferencesKey("remember_me")
    private val rememberUsernameKey = stringPreferencesKey("remember_username")

    @Volatile
    private var _cachedToken: String? = null
    
    /** 暴露给拦截器的高速非阻塞 Token 获取方法 */
    val cachedToken: String? get() = _cachedToken

    init {
        // 在后台线程持续同步 Token 到内存，供拦截器快速读取
        CoroutineScope(Dispatchers.IO).launch {
            context.dataStore.data.map { it[tokenKey] }.collect { 
                _cachedToken = it 
            }
        }
    }

    val tokenFlow: Flow<String?> = context.dataStore.data.map { it[tokenKey] }

    val userFlow: Flow<UserDto?> = context.dataStore.data.map { prefs ->
        val json = prefs[userKey] ?: return@map null
        runCatching { userAdapter.fromJson(json) }.getOrNull()
    }

    val apiBaseUrlFlow: Flow<String?> = context.dataStore.data.map { it[apiBaseUrlKey] }

    suspend fun getToken(): String? = _cachedToken ?: context.dataStore.data.first()[tokenKey]

    suspend fun getUser(): UserDto? {
        val json = context.dataStore.data.first()[userKey] ?: return null
        return runCatching { userAdapter.fromJson(json) }.getOrNull()
    }

    suspend fun getApiBaseUrl(defaultUrl: String): String {
        val raw = context.dataStore.data.first()[apiBaseUrlKey]?.trim().orEmpty()
        val resolved = ApiDefaults.resolveApiBaseUrl(raw.ifBlank { defaultUrl })
        val resolvedNormalized = ApiDefaults.ensureTrailingSlash(resolved)
        if (raw.isNotBlank()) {
            val storedNormalized = ApiDefaults.ensureTrailingSlash(raw)
            if (storedNormalized != resolvedNormalized) {
                context.dataStore.edit { prefs ->
                    prefs[apiBaseUrlKey] = resolvedNormalized
                }
            }
        }
        return resolvedNormalized
    }

    suspend fun getRememberedCredentials(): RememberedCredentials {
        val prefs = context.dataStore.data.first()
        val remember = prefs[rememberMeKey] == true
        return RememberedCredentials(
            rememberMe = remember,
            username = if (remember) prefs[rememberUsernameKey].orEmpty() else "",
        )
    }

    suspend fun saveSession(token: String, user: UserDto) {
        _cachedToken = token
        context.dataStore.edit { prefs ->
            prefs[tokenKey] = token
            prefs[userKey] = userAdapter.toJson(user)
        }
    }

    suspend fun saveApiBaseUrl(url: String) {
        val migrated = ApiDefaults.ensureTrailingSlash(
            ApiDefaults.migrateDevApiUrl(url.trim().trimEnd('/')),
        )
        context.dataStore.edit { prefs ->
            prefs[apiBaseUrlKey] = migrated
        }
    }

    suspend fun saveRememberMe(remember: Boolean, username: String) {
        context.dataStore.edit { prefs ->
            prefs[rememberMeKey] = remember
            if (remember) {
                prefs[rememberUsernameKey] = username
            } else {
                prefs.remove(rememberUsernameKey)
            }
            // 确保旧的明文密码被移除
            val oldPasswordKey = stringPreferencesKey("remember_password")
            prefs.remove(oldPasswordKey)
        }
    }

    suspend fun clear() {
        _cachedToken = null
        context.dataStore.edit { prefs ->
            prefs.remove(tokenKey)
            prefs.remove(userKey)
        }
    }
}
