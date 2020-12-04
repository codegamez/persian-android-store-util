package com.codegames.pasu

import android.content.Context
import com.codegames.pasu.util.MarketUpdate
import java.lang.Exception

interface MarketUpdateInterface {

    suspend fun initService(context: Context): MarketUpdate
    fun initService(context: Context, connection: MarketUpdateConnection.() -> Unit): Boolean
    fun releaseService(context: Context)

}

class MarketUpdateConnection {
    var onConnect: ((update: MarketUpdate) -> Unit)? = null
    var onDisconnect: (() -> Unit)? = null
    var onError: ((e: Exception) -> Unit)? = null
}