package com.codegames.pasu

import android.content.Context

interface MarketLoginCheckInterface {

    suspend fun initService(context: Context): Boolean
    fun initService(context: Context, connection: MarketLoginCheckConnection.() -> Unit): Boolean
    fun releaseService(context: Context)

}

class MarketLoginCheckConnection {
    var onConnect: ((isLoggedIn: Boolean) -> Unit)? = null
    var onDisconnect: (() -> Unit)? = null
    var onError: ((e: Exception) -> Unit)? = null
}