package o.dyoo.hook

import com.highcapable.yukihookapi.annotation.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.log.YukiHookLogger
import com.highcapable.yukihookapi.hook.param.PackageParam
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import o.dyoo.hook.impl.VideoHook
import o.dyoo.hook.impl.ImageHook
import o.dyoo.hook.impl.WatermarkHook
import o.dyoo.hook.impl.PopupHook

@InjectYukiHookWithXposed
class HookEntry : IYukiHookXposedInit {

    override fun onHook(param: PackageParam) {
        param.apply {
            configs {
                YukiHookLogger.Configs.isDebug = true
            }
            encase {
                loadApp(name = "com.ss.android.ugc.aweme") {
                    YukiHookLogger.info("Dyoo: Hooking Douyin...")
                    VideoHook.setup(this)
                    ImageHook.setup(this)
                    WatermarkHook.setup(this)
                    PopupHook.setup(this)
                    YukiHookLogger.info("Dyoo: All hooks installed")
                }
            }
        }
    }
}
