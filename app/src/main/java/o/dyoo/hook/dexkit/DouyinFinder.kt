package o.dyoo.hook.dexkit

import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.param.PackageParam
import io.github.luckypray.dexkit.DexKitBridge

/**
 * 抖音类查找器
 * 使用 DexKit 在运行时搜索混淆后的目标类
 */
object DouyinFinder {

    // 查找到的类缓存
    var videoUrlFieldClass: String? = null
    var awemeModelClass: String? = null
    var feedAdapterClass: String? = null
    var imageActivityClass: String? = null
    var activityThreadClass: String? = "android.app.ActivityThread"

    private var initialized = false

    /**
     * 初始化 DexKitBridge 并搜索目标类
     * 在 onAppLifecycle 中调用
     */
    fun init(packageParam: PackageParam) {
        if (initialized) return

        try {
            val apkPath = packageParam.appInfo.sourceDir
            val bridge = DexKitBridge.create(apkPath)

            findVideoClasses(bridge)
            findAwemeModelClass(bridge)
            findFeedAdapterClass(bridge)
            findImageActivityClass(bridge)

            bridge.close()
            initialized = true

            YLog.info("Dyoo DexKit: Video=${videoUrlFieldClass}, Aweme=${awemeModelClass}, Feed=${feedAdapterClass}, Image=${imageActivityClass}")
        } catch (e: Throwable) {
            YLog.error("Dyoo DexKit init failed: ${e.message}")
        }
    }

    /**
     * 查找视频 URL 相关类
     * 抖音视频数据中包含 play_addr, url_list 等字段
     */
    private fun findVideoClasses(bridge: DexKitBridge) {
        try {
            // 策略1: 搜索包含 play_addr 的类
            val result1 = bridge.findClass {
                matcher {
                    usingStrings = listOf("play_addr", "url_list")
                }
            }
            if (result1.isNotEmpty()) {
                videoUrlFieldClass = result1.first().name
                YLog.info("DexKit: Found video class via play_addr: $videoUrlFieldClass")
                return
            }

            // 策略2: 搜索包含 bit_rate 的类
            val result2 = bridge.findClass {
                matcher {
                    usingStrings = listOf("bit_rate", "play_addr")
                }
            }
            if (result2.isNotEmpty()) {
                videoUrlFieldClass = result2.first().name
                YLog.info("DexKit: Found video class via bit_rate: $videoUrlFieldClass")
                return
            }

            // 策略3: 搜索包含 uri 和 url 的类（视频数据模型）
            val result3 = bridge.findClass {
                matcher {
                    usingStrings = listOf("uri", "url_list", "width")
                }
            }
            if (result3.isNotEmpty()) {
                videoUrlFieldClass = result3.first().name
                YLog.info("DexKit: Found video class via uri: $videoUrlFieldClass")
                return
            }

            YLog.warn("DexKit: Could not find video URL class")
        } catch (e: Throwable) {
            YLog.error("DexKit findVideoClasses error: ${e.message}")
        }
    }

    /**
     * 查找 Aweme 数据模型类
     * 抖音核心数据类，包含视频信息
     */
    private fun findAwemeModelClass(bridge: DexKitBridge) {
        try {
            val result = bridge.findClass {
                matcher {
                    usingStrings = listOf("aweme_id", "desc", "author")
                }
            }
            if (result.isNotEmpty()) {
                awemeModelClass = result.first().name
                YLog.info("DexKit: Found Aweme model: $awemeModelClass")
            }
        } catch (e: Throwable) {
            YLog.error("DexKit findAwemeModelClass error: ${e.message}")
        }
    }

    /**
     * 查找 Feed 列表 Adapter 类
     * 视频列表的 RecyclerView Adapter
     */
    private fun findFeedAdapterClass(bridge: DexKitBridge) {
        try {
            val result = bridge.findClass {
                matcher {
                    // Feed Adapter 通常有 getItemViewType 或 onViewRecycled
                    methods {
                        add {
                            paramCount = 2 // (position, type)
                        }
                    }
                    usingStrings = listOf("feed")
                }
            }
            if (result.isNotEmpty()) {
                feedAdapterClass = result.first().name
                YLog.info("DexKit: Found Feed adapter: $feedAdapterClass")
            }
        } catch (e: Throwable) {
            YLog.error("DexKit findFeedAdapterClass error: ${e.message}")
        }
    }

    /**
     * 查找图片查看 Activity
     */
    private fun findImageActivityClass(bridge: DexKitBridge) {
        try {
            val result = bridge.findClass {
                matcher {
                    superclass = "android.app.Activity"
                    usingStrings = listOf("image", "gallery", "photo")
                }
            }
            if (result.isNotEmpty()) {
                imageActivityClass = result.first().name
                YLog.info("DexKit: Found Image activity: $imageActivityClass")
            }
        } catch (e: Throwable) {
            YLog.error("DexKit findImageActivityClass error: ${e.message}")
        }
    }
}
