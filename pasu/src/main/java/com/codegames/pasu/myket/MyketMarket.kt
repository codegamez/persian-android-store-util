package com.codegames.pasu.myket

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.codegames.pasu.MarketIabInterface
import com.codegames.pasu.MarketInterface
import com.codegames.pasu.MarketLoginCheckInterface
import com.codegames.pasu.MarketUpdateInterface
import com.codegames.pasu.bazaar.BazaarMarketLoginCheck


class MyketMarket : MarketInterface {

    override val marketName = "myket"
    override val marketId = "ir.mservices.market"
    override val marketPackage = "ir.mservices.market"

    override var tag: String = "MarketInterface"
    override var enableDebugLogging: Boolean = false

    override val updater: MarketUpdateInterface =
        MyketMarketUpdate(this)
    override val loginChecker: MarketLoginCheckInterface =
        BazaarMarketLoginCheck(this)
    override val iab: MarketIabInterface =
        MyketMarketIab(this)

    override fun openAppPage(context: Context): Boolean {
        return openAppPage(context, context.applicationContext.packageName)
    }

    override fun openAppPage(context: Context, packageName: String): Boolean {
        return try {
            val url = "myket://details?id=$packageName"
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.data = Uri.parse(url)
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun openCommentPage(context: Context): Boolean {
        return openCommentPage(context, context.applicationContext.packageName)
    }

    override fun openCommentPage(context: Context, packageName: String): Boolean {
        return try {
            val url = "myket://comment?id=$packageName"
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.data = Uri.parse(url)
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /*
        id is one of developer app packageName. null is current packageName
    */
    override fun openDeveloperPage(context: Context, id: String?): Boolean {
        val d = id ?: context.applicationContext.packageName
        return try {
            val url = "myket://developer/$d"
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.data = Uri.parse(url)
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun openLoginPage(context: Context): Boolean {
        return false
    }

}