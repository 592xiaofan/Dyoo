package o.dyoo.hook.impl

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.param.PackageParam
import o.dyoo.core.config.ModuleConfig
import o.dyoo.core.download.Downloader

/**
 * 视频下载 Hook
 * 通过 DexKit 搜索 + 反射拦截抖音视频 URL
 */
object VideoHook {

    var lastVideoUrl: String? = null

    fun setup(param: PackageParam) {
        if (!ModuleConfig.isVideoDownloadEnabled) return

        param.apply {
            hookVideoUrlField()
            hookDownloadManager()
        }
    }

    /**
     * Hook 视频数据模型类的 URL 获取
     * 抖音视频播放地址通过 play_addr 字段暴露
     * 我们拦截所有返回 List<String> 的方法来捕获 URL
     */
    private fun PackageParam.hookVideoUrlField() {
        val videoClassName = o.dyoo.hook.dexkit.DouyinFinder.videoUrlFieldClass ?: return

        try {
            // Hook 所有返回 java.util.List 的方法
            videoClassName.toClass().hook {
                injectMember {
                    allMethods {
                        returnType == List::class.java.name
                    }
                    after {
                        val list = result as? List<*>
                        if (list != null) {
                            for (item in list) {
                                val str = item as? String ?: continue
                                if (str.startsWith("http") && str.contains("douyin")) {
                                    lastVideoUrl = str
                                    YLog.info("Dyoo: Captured video URL from model")
                                    break
                                }
                            }
                        }
                    }
                }
            }
            YLog.info("Dyoo: Hooked video model class: $videoClassName")
        } catch (e: Throwable) {
            YLog.error("Dyoo: Hook video model failed: ${e.message}")
        }
    }

    /**
     * Hook 系统 DownloadManager
     * 抖音可能使用系统下载器来缓存视频
     */
    private fun PackageParam.hookDownloadManager() {
        try {
            android.app.DownloadManager::class.java.hook {
                injectMember {
                    method { name = "enqueue" }
                    before {
                        try {
                            val request = args[0]
                            val uriField = request.javaClass.getDeclaredField("mUri")
                            uriField.isAccessible = true
                            val uri = uriField.get(request) as? String
                            if (!uri.isNullOrEmpty() && uri.startsWith("http")) {
                                lastVideoUrl = uri
                                YLog.info("Dyoo: Captured URL from DownloadManager: $uri")
                            }
                        } catch (_: Throwable) {}
                    }
                }
            }
        } catch (e: Throwable) {
            YLog.error("Dyoo: Hook DownloadManager failed: ${e.message}")
        }
    }

    fun downloadCurrentVideo(context: Context) {
        val url = lastVideoUrl
        if (url.isNullOrEmpty()) {
            Toast.makeText(context, "未捕获到视频链接，请先播放视频", Toast.LENGTH_SHORT).show()
            return
        }
        Downloader.downloadVideo(url, context)
    }

    fun copyCurrentLink(context: Context) {
        val url = lastVideoUrl
        if (url.isNullOrEmpty()) {
            Toast.makeText(context, "未捕获到视频链接", Toast.LENGTH_SHORT).show()
            return
        }
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("Dyoo Video URL", url))
        Toast.makeText(context, "链接已复制", Toast.LENGTH_SHORT).show()
    }
}
