package o.dyoo.hook.impl

import android.content.Context
import android.widget.Toast
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.param.PackageParam
import o.dyoo.core.config.ModuleConfig
import o.dyoo.core.download.Downloader

/**
 * 图片下载 Hook
 * 拦截抖音图片查看，捕获图片 URL
 */
object ImageHook {

    var lastImageUrl: String? = null

    fun setup(param: PackageParam) {
        if (!ModuleConfig.isImageDownloadEnabled) return

        param.apply {
            // Hook ImageView 的 setImageURI/setImageBitmap 来捕获图片 URL
            hookImageView()
        }
    }

    /**
     * Hook ImageView
     * 抖音图片查看器通常使用 ImageView 展示图片
     * 通过 Glide/Picasso 加载，我们可以 hook 加载过程
     */
    private fun PackageParam.hookImageView() {
        try {
            // Hook ImageView.setImageBitmap 来检测图片加载
            "android.widget.ImageView".toClass().hook {
                injectMember {
                    method {
                        name = "setImageURI"
                        param(android.net.Uri::class.java)
                    }
                    beforeHook {
                        val uri = args[0] as? android.net.Uri
                        val url = uri?.toString()
                        if (!url.isNullOrEmpty() && (url.contains("http") || url.contains("douyin"))) {
                            lastImageUrl = url
                            YLog.info("Dyoo: Captured image URL: $url")
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            YLog.error("Dyoo: Hook ImageView failed: ${e.message}")
        }
    }

    fun saveCurrentImage(context: Context) {
        val url = lastImageUrl
        if (url.isNullOrEmpty()) {
            Toast.makeText(context, "未捕获到图片链接", Toast.LENGTH_SHORT).show()
            return
        }
        Downloader.downloadImage(url, context)
    }
}
