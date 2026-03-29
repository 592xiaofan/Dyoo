package o.dyoo.hook.impl

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.highcapable.yukihookapi.hook.param.PackageParam
import o.dyoo.core.config.ModuleConfig

/**
 * 清爽模式 - 播放时隐藏所有 UI 组件，暂停时显示
 */
object CleanModeHook {
    private val handler = Handler(Looper.getMainLooper())
    private val hideRunnable = Runnable { hideAllUI() }
    private var isCleanMode = false
    private var lastPlayState = false // false=暂停, true=播放

    fun setup(param: PackageParam) {
        if (!ModuleConfig.isCleanModeEnabled) return
        param.apply {
            // Hook 抖音主 Activity 的触摸事件
            Activity::class.java.hook {
                injectMember {
                    method {
                        name = "onCreate"
                        paramCount = 1
                    }
                    afterHook {
                        val activity = instance as? Activity ?: return@afterHook
                        if (activity.packageName != "com.ss.android.ugc.aweme") return@afterHook
                        setupTouchListener(activity)
                    }
                }
            }
        }
    }

    /**
     * 在主界面设置触摸监听，通过定时器模拟播放/暂停切换
     * 播放时隐藏所有非视频 UI，暂停时显示
     */
    private fun setupTouchListener(activity: Activity) {
        val decorView = activity.window?.decorView ?: return
        decorView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (isCleanMode) {
                    // 清爽模式下触摸 → 显示 UI 3 秒
                    showAllUI()
                    handler.removeCallbacks(hideRunnable)
                    handler.postDelayed(hideRunnable, 3000)
                }
            }
            false // 不拦截
        }
    }

    /**
     * 启动清爽模式 - 隐藏所有 UI 组件
     */
    fun activateCleanMode() {
        isCleanMode = true
        hideAllUI()
    }

    /**
     * 停止清爽模式 - 显示所有 UI 组件
     */
    fun deactivateCleanMode() {
        isCleanMode = false
        handler.removeCallbacks(hideRunnable)
        showAllUI()
    }

    private fun hideAllUI() {
        isCleanMode = true
        // 通过遍历窗口隐藏非视频元素
        try {
            val activity = currentActivity() ?: return
            val root = activity.window?.decorView as? ViewGroup ?: return
            applyCleanMode(root, true)
        } catch (_: Throwable) {}
    }

    private fun showAllUI() {
        try {
            val activity = currentActivity() ?: return
            val root = activity.window?.decorView as? ViewGroup ?: return
            applyCleanMode(root, false)
        } catch (_: Throwable) {}
    }

    /**
     * 遍历视图树，隐藏/显示非视频组件
     * 视频组件通常 ID 包含 "video"，其他 UI 一律处理
     */
    private fun applyCleanMode(view: View, hide: Boolean) {
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i) ?: continue
                applyCleanMode(child, hide)
            }
        }
        // 根据 ID 名称判断是否为视频播放器
        val idName = try {
            view.resources?.getResourceEntryName(view.id) ?: ""
        } catch (_: Throwable) { "" }

        // 需要隐藏的组件关键字
        val shouldHide = listOf(
            "user", "nick", "name", "desc", "author",  // 用户信息
            "like", "heart", "favorite",                // 点赞
            "comment",                                  // 评论
            "share",                                    // 分享
            "caption", "subtitle",                      // 字幕
            "tag", "hash",                              // 标签
            "music", "music_icon",                      // 音乐
            "progress", "seekbar",                      // 进度条
            "action", "button", "icon",                 // 操作按钮
            "bottom", "tab",                            // 底部导航
            "feed_container", "recycler_view"           // 容器
        )

        // 视频播放器区域（不隐藏）
        val isVideoPlayer = idName.contains("video") ||
                idName.contains("player") ||
                idName.contains("surface") ||
                idName.contains("texture")

        if (!isVideoPlayer && shouldHide.any { idName.contains(it) }) {
            view.visibility = if (hide) View.INVISIBLE else View.VISIBLE
        }
    }

    private fun currentActivity(): Activity? {
        // 通过反射获取当前 Activity
        return try {
            val activityThread = Class.forName("android.app.ActivityThread")
            val method = activityThread.getMethod("currentActivity")
            method.invoke(null) as? Activity
        } catch (_: Throwable) { null }
    }
}
