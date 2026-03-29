package o.dyoo.hook.impl

import android.app.Activity
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.param.PackageParam
import o.dyoo.core.config.ModuleConfig
import o.dyoo.core.ui.FloatingView

/**
 * 悬浮窗 Hook
 */
object PopupHook {

    fun setup(param: PackageParam) {
        if (!ModuleConfig.showFloatingButton) return

        param.apply {
            try {
                Activity::class.java.method {
                    name = "onResume"
                }.hook {
                    after {
                        val activity = instance as? Activity ?: return@after
                        if (activity.packageName == "com.ss.android.ugc.aweme") {
                            FloatingView.show(activity)
                        }
                    }
                }

                Activity::class.java.method {
                    name = "onPause"
                }.hook {
                    before {
                        val activity = instance as? Activity ?: return@before
                        if (activity.packageName == "com.ss.android.ugc.aweme") {
                            FloatingView.hide()
                        }
                    }
                }
            } catch (_: Throwable) {}
        }
    }
}