package com.codegames.pasu.bazaar

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.codegames.pasu.MarketIabInterface
import com.codegames.pasu.MarketInterface
import com.codegames.pasu.MarketLoginCheckInterface
import com.codegames.pasu.MarketUpdateInterface
import com.codegames.pasu.bazaar.BazaarMarketIab
import com.codegames.pasu.bazaar.BazaarMarketLoginCheck
import com.codegames.pasu.bazaar.BazaarMarketUpdate
import java.lang.Exception


class BazaarMarket: MarketInterface {

    override val marketId = "ir.cafebazaar.pardakht"
    override val marketPackage = "com.farsitel.bazaar"

    override var tag: String = "MarketInterface"
    override var enableDebugLogging: Boolean = false

    override val updater: MarketUpdateInterface =
        BazaarMarketUpdate(this)
    override val loginChecker: MarketLoginCheckInterface =
        BazaarMarketLoginCheck(this)
    override val iab: MarketIabInterface =
        BazaarMarketIab(this)

    override fun openAppPage(context: Context): Boolean {
        return openAppPage(context, context.applicationContext.packageName)
    }

    override fun openAppPage(context: Context, packageName: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("bazaar://details?id=$packageName")
            intent.setPackage("com.farsitel.bazaar")
            context.startActivity(intent)
            true
        }catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun openCommentPage(context: Context): Boolean {
        return openCommentPage(context, context.applicationContext.packageName)
    }

    override fun openCommentPage(context: Context, packageName: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_EDIT)
            intent.data = Uri.parse("bazaar://details?id=$packageName")
            intent.setPackage("com.farsitel.bazaar")
            context.startActivity(intent)
            true
        }catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /*
        id is developerId
    */
    override fun openDeveloperPage(context: Context, id: String?): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("bazaar://collection?slug=by_author&aid=$id")
            intent.setPackage("com.farsitel.bazaar")
            context.startActivity(intent)
            true
        }catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun openLoginPage(context: Context): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("bazaar://login")
            intent.setPackage("com.farsitel.bazaar")
            context.startActivity(intent)
            true
        }catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

}