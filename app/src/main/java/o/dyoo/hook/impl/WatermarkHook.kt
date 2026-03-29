package o.dyoo.hook.impl

import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.param.PackageParam
import o.dyoo.core.config.ModuleConfig

/**
 * 水印去除 Hook
 */
object WatermarkHook {

    fun setup(param: PackageParam) {
        if (!ModuleConfig.isWatermarkRemoveEnabled) return

        param.apply {
            try {
                "okhttp3.Request\$Builder".toClass().method {
                    name = "url"
                    paramCount = 1
                }.hook {
                    before {
                        val arg = args[0]
                        if (arg is String && (arg.contains("watermark") || arg.contains("wm_aid"))) {
                            args[0] = arg
                                .replace("watermark=1", "watermark=0")
                                .replace("wm_aid=1", "wm_aid=0")
                        }
                    }
                }
            } catch (_: Throwable) {}
        }
    }
}