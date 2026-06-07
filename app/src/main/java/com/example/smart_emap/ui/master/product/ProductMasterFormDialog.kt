package com.example.smart_emap.ui.master.product

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.max
import kotlin.math.roundToInt

private val formPrimary = Color(0xFF409EFF)
private val formShellBg = Color(0xFFEEF2F7)
private val formTabBarBg = Color(0xFFE4EBF3)
private val formFooterBg = Color(0xFFF5F8FC)
private val formLabelWidth = 108.dp
private val formControlHeight = 34.dp
private val formDialogWidthFraction = 0.864f // 0.96 * 0.9

private val formTabAccents = listOf(
    Color(0xFF409EFF),
    Color(0xFFE6A23C),
    Color(0xFF67C23A),
    Color(0xFF9B59B6),
    Color(0xFF64748B),
)

private val LocalFormTabAccent = staticCompositionLocalOf { formPrimary }

private data class ProductFormTab(val title: String)

private val productFormTabs = listOf(
    ProductFormTab("🧾 基本情報"),
    ProductFormTab("🏭 製造設定"),
    ProductFormTab("📦 梱包・物流"),
    ProductFormTab("🧱 材料・加工"),
    ProductFormTab("📝 備考"),
)

@Composable
fun ProductMasterFormDialog(
    isEdit: Boolean,
    values: Map<String, String>,
    loading: Boolean,
    materialOptions: List<Pair<String, String>>,
    destinationOptions: List<Pair<String, String>>,
    routeOptions: List<Pair<String, String>>,
    onValueChange: (String, String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabScroll = rememberScrollState()
    var entranceReady by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entranceReady = true }
    val dialogScale by animateFloatAsState(
        targetValue = if (entranceReady) 1f else 0.9f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 380f),
        label = "dialog-scale",
    )
    val dialogAlpha by animateFloatAsState(
        targetValue = if (entranceReady) 1f else 0f,
        animationSpec = tween(260),
        label = "dialog-alpha",
    )
    val tabAccent = formTabAccents.getOrElse(selectedTab) { formPrimary }

    Dialog(
        onDismissRequest = { if (!loading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = formShellBg,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth(formDialogWidthFraction)
                .heightIn(max = 640.dp)
                .scale(dialogScale)
                .alpha(dialogAlpha)
                .shadow(
                    elevation = 28.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = Color(0x59000000),
                    spotColor = Color(0x73000000),
                ),
        ) {
            Column {
                ProductFormDialogHeader(isEdit = isEdit, loading = loading, onDismiss = onDismiss)
                ProductFormTabBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                )
                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(14.dp),
                                ambientColor = tabAccent.copy(alpha = 0.18f),
                                spotColor = tabAccent.copy(alpha = 0.28f),
                            ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, tabAccent.copy(alpha = 0.22f)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(tabAccent.copy(alpha = 0.06f), Color(0xFFFAFBFC), Color.White),
                                    ),
                                ),
                        ) {
                            CompositionLocalProvider(LocalFormTabAccent provides tabAccent) {
                                AnimatedContent(
                                    targetState = selectedTab,
                                    transitionSpec = {
                                        (fadeIn(tween(220)) + slideInHorizontally { it / 5 })
                                            .togetherWith(fadeOut(tween(160)) + slideOutHorizontally { -it / 5 })
                                    },
                                    label = "product-form-tab-content",
                                ) { tab ->
                                    Column(
                                        modifier = Modifier
                                            .padding(horizontal = 14.dp, vertical = 12.dp)
                                            .verticalScroll(tabScroll),
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        when (tab) {
                                            0 -> ProductFormBasicTab(values, isEdit, onValueChange)
                                            1 -> ProductFormManufactureTab(values, routeOptions, onValueChange)
                                            2 -> ProductFormLogisticsTab(values, destinationOptions, onValueChange)
                                            3 -> ProductFormMaterialTab(values, materialOptions, onValueChange)
                                            4 -> ProductFormNoteTab(values, onValueChange)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                ProductFormDialogFooter(loading = loading, onDismiss = onDismiss, onConfirm = onConfirm)
            }
        }
    }
}

@Composable
private fun ProductFormTabBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    ScrollableTabRow(
        selectedTabIndex = selectedTab,
        edgePadding = 10.dp,
        containerColor = formTabBarBg,
        contentColor = formPrimary,
        indicator = { tabPositions ->
            if (selectedTab < tabPositions.size) {
                val accent = formTabAccents.getOrElse(selectedTab) { formPrimary }
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[selectedTab])
                        .shadow(4.dp, RoundedCornerShape(2.dp), spotColor = accent.copy(alpha = 0.5f)),
                    height = 3.dp,
                    color = accent,
                )
            }
        },
        divider = { HorizontalDivider(color = Color(0xFFD8E0EA)) },
    ) {
        productFormTabs.forEachIndexed { index, tab ->
            val selected = selectedTab == index
            val accent = formTabAccents.getOrElse(index) { formPrimary }
            val tabColor by animateColorAsState(
                targetValue = if (selected) accent else Color(0xFF64748B),
                animationSpec = tween(200),
                label = "tab-color-$index",
            )
            val tabScale by animateFloatAsState(
                targetValue = if (selected) 1.04f else 1f,
                animationSpec = spring(dampingRatio = 0.65f, stiffness = 420f),
                label = "tab-scale-$index",
            )
            Tab(
                selected = selected,
                onClick = { onTabSelected(index) },
                selectedContentColor = accent,
                unselectedContentColor = Color(0xFF64748B),
                text = {
                    Text(
                        tab.title,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = tabColor,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier.scale(tabScale),
                    )
                },
            )
        }
    }
}

