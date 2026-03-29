package o.dyoo.hook.impl

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import com.highcapable.yukihookapi.hook.factory.method
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
                paramCount = 1
            }.hook {
                before {
                    try {
                        val request = args[0]
                        val uriField = request.javaClass.getDeclaredField("mUri")
                        uriField.isAccessible = true
                        val uri = uriField.get(request) as? String
                        if (!uri.isNullOrEmpty() && uri.startsWith("http")) {
                            lastVideoUrl = uri
                        }
                    } catch (_: Throwable) {}
                }
            }
        } catch (_: Throwable) {}

        try {
            Class.forName("okhttp3.RealCall").method {
                name = "execute"
            }.hook {
                after {
                    val response = result ?: return@after
                    try {
                        @Suppress("UNCHECKED_CAST")
                        val r = response as Any
                        val requestMethod = r.javaClass.getDeclaredMethod("request")
                        val request = requestMethod.invoke(r) ?: return@after
                        val urlMethod = request.javaClass.getDeclaredMethod("url")
                        val url = urlMethod.invoke(request)?.toString() ?: return@after
                        if (url.contains("douyin") && (url.contains("video") || url.contains("play"))) {
                            lastVideoUrl = url
                        }
                    } catch (_: Throwable) {}
                }
            }
        } catch (_: Throwable) {}
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