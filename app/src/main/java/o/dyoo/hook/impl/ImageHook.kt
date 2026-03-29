package o.dyoo.hook.impl

import android.widget.Toast
import com.highcapable.yukihookapi.hook.param.PackageParam
import o.dyoo.core.config.ModuleConfig
import o.dyoo.core.download.Downloader

object ImageHook {
    var lastImageUrl: String? = null

    fun setup(param: PackageParam) {
        if (!ModuleConfig.isImageDownloadEnabled) return
        param.apply {
            android.widget.ImageView::class.java.hook {
                injectMember {
                    method { name = "setImageURI"; paramCount = 1 }
                    beforeHook {
                        val uri = args[0] as? android.net.Uri
                        uri?.toString()?.let { url ->
                            if (url.startsWith("http")) lastImageUrl = url
                        }
                    }
                }
            }
        }
    }

    fun saveCurrentImage(context: android.content.Context) {
        lastImageUrl?.let { Downloader.downloadImage(it, context) }
            ?: Toast.makeText(context, "未捕获到图片链接", Toast.LENGTH_SHORT).show()
    }
}
