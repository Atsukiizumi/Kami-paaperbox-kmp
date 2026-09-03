# Changelog

All notable changes to **Kami Paperbox** will be documented in this file.

## [v1.0.0-beta.2] - 2026-09-03

### 🌸 原项目全功能深度复刻 (Complete Feature Parity)
- **Fanbox 创作者生态全流程对接 (`fetchFanbox`)**:
  - 对接 Fanbox 创作者博文接口，支持按创作者 ID（如 `c-row`, `mignon`, `morikuraen` 等）即时检索与精选轮播。
  - 解析博文内图集数据（包含 `images` 与 `imageMap` 文章混排），完整提取多图图集与赞助限定状态（`isRestricted` / `feeRequired`）。
  - 卡片集成「赞助限定」专属琥珀色勋章标识。
- **Pixiv 多图集翻页与双语标签译名**:
  - 完整支持 Pixiv 漫画与多图作品（`meta_pages`），在卡片右上角显示页数标牌（如 `5P`）。
  - 灯箱内置翻页控制条（`[<] 第 1 / N 页 [>]`），翻页时自动重置缩放与位移。
  - 标签双语翻译支持：关联原名与 `translated_name`，在灯箱中直观呈现中日双语胶囊。
  - 集成 `i.pixiv.re` 反盗链镜像加速方案，彻底解决官方图源 403 跨域防盗链拦截与加载超时问题。
- **多 Tag 空格分割检索与动态胶囊编辑**:
  - 检索栏全面支持标准多标签空格切分语法（例如 `1girl landscape original`）。
  - 检索框下方自动提取激活标签的独立胶囊，支持点击单个标签的关闭按钮直接从当前语句中剔除并自动重新检索。
  - 提供多源推荐热搜词与本地历史搜索词快速注入。
- **详情页标签与作者穿透联动**:
  - 在大图灯箱中点击任意标签，自动填入或追加到检索框、同步对应图源并即时触发全局搜索。
  - 点击作品作者名，精准触发以该作者为关键词或 `artist:` 过滤的穿透搜索。
- **内容安全与分级全局管控 (`SettingsView`)**:
  - **R18 敏感内容远端开关**: 远端向 Pixiv 与 Fanbox 传递 `include_restrict_safe` 与 `restrict` 分级参数，Booru 站点放开限制。
  - **瀑布流高斯模糊保护**: 对非受控或敏感作品在卷轴流中默认开启 22dp 高斯模糊保护，灯箱内正常呈现。
  - **三态 AI 作品过滤规则**:
    - `SHOW_ALL`：全部正常显示；
    - `BADGE_ONLY`：仅在作品左上角悬浮黑色 AI 标识勋章；
    - `HIDE_AI`：在 Booru API 请求阶段自动追加 `-ai_generated`，并在客户端全面剔除 AI 生成内容。
- **架构解耦与模块化重构**:
  - 将庞大的主页面拆分为清晰独立的组件架构：`ArtworkCard`、`BrowseView`、`SearchView`、`VaultView`、`HistoryView`、`SettingsView` 与 `LightboxView`，遵循单一职责规范。

### 📦 CI/CD 编译产物打包优化
- 修正 CI 流水线产物上传策略：独立原生安装包（Windows `.msi`、Linux `.deb`、Android `.apk`）保持直接单文件提供下载；仅免安装便携版（Windows Portable `.zip`、Linux Portable `.tar.gz`）打包为压缩包。

## [v1.0.0-beta.1] - 2026-09-03

### 🚀 Desktop-First Multiplatform Architecture Rewrite
- **Framework Migration**: Rewritten from React/Node.js to **Compose Multiplatform (CMP / KMP)** with a **Desktop-First** approach powered by Google Gemini.
- **Unified Engine**: Powered by Kotlin Multiplatform, targeting Desktop (Windows, macOS, Linux), Mobile (Android, iOS), and Web.
- **Graphics & Performance**: Native Skia hardware acceleration (DirectX / Metal / Vulkan) delivering 60+ FPS masonry scrolling.

### ✨ Core Features
- **Browse (卷轴浏览)**: Adaptive multi-column masonry grid (`LazyVerticalStaggeredGrid`) automatically scaling from 2 columns on mobile to 3-6 columns on ultra-wide desktop displays with aspect ratio preservation.
- **Search (图谱检索)**: Tag-based search, multi-source switching (Safebooru / Danbooru / Yande), and intelligent filtering.
- **Vault (本地画匣)**: Local-first offline artwork repository, persistent deduplication, and metadata caching.
- **History (浏览足迹)**: Automatic tracking of viewed artworks with quick recovery and clear actions.
- **Lightbox (交互灯箱)**: Full-screen original image lightbox with mouse-wheel & pinch-to-zoom, panning gestures, and one-click vaulting.
- **Theming (纸谱和风)**: Complete recreation of Japanese aesthetic themes:
  - **和纸 (Washi)** (Light & Dark)
  - **青墨 (Aosumi)** (Dark)
  - **朱砂 (Shusha)** (Dark)

### 🛠 CI/CD & Automation
- Added GitHub Actions workflow (`.github/workflows/build-artifacts.yml`) supporting:
  - **Continuous Testing Artifacts**: Automated build and upload of `.msi` (Windows), `.dmg` (macOS), and `.deb` (Linux) on every push and pull request.
  - **Automated Releases**: Automatic generation of GitHub Releases with standalone installers when pushing tags (e.g. `v1.0.0-beta.1`).
