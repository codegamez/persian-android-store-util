package com.codegames.pasu.myket

import android.content.Context
import com.codegames.pasu.MarketInterface
import com.codegames.pasu.MarketUpdateConnection
import com.codegames.pasu.MarketUpdateInterface
import com.codegames.pasu.myket.util.MyketResult
import com.codegames.pasu.myket.util.MyketSupportHelper
import com.codegames.pasu.util.IabException
import com.codegames.pasu.util.MarketUpdate
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class MyketMarketUpdate(market: MarketInterface) : MarketUpdateInterface {

    private var packageName: String? = null
    private var mMyketHelper: MyketSupportHelper? = null

    private var connection: MarketUpdateConnection? = null

    private var mCheckAppUpdateListener = object : MyketSupportHelper
    .CheckAppUpdateListener {
        override fun onCheckAppUpdateFinished(result: MyketResult, update: MarketUpdate?) {
            when {
                !result.isSuccess -> {
                    connection?.onError?.invoke(
                        IabException(
                            result.response,
                            result.message
                        )
                    )
                    return
                }
                update === null -> {
                    connection?.onError?.invoke(
                        IabException(
                            -1,
                            "error update"
                        )
                    )
                    return
                }
                else -> {
                    connection?.onConnect?.invoke(update)
                }
            }
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
        packageName = context.applicationContext.packageName
        if (mMyketHelper?.isSetupDone != true)
            mMyketHelper = MyketSupportHelper(context)
        MarketUpdateConnection().also {
            connection(it)
            this.connection = it
        }
        mMyketHelper?.startSetup { result ->
            if (!result.isSuccess) {
                this.connection?.onError?.invoke(
                    IabException(
                        result.response,
                        result.message
                    )
                )
            } else {
                mMyketHelper?.getAppUpdateStateAsync(mCheckAppUpdateListener)
            }
        }
        return true
    }

    override fun releaseService(context: Context) {
        mMyketHelper?.also {
            it.dispose()
            mMyketHelper = null
        }
    }

}