package com.example.smart_emap.ui.mes.monitoring

enum class MonitorProcessKey(val slug: String, val pageTitle: String, val processLabel: String) {
    INSPECTION("inspection", "検査モニタ", "検査工程"),
    WELDING("welding", "溶接モニタ", "溶接工程"),
}
