package o.dyoo.hook.impl

import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.param.PackageParam
import o.dyoo.core.config.ModuleConfig

/**
 * 水印去除 Hook
 * 修改视频 URL 参数去除水印
 */
object WatermarkHook {

    fun setup(param: PackageParam) {
        if (!ModuleConfig.isWatermarkRemoveEnabled) return

        param.apply {
            // Hook URL 构建，修改水印参数
            hookUrlBuilder()
        }
    }

    /**
     * Hook URL 构建过程
     * 抖音视频 URL 中的 watermark 参数控制水印
     * watermark=1 有水印, watermark=0 无水印（部分版本支持）
     * 
     * 更可靠的策略: Hook 视频播放 URL 的请求参数
     */
    private fun PackageParam.hookUrlBuilder() {
        try {
            // Hook okhttp3 Request.Builder.addHeader 或 url 方法
            "okhttp3.Request\$Builder".toClass().hook {
                injectMember {
                    method {
                        name = "url"
                        paramCount = 1
                    }
                    beforeHook {
                        val arg = args[0]
                        if (arg is String && arg.contains("watermark")) {
                            val cleanUrl = arg
                                .replace("watermark=1", "watermark=0")
                                .replace("wm_aid=1", "wm_aid=0")
                            args[0] = cleanUrl
                            YLog.info("Dyoo: Removed watermark from URL")
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            YLog.error("Dyoo: Hook URL builder failed: ${e.message}")
        }

        // 备用策略: Hook 视频播放地址中的 watermark 相关类
        try {
            val wmClass = DouyinFinder.videoUrlFieldClass ?: return
            wmClass.toClass().hook {
                injectMember {
                    // 所有返回 String 的方法都检查是否包含水印参数
                    allMethods {
                        returnType == "java.lang.String"
                    }
                    afterHook {
                        val result = this.result as? String ?: return@afterHook
                        if (result.contains("watermark") || result.contains("wm_aid")) {
                            val clean = result
                                .replace("watermark=1", "watermark=0")
                                .replace("wm_aid=1", "wm_aid=0")
                            this.result = clean
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            YLog.error("Dyoo: Hook watermark class failed: ${e.message}")
        }
    }
}