@Composable
private fun ProductFormDialogHeader(isEdit: Boolean, loading: Boolean, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFFD6EEFF), Color(0xFFEAF6FF), Color(0xFFF8FCFF)),
                ),
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.85f), Color(0xFFBFDFFF).copy(alpha = 0.35f))),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            )
            .padding(start = 18.dp, end = 4.dp, top = 14.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .shadow(6.dp, RoundedCornerShape(10.dp), spotColor = formPrimary.copy(alpha = 0.35f))
                    .background(Color.White, RoundedCornerShape(10.dp))
                    .border(1.dp, formPrimary.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(if (isEdit) "✏️" else "🆕", fontSize = 18.sp)
            }
            Spacer(Modifier.width(10.dp))
            Text(
                if (isEdit) "製品編集" else "新規製品登録",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50),
            )
        }
        IconButton(onClick = onDismiss, enabled = !loading) {
            Icon(Icons.Default.Close, contentDescription = "閉じる", tint = Color(0xFF909399))
        }
    }
}

@Composable
private fun ProductFormDialogFooter(loading: Boolean, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    val cancelInteraction = remember { MutableInteractionSource() }
    val saveInteraction = remember { MutableInteractionSource() }
    val cancelPressed by cancelInteraction.collectIsPressedAsState()
    val savePressed by saveInteraction.collectIsPressedAsState()
    val cancelScale by animateFloatAsState(if (cancelPressed) 0.96f else 1f, tween(100), label = "cancel-scale")
    val saveScale by animateFloatAsState(if (savePressed) 0.96f else 1f, tween(100), label = "save-scale")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(Color(0xFFF8FAFD), formFooterBg)),
            )
            .border(1.dp, Color(0xFFDCE4EE))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedButton(
            onClick = onDismiss,
            enabled = !loading,
            interactionSource = cancelInteraction,
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCDFE6)),
            modifier = Modifier.scale(cancelScale).shadow(2.dp, RoundedCornerShape(10.dp)),
        ) {
            Text("キャンセル", color = Color(0xFF606266), fontSize = 13.sp)
        }
        Spacer(Modifier.width(14.dp))
        Button(
            onClick = onConfirm,
            enabled = !loading,
            interactionSource = saveInteraction,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
            modifier = Modifier
                .scale(saveScale)
                .shadow(10.dp, RoundedCornerShape(10.dp), spotColor = formPrimary.copy(alpha = 0.55f))
                .background(
                    Brush.linearGradient(listOf(Color(0xFF66B1FF), formPrimary, Color(0xFF337ECC))),
                    RoundedCornerShape(10.dp),
                )
                .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("💾 保存", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ProductFormBasicTab(values: Map<String, String>, isEdit: Boolean, onValueChange: (String, String) -> Unit) {
    ProductFormTwoColRow {
        Row(
            { ProductFormTextItem("製品CD", values["product_cd"].orEmpty(), { onValueChange("product_cd", it) }, required = true, enabled = !isEdit, placeholder = "例:90011") },
            { ProductFormTextItem("製品名称", values["product_name"].orEmpty(), { onValueChange("product_name", it) }, required = true, placeholder = "例:011B CTR") },
        )
        Row(
            { ProductFormTextItem("品番", values["part_number"].orEmpty(), { onValueChange("part_number", it) }, placeholder = "例:71941-X1453") },
            { ProductFormTextItem("別名", values["product_alias"].orEmpty(), { onValueChange("product_alias", it) }, placeholder = "製品の別名") },
        )
        Row(
            { ProductFormSelectItem("製品種別", values["product_type"].orEmpty(), { onValueChange("product_type", it) }, listOf("" to "選択", "量産品" to "量産品", "試作品" to "試作品", "補給品" to "補給品", "その他" to "その他")) },
            { ProductFormSelectItem("カテゴリ", values["category"].orEmpty(), { onValueChange("category", it) }, listOf("" to "選択", "一般" to "一般", "一般溶接" to "一般溶接", "メカ溶接" to "メカ溶接", "自動車" to "自動車", "その他" to "その他")) },
        )
        Row(
            { ProductFormSelectItem("分類(kind)", values["kind"].orEmpty(), { onValueChange("kind", it) }, listOf("" to "選択", "T" to "T", "N" to "N", "F" to "F")) },
            { ProductFormSelectItem("優先度", values["priority"].orEmpty(), { onValueChange("priority", it) }, listOf("1" to "高", "2" to "中", "3" to "低")) },
        )
        Row(
            { ProductFormSelectItem("ステータス", values["status"].orEmpty(), { onValueChange("status", it) }, listOf("active" to "active", "inactive" to "inactive")) },
            { ProductFormNumberItem("売価（円）", values["unit_price"].orEmpty(), { onValueChange("unit_price", it) }, decimals = 2, min = 0.0) },
        )
    }
}

@Composable
private fun ProductFormManufactureTab(
    values: Map<String, String>,
    routeOptions: List<Pair<String, String>>,
    onValueChange: (String, String) -> Unit,
) {
    ProductFormTwoColRow {
        Row(
            { ProductFormNumberItem("工程数", values["process_count"].orEmpty(), { onValueChange("process_count", it) }, min = 1.0) },
            { ProductFormSwitchItem("多段階工程", values["is_multistage"] == "true", { onValueChange("is_multistage", it.toString()) }) },
        )
        Row(
            { ProductFormNumberItem("リードタイム(日)", values["lead_time"].orEmpty(), { onValueChange("lead_time", it) }, min = 0.0) },
            { ProductFormNumberItem("安全在庫日数", values["safety_days"].orEmpty(), { onValueChange("safety_days", it) }, min = 0.0) },
        )
        Row(
            { ProductFormNumberItem("生産ロット", values["lot_size"].orEmpty(), { onValueChange("lot_size", it) }, min = 1.0) },
            {
                ProductFormSelectItem(
                    "工程ルート",
                    values["route_cd"].orEmpty(),
                    { onValueChange("route_cd", it) },
                    listOf("" to "選択") + routeOptions.map { (cd, name) -> cd to "$cd|$name" },
                    menuMatchAnchorWidth = false,
                )
            },
        )
    }
}

@Composable
private fun ProductFormLogisticsTab(
    values: Map<String, String>,
    destinationOptions: List<Pair<String, String>>,
    onValueChange: (String, String) -> Unit,
) {
    ProductFormTwoColRow {
        Row(
            {
                ProductFormSelectItem(
                    "梱包タイプ",
                    values["box_type"].orEmpty(),
                    { onValueChange("box_type", it) },
                    listOf("" to "選択", "小箱" to "小箱", "大箱" to "大箱", "TP箱" to "TP箱", "段ボール" to "段ボール", "加工箱" to "加工箱", "特殊箱" to "特殊箱"),
                )
            },
            { ProductFormNumberItem("入数/箱", values["unit_per_box"].orEmpty(), { onValueChange("unit_per_box", it) }, min = 0.0) },
        )
        Row(
            { ProductFormTextItem("寸法", values["dimensions"].orEmpty(), { onValueChange("dimensions", it) }, placeholder = "例:14Φx1.0") },
            { ProductFormNumberItem("重量 (g)", values["weight"].orEmpty(), { onValueChange("weight", it) }, min = 0.0) },
        )
        Row(
            {
                ProductFormSelectItem(
                    "納入先",
                    values["destination_cd"].orEmpty(),
                    { onValueChange("destination_cd", it) },
                    listOf("" to "選択") + destinationOptions.map { (cd, name) -> cd to "$cd|$name" },
                    menuMatchAnchorWidth = false,
                )
            },
            { ProductFormTextItem("対応車種", values["vehicle_model"].orEmpty(), { onValueChange("vehicle_model", it) }) },
        )
        Row(
            {
                ProductFormSelectItem(
                    "保管場所",
                    values["location_cd"].orEmpty(),
                    { onValueChange("location_cd", it) },
                    listOf("" to "選択") + productLocationOptions,
                )
            },
            { ProductFormTextItem("使用開始日", values["start_use_date"].orEmpty(), { onValueChange("start_use_date", it) }, placeholder = "YYYY-MM-DD") },
        )
    }
}

@Composable
private fun ProductFormMaterialTab(
    values: Map<String, String>,
    materialOptions: List<Pair<String, String>>,
    onValueChange: (String, String) -> Unit,
) {
    ProductFormTwoColRow {
        Row(
            {
                ProductFormSelectItem(
                    "材料",
                    values["material_cd"].orEmpty(),
                    { onValueChange("material_cd", it) },
                    listOf("" to "選択") + materialOptions.map { (cd, name) -> cd to "$cd|$name" },
                    menuMatchAnchorWidth = false,
                )
            },
            { ProductFormNumberItem("切断長 (mm)", values["cut_length"].orEmpty(), { onValueChange("cut_length", it) }, decimals = 2, min = 0.0) },
        )
        Row(
            { ProductFormNumberItem("面取長 (mm)", values["chamfer_length"].orEmpty(), { onValueChange("chamfer_length", it) }, decimals = 2, min = 0.0) },
            { ProductFormNumberItem("展開長 (mm)", values["developed_length"].orEmpty(), { onValueChange("developed_length", it) }, decimals = 2, min = 0.0) },
        )
        Row(
            { ProductFormNumberItem("端材長 (mm)", values["scrap_length"].orEmpty(), { onValueChange("scrap_length", it) }, decimals = 2, min = 0.0) },
            { ProductFormNumberItem("取り数", values["take_count"].orEmpty(), { onValueChange("take_count", it) }, min = 0.0) },
        )
    }
}

@Composable
private fun ProductFormNoteTab(values: Map<String, String>, onValueChange: (String, String) -> Unit) {
    val accent = LocalFormTabAccent.current
    ProductFormLabeledField("備考") {
        BasicTextField(
            value = values["note"].orEmpty(),
            onValueChange = { onValueChange("note", it) },
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .shadow(3.dp, RoundedCornerShape(10.dp), spotColor = accent.copy(alpha = 0.2f))
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.verticalGradient(listOf(Color.White, accent.copy(alpha = 0.04f))),
                )
                .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 12.sp,
                color = Color(0xFF334155),
                textAlign = TextAlign.Center,
            ),
            decorationBox = { inner ->
                Box(
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (values["note"].isNullOrBlank()) {
                        Text(
                            "自由記述欄",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { inner() }
                }
            },
        )
    }
}

@Composable
private fun ProductFormTwoColRow(content: @Composable ProductFormRowScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ProductFormRowScope().content()
    }
}

private class ProductFormRowScope {
    @Composable
    fun Row(left: @Composable () -> Unit, right: @Composable () -> Unit) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { left() }
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { right() }
        }
    }
}

