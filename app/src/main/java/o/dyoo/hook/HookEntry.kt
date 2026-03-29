package o.dyoo.hook

import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.param.PackageParam
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

    override fun onHook(param: PackageParam) {
        param.apply {
            loadApp(name = "com.ss.android.ugc.aweme") {
                YLog.info("Dyoo: Hooking Douyin...")

                DouyinFinder.init(this@loadApp)

                VideoHook.setup(this)
                ImageHook.setup(this)
                WatermarkHook.setup(this)
                PopupHook.setup(this)

                YLog.info("Dyoo: All hooks installed")
            }
        }
    }
}