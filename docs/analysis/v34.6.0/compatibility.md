# Dyoo 版本兼容性指南

## 抖音版本兼容性

| Dyoo 版本 | 兼容抖音版本 | 说明 |
|-----------|-------------|------|
| v1.0.0 | v30.0.0 - v34.6.0 | 初始版本 |
| v1.0.1 | v30.0.0 - v34.6.0 | Hook 点优化 |
| v1.0.2 | v30.0.0 - v34.6.0 | 新增清爽模式 |
| v1.0.3 | v30.0.0 - v34.6.0 | 运行时类搜索 |

---

## Hook 点兼容性

### 始终兼容 (系统 API)

| Hook 类 | 方法 | 兼容版本 |
|---------|------|---------|
| DownloadManager | enqueue() | v1.0+ |
| MediaPlayer | start(), pause(), stop() | v1.0+ |
| ImageView | setImageURI() | v1.0+ |
| Activity | onCreate(), onResume(), onPause() | v1.0+ |
| OkHttp | Request.Builder.url() | v3.0+ |

### 抖音内部类 (需要运行时搜索)

| 功能 | 最初类名 | 搜索方法 | 兼容版本 |
|------|---------|---------|---------|
| 视频下载辅助 | b | DouyinFinder.searchByField() | v30.0.0+ |
| 分享处理 | c | DouyinFinder.searchByMethod() | v30.0.0+ |
| 水印处理 | d | DouyinFinder.searchByField() | v30.0.0+ |
| Feed 数据 | e | DouyinFinder.searchByField() | v30.0.0+ |
| 播放器管理 | f | DouyinFinder.searchByField() | v30.0.0+ |

---

## 版本更新指南

### 检查清单

当抖音更新后，按以下步骤更新 Dyoo：

1. **收集信息**
   - 运行 Dyoo 模块查看日志
   - 确认类名是否变化
   - 检查方法签名是否变化

2. **更新 Hook 点**
   - 更新 HOOK_POINTS.md
   - 更新 obfuscated_classes.md
   - 提交新版本文件夹

3. **测试功能**
   - 测试视频下载
   - 测试图片下载
   - 测试去水印
   - 测试清爽模式
   - 测试悬浮窗

4. **发布新版本**
   - 更新 build.gradle.kts 版本号
   - 创建 GitHub Release
   - 更新文档

---

## 常见问题

### Q: 抖音更新后，模块还能用吗？
A: 系统 API Hook 点 (DownloadManager, MediaPlayer, ImageView, OkHttp) 始终兼容。
   内部类 Hook 点需要运行时重新搜索。

### Q: 如何检测抖音版本变化？
A: 运行 Dyoo 模块，查看日志输出。如果类名变化，日志会显示 "搜索失败"。

### Q: 如何手动更新？
A: 如果自动搜索失败，需要手动反编译抖音 APK，使用 jadx-gui 查找混淆类名。

---

## 版本对比

| 版本 | 变化 | 影响 |
|------|------|------|
| v30.0.0 | 无 | 无 |
| v31.0.0 | 无 | 无 |
| v32.0.0 | 无 | 无 |
| v33.0.0 | 水印参数变化 | watermark -> wm_aid |
| v34.0.0 | 分享类变化 | share.c -> share.d |
| v34.6.0 | 当前分析版本 | 无 |

---

**更新日期：** 2026-03-29
**维护者：** Dyoo 项目
**下次审查：** 抖音版本更新后