@Composable
private fun ProductFormTextItem(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    required: Boolean = false,
    enabled: Boolean = true,
    placeholder: String = "",
) {
    ProductFormLabeledField(label, required) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            modifier = productFormInputModifier(enabled),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 12.sp,
                color = if (enabled) Color(0xFF334155) else Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
            ),
            decorationBox = { inner ->
                Box(
                    modifier = Modifier.fillMaxWidth().height(formControlHeight),
                    contentAlignment = Alignment.Center,
                ) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(
                            placeholder,
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { inner() }
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductFormSelectItem(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    options: List<Pair<String, String>>,
    menuMatchAnchorWidth: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    val display = options.find { it.first == value }?.second ?: value.ifBlank { "選択" }
    ProductFormLabeledField(label) {
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(formControlHeight)
                    .shadow(2.dp, RoundedCornerShape(10.dp), spotColor = LocalFormTabAccent.current.copy(alpha = 0.25f))
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LocalFormTabAccent.current.copy(alpha = 0.28f)),
                color = Color.White,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        display,
                        fontSize = 12.sp,
                        color = if (value.isBlank()) Color(0xFF94A3B8) else Color(0xFF334155),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                    )
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                }
            }
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.exposedDropdownSize(matchAnchorWidth = menuMatchAnchorWidth),
            ) {
                options.forEach { (v, labelText) ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                labelText,
                                fontSize = 12.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = if (menuMatchAnchorWidth) Modifier else Modifier.widthIn(min = 220.dp),
                            )
                        },
                        onClick = { onChange(v); expanded = false },
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductFormNumberItem(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    decimals: Int = 0,
    min: Double = 0.0,
    step: Double = if (decimals > 0) 0.01 else 1.0,
) {
    ProductFormLabeledField(label) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            OutlinedButton(
                onClick = {
                    val current = value.toDoubleOrNull() ?: min
                    val next = max(min, current - step)
                    onValueChange(formatNumber(next, decimals))
                },
                modifier = Modifier
                    .size(32.dp)
                    .shadow(2.dp, RoundedCornerShape(8.dp), spotColor = LocalFormTabAccent.current.copy(alpha = 0.3f)),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LocalFormTabAccent.current.copy(alpha = 0.25f)),
            ) {
                Icon(Icons.Default.Remove, contentDescription = "減", modifier = Modifier.size(14.dp), tint = LocalFormTabAccent.current)
            }
            BasicTextField(
                value = value,
                onValueChange = { raw ->
                    if (raw.isEmpty() || raw.toDoubleOrNull() != null) onValueChange(raw)
                },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .height(formControlHeight)
                    .shadow(2.dp, RoundedCornerShape(10.dp), spotColor = LocalFormTabAccent.current.copy(alpha = 0.2f))
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.verticalGradient(listOf(Color.White, LocalFormTabAccent.current.copy(alpha = 0.05f))),
                    )
                    .border(1.dp, LocalFormTabAccent.current.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 12.sp,
                    color = Color(0xFF334155),
                    textAlign = TextAlign.Center,
                ),
                decorationBox = { inner ->
                    Box(
                        modifier = Modifier.fillMaxWidth().height(formControlHeight),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { inner() }
                    }
                },
            )
            OutlinedButton(
                onClick = {
                    val current = value.toDoubleOrNull() ?: min
                    onValueChange(formatNumber(current + step, decimals))
                },
                modifier = Modifier
                    .size(32.dp)
                    .shadow(2.dp, RoundedCornerShape(8.dp), spotColor = LocalFormTabAccent.current.copy(alpha = 0.3f)),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LocalFormTabAccent.current.copy(alpha = 0.25f)),
            ) {
                Icon(Icons.Default.Add, contentDescription = "増", modifier = Modifier.size(14.dp), tint = LocalFormTabAccent.current)
            }
        }
    }
}

