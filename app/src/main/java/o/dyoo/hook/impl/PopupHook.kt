package o.dyoo.hook.impl

import android.app.Activity
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.param.PackageParam
import o.dyoo.core.config.ModuleConfig
import o.dyoo.core.ui.FloatingView

/**
 * 悬浮窗 Hook
 * Hook Activity 生命周期，在抖音界面显示悬浮按钮
 */
object PopupHook {

    fun setup(param: PackageParam) {
        if (!ModuleConfig.showFloatingButton) return

        param.apply {
            // Hook onResume
            Activity::class.java.hook {
                injectMember {
                    method { name = "onResume" }
                    after {
                        val activity = instance as? Activity ?: return@after
                        if (activity.packageName == "com.ss.android.ugc.aweme") {
                            FloatingView.show(activity)
                        }
                    }
                }
            }

            // Hook onPause
            Activity::class.java.hook {
                injectMember {
                    method { name = "onPause" }
                    before {
                        val activity = instance as? Activity ?: return@before
                        if (activity.packageName == "com.ss.android.ugc.aweme") {
                            FloatingView.hide()
                        }
                    }
                }
            }

            YLog.info("Dyoo: PopupHook installed")
        }
    }
}
