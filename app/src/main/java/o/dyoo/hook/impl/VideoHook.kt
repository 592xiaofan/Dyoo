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
 */
object VideoHook {

    var lastVideoUrl: String? = null

    fun setup(param: PackageParam) {
        if (!ModuleConfig.isVideoDownloadEnabled) return

        param.apply {
            hookDownloadManager()
        }
    }

    private fun PackageParam.hookDownloadManager() {
        try {
            android.app.DownloadManager::class.java.method {
                name = "enqueue"
            }.hook {
                before {
                    try {
                        val request = args[0]
                        val uriField = request.javaClass.getDeclaredField("mUri")
                        uriField.isAccessible = true
                        val uri = uriField.get(request) as? String
                        if (!uri.isNullOrEmpty() && uri.startsWith("http")) {
                            lastVideoUrl = uri
                            YLog.info("Dyoo: Captured URL: $uri")
                        }
                    } catch (_: Throwable) {}
                }
            }
            YLog.info("Dyoo: DownloadManager hook installed")
        } catch (e: Throwable) {
            YLog.error("Dyoo: Hook DownloadManager failed: ${e.message}")
        }

        // Hook OkHttpClient 以捕获视频请求
        try {
            "okhttp3.RealCall".toClass().method {
                name = "execute"
            }.hook {
                after {
                    val response = result
                    if (response != null) {
                        try {
                            val requestField = response.javaClass.getDeclaredMethod("request")
                            val request = requestField.invoke(response)
                            val urlMethod = request.javaClass.getDeclaredMethod("url")
                            val url = urlMethod.invoke(request).toString()
                            if (url.contains("douyin") && (url.contains("video") || url.contains("play"))) {
                                lastVideoUrl = url
                                YLog.info("Dyoo: Captured video URL: $url")
                            }
                        } catch (_: Throwable) {}
                    }
                }
            }
        } catch (e: Throwable) {
            YLog.error("Dyoo: Hook OkHttpClient failed: ${e.message}")
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
