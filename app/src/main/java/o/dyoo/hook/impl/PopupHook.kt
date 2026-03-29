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
                    method { name = "onResume" }
                    afterHook {
                        (instance as? Activity)?.let {
                            if (it.packageName == "com.ss.android.ugc.aweme") FloatingView.show(it)
                        }
                    }
                }
            }
            Activity::class.java.hook {
                injectMember {
                    method { name = "onPause" }
                    beforeHook {
                        (instance as? Activity)?.let {
                            if (it.packageName == "com.ss.android.ugc.aweme") FloatingView.hide()
                        }
                    }
                }
            }
        }
    }
}
