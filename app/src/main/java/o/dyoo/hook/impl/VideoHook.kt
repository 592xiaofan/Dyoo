package o.dyoo.hook.impl

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import android.util.Log
import com.highcapable.yukihookapi.hook.param.PackageParam
import o.dyoo.core.config.ModuleConfig
import o.dyoo.core.download.Downloader

object VideoHook {
    var lastVideoUrl: String? = null

    fun setup(param: PackageParam) {
        if (!ModuleConfig.isVideoDownloadEnabled) return
        param.hookDownloadManager()
    }

    private fun PackageParam.hookDownloadManager() {
        try {
            android.app.DownloadManager::class.java.hook {
                injectMember {
                    method { name = "enqueue" }
                    beforeHook {
                        try {
                            val request = args[0] ?: return@beforeHook
                            val uriField = request.javaClass.getDeclaredField("mUri")
                            uriField.isAccessible = true
                            val uri = uriField.get(request) as? String
                            if (!uri.isNullOrEmpty() && uri.startsWith("http")) lastVideoUrl = uri
                        } catch (_: Throwable) {}
                    }
                }
            }
        } catch (e: Throwable) {
            Log.e("Dyoo", "Hook DownloadManager failed: ${e.message}")
        }
    }

    fun downloadCurrentVideo(context: Context) {
        lastVideoUrl?.let { Downloader.downloadVideo(it, context) }
            ?: Toast.makeText(context, "未捕获到视频链接", Toast.LENGTH_SHORT).show()
    }

    fun copyCurrentLink(context: Context) {
        lastVideoUrl?.let {
            (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                .setPrimaryClip(ClipData.newPlainText("Dyoo", it))
            Toast.makeText(context, "链接已复制", Toast.LENGTH_SHORT).show()
        } ?: Toast.makeText(context, "未捕获到视频链接", Toast.LENGTH_SHORT).show()
    }
}
