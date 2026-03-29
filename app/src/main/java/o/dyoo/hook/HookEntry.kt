package o.dyoo.hook

import com.highcapable.yukihookapi.annotation.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import o.dyoo.hook.impl.VideoHook
import o.dyoo.hook.impl.ImageHook
import o.dyoo.hook.impl.WatermarkHook
import o.dyoo.hook.impl.PopupHook

@InjectYukiHookWithXposed
class HookEntry : IYukiHookXposedInit {

    override fun onInit() {
        // 配置初始化
    }

    override fun onXposedEvent() {
        encase {
            loadApp(name = "com.ss.android.ugc.aweme") {
                VideoHook.setup(this)
                ImageHook.setup(this)
                WatermarkHook.setup(this)
                PopupHook.setup(this)
            }
        }
    }
}
