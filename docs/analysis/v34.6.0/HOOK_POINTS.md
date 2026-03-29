# Dyoo Hook 点分析

**抖音版本：** v34.6.0
**分析日期：** 2026-03-29
**分析工具：** GitHub API + 开源项目对比 + 运行时测试
**分析来源：** javadevelopuser/DouyinHooker, Dyoo 复刻版, 运行时日志

---

## 📌 Hook 点总览

| # | 功能 | Hook 类 | Hook 方法 | 稳定性 | Hook 类型 |
|---|------|---------|-----------|--------|----------|
| 1 | 视频下载 | android.app.DownloadManager | enqueue() | ⭐⭐⭐⭐⭐ | 系统 API |
| 2 | 视频 URL 捕获 | okhttp3.internal.connection.RealCall | execute() | ⭐⭐⭐⭐⭐ | 网络层 |
| 3 | 图片保存 | android.widget.ImageView | setImageURI() | ⭐⭐⭐⭐⭐ | 系统 API |
| 4 | 去水印 | okhttp3.Request.Builder | url() | ⭐⭐⭐⭐⭐ | 网络层 |
| 5 | 分享解锁 | com.ss.android.ugc.aweme.share.c | share() | ⭐⭐ | 抖音内部 |
| 6 | 点赞自动下载 | com.ss.android.ugc.aweme.ui.a | handleDiggClick() | ⭐⭐ | 抖音内部 |
| 7 | 清爽模式播放 | android.media.MediaPlayer | start() | ⭐⭐⭐⭐⭐ | 系统 API |
| 8 | 清爽模式暂停 | android.media.MediaPlayer | pause()/stop() | ⭐⭐⭐⭐⭐ | 系统 API |
| 9 | 悬浮窗 | android.app.Activity | onResume()/onPause() | ⭐⭐⭐⭐⭐ | 系统 API |
| 10 | Feed 数据 | com.ss.android.ugc.aweme.feed.model.Aweme | 构造函数 | ⭐⭐⭐ | 抖音模型 |
| 11 | Feed 列表 | com.ss.android.ugc.aweme.feed.ui.FeedRecyclerFragment | onCreateView() | ⭐⭐⭐ | 抖音 UI |
| 12 | 视频详情 | com.ss.android.ugc.aweme.detail.ui.DetailActivity | onCreate() | ⭐⭐⭐ | 抖音 UI |
| 13 | 搜索功能 | com.ss.android.ugc.aweme.search.ui.SearchActivity | onCreate() | ⭐⭐⭐ | 抖音 UI |
| 14 | 评论功能 | com.ss.android.ugc.aweme.comment.ui.CommentActivity | onCreate() | ⭐⭐⭐ | 抖音 UI |
| 15 | 消息页面 | com.ss.android.ugc.aweme.message.ui.MessageActivity | onCreate() | ⭐⭐⭐ | 抖音 UI |

---

## 📌 详细分析

### 1. 视频下载
```
Hook 类: android.app.DownloadManager
Hook 方法: enqueue(DownloadManager.Request)
Hook 前: beforeHook
Hook 策略: 捕获 Request 对象中的 mUri 字段
稳定性: ⭐⭐⭐⭐⭐ (系统 API，永不混淆)
```

### 2. 视频 URL 捕获
```
Hook 类: okhttp3.internal.connection.RealCall
Hook 方法: execute()
Hook 前: afterHook
Hook 策略: 从 Response 对象获取 Request.url()
稳定性: ⭐⭐⭐⭐⭐ (OkHttp 公开 API)
```

### 3. 图片保存
```
Hook 类: android.widget.ImageView
Hook 方法: setImageURI(Uri)
Hook 前: beforeHook
Hook 策略: 捕获 Uri 参数中的图片 URL
稳定性: ⭐⭐⭐⭐⭐ (系统 API，永不混淆)
```

### 4. 去水印
```
Hook 类: okhttp3.Request.Builder
Hook 方法: url(String)
Hook 前: beforeHook
Hook 策略: 检测 URL 中的水印参数 (watermark, wm_aid, wm_tt)
处理: 移除水印参数后重新设置 URL
稳定性: ⭐⭐⭐⭐⭐ (OkHttp 公开 API)
```

### 5. 分享解锁
```
Hook 类: com.ss.android.ugc.aweme.share.c
Hook 方法: share(Aweme, String, Boolean)
Hook 前: beforeHook
Hook 策略: 修改 Boolean 参数为 true (解锁分享限制)
参数说明: Param[0]=Aweme对象, Param[1]=分享渠道, Param[2]=布尔值
稳定性: ⭐⭐ (抖音内部类，混淆后变化)
来源: javadevelopuser/DouyinHooker
```

### 6. 点赞自动下载
```
Hook 类: com.ss.android.ugc.aweme.ui.a
Hook 方法: handleDiggClick(Aweme)
Hook 前: beforeHook
Hook 策略: 在点赞操作时自动调用分享并下载视频
依赖: 需要先 hook 分享类
稳定性: ⭐⭐ (抖音内部类，混淆后变化)
来源: javadevelopuser/DouyinHooker
```

### 7. 清爽模式 - 播放
```
Hook 类: android.media.MediaPlayer
Hook 方法: start()
Hook 前: afterHook
Hook 策略: 检测到视频播放后隐藏所有非视频 UI 组件
稳定性: ⭐⭐⭐⭐⭐ (系统 API，永不混淆)
```

### 8. 清爽模式 - 暂停
```
Hook 类: android.media.MediaPlayer
Hook 方法: pause() / stop()
Hook 前: afterHook
Hook 策略: 检测到视频暂停/停止后显示所有 UI 组件
稳定性: ⭐⭐⭐⭐⭐ (系统 API，永不混淆)
```

### 9. 悬浮窗
```
Hook 类: android.app.Activity
Hook 方法: onResume() / onPause()
Hook 前: afterHook / beforeHook
Hook 策略: 在 Activity 生命周期中显示/隐藏悬浮窗
稳定性: ⭐⭐⭐⭐⭐ (系统 API，永不混淆)
```

### 10. Feed 数据
```
Hook 类: com.ss.android.ugc.aweme.feed.model.Aweme
Hook 策略: 搜索包含 "play_addr" 字段的类 (运行时发现)
稳定性: ⭐⭐⭐ (抖音模型类，混淆后需要重新搜索)
```

### 11. Feed 列表
```
Hook 类: com.ss.android.ugc.aweme.feed.ui.FeedRecyclerFragment
Hook 方法: onCreateView()
Hook 前: afterHook
Hook 策略: 在 Feed 列表创建时注入自定义 UI
稳定性: ⭐⭐⭐ (抖音 UI 类，混淆后需要重新搜索)
```

### 12. 视频详情页
```
Hook 类: com.ss.android.ugc.aweme.detail.ui.DetailActivity
Hook 方法: onCreate()
Hook 前: afterHook
Hook 策略: 在视频详情页创建时注入自定义 UI
稳定性: ⭐⭐⭐ (抖音 Activity 类，混淆后需要重新搜索)
```

### 13. 搜索页
```
Hook 类: com.ss.android.ugc.aweme.search.ui.SearchActivity
Hook 方法: onCreate()
Hook 策略: 在搜索页面创建时注入自定义 UI
稳定性: ⭐⭐ (抖音 UI 类，混淆后需要重新搜索)
```

### 14. 评论页
```
Hook 类: com.ss.android.ugc.aweme.comment.ui.CommentActivity
Hook 方法: onCreate()
Hook 策略: 在评论页面创建时注入自定义 UI
稳定性: ⭐⭐ (抖音 UI 类，混淆后需要重新搜索)
```

### 15. 消息页
```
Hook 类: com.ss.android.ugc.aweme.message.ui.MessageActivity
Hook 方法: onCreate()
Hook 策略: 在消息页面创建时注入自定义 UI
稳定性: ⭐⭐ (抖音 UI 类，混淆后需要重新搜索)
```

---

## 📌 抖音包结构 (v34.6.0)

```
com.ss.android.ugc.aweme
├── activity          - Activity 类
├── detail.ui         - 详情页面 UI
├── feed.model        - Feed 数据模型
├── feed.ui           - Feed 列表 UI
├── message.ui        - 消息页面 UI
├── player            - 播放器相关
├── search.ui         - 搜索页面 UI
├── share             - 分享功能
├── ui                - 通用 UI 组件
├── xlog              - 日志系统
└── comment.ui        - 评论页面 UI
```

---

## 📌 Hook 稳定性等级说明

| 等级 | 说明 | 适用场景 |
|------|------|---------|
| ⭐⭐⭐⭐⭐ | 系统 API | DownloadManager, MediaPlayer, ImageView, OkHttp |
| ⭐⭐⭐⭐ | 稳定库 API | OkHttp (公开方法) |
| ⭐⭐⭐ | 抖音内部类 (混淆后可能需要搜索) | Feed, Detail, Comment, Search |
| ⭐⭐ | 抖音内部方法 (混淆后可能需要搜索) | share, handleDiggClick |
| ⭐ | 极不稳定 (强烈不推荐) | 无 |

---

## 📌 版本兼容性说明

**兼容版本范围：** v30.0.0 - v34.6.0

| 版本范围 | Hook 点变化 | 说明 |
|---------|-------------|------|
| v30.0.0-v31.0.0 | 无 | 稳定 API |
| v31.0.0-v32.0.0 | 无 | 稳定 API |
| v32.0.0-v33.0.0 | 无 | 稳定 API |
| v33.0.0-v34.0.0 | 水印参数略有变化 | watermark -> wm_aid 系列 |
| v34.0.0-v34.6.0 | 分享类类名变化 | share.c -> share.d 系列 |

---

## 📌 与 Dyoo 复刻版对应关系

| Dyoo 功能 | 对应 Hook 点 | 实现方式 |
|-----------|-------------|---------|
| 视频下载 | #1, #2 | DownloadManager + OkHttp |
| 图片下载 | #3 | ImageView.setImageURI |
| 去水印 | #4 | okhttp3.Request.Builder.url |
| 分享解锁 | #5 | 分享类 share 方法 |
| 点赞下载 | #6 | 点赞类 handleDiggClick |
| 清爽模式 | #7, #8 | MediaPlayer.start/pause |
| 悬浮窗 | #9 | Activity onResume/onPause |

---

## 📌 未来版本对比指南

当抖音更新后，需要重新运行以下搜索：
1. DouyinFinder.searchByStaticField() - 搜索视频模型类
2. 查看日志输出确认类名是否变化
3. 对比本文件夹中记录的 Hook 点
4. 更新对应版本的文件夹

---

## 📌 参考项目

| 项目 | GitHub | Stars |
|------|--------|-------|
| DouyinHooker | javadevelopuser/DouyinHooker | 106 |
| douyin | leslie10150/douyin | 52 |
| DouyinHook | Lemniscate317/DouyinHook | 5 |

---

**分析完成：** 2026-03-29
**分析工具：** GitHub API + 开源项目对比 + Dyoo 复刻版验证
**下次更新：** 抖音版本更新后
