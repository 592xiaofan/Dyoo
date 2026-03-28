package o.dyoo.hook

import com.highcapable.yukihookapi.annotation.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import o.dyoo.hook.dexkit.DouyinFinder
import o.dyoo.hook.impl.VideoHook
import o.dyoo.hook.impl.ImageHook
import o.dyoo.hook.impl.WatermarkHook
import o.dyoo.hook.impl.PopupHook

/**
 * Dyoo Xposed 入口
 * 使用 YukiHookAPI 的 @InjectYukiHookWithXposed 注解
 * 不需要 assets/xposed_init，KSP 自动生成
 */
@InjectYukiHookWithXposed
class HookEntry : IYukiHookXposedInit {

    override fun onInit() = configs {
        debugLog {
            tag = "Dyoo"
            isEnable = true
        }
        isEnableModuleAppResourcesCache = true
    }

    override fun onHook() = encase {
        loadApp(name = "com.ss.android.ugc.aweme") {
            YLog.info("Dyoo: Hooking Douyin...")

            // 先用 DexKit 查找目标类
            onAppLifecycle(isOnFailureThrowToApp = false) {
                DouyinFinder.init(this@loadApp)
            }

            // 安装各个 Hook 模块
            VideoHook.setup(this)
            ImageHook.setup(this)
            WatermarkHook.setup(this)
            PopupHook.setup(this)

            YLog.info("Dyoo: All hooks installed")
        }
    }
}
