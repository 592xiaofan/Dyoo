package o.dyoo.hook.impl

import android.app.Activity
import com.highcapable.yukihookapi.hook.param.PackageParam
import o.dyoo.core.config.ModuleConfig
import o.dyoo.core.ui.FloatingView

object PopupHook {

    fun setup(param: PackageParam) {
        if (!ModuleConfig.showFloatingButton) return
        param.apply {
            Activity::class.java.hook {
                injectMember {
                    method {
                        name = "onResume"
                    }
                    afterUnit {
                        val activity = instance as? Activity ?: return@afterUnit
                        if (activity.packageName == "com.ss.android.ugc.aweme") {
                            FloatingView.show(activity)
                        }
                    }
                }
            }
            Activity::class.java.hook {
                injectMember {
                    method {
                        name = "onPause"
                    }
                    beforeUnit {
                        val activity = instance as? Activity ?: return@beforeUnit
                        if (activity.packageName == "com.ss.android.ugc.aweme") {
                            FloatingView.hide()
                        }
                    }
                }
            }
        }
    }
}
