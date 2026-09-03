# Kami Paperbox (纸匣) — Compose Multiplatform Edition

> ✨ **AI Powered by Google Gemini**

本项目是原项目 [Atsukiizumi/Kami-paperbox](https://github.com/Atsukiizumi/Kami-paperbox) 的 **Desktop-First (桌面优先)** 现代化跨平台重构版本，基于 **Kotlin + Compose Multiplatform (CMP)** 构建。

前作项目地址: [https://github.com/Atsukiizumi/Kami-paperbox](https://github.com/Atsukiizumi/Kami-paperbox)

一套单一核心代码库，深度优化并覆盖 **Desktop (Windows, macOS, Linux)**，后续兼容 **Mobile (iOS, Android)** 与 **Web**。

---

## 核心特性

- **卷轴浏览 (Browse)**:
  - 桌面大屏自适应 3~6 列瀑布流排版 (`LazyVerticalStaggeredGrid`)；
  - 根据每张图真实宽高比动态布局，彻底消除卡片跳变。
- **图谱检索 (Search)**:
  - 支持标签搜索、多图源切换与智能标签过滤（如 AI 标记折叠）。
- **本地画匣 (Vault)**:
  - 本地离线收藏管理系统，支持作品本地归档、去重与元数据快速检索。
- **浏览足迹 (History)**:
  - 自动记录查阅记录，支持一键清空与快速回溯。
- **交互灯箱 (Lightbox)**:
  - 沉浸式大图查看，原生支持鼠标滚轮 / 触摸板手势平滑缩放、自由平移拖拽，支持一键归入画匣。
- **纸谱和风主题 (Theming)**:
  - 原汁原味还原原版的 **和纸 (Washi)**、**青墨 (Aosumi)**、**朱砂 (Shusha)** 和风配色方案。

---

## 技术架构

- **UI 框架**: Compose Multiplatform (Jetpack Compose for Desktop)
- **底层图形渲染**: Skia (DirectX / Metal / Vulkan 硬件 GPU 加速)
- **异步与状态管理**: Kotlin Coroutines + StateFlow
- **跨平台网络引擎**: Ktor Client (异步网络通信) + Kotlinx.serialization (JSON)
- **图像管线**: Coil 3 (全平台支持内存/磁盘二级缓存)
- **自动化流水线**: GitHub Actions 矩阵多系统（Windows, macOS, Linux）自动构建与发布

---

## 下载测试版本 (Pre-built Binaries)

每次代码提交都会触发 CI 自动构建。您可以在 GitHub 仓库的 **Actions** 页面进入最新的构建任务，在底部的 **Artifacts** 处直接下载各平台测试安装包：

- **Windows**: `KamiPaperbox-Desktop-Windows` (`.msi` 安装包)
- **macOS**: `KamiPaperbox-Desktop-macOS` (`.dmg` 安装包)
- **Linux**: `KamiPaperbox-Desktop-Linux` (`.deb` 安装包)

当推送以 `v*` 开头的 tag（如 `v1.0.0-beta.1`）时，GitHub Actions 会自动打包并发布在 [Releases](https://github.com/Atsukiizumi/Kami-paperbox/releases) 页面。

---

## 本地编译与运行

### 运行桌面端 (开发模式)
```bash
./gradlew :composeApp:run
```

### 打包当前系统的独立安装包
```bash
./gradlew :composeApp:packageDistributionForCurrentOS
```
生成的安装包将存放在 `composeApp/build/compose/binaries/main/` 目录下。

---

## 致谢与参考

- 原项目：[Atsukiizumi/Kami-paperbox](https://github.com/Atsukiizumi/Kami-paperbox)
- AI 协作：**AI Powered by Google Gemini**
