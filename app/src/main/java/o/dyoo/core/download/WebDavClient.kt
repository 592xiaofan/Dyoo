package o.dyoo.core.download

import com.github.thegrizzlylabs.sardine.android.SardineFactory
import com.highcapable.yukihookapi.hook.log.YLog
import kotlinx.coroutines.*
import o.dyoo.core.config.ModuleConfig
import java.io.File

/**
 * WebDav 客户端
 * 下载文件后自动上传到 WebDav 服务器
 */
object WebDavClient {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun upload(file: File, callback: ((Boolean, String) -> Unit)? = null) {
        if (!ModuleConfig.isWebDavEnabled) {
            callback?.invoke(false, "WebDav 未启用")
            return
        }

        val serverUrl = ModuleConfig.webDavUrl
        val username = ModuleConfig.webDavUsername
        val password = ModuleConfig.webDavPassword

        if (serverUrl.isBlank() || username.isBlank()) {
            callback?.invoke(false, "WebDav 配置不完整")
            return
        }

        scope.launch {
            try {
                val sardine = SardineFactory.create(username, password)
                val baseUrl = if (serverUrl.endsWith("/")) serverUrl else "$serverUrl/"
                val remotePath = "${baseUrl}Dyoo/${file.name}"

                // 创建 Dyoo 目录
                if (!sardine.exists("${baseUrl}Dyoo/")) {
                    sardine.createDirectory("${baseUrl}Dyoo/")
                }

                sardine.put(remotePath, file.readBytes(), guessContentType(file.name))

                YLog.info("Dyoo WebDav: Uploaded ${file.name}")
                withContext(Dispatchers.Main) {
                    callback?.invoke(true, "上传成功")
                }

            } catch (e: Throwable) {
                YLog.error("Dyoo WebDav: Upload failed: ${e.message}")
                withContext(Dispatchers.Main) {
                    callback?.invoke(false, "上传失败: ${e.message}")
                }
            }
        }
    }

    /**
     * 测试 WebDav 连接
     */
    fun test(url: String, username: String, password: String, callback: (Boolean, String) -> Unit) {
        scope.launch {
            try {
                val sardine = SardineFactory.create(username, password)
                sardine.list(url)
                withContext(Dispatchers.Main) {
                    callback(true, "连接成功")
                }
            } catch (e: Throwable) {
                withContext(Dispatchers.Main) {
                    callback(false, "连接失败: ${e.message}")
                }
            }
        }
    }

    private fun guessContentType(fileName: String): String {
        return when {
            fileName.endsWith(".mp4", true) -> "video/mp4"
            fileName.endsWith(".jpg", true) || fileName.endsWith(".jpeg", true) -> "image/jpeg"
            fileName.endsWith(".png", true) -> "image/png"
            fileName.endsWith(".webp", true) -> "image/webp"
            else -> "application/octet-stream"
        }
    }
}
