package com.example.smart_emap.ui.master.material

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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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

private val materialPrimary = Color(0xFF2980B9)
private val materialSaveGreen = Color(0xFF27AE60)
private val materialShellBg = Color(0xFFF8FAFC)
private val materialTabBarBg = Color(0xFFF0F4F8)
private val materialFooterBg = Color(0xFFFAFBFC)
private val materialControlHeight = 34.dp
private val materialDialogWidthFraction = 0.92f

private val materialTabAccents = listOf(
    Color(0xFF2980B9),
    Color(0xFF27AE60),
    Color(0xFFE6A23C),
    Color(0xFF9B59B6),
    Color(0xFF64748B),
)

private val LocalMaterialFormAccent = staticCompositionLocalOf { materialPrimary }

private data class MaterialFormTab(val title: String)

private val materialFormTabs = listOf(
    MaterialFormTab("基本情報"),
    MaterialFormTab("寸法・仕様"),
    MaterialFormTab("仕入・在庫"),
    MaterialFormTab("公差・範囲"),
    MaterialFormTab("備考"),
)

@Composable
fun MaterialMasterFormDialog(
    @Suppress("UNUSED_PARAMETER") isEdit: Boolean,
    values: Map<String, String>,
    loading: Boolean,
    supplierOptions: List<Pair<String, String>>,
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
        label = "material-dialog-scale",
    )
    val dialogAlpha by animateFloatAsState(
        targetValue = if (entranceReady) 1f else 0f,
        animationSpec = tween(260),
        label = "material-dialog-alpha",
    )
    val tabAccent = materialTabAccents.getOrElse(selectedTab) { materialPrimary }

    Dialog(
        onDismissRequest = { if (!loading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = materialShellBg,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth(materialDialogWidthFraction)
                .heightIn(max = 640.dp)
                .scale(dialogScale)
                .alpha(dialogAlpha)
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = Color(0x592980B9),
                    spotColor = Color(0x732980B9),
                ),
        ) {
            Column {
                MaterialFormDialogHeader(loading = loading, onDismiss = onDismiss)
                MaterialFormTabBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                )
                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    val tabBg = if (selectedTab == 3) {
                        Brush.linearGradient(listOf(Color(0xFFF0F9FF), Color(0xFFE0F2FE)))
                    } else {
                        Brush.verticalGradient(listOf(Color(0xFFF8FAFC), Color.White))
                    }
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(14.dp),
                                ambientColor = tabAccent.copy(alpha = 0.12f),
                                spotColor = tabAccent.copy(alpha = 0.2f),
                            ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, tabAccent.copy(alpha = 0.18f)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(tabBg),
                        ) {
                            CompositionLocalProvider(LocalMaterialFormAccent provides tabAccent) {
                                AnimatedContent(
                                    targetState = selectedTab,
                                    transitionSpec = {
                                        (fadeIn(tween(220)) + slideInHorizontally { it / 5 })
                                            .togetherWith(fadeOut(tween(160)) + slideOutHorizontally { -it / 5 })
                                    },
                                    label = "material-form-tab-content",
                                ) { tab ->
                                    Column(
                                        modifier = Modifier
                                            .padding(horizontal = 14.dp, vertical = 12.dp)
                                            .verticalScroll(tabScroll),
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        when (tab) {
                                            0 -> MaterialFormBasicTab(values, onValueChange)
                                            1 -> MaterialFormSpecsTab(values, onValueChange)
                                            2 -> MaterialFormPurchaseTab(values, supplierOptions, onValueChange)
                                            3 -> MaterialFormToleranceTab(values, onValueChange)
                                            4 -> MaterialFormNoteTab(values, onValueChange)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                MaterialFormDialogFooter(loading = loading, onDismiss = onDismiss, onConfirm = onConfirm)
            }
        }
    }
}

@Composable
private fun MaterialFormDialogHeader(loading: Boolean, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFFE6F7FF), Color(0xFFF5FBFF), Color.White),
                ),
            )
            .border(
                width = 1.dp,
                color = Color(0xFFEBEEF5),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            )
            .padding(start = 20.dp, end = 4.dp, top = 16.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🧱", fontSize = 22.sp)
            Spacer(Modifier.width(10.dp))
            Text(
                "材料情報の登録・編集",
                fontSize = 18.sp,
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
private fun MaterialFormTabBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    ScrollableTabRow(
        selectedTabIndex = selectedTab,
        edgePadding = 10.dp,
        containerColor = materialTabBarBg,
        contentColor = materialPrimary,
        indicator = { tabPositions ->
            if (selectedTab < tabPositions.size) {
                val accent = materialTabAccents.getOrElse(selectedTab) { materialPrimary }
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[selectedTab])
                        .shadow(3.dp, RoundedCornerShape(2.dp), spotColor = accent.copy(alpha = 0.4f)),
                    height = 3.dp,
                    color = accent,
                )
            }
        },
        divider = { HorizontalDivider(color = Color(0xFFEBEEF5)) },
    ) {
        materialFormTabs.forEachIndexed { index, tab ->
            val selected = selectedTab == index
            val accent = materialTabAccents.getOrElse(index) { materialPrimary }
            val tabColor by animateColorAsState(
                targetValue = if (selected) accent else Color(0xFF64748B),
                animationSpec = tween(200),
                label = "material-tab-color-$index",
            )
            Tab(
                selected = selected,
                onClick = { onTabSelected(index) },
                selectedContentColor = accent,
                unselectedContentColor = Color(0xFF64748B),
                text = {
                    Text(
                        tab.title,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = tabColor,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    )
                },
            )
        }
    }
}

@Composable
private fun MaterialFormDialogFooter(loading: Boolean, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    val cancelInteraction = remember { MutableInteractionSource() }
    val saveInteraction = remember { MutableInteractionSource() }
    val cancelPressed by cancelInteraction.collectIsPressedAsState()
    val savePressed by saveInteraction.collectIsPressedAsState()
    val cancelScale by animateFloatAsState(if (cancelPressed) 0.96f else 1f, tween(100), label = "cancel-scale")
    val saveScale by animateFloatAsState(if (savePressed) 0.96f else 1f, tween(100), label = "save-scale")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(materialFooterBg)
            .border(1.dp, Color(0xFFEBEEF5))
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedButton(
            onClick = onDismiss,
            enabled = !loading,
            interactionSource = cancelInteraction,
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, materialPrimary),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = materialPrimary),
            modifier = Modifier
                .scale(cancelScale)
                .background(Color(0xFFF5F7FA), RoundedCornerShape(8.dp)),
        ) {
            Text("キャンセル", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }
        Spacer(Modifier.width(12.dp))
        Button(
            onClick = onConfirm,
            enabled = !loading,
            interactionSource = saveInteraction,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(horizontal = 22.dp, vertical = 10.dp),
            modifier = Modifier
                .scale(saveScale)
                .shadow(6.dp, RoundedCornerShape(8.dp), spotColor = materialPrimary.copy(alpha = 0.35f))
                .background(
                    Brush.horizontalGradient(listOf(materialSaveGreen, materialPrimary)),
                    RoundedCornerShape(8.dp),
                ),
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("保存", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun MaterialFormBasicTab(values: Map<String, String>, onValueChange: (String, String) -> Unit) {
    MaterialFormTwoColLayout {
        Row(
            {
                MaterialFormTextItem(
                    "材料CD",
                    values["material_cd"].orEmpty(),
                    { onValueChange("material_cd", it) },
                    required = true,
                    placeholder = "材料コードを入力",
                )
            },
            {
                MaterialFormTextItem(
                    "材料名",
                    values["material_name"].orEmpty(),
                    { onValueChange("material_name", it) },
                    required = true,
                    placeholder = "材料名を入力",
                )
            },
        )
        Row(
            {
                MaterialFormSelectItem(
                    "材料種類",
                    values["material_type"].orEmpty(),
                    { onValueChange("material_type", it) },
                    listOf("" to "選択してください") + listOf("鋼材", "鋼管", "樹脂", "アルミ", "その他").map { it to it },
                )
            },
            { MaterialFormTextItem("規格", values["standard_spec"].orEmpty(), { onValueChange("standard_spec", it) }, placeholder = "規格を入力") },
        )
        Row(
            {
                MaterialFormSelectItem(
                    "用途",
                    values["usegae"].orEmpty(),
                    { onValueChange("usegae", it) },
                    listOf("" to "選択してください") + listOf("生産用", "試作用", "支給用", "その他").map { it to it },
                )
            },
            {
                MaterialFormTextItem(
                    "代表品種",
                    values["representative_model"].orEmpty(),
                    { onValueChange("representative_model", it) },
                    placeholder = "代表品種を入力",
                )
            },
        )
    }
}

@Composable
private fun MaterialFormSpecsTab(values: Map<String, String>, onValueChange: (String, String) -> Unit) {
    MaterialFormTwoColLayout {
        Row(
            {
                MaterialFormSelectItem(
                    "単位",
                    values["unit"].orEmpty(),
                    { onValueChange("unit", it) },
                    listOf("" to "選択してください", "kg" to "kg", "本" to "本", "m" to "m", "枚" to "枚", "個" to "個", "セット" to "セット"),
                )
            },
            { MaterialFormNumberItem("直径（mm）", values["diameter"].orEmpty(), { onValueChange("diameter", it) }, decimals = 2, step = 0.1) },
        )
        Row(
            { MaterialFormNumberItem("厚さ（mm）", values["thickness"].orEmpty(), { onValueChange("thickness", it) }, decimals = 3, step = 0.01) },
            { MaterialFormNumberItem("長さ（mm）", values["length"].orEmpty(), { onValueChange("length", it) }, decimals = 0, step = 1.0) },
        )
        Row(
            { MaterialFormNumberItem("束本数", values["pieces_per_bundle"].orEmpty(), { onValueChange("pieces_per_bundle", it) }, decimals = 0, step = 1.0) },
            { MaterialFormNumberItem("長尺単重（kg/本）", values["long_weight"].orEmpty(), { onValueChange("long_weight", it) }, decimals = 5, step = 0.001) },
        )
    }
}

@Composable
private fun MaterialFormPurchaseTab(
    values: Map<String, String>,
    supplierOptions: List<Pair<String, String>>,
    onValueChange: (String, String) -> Unit,
) {
    MaterialFormTwoColLayout {
        Row(
            {
                MaterialFormSelectItem(
                    "支給区分",
                    values["supply_classification"].orEmpty(),
                    { onValueChange("supply_classification", it) },
                    listOf("" to "選択してください", "自給" to "自給", "有償" to "有償", "無償" to "無償"),
                )
            },
            {
                MaterialFormSearchableSelectItem(
                    "仕入先CD",
                    values["supplier_cd"].orEmpty(),
                    { onValueChange("supplier_cd", it) },
                    listOf("" to "選択してください") + supplierOptions.map { (cd, name) -> cd to "$cd｜$name" },
                )
            },
        )
        Row(
            { MaterialFormNumberItem("単重単価（円/kg）", values["unit_price"].orEmpty(), { onValueChange("unit_price", it) }, decimals = 2, step = 0.01) },
            { MaterialFormNumberItem("一本単価（円）", values["single_price"].orEmpty(), { onValueChange("single_price", it) }, decimals = 2, step = 0.01) },
        )
        Row(
            { MaterialFormNumberItem("安全在庫", values["safety_stock"].orEmpty(), { onValueChange("safety_stock", it) }, decimals = 0, step = 1.0) },
            { MaterialFormNumberItem("リードタイム（日）", values["lead_time"].orEmpty(), { onValueChange("lead_time", it) }, decimals = 0, step = 1.0) },
        )
        Row(
            {
                MaterialFormTextItem(
                    "保管場所",
                    values["storage_location"].orEmpty(),
                    { onValueChange("storage_location", it) },
                    placeholder = "保管場所を入力",
                )
            },
            { MaterialFormStatusSwitch(values["status"].orEmpty(), { onValueChange("status", it) }) },
        )
    }
}

@Composable
private fun MaterialFormToleranceTab(values: Map<String, String>, onValueChange: (String, String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MaterialFormToleranceRow(
            {
                MaterialFormTextItem("公差範囲", values["tolerance_range"].orEmpty(), { onValueChange("tolerance_range", it) }, placeholder = "公差範囲を入力")
            },
            {
                MaterialFormNumberItem("公差１", values["tolerance_1"].orEmpty(), { onValueChange("tolerance_1", it) }, decimals = 2, step = 0.01)
            },
            {
                MaterialFormNumberItem("公差２", values["tolerance_2"].orEmpty(), { onValueChange("tolerance_2", it) }, decimals = 2, step = 0.01)
            },
        )
        MaterialFormToleranceRow(
            {
                MaterialFormTextItem("範囲", values["range_value"].orEmpty(), { onValueChange("range_value", it) }, placeholder = "範囲を入力")
            },
            {
                MaterialFormNumberItem("最小値", values["min_value"].orEmpty(), { onValueChange("min_value", it) }, decimals = 2, step = 0.01)
            },
            {
                MaterialFormNumberItem("最大値", values["max_value"].orEmpty(), { onValueChange("max_value", it) }, decimals = 2, step = 0.01)
            },
        )
        MaterialFormToleranceRow(
            {
                MaterialFormNumberItem("実力値１", values["actual_value_1"].orEmpty(), { onValueChange("actual_value_1", it) }, decimals = 3, step = 0.001)
            },
            {
                MaterialFormNumberItem("実力値２", values["actual_value_2"].orEmpty(), { onValueChange("actual_value_2", it) }, decimals = 3, step = 0.001)
            },
            {
                MaterialFormNumberItem("実力値３", values["actual_value_3"].orEmpty(), { onValueChange("actual_value_3", it) }, decimals = 3, step = 0.001)
            },
        )
    }
}

@Composable
private fun MaterialFormNoteTab(values: Map<String, String>, onValueChange: (String, String) -> Unit) {
    val accent = LocalMaterialFormAccent.current
    MaterialFormLabeledField("備考") {
        BasicTextField(
            value = values["note"].orEmpty(),
            onValueChange = { onValueChange("note", it) },
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .shadow(3.dp, RoundedCornerShape(8.dp), spotColor = accent.copy(alpha = 0.15f))
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(1.dp, accent.copy(alpha = 0.22f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 13.sp,
                color = Color(0xFF334155),
                lineHeight = 20.sp,
            ),
            decorationBox = { inner ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (values["note"].isNullOrBlank()) {
                        Text(
                            "備考を入力してください",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8),
                        )
                    }
                    inner()
                }
            },
        )
    }
}

@Composable
private fun MaterialFormStatusSwitch(status: String, onChange: (String) -> Unit) {
    val active = status != "0"
    MaterialFormLabeledField("状態") {
        Row(
            modifier = Modifier.fillMaxWidth().height(materialControlHeight),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Switch(
                checked = active,
                onCheckedChange = { onChange(if (it) "1" else "0") },
                colors = SwitchDefaults.colors(
                    checkedTrackColor = materialPrimary,
                    checkedThumbColor = Color.White,
                ),
            )
            Spacer(Modifier.width(6.dp))
            Text(if (active) "有効" else "無効", fontSize = 12.sp, color = Color(0xFF475569))
        }
    }
}

@Composable
private fun MaterialFormTwoColLayout(content: @Composable MaterialFormRowScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        MaterialFormRowScope().content()
    }
}

@Composable
private fun MaterialFormToleranceRow(
    first: @Composable () -> Unit,
    second: @Composable () -> Unit,
    third: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(Modifier.weight(1f)) { first() }
        Box(Modifier.weight(1f)) { second() }
        Box(Modifier.weight(1f)) { third() }
    }
}

private class MaterialFormRowScope {
    @Composable
    fun Row(left: @Composable () -> Unit, right: @Composable () -> Unit) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(Modifier.weight(1f)) { left() }
            Box(Modifier.weight(1f)) { right() }
        }
    }
}

@Composable
private fun MaterialFormTextItem(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    required: Boolean = false,
    enabled: Boolean = true,
    placeholder: String = "",
) {
    MaterialFormLabeledField(label, required) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            modifier = materialFormInputModifier(enabled),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 12.sp,
                color = if (enabled) Color(0xFF334155) else Color(0xFF94A3B8),
            ),
            decorationBox = { inner ->
                Box(
                    modifier = Modifier.fillMaxWidth().height(materialControlHeight),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(placeholder, fontSize = 11.sp, color = Color(0xFF94A3B8), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    inner()
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaterialFormSelectItem(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    options: List<Pair<String, String>>,
    menuMatchAnchorWidth: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    val display = options.find { it.first == value }?.second ?: "選択してください"
    MaterialFormLabeledField(label) {
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(materialControlHeight)
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LocalMaterialFormAccent.current.copy(alpha = 0.28f)),
                color = Color.White,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        display,
                        fontSize = 12.sp,
                        color = if (value.isBlank()) Color(0xFF94A3B8) else Color(0xFF334155),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
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
                                modifier = if (menuMatchAnchorWidth) Modifier else Modifier.widthIn(min = 240.dp),
                            )
                        },
                        onClick = { onChange(v); expanded = false },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaterialFormSearchableSelectItem(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    options: List<Pair<String, String>>,
) {
    var expanded by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }
    val display = options.find { it.first == value }?.second ?: "選択してください"
    val filtered = remember(search, options) {
        if (search.isBlank()) options
        else {
            val q = search.lowercase()
            options.filter { (_, text) -> text.lowercase().contains(q) }
        }
    }
    MaterialFormLabeledField(label) {
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it; if (!it) search = "" }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(materialControlHeight)
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LocalMaterialFormAccent.current.copy(alpha = 0.28f)),
                color = Color.White,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        display,
                        fontSize = 12.sp,
                        color = if (value.isBlank()) Color(0xFF94A3B8) else Color(0xFF334155),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                }
            }
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false; search = "" },
                modifier = Modifier.widthIn(min = 280.dp),
            ) {
                DropdownMenuItem(
                    text = {
                        BasicTextField(
                            value = search,
                            onValueChange = { search = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                            decorationBox = { inner ->
                                Box {
                                    if (search.isEmpty()) {
                                        Text("検索...", fontSize = 12.sp, color = Color(0xFF94A3B8))
                                    }
                                    inner()
                                }
                            },
                        )
                    },
                    onClick = {},
                    enabled = false,
                )
                filtered.forEach { (v, labelText) ->
                    DropdownMenuItem(
                        text = { Text(labelText, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis) },
                        onClick = { onChange(v); expanded = false; search = "" },
                    )
                }
            }
        }
    }
}

