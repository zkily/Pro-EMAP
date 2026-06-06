package com.example.smart_emap.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.ui.theme.LoginColors

@Composable
fun PlaceholderScreen(
    path: String,
    modifier: Modifier = Modifier,
) {
    val menuLeaf = AppMenuConfig.findLeaf(path)
    val title = menuLeaf?.label ?: AppMenuConfig.titleForPath(path)
    val isKnownRoute = menuLeaf != null
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(LayoutColors.ContentBgStart, LayoutColors.ContentBgEnd))),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LoginColors.TitleDark)
                Text(text = path, fontSize = 12.sp, color = LoginColors.TextMuted)
                Text(
                    text = if (isKnownRoute) {
                        "メニューからこのページを開きました。モバイル画面は順次実装予定です。"
                    } else {
                        "このルートはメニューに登録されていません。"
                    },
                    fontSize = 13.sp,
                    color = LoginColors.TextMuted,
                )
            }
        }
    }
}
