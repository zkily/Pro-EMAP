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

### 6. 清除 Device Owner / 初期化できない場合

成为 Device Owner 后，App 会设置 `DISALLOW_FACTORY_RESET`，**設定の「初期化」が使えなくなります**。

#### 正确顺序

1. 端末管理 → PIN → **退出 Kiosk**（维护模式会暂时解除「禁止初期化」）
2. 再尝试清除 Owner，或直接在設定里执行初期化

```powershell
# 先退出 Lock Task / 维护模式后：
adb shell dpm remove-active-admin com.example.smart_emap/.admin.SmartEmapDeviceAdminReceiver
```

或代码 `DeviceOwnerController.clearDeviceOwner`（会先清用户限制再 clear）。

#### 仍无法初期化时

- `adb shell dpm list-owners`：若仍有 Owner，说明解除失败（Android 8+ 对正式 Provisioning 的 Owner，`clearDeviceOwnerApp` 经常无效）
- `adb shell dumpsys user | findstr restriction`：确认是否仍有 `no_factory_reset`
- **最终手段**：关机进 **Recovery / fastboot** 做 wipe（不依赖系统設定里的初期化）

文档注意：仅「退出 Kiosk」≠ 解除 Device Owner；Owner 还在时，重新开 Kiosk 会再次禁止初期化。
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

## QR Provisioning（Cloudflare Tunnel，无自签证书）

内网没有受信任证书时，用 **Cloudflare Quick Tunnel** 把本机 `apk-dist` 暴露成公网 HTTPS，再写进 QR。

### 前提

- 本机已安装 [cloudflared](https://developers.cloudflare.com/cloudflare-one/connections/connect-apps/install-and-setup/installation/)（可用 `winget install Cloudflare.cloudflared`）
- 本机已安装 Python（用于本地静态文件服务）
- 开通时平板 Wi‑Fi **能访问公网**（能打开 `*.trycloudflare.com`）
- App 已支持 Android 12+ 的 `GET_PROVISIONING_MODE` / `ADMIN_POLICY_COMPLIANCE`（`admin/GetProvisioningModeActivity`、`admin/AdminPolicyComplianceActivity`）
- 使用 **release** 签名 APK（不要 debug/`testOnly`）

### 步骤

1. 打 release 包，复制到 `apk-dist`：

```powershell
cd C:\Users\arai-235\Desktop\SmartEMAP
.\gradlew.bat assembleRelease
copy app\build\outputs\apk\release\app-release.apk apk-dist\smart-emap.apk
```

2. 启动隧道（保持窗口开启）：

```powershell
.\scripts\serve-apk-tunnel.ps1
```

成功后会打印类似：

```text
https://xxxx.trycloudflare.com/smart-emap.apk
```

并生成 `apk-dist/provisioning-qr.json`。

3. （推荐）再算签名 checksum：

```powershell
.\scripts\apk-checksums.ps1 -ApkPath apk-dist\smart-emap.apk
```

把输出的 `SIGNATURE_CHECKSUM` 补进 JSON（与 `PACKAGE_CHECKSUM` 一起更稳）。

4. 用任意「文本 → 二维码」工具，把 `provisioning-qr.json` **全文**做成 QR。

5. 平板：恢复出厂 → 欢迎页连点约 6 次 → 扫码。开通期间本机隧道不要关。

### QR JSON 示例

```json
{
  "android.app.extra.PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME": "com.example.smart_emap/com.example.smart_emap.admin.SmartEmapDeviceAdminReceiver",
  "android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_LOCATION": "https://xxxx.trycloudflare.com/smart-emap.apk",
  "android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_CHECKSUM": "<URL-safe-Base64-of-APK-SHA256>",
  "android.app.extra.PROVISIONING_LEAVE_ALL_SYSTEM_APPS_ENABLED": true,
  "android.app.extra.PROVISIONING_SKIP_ENCRYPTION": true
}
```

### 注意

- Quick Tunnel 的 `xxxx.trycloudflare.com` **每次启动都会变**，变了必须重做 QR。
- 大批量、长期部署建议改用 Cloudflare **Named Tunnel**（固定子域名）或自有域名证书。
- 浏览器先打开 APK 直链，确认能直接下载，再扫码。

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
