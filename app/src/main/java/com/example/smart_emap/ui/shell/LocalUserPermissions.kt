package com.example.smart_emap.ui.shell

import androidx.compose.runtime.compositionLocalOf
import com.example.smart_emap.data.model.UserDto

val LocalCurrentUser = compositionLocalOf<UserDto> {
    error("LocalCurrentUser is not provided")
}
