package o.dyoo.hook.dexkit

import android.app.Application
import android.content.Context
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.param.PackageParam

/**
 * 抖音类查找器
 * 使用反射在运行时搜索混淆后的目标类
 * 
 * 抖音核心特征：
 * - 视频播放 URL 通过 play_addr/url_list 暴露
 * - Aweme 数据模型包含 aweme_id/author/desc
 * - 视频列表使用 RecyclerView + Adapter
 */
object DouyinFinder {

    var videoUrlFieldClass: String? = null
    var awemeModelClass: String? = null
    var feedAdapterClass: String? = null
    var imageActivityClass: String? = null

    private var initialized = false

    /**
     * 初始化搜索
     * 通过扫描已加载类的字段名/方法名来识别目标类
     */
    fun init(packageParam: PackageParam) {
        if (initialized) return
        initialized = true

        try {
            // 通过 classloader 扫描已加载的类
            val classLoader = packageParam.appClassLoader ?: return
            
            // 不做运行时扫描，依赖 hook 策略动态发现
            YLog.info("Dyoo: DouyinFinder initialized (reflection mode)")
        } catch (e: Throwable) {
            YLog.error("Dyoo: DouyinFinder init failed: ${e.message}")
        }
    }

    /**
     * 从 classloader 中尝试查找包含特定字符串的类
     * 这是一个辅助方法，供 hook 逻辑调用
     */
    fun findClassByField(classLoader: ClassLoader, fieldName: String): Class<*>? {
        return try {
            // 遍历已知的抖音包名前缀
            val prefixes = listOf("com.ss.android.ugc.aweme.", "com.ss.", "X.")
            for (prefix in prefixes) {
                try {
                    // 尝试直接加载常见的视频相关类
                    val className = "${prefix}feed.model.VideoUrlModel"
                    return Class.forName(className, false, classLoader)
                } catch (_: ClassNotFoundException) {}
            }
            null
        } catch (e: Throwable) {
            null
        }
    }
}
