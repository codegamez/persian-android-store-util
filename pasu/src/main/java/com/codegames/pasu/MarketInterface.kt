package com.codegames.pasu

import android.content.Context

interface MarketInterface {

    val marketId: String
    val marketPackage: String
    var tag: String
    var enableDebugLogging: Boolean

    val updater: MarketUpdateInterface
    val loginChecker: MarketLoginCheckInterface
    val iab: MarketIabInterface

    fun openAppPage(context: Context): Boolean
    fun openAppPage(context: Context, packageName: String): Boolean
    fun openCommentPage(context: Context): Boolean

    fun openCommentPage(context: Context, packageName: String): Boolean

    /*
        id in some market like bazaar is developerId
        but in some market like myket is one of developer app packageName
     */
    fun openDeveloperPage(context: Context, id: String?): Boolean

    fun openLoginPage(context: Context): Boolean

}