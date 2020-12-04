package com.codegames.pasu

import com.codegames.pasu.bazaar.BazaarMarket
import com.codegames.pasu.myket.MyketMarket

@Suppress("MemberVisibilityCanBePrivate")
object MarketUtil {

    const val MARKET_BAZAAR = 1
    const val MARKET_MYKET = 2

    var targetMarket: MarketInterface? = null

    val updater get() = targetMarket?.updater
    val loginChecker get() = targetMarket?.loginChecker
    val iab get() = targetMarket?.iab

    fun setTargetMarket(targetMarket: Int) {
        this.targetMarket = when(targetMarket) {
            MARKET_BAZAAR -> BazaarMarket()
            MARKET_MYKET -> MyketMarket()
            else -> throw ClassNotFoundException("target market is not valid ($targetMarket)")
        }
    }

}