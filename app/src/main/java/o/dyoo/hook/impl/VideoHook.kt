package o.dyoo.hook.impl

import android.app.Activity
import android.content.Context
import android.widget.Toast
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.param.PackageParam
import o.dyoo.core.config.ModuleConfig
import o.dyoo.core.download.Downloader
import o.dyoo.hook.dexkit.DouyinFinder

/**
 * 视频下载 Hook
 * 拦截抖音视频数据，捕获无水印视频 URL
 */
object VideoHook {

    // 运行时捕获到的视频 URL
    var lastVideoUrl: String? = null
    var lastAwemeId: String? = null
    var lastAuthorName: String? = null

    fun setup(param: PackageParam) {
        if (!ModuleConfig.isVideoDownloadEnabled) return

        param.apply {
            // 策略1: Hook 视频数据模型的 getUrlList 或 play_addr
            hookVideoModel()

            // 策略2: Hook 下载管理器调用（拦截抖音自带下载）
            hookDownloadManager()
        }
    }

    /**
     * Hook 视频数据模型
     * 通过 DexKit 找到的视频类来 hook
     */
    private fun PackageParam.hookVideoModel() {
        val videoClass = DouyinFinder.videoUrlFieldClass ?: return

        try {
            // Hook 视频数据类的获取 URL 方法
            videoClass.toClass().hook {
                injectMember {
                    // 找所有返回 String 或 List<String> 的方法
                    allMethods {
                        returnType == "java.util.List" || returnType == "java.lang.String"
                    }
                    afterHook {
                        val result = this.result ?: return@afterHook

                        when (result) {
                            is List<*> -> {
                                val urls = result.filterIsInstance<String>()
                                if (urls.isNotEmpty() && urls.first().contains("http")) {
                                    lastVideoUrl = urls.first()
                                    YLog.info("Dyoo: Captured video URL: $lastVideoUrl")
                                }
                            }
                            is String -> {
                                if (result.contains("http") && result.contains("video")) {
                                    lastVideoUrl = result
                                    YLog.info("Dyoo: Captured URL string: $lastVideoUrl")
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            YLog.error("Dyoo: Hook video model failed: ${e.message}")
        }
    }

    /**
     * Hook DownloadManager
     * 拦截系统的下载请求，捕获抖音发起的下载
     */
    private fun PackageParam.hookDownloadManager() {
        try {
            "android.app.DownloadManager".toClass().hook {
                injectMember {
                    method {
                        name = "enqueue"
                    }
                    beforeHook {
                        val request = args[0]
                        try {
                            val uriField = request.javaClass.getDeclaredField("mUri")
                            uriField.isAccessible = true
                            val uri = uriField.get(request) as? String
                            if (!uri.isNullOrEmpty() && uri.contains("http")) {
                                lastVideoUrl = uri
                                YLog.info("Dyoo: Captured download URL: $uri")
                            }
                        } catch (_: Throwable) {
                            // 反射获取失败，尝试其他方式
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            YLog.error("Dyoo: Hook download manager failed: ${e.message}")
        }
    }

    /**
     * 提供下载视频的方法供 PopupHook 调用
     */
    fun downloadCurrentVideo(context: Context) {
        val url = lastVideoUrl
        if (url.isNullOrEmpty()) {
            Toast.makeText(context, "未捕获到视频链接，请先播放视频", Toast.LENGTH_SHORT).show()
            return
        }
        Downloader.downloadVideo(url, context)
    }

    /**
     * 复制当前视频链接
     */
    fun copyCurrentLink(context: Context) {
        val url = lastVideoUrl
        if (url.isNullOrEmpty()) {
            Toast.makeText(context, "未捕获到视频链接", Toast.LENGTH_SHORT).show()
            return
        }
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Dyoo Video URL", url))
        Toast.makeText(context, "链接已复制", Toast.LENGTH_SHORT).show()
    }
}