@Composable
private fun MaterialFormNumberItem(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    decimals: Int = 0,
    min: Double = 0.0,
    step: Double = if (decimals > 0) 0.01 else 1.0,
) {
    MaterialFormLabeledField(label) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            OutlinedButton(
                onClick = {
                    val current = value.toDoubleOrNull() ?: min
                    onValueChange(formatMaterialNumber(max(min, current - step), decimals))
                },
                modifier = Modifier.size(30.dp),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LocalMaterialFormAccent.current.copy(alpha = 0.25f)),
            ) {
                Icon(Icons.Default.Remove, contentDescription = "減", modifier = Modifier.size(14.dp), tint = LocalMaterialFormAccent.current)
            }
            BasicTextField(
                value = value,
                onValueChange = { raw ->
                    if (raw.isEmpty() || raw.toDoubleOrNull() != null) onValueChange(raw)
                },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .height(materialControlHeight)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .border(1.dp, LocalMaterialFormAccent.current.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 12.sp,
                    color = Color(0xFF334155),
                    textAlign = TextAlign.End,
                ),
                decorationBox = { inner ->
                    Box(
                        modifier = Modifier.fillMaxWidth().height(materialControlHeight),
                        contentAlignment = Alignment.CenterEnd,
                    ) { inner() }
                },
            )
            OutlinedButton(
                onClick = {
                    val current = value.toDoubleOrNull() ?: min
                    onValueChange(formatMaterialNumber(current + step, decimals))
                },
                modifier = Modifier.size(30.dp),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LocalMaterialFormAccent.current.copy(alpha = 0.25f)),
            ) {
                Icon(Icons.Default.Add, contentDescription = "増", modifier = Modifier.size(14.dp), tint = LocalMaterialFormAccent.current)
            }
        }
    }
}

@Composable
private fun MaterialFormLabeledField(label: String, required: Boolean = false, content: @Composable () -> Unit) {
    val accent = LocalMaterialFormAccent.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontSize = 12.sp, color = Color(0xFF2C3E50), fontWeight = FontWeight.Medium)
            if (required) Text(" *", fontSize = 12.sp, color = Color(0xFFE74C3C), fontWeight = FontWeight.Bold)
        }
        content()
    }
}

@Composable
private fun materialFormInputModifier(enabled: Boolean): Modifier {
    val accent = LocalMaterialFormAccent.current
    return Modifier
        .fillMaxWidth()
        .height(materialControlHeight)
        .clip(RoundedCornerShape(8.dp))
        .background(if (enabled) Color.White else Color(0xFFF5F7FA))
        .border(1.dp, if (enabled) accent.copy(alpha = 0.28f) else Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
        .padding(horizontal = 10.dp)
}

private fun formatMaterialNumber(value: Double, decimals: Int): String {
    if (decimals <= 0) return max(0, value.roundToInt()).toString()
    return String.format("%.${decimals}f", value)
}
