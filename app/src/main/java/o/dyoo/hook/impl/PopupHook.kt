package o.dyoo.hook.impl

import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.Toast
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.param.PackageParam
import o.dyoo.core.config.ModuleConfig
import o.dyoo.core.ui.FloatingView

/**
 * 悬浮窗 Hook
 * 在抖音界面添加 Dyoo 快捷悬浮按钮
 */
object PopupHook {

    fun setup(param: PackageParam) {
        if (!ModuleConfig.showFloatingButton) return

        param.apply {
            // Hook Activity.onResume 在抖音页面显示悬浮窗
            "android.app.Activity".toClass().hook {
                injectMember {
                    method { name = "onResume" }
                    afterHook {
                        val activity = instance as? Activity ?: return@afterHook
                        if (activity.packageName == "com.ss.android.ugc.aweme") {
                            FloatingView.show(activity)
                        }
                    }
                }
            }

            // Hook Activity.onPause 移除悬浮窗
            "android.app.Activity".toClass().hook {
                injectMember {
                    method { name = "onPause" }
                    beforeHook {
                        val activity = instance as? Activity ?: return@beforeHook
                        if (activity.packageName == "com.ss.android.ugc.aweme") {
                            FloatingView.hide()
                        }
                    }
                }
            }
        }
    }
}
