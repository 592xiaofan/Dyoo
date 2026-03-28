package o.dyoo.hook.dexkit

import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.param.PackageParam
import org.luckypray.dexkit.DexKitBridge

/**
 * 抖音类查找器
 * 使用 DexKit 1.x 在运行时搜索混淆后的目标类
 */
object DouyinFinder {

    var videoUrlFieldClass: String? = null
    var awemeModelClass: String? = null
    var feedAdapterClass: String? = null
    var imageActivityClass: String? = null

    private var initialized = false

    fun init(packageParam: PackageParam) {
        if (initialized) return

        try {
            val apkPath = packageParam.appInfo.sourceDir
            System.loadLibrary("dexkit")
            val bridge = DexKitBridge.create(apkPath)

            findVideoClasses(bridge)
            findAwemeModelClass(bridge)

            bridge.close()
            initialized = true

            YLog.info("Dyoo DexKit: Video=${videoUrlFieldClass}, Aweme=${awemeModelClass}")
        } catch (e: Throwable) {
            YLog.error("Dyoo DexKit init failed: ${e.message}")
        }
    }

    private fun findVideoClasses(bridge: DexKitBridge) {
        try {
            // 策略1: 搜索包含 play_addr 的类
            bridge.findClassUsingString(
                "play_addr",
                excludeStrings = false,
                excludeNumber = true,
                excludeNumberPrefix = true
            ).let { results ->
                if (results.isNotEmpty()) {
                    videoUrlFieldClass = results.first().descriptor
                    YLog.info("DexKit: Found video class: $videoUrlFieldClass")
                    return
                }
            }

            // 策略2: 搜索包含 url_list 的类
            bridge.findClassUsingString(
                "url_list",
                excludeStrings = false,
                excludeNumber = true,
                excludeNumberPrefix = true
            ).let { results ->
                if (results.isNotEmpty()) {
                    videoUrlFieldClass = results.first().descriptor
                    YLog.info("DexKit: Found video class (url_list): $videoUrlFieldClass")
                }
            }
        } catch (e: Throwable) {
            YLog.error("DexKit findVideoClasses error: ${e.message}")
        }
    }

    private fun findAwemeModelClass(bridge: DexKitBridge) {
        try {
            bridge.findClassUsingString(
                "aweme_id",
                excludeStrings = false,
                excludeNumber = true,
                excludeNumberPrefix = true
            ).let { results ->
                if (results.isNotEmpty()) {
                    awemeModelClass = results.first().descriptor
                    YLog.info("DexKit: Found Aweme model: $awemeModelClass")
                }
            }
        } catch (e: Throwable) {
            YLog.error("DexKit findAwemeModelClass error: ${e.message}")
        }
    }
}
