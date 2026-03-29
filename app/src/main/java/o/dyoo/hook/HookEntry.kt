package o.dyoo.hook

import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import o.dyoo.hook.dexkit.DouyinFinder
import o.dyoo.hook.impl.VideoHook
import o.dyoo.hook.impl.ImageHook
import o.dyoo.hook.impl.WatermarkHook
import o.dyoo.hook.impl.PopupHook
import o.dyoo.hook.impl.CleanModeHook

@InjectYukiHookWithXposed
class HookEntry : IYukiHookXposedInit {

    override fun onHook() {
        encase {
            loadApp(name = "com.ss.android.ugc.aweme") {
                // 1. 运行时搜索抖音关键类
                DouyinFinder.init(this)

                // 2. 注册各模块 Hook
                VideoHook.setup(this)
                ImageHook.setup(this)
                WatermarkHook.setup(this)
                PopupHook.setup(this)
                CleanModeHook.setup(this)
            }
        }
    }
}
