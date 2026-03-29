package o.dyoo.hook.dexkit

import android.util.Log
import com.highcapable.yukihookapi.hook.param.PackageParam

object DouyinFinder {
    private var initialized = false

    fun init(packageParam: PackageParam) {
        if (initialized) return
        initialized = true
        Log.i("Dyoo", "DouyinFinder initialized")
    }
}
