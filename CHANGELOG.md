# Changelog

All notable changes to **Kami Paperbox** will be documented in this file.

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
