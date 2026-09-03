# Kami Paperbox (Compose Multiplatform 跨平台版)

本项目是原 **Kami-paperbox** 的 **Desktop-First (桌面优先)** 现代化跨平台重构版本，基于 **Kotlin + Compose Multiplatform (CMP)** 构建。

一套代码库统一支持 **Desktop (Windows, macOS, Linux)**、**Mobile (iOS, Android)** 以及 **Web**。

---

## 核心架构与技术选型

- **UI 框架**: Compose Multiplatform (Jetpack Compose for Desktop / Android / iOS)
- **图形渲染**: Skia / DirectX / Metal 硬件级 GPU 加速
- **异步与状态管理**: Kotlin Coroutines + StateFlow 响应式驱动
- **网络引擎**: Ktor Client (跨平台全异步 HTTP) + ContentNegotiation (Json)
- **图片与缓存**: Coil 3 (全平台支持内存/磁盘二级缓存)
- **本地画匣 (Vault)**: 本地持久化与状态归档引擎

---

## 包含的核心功能

1. **卷轴浏览 (Browse)**:
   - 大屏自适应多列瀑布流 (`LazyVerticalStaggeredGrid`)，在桌面端自适应 3~6 列，移动端 2 列；
   - 自动按图片实际宽高比排版，防止跳变。
2. **图谱检索 (Search)**:
   - 支持多源站点切换（Safebooru / Danbooru / Yande）；
   - 支持标签聚合过滤与精准检索。
3. **本地画匣 (Vault)**:
   - 收藏管理，本地离线归档，快速查看与筛选。
4. **浏览足迹 (History)**:
   - 自动记录查阅过的画卷，支持一键清空与回溯。
5. **交互灯箱 (Lightbox)**:
   - 原生支持平移与双指/手势平滑缩放查看原图；
   - 浮层快捷归入/移出画匣。
6. **纸谱和风主题 (Theming)**:
   - 完整移植原版的 **和纸 (Washi)**、**青墨 (Aosumi)**、**朱砂 (Shusha)** 多套主题色彩。

---

## 编译与运行方式

### 1. 桌面端 (Desktop - Windows / macOS / Linux) 🚀 **(主要平台)**
在终端执行以下 Gradle 命令即可直接启动桌面应用：
```bash
./gradlew :composeApp:run
```
打包桌面原生安装包（.exe / .dmg / .deb）：
```bash
./gradlew :composeApp:packageDistributionForCurrentOS
```

### 2. Android 移动端
使用 Android Studio 打开本项目根目录，直接连接设备运行 `:composeApp`，或执行：
```bash
./gradlew :composeApp:assembleDebug
```

### 3. iOS 移动端
打开生成的 Xcode 工程或通过 Kotlin Multiplatform Mobile 插件运行至 iOS 模拟器/真机。
