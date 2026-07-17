# Smart-EMAP Device Owner / Kiosk 配置指南

本应用已内置 **Device Owner（デバイスオーナー）** 与 **Lock Task（キオスク）** 支持。

普通安装时功能不会生效；只有把本 App 设为 Device Owner 后，才会：

- 自动进入锁定任务模式（无法用 Home / 多任务退出）
- 禁用状态栏与锁屏
- 限制恢复出厂、添加用户、未知来源安装、USB 文件传输等
- 开机后自动启动 Smart-EMAP

---

## 架构

| 文件 | 作用 |
|------|------|
| `admin/SmartEmapDeviceAdminReceiver.kt` | Device Admin 接收器 |
| `admin/SmartEmapBootReceiver.kt` | 开机自动启动 |
| `core/deviceowner/DeviceOwnerController.kt` | 策略 / Lock Task 控制 |
| `core/deviceowner/KioskSettingsStore.kt` | PIN、Kiosk 开关 |
| `ui/deviceowner/KioskAdminDialog.kt` | 管理退出 / 改 PIN / 再锁定 |
| `res/xml/device_admin_receiver.xml` | 策略声明 |

默认管理 PIN：`2468`（部署后请立刻修改）

App 内入口：登录后右上角用户菜单 → **端末管理（Kiosk）**（仅 Device Owner 时显示）

---

## 开发机测试（ADB）

> 要求：设备无 Google 账户、未完成“已有用户配置”的状态；多数情况下需先 **恢复出厂设置**，跳过登录账户后再操作。

### 1. 安装 Debug 包

```powershell
cd C:\Users\arai-235\Desktop\SmartEMAP
.\gradlew.bat installDebug
```

### 2. 设为 Device Owner

```powershell
adb shell dpm set-device-owner com.example.smart_emap/.admin.SmartEmapDeviceAdminReceiver
```

成功输出示例：

```text
Success: Device owner set to package com.example.smart_emap
Active admin component: ...
```

### 3. 确认

```powershell
adb shell dpm list-owners
```

### 4. 启动 App

打开 Smart-EMAP，启动画面结束后应进入 Kiosk（Home 键无效）。

### 5. 退出维护（App 内）

1. 右上角用户菜单 → 端末管理（Kiosk）
2. 输入 PIN（默认 `2468`）
3. 点 **退出 Kiosk**
4. 可同时填写新 PIN 后更新

### 6. 清除 Device Owner（仅调试）

```powershell
adb shell dpm remove-active-admin com.example.smart_emap/.admin.SmartEmapDeviceAdminReceiver
```

或在代码中调用 `DeviceOwnerController.clearDeviceOwner(context)`（需先退出 Lock Task）。

若清除失败，通常需要恢复出厂设置。

---

## 生产部署建议

正式产线平板请勿依赖 ADB，推荐：

1. **QR Code Provisioning**（Android Enterprise）
2. **Zero-touch Enrollment**
3. 企业 EMM / MDM（Intune、Workspace ONE 等）

Provisioning 时指定 DPC 组件：

```text
com.example.smart_emap/.admin.SmartEmapDeviceAdminReceiver
```

并指向你们的企业注册服务器（如适用）。

---

## 与沉浸式全屏的关系

| 能力 | 无 Device Owner | 有 Device Owner |
|------|-----------------|-----------------|
| 隐藏状态栏/导航栏 | ✅ `MainActivity` 沉浸式 | ✅ + `setStatusBarDisabled` |
| 禁止 Home / Overview | ❌ | ✅ Lock Task |
| 开机自启 | ❌ | ✅ BootReceiver |
| 禁止恢复出厂 | ❌ | ✅ UserRestriction |

---

## 注意事项

1. Device Owner **不能**在已登录 Google 账户的设备上直接设置。
2. 权限极高，只用于工厂专用平板，不要装到个人手机。
3. 修改 `applicationId` 后，`dpm set-device-owner` 的包名也必须同步修改。
4. 默认 PIN 务必在首批设备部署时通过管理对话框改掉。
