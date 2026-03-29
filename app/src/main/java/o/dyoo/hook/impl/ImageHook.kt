package o.dyoo.hook.impl

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import com.highcapable.yukihookapi.hook.param.PackageParam
import o.dyoo.core.config.ModuleConfig
import o.dyoo.core.download.Downloader

/**
 * 图片下载 Hook
 *
 * 策略：
 * Hook ImageView.setImageURI(Uri) - Android 稳定 API
 * 抖音图片通过 Glide/自定义加载器传入 URI，Hook 捕获图片 URL
 */
object ImageHook {
    private const val TAG = "Dyoo.ImageHook"
    var lastImageUrl: String? = null

    fun setup(param: PackageParam) {
        if (!ModuleConfig.isImageDownloadEnabled) return
        Log.i(TAG, "初始化图片下载 Hook")
        param.apply {
            ImageView::class.java.hook {
                injectMember {
                    method { name = "setImageURI"; paramCount = 1 }
                    beforeHook {
                        val uri = args[0] as? Uri
                        uri?.toString()?.let { url ->
                            if (url.startsWith("http")) {
                                lastImageUrl = url
                                Log.d(TAG, "捕获图片URL: $url")
                            }
                        }
                    }
                }
            }
            Log.i(TAG, "图片 Hook 成功")
        }
    }

    fun saveCurrentImage(context: Context) {
        lastImageUrl?.let { Downloader.downloadImage(it, context) }
            ?: Toast.makeText(context, "未捕获到图片链接", Toast.LENGTH_SHORT).show()
    }
}
