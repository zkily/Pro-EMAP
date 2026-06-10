package com.example.smart_emap.core.auth

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.concurrent.atomic.AtomicBoolean

/** 跨层通知会话失效（如 API 返回 401），由导航层统一跳转登录。 */
class SessionEvents {
    private val _unauthorized = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val unauthorized: SharedFlow<Unit> = _unauthorized.asSharedFlow()

    private val emitted = AtomicBoolean(false)

    fun notifyUnauthorized() {
        if (emitted.compareAndSet(false, true)) {
            _unauthorized.tryEmit(Unit)
        }
    }

    fun reset() {
        emitted.set(false)
    }
}
