package com.codegames.pasu.bazaar

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.codegames.pasu.MarketInterface
import com.codegames.pasu.MarketLoginCheckConnection
import com.codegames.pasu.MarketLoginCheckInterface
import com.farsitel.bazaar.ILoginCheckService
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class BazaarMarketLoginCheck(market: MarketInterface) : MarketLoginCheckInterface {

    private var service: ILoginCheckService? = null
    private var mConnection: LoginCheckServiceConnection? = null

    private var connection: MarketLoginCheckConnection? = null

    private inner class LoginCheckServiceConnection : ServiceConnection {

        override fun onServiceConnected(
            name: ComponentName,
            boundService: IBinder
        ) {
            service = ILoginCheckService.Stub
                .asInterface(boundService)
            try {
                val isLoggedIn = service?.isLoggedIn ?: false
                connection?.onConnect?.invoke(isLoggedIn)
            } catch (e: Exception) {
                e.printStackTrace()
                connection?.onError?.invoke(e)
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            service = null
            connection?.onDisconnect?.invoke()
        }
    }

    override suspend fun initService(context: Context): Boolean {
        return suspendCoroutine { cont ->
            initService(context) {
                onConnect = {
                    cont.resume(it)
                }
                onError = {
                    cont.resumeWithException(it)
                }
                onDisconnect = {
                    cont.resume(false)
                }
            }
        }
    }

    override fun initService(
        context: Context,
        connection: MarketLoginCheckConnection.() -> Unit
    ): Boolean {
        val mc = LoginCheckServiceConnection()
        mConnection = mc
        MarketLoginCheckConnection().also {
            connection(it)
            this.connection = it
        }
        val i = Intent(
            "com.farsitel.bazaar.service.LoginCheckService.BIND"
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