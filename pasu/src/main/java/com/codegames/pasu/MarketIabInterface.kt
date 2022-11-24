package com.codegames.pasu

import android.content.Context
import com.codegames.pasu.util.*

interface MarketIabInterface {

    fun isEnable(): Boolean

    fun initService(context: Context, publicKey: String, listener: MarketIabListener.() -> Unit)
    suspend fun initService(context: Context, publicKey: String): IabResult
    fun releaseService(context: Context)

    fun getInventory(skuList: List<String>?, listener: MarketInventoryListener.() -> Unit)
    suspend fun getInventory(skuList: List<String>?): InventoryResult

    fun launchPurchase(
        context: Context,
        sku: String,
        extraData: String?,
        listener: MarketPurchaseListener.() -> Unit
    )

    suspend fun launchPurchase(
        context: Context,
        sku: String,
        extraData: String?
    ): Purchase

    fun consume(purchase: Purchase, listener: MarketConsumeListener.() -> Unit)
    suspend fun consume(purchase: Purchase): Purchase
}

class MarketIabListener {
    var onFailure: ((e: IabException) -> Unit)? = null
    var onSuccess: ((product: IabResult) -> Unit)? = null
}

class MarketInventoryListener {
    internal var skuList: List<String>? = null
    var onFailure: ((e: IabException) -> Unit)? = null
    var onSuccess: ((inventory: InventoryResult) -> Unit)? = null
}

class MarketPurchaseListener {
    var onFailure: ((e: IabException) -> Unit)? = null
    var onSuccess: ((purchase: Purchase) -> Unit)? = null
}

class MarketConsumeListener {
    var onFailure: ((e: IabException) -> Unit)? = null
    var onSuccess: ((purchase: Purchase) -> Unit)? = null
}