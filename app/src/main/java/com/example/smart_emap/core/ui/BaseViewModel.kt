package com.example.smart_emap.core.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/** 带有统一异常处理和日志记录的基础 ViewModel */
abstract class BaseViewModel : ViewModel() {

    protected val tag: String = this::class.java.simpleName

    private val defaultExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(tag, "协程中捕获到未处理异常: ${throwable.message}", throwable)
        onUnhandledException(throwable)
    }

    /** 快捷启动协程，带默认异常处理 */
    protected fun launch(
        context: CoroutineContext = EmptyCoroutineContext,
        block: suspend CoroutineScope.() -> Unit
    ) {
        viewModelScope.launch(context + defaultExceptionHandler, block = block)
    }

    /** 子类可重写以处理全局未捕获异常（如展示全局弹窗提示） */
    protected open fun onUnhandledException(throwable: Throwable) {
        // 默认仅打日志
    }
}
