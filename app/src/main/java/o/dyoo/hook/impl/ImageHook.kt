package o.dyoo.hook.impl

import android.widget.Toast
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.param.PackageParam
import o.dyoo.core.config.ModuleConfig
import o.dyoo.core.download.Downloader

/**
 * 图片下载 Hook
 * Hook ImageView 的 setImageURI 来捕获图片 URL
 */
object ImageHook {

    var lastImageUrl: String? = null

    fun setup(param: PackageParam) {
        if (!ModuleConfig.isImageDownloadEnabled) return

        param.apply {
            hookImageView()
        }
    }

    private fun PackageParam.hookImageView() {
        try {
            android.widget.ImageView::class.java.hook {
                injectMember {
                    method {
                        name = "setImageURI"
                        paramCount = 1
                    }
                    before {
                        val uri = args[0] as? android.net.Uri
                        val url = uri?.toString()
                        if (!url.isNullOrEmpty() && url.startsWith("http")) {
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

    fun saveCurrentImage(context: android.content.Context) {
        val url = lastImageUrl
        if (url.isNullOrEmpty()) {
            Toast.makeText(context, "未捕获到图片链接", Toast.LENGTH_SHORT).show()
            return
        }
        Downloader.downloadImage(url, context)
    }
}
