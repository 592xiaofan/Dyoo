package o.dyoo.hook.impl

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.highcapable.yukihookapi.hook.param.PackageParam
import o.dyoo.core.config.ModuleConfig
import o.dyoo.core.download.Downloader

/**
 * 视频下载 Hook
 * 
 * 策略：
 * 1. Hook DownloadManager.enqueue() 捕获视频下载请求的 URL (稳定 API)
 * 2. Hook okhttp3.Response 捕获包含视频播放地址的响应
 * 3. 通过 DouyinFinder 运行时搜索视频数据模型类的 getVideoUrl() 方法
 */
object VideoHook {
    private const val TAG = "Dyoo.VideoHook"
    var lastVideoUrl: String? = null

    fun setup(param: PackageParam) {
        if (!ModuleConfig.isVideoDownloadEnabled) return
        Log.i(TAG, "初始化视频下载 Hook")
        param.apply {
            hookDownloadManager()
            hookOkHttpForVideo()
        }
    }

    /**
     * 策略1: Hook DownloadManager - 系统级 API，完全稳定
     */
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
                            if (!uri.isNullOrEmpty() && uri.contains("douyin")) {
                                lastVideoUrl = uri
                                Log.d(TAG, "捕获视频URL: $uri")
                            }
                        } catch (_: Throwable) {}
                    }
                }
            }
            Log.i(TAG, "DownloadManager hook 成功")
        } catch (e: Throwable) {
            Log.e(TAG, "Hook DownloadManager 失败: ${e.message}")
        }
    }

    /**
     * 策略2: Hook OkHttp 拦截视频响应 - 网络层 API，稳定
     */
    private fun PackageParam.hookOkHttpForVideo() {
        try {
            "okhttp3.internal.connection.RealCall".toClass().hook {
                injectMember {
                    method { name = "execute" }
                    afterHook {
                        try {
                            val response = result
                            val requestField = response?.javaClass?.getDeclaredMethod("request")
                            val request = requestField?.invoke(response)
                            val urlMethod = request?.javaClass?.getMethod("url", String::class.java)
                            val url = urlMethod?.invoke(request) as? String
                            if (url != null && (url.contains(".mp4") || url.contains("video"))) {
                                lastVideoUrl = url
                                Log.d(TAG, "OkHttp捕获视频URL: $url")
                            }
                        } catch (_: Throwable) {}
                    }
                }
            }
            Log.i(TAG, "OkHttp hook 成功")
        } catch (e: Throwable) {
            Log.w(TAG, "OkHttp hook 失败 (非致命): ${e.message}")
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
