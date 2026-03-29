package o.dyoo.hook

import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.initiate
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import o.dyoo.hook.dexkit.DouyinFinder
import o.dyoo.hook.impl.VideoHook
import o.dyoo.hook.impl.ImageHook
import o.dyoo.hook.impl.WatermarkHook
import o.dyoo.hook.impl.PopupHook

/**
 * Dyoo Xposed 入口
 */
@InjectYukiHookWithXposed
class HookEntry : IYukiHookXposedInit {

    override fun onInit() {
        configs {
            isDebug = true
        }
    }

    override fun onXposedEvent() {
        initiate { app ->
            app.loadApp(name = "com.ss.android.ugc.aweme") {
                YLog.info("Dyoo: Hooking Douyin...")

                DouyinFinder.init(this)

                VideoHook.setup(this)
                ImageHook.setup(this)
                WatermarkHook.setup(this)
                PopupHook.setup(this)

                YLog.info("Dyoo: All hooks installed")
            }
        }
    }
}