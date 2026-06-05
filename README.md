# Smart-EMAP Android

原生 Android 客户端（Kotlin + Jetpack Compose），对接桌面项目 **Smart-EMAPs** 的 FastAPI 后端。

```
Desktop/
├── Smart-EMAPs/     ← 后端 + Web 前端（共用 API）
└── SmartEMAP/       ← 本 Android 工程
```

## 当前已实现

- 登录页（用户名 / 密码 / 可配置服务器地址）
- JWT 持久化（DataStore）
- 调用 `POST /api/auth/login`、`GET /api/auth/me`、`POST /api/auth/logout`
- 登录后首页（显示用户信息与开发路线图）

## 环境要求

- Android Studio Ladybug 或更新版本
- JDK 17+
- Android SDK（compileSdk 36）
- 已运行的 Smart-EMAPs 后端（默认 `http://localhost:8005`）

## 快速开始

### 1. 启动后端

在 `Smart-EMAPs` 目录：

```bash
# Windows
start.bat

# 或
cd backend
uvicorn app.main:app --host 0.0.0.0 --port 8005
```

### 2. 用 Android Studio 打开

打开文件夹：`C:\Users\arai-235\Desktop\SmartEMAP`

同步 Gradle 后点击 **Run**。

### 3. 配置服务器地址

| 运行方式 | 服务器地址 |
|----------|------------|
| 开发模式（默认） | `https://192.168.1.62:5010/` |
| 生产 HTTPS | `https://your-domain.com` |

登录页可修改地址，会保存到本地。

## 项目结构

```
app/src/main/java/com/example/smart_emap/
├── MainActivity.kt
├── SmartEmapAppContainer.kt      # 依赖容器
├── core/
│   ├── auth/SessionStore.kt      # Token / 用户 / 服务器 URL
│   └── network/
│       ├── ApiClient.kt          # Retrofit 工厂
│       └── AuthInterceptor.kt    # Bearer JWT
├── data/
│   ├── api/AuthApiService.kt
│   ├── model/AuthDtos.kt
│   └── repository/AuthRepository.kt
└── ui/
    ├── auth/                     # 登录
    ├── home/                     # 首页
    └── navigation/               # 路由
```

## 与 Smart-EMAPs 的关系

| 层 | Smart-EMAPs | SmartEMAP Android |
|----|-------------|-------------------|
| 业务逻辑 | `backend/` FastAPI | **不实现**，只调 API |
| 数据库 | MySQL | 仅本地缓存（后续 Room） |
| UI | Vue + Element Plus | Jetpack Compose |
| 认证 | JWT Bearer | 相同 |

## 下一步开发

1. **导出 OpenAPI**（后端启动后）：
   ```powershell
   .\scripts\export-openapi.ps1
   ```
2. 用 OpenAPI Generator 生成 Kotlin Retrofit 接口
3. 从 `Smart-EMAPs/frontend/src/router/menuConfig.ts` 同步菜单
4. 按模块添加 Screen：ERP → MES → APS → FIN

## 命令行构建

```bash
cd SmartEMAP
.\gradlew.bat assembleDebug
```

APK 输出：`app/build/outputs/apk/debug/app-debug.apk`

## 注意事项

- 开发环境允许 HTTP（`network_security_config.xml`）；上线请改 HTTPS
- `minSdk = 26`（Android 8.0+）
- 后端需监听 `0.0.0.0`，真机才能访问