@Composable
private fun ProductFormSwitchItem(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    ProductFormLabeledField(label) {
        Row(
            modifier = Modifier.fillMaxWidth().height(formControlHeight),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedTrackColor = formPrimary),
            )
        }
    }
}

@Composable
private fun ProductFormLabeledField(label: String, required: Boolean = false, content: @Composable () -> Unit) {
    val accent = LocalFormTabAccent.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = formControlHeight)
            .clip(RoundedCornerShape(10.dp))
            .background(accent.copy(alpha = 0.04f))
            .border(1.dp, accent.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier
                .width(formLabelWidth)
                .height(formControlHeight)
                .shadow(1.dp, RoundedCornerShape(8.dp), spotColor = accent.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(8.dp),
            color = accent.copy(alpha = 0.12f),
            border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.2f)),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    label,
                    fontSize = 11.sp,
                    color = Color(0xFF475569),
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                )
                if (required) Text(" *", fontSize = 11.sp, color = Color(0xFFF56C6C))
            }
        }
        Spacer(Modifier.width(8.dp))
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { content() }
    }
}

@Composable
private fun productFormInputModifier(enabled: Boolean): Modifier {
    val accent = LocalFormTabAccent.current
    return Modifier
        .fillMaxWidth()
        .height(formControlHeight)
        .shadow(2.dp, RoundedCornerShape(10.dp), spotColor = accent.copy(alpha = 0.22f))
        .clip(RoundedCornerShape(10.dp))
        .background(
            if (enabled) {
                Brush.verticalGradient(listOf(Color.White, accent.copy(alpha = 0.05f)))
            } else {
                Brush.verticalGradient(listOf(Color(0xFFF5F7FA), Color(0xFFEEF2F6)))
            },
        )
        .border(1.dp, if (enabled) accent.copy(alpha = 0.28f) else Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
}

private fun formatNumber(value: Double, decimals: Int): String {
    if (decimals <= 0) return max(0, value.roundToInt()).toString()
    return String.format("%.${decimals}f", value)
}
