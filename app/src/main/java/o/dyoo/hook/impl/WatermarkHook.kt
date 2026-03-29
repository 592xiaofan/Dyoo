package o.dyoo.hook.impl

import com.highcapable.yukihookapi.hook.param.PackageParam
import o.dyoo.core.config.ModuleConfig

object WatermarkHook {

    fun setup(param: PackageParam) {
        if (!ModuleConfig.isWatermarkRemoveEnabled) return
        param.apply {
            "okhttp3.Request\$Builder".toClass().hook {
                injectMember {
                    method {
                        name = "url"
                        paramCount = 1
                    }
                    beforeUnit {
                        val arg = args(0)
                        if (arg is String && (arg.contains("watermark") || arg.contains("wm_aid"))) {
                            args(0, arg
                                .replace("watermark=1", "watermark=0")
                                .replace("wm_aid=1", "wm_aid=0"))
                        }
                    }
                }
            }
        }
    }
}
