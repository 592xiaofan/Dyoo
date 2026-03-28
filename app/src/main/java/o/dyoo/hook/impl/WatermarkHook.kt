package o.dyoo.hook.impl

import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.param.PackageParam
import o.dyoo.core.config.ModuleConfig

/**
 * 水印去除 Hook
 * Hook URL 构建过程去除水印参数
 */
object WatermarkHook {

    fun setup(param: PackageParam) {
        if (!ModuleConfig.isWatermarkRemoveEnabled) return

        param.apply {
            hookUrlConstruction()
        }
    }

    private fun PackageParam.hookUrlConstruction() {
        // 策略1: Hook okhttp3 Request.Builder 的 url 方法
        try {
            "okhttp3.Request\$Builder".toClass().hook {
                injectMember {
                    method {
                        name = "url"
                        paramCount = 1
                    }
                    before {
                        val arg = args[0]
                        if (arg is String && (arg.contains("watermark") || arg.contains("wm_aid"))) {
                            val clean = arg
                                .replace("watermark=1", "watermark=0")
                                .replace("wm_aid=1", "wm_aid=0")
                            args[0] = clean
                            YLog.info("Dyoo: Watermark removed from URL")
                        }
                    }
                }
            }
            YLog.info("Dyoo: Hooked okhttp3 Request.Builder for watermark removal")
        } catch (e: Throwable) {
            YLog.error("Dyoo: Hook watermark (okhttp) failed: ${e.message}")
        }

        // 策略2: 如果 DexKit 找到了视频类，hook 其 String 返回值
        val videoClassName = o.dyoo.hook.dexkit.DouyinFinder.videoUrlFieldClass ?: return
        try {
            videoClassName.toClass().hook {
                injectMember {
                    allMethods {
                        returnType == String::class.java.name
                    }
                    after {
                        val str = result as? String ?: return@after
                        if (str.contains("watermark") || str.contains("wm_aid")) {
                            result = str
                                .replace("watermark=1", "watermark=0")
                                .replace("wm_aid=1", "wm_aid=0")
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            YLog.error("Dyoo: Hook watermark (video class) failed: ${e.message}")
        }
    }
}
