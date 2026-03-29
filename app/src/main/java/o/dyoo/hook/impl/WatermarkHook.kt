package o.dyoo.hook.impl

import android.util.Log
import com.highcapable.yukihookapi.hook.param.PackageParam
import o.dyoo.core.config.ModuleConfig

/**
 * 去水印 Hook
 *
 * 策略：
 * Hook okhttp3.Request.Builder.url() - OkHttp 稳定 API
 * 拦截包含水印参数的 URL，移除水印标记
 * 水印参数: watermark, wm_aid, wm_tt 等
 */
object WatermarkHook {
    private const val TAG = "Dyoo.WatermarkHook"

    fun setup(param: PackageParam) {
        if (!ModuleConfig.isWatermarkRemoveEnabled) return
        Log.i(TAG, "初始化去水印 Hook")
        param.apply {
            hookUrlBuilder()
        }
    }

    /**
     * Hook OkHttp URL Builder - 拦截请求构建过程
     * 这是最可靠的去水印方案：在请求发出前修改 URL
     */
    private fun PackageParam.hookUrlBuilder() {
        try {
            "okhttp3.Request\$Builder".toClass().hook {
                injectMember {
                    method { name = "url"; paramCount = 1 }
                    beforeHook {
                        val arg = args[0]
                        if (arg is String) {
                            val modified = removeWatermarkParams(arg)
                            if (modified != arg) {
                                args[0] = modified
                                Log.d(TAG, "去除水印: $arg -> $modified")
                            }
                        }
                    }
                }
            }
            Log.i(TAG, "去水印 Hook 成功")
        } catch (e: Throwable) {
            Log.e(TAG, "去水印 Hook 失败: ${e.message}")
        }
    }

    /**
     * 移除 URL 中的水印参数
     */
    private fun removeWatermarkParams(url: String): String {
        var result = url
        // 常见水印参数
        val watermarkParams = listOf(
            "watermark=1", "wm_aid=1", "wm_tt=1",
            "watermark_type", "wm_logo", "watermark_logo",
            "wm_aid=1", "wm_tt=1"
        )
        for (param in watermarkParams) {
            if (result.contains(param)) {
                result = result.replace(param, "")
            }
        }
        // 清理多余的 & 符号
        result = result.replace("&&", "&")
            .replace("?&", "?")
            .replace("&?", "?")
            .trimEnd('&')
        return result
    }
}
