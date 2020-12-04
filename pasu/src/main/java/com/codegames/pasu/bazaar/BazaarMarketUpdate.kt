package com.codegames.pasu.bazaar

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import com.codegames.pasu.MarketInterface
import com.codegames.pasu.MarketUpdateConnection
import com.codegames.pasu.MarketUpdateInterface
import com.codegames.pasu.util.MarketUpdate
import com.farsitel.bazaar.IUpdateCheckService
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class BazaarMarketUpdate(market: MarketInterface) : MarketUpdateInterface {

    private var packageName: String? = null
    private var versionCode: Int? = null
    private var service: IUpdateCheckService? = null
    private var mConnection: UpdateServiceConnection? = null

    private var connection: MarketUpdateConnection? = null

    private inner class UpdateServiceConnection : ServiceConnection {
        override fun onServiceConnected(
            name: ComponentName,
            boundService: IBinder
        ) {
            service = IUpdateCheckService.Stub
                .asInterface(boundService)
            try {
                val v = service?.getVersionCode(packageName) ?: -1
                connection?.onConnect?.invoke(
                    MarketUpdate(
                        versionCode = v.toInt(),
                        description = "",
                        isUpdateAvailable = versionCode?.let { ov -> ov < v } ?: false
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
                connection?.onError?.invoke(e)
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            service = null
            packageName = null
            versionCode = null
            connection?.onDisconnect?.invoke()
        }
    }

    override suspend fun initService(context: Context): MarketUpdate {
        return suspendCoroutine { cont ->
            initService(context) {
                onConnect = {
                    cont.resume(it)
                }
                onError = {
                    cont.resumeWithException(it)
                }
                onDisconnect = {
                    cont.resume(
                        MarketUpdate(
                            versionCode = -1,
                            isUpdateAvailable = false,
                            description = ""
                        )
                    )
                }
            }
        }
    }

    override fun initService(
        context: Context,
        connection: MarketUpdateConnection.() -> Unit
    ): Boolean {
        val p = context.applicationContext.packageName
        packageName = p
        versionCode = try {
            val pInfo: PackageInfo = context.packageManager.getPackageInfo(
                p, 0
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode
            }
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
        val mc = UpdateServiceConnection()
        mConnection = mc
        MarketUpdateConnection().also {
            connection(it)
            this.connection = it
        }
        val i = Intent(
            "com.farsitel.bazaar.service.UpdateCheckService.BIND"
        )
        i.setPackage("com.farsitel.bazaar")
        return context.bindService(i, mc, Context.BIND_AUTO_CREATE)
    }

    override fun releaseService(context: Context) {
        mConnection?.also {
            context.unbindService(it)
            mConnection = null
        }
    }


}