package o.dyoo.hook.impl

import android.widget.Toast
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.param.PackageParam
import o.dyoo.core.config.ModuleConfig
import o.dyoo.core.download.Downloader

/**
 * 图片下载 Hook
 */
object ImageHook {

    var lastImageUrl: String? = null

    fun setup(param: PackageParam) {
        if (!ModuleConfig.isImageDownloadEnabled) return

        param.apply {
            try {
                android.widget.ImageView::class.java.method {
                    name = "setImageURI"
                    paramCount = 1
                }.hook {
                    before {
                        val uri = args[0] as? android.net.Uri
                        val url = uri?.toString()
                        if (!url.isNullOrEmpty() && url.startsWith("http")) {
                            lastImageUrl = url
                        }
                    }
                }
            } catch (_: Throwable) {}
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