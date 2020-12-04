package com.codegames.pasu.myket

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.codegames.pasu.*
import com.codegames.pasu.util.*
import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class MyketMarketIab(val market: MarketInterface) : MarketIabInterface {

    private var enable: Boolean = false

    private var iabListener: MarketIabListener? = null
    private var inventoryListener: MarketInventoryListener? = null
    private var purchaseListener: MarketPurchaseListener? = null
    private var consumeListener: MarketConsumeListener? = null
    private var mHelper: IabHelper? = null

    private var mGotInventoryListener =
        IabHelper.QueryInventoryFinishedListener { result, inventory ->
            if (mHelper == null) return@QueryInventoryFinishedListener
            if (result.isFailure) {
                inventoryListener?.onFailure?.invoke(IabException(result))
                return@QueryInventoryFinishedListener
            } else {
                val skuList = inventory.mSkuMap.values.toList()
                val purchaseList = inventory.mPurchaseMap.values.toList()
                inventoryListener?.onSuccess?.invoke(
                    InventoryResult(
                        skuList,
                        purchaseList
                    )
                )
                return@QueryInventoryFinishedListener
            }
        }

    private var mPurchaseFinishedListener =
        IabHelper.OnIabPurchaseFinishedListener { result, purchase ->
            if (mHelper == null) return@OnIabPurchaseFinishedListener
            if (result.isFailure) {
                purchaseListener?.onFailure?.invoke(IabException(result))
                return@OnIabPurchaseFinishedListener
            } else {
                purchaseListener?.onSuccess?.invoke(purchase)
                return@OnIabPurchaseFinishedListener
            }
        }

    private var mConsumeFinishedListener =
        IabHelper.OnConsumeFinishedListener { purchase, result ->
            if (mHelper == null) return@OnConsumeFinishedListener
            if (result.isSuccess) {
                consumeListener?.onSuccess?.invoke(purchase)
                return@OnConsumeFinishedListener
            } else {
                consumeListener?.onFailure?.invoke(IabException(result))
                return@OnConsumeFinishedListener
            }
        }

    override fun isEnable(): Boolean = enable


    override fun initService(
        context: Context,
        publicKey: String,
        listener: MarketIabListener.() -> Unit
    ) {
        MarketIabListener().also {
            listener(it)
            this.iabListener = it
        }
        mHelper =
            IabHelper(context, publicKey, market.marketId, market.marketPackage)
        mHelper?.enableDebugLogging(market.enableDebugLogging, market.tag)
        mHelper?.startSetup { result ->
            if (!result.isSuccess) {
                enable = false
                this.iabListener?.onFailure?.invoke(IabException(result))
            } else {
                enable = true
                this.iabListener?.onSuccess?.invoke(result)
            }
        }
    }

    override suspend fun initService(
        context: Context,
        publicKey: String
    ): IabResult {
        return suspendCoroutine { cont ->
            initService(context, publicKey) {
                onSuccess = {
                    cont.resume(it)
                }
                onFailure = {
                    cont.resumeWithException(it)
                }
            }
        }
    }

    override fun releaseService(context: Context) {
        mHelper?.dispose()
        mHelper = null
        enable = false
    }

    override fun getInventory(skuList: List<String>?, listener: MarketInventoryListener.() -> Unit) {
        if (!enable) {
            throw Throwable("iab service is not enabled. first call initService.")
        }
        MarketInventoryListener().also {
            listener(it)
            inventoryListener = it
            inventoryListener?.skuList = skuList
        }
        try {
            mHelper?.queryInventoryAsync(true, skuList, mGotInventoryListener)
        } catch (e: Exception) {
            inventoryListener?.onFailure?.invoke(
                IabException(
                    IabHelper.IABHELPER_UNKNOWN_ERROR, e.message
                )
            )
        }
    }

    override suspend fun getInventory(skuList: List<String>?): InventoryResult {
        return suspendCoroutine { cont ->
            getInventory(skuList) {
                onSuccess = {
                    cont.resume(it)
                }
                onFailure = { e ->
                    cont.resumeWithException(e)
                }
            }
        }
    }

    override fun launchPurchase(
        context: Context,
        sku: String,
        requestCode: Int,
        extraData: String?,
        listener: MarketPurchaseListener.() -> Unit
    ) {
        if (!enable) {
            throw Throwable("iab service is not enabled. first call initService.")
        }
        MarketPurchaseListener().also {
            listener(it)
            this.purchaseListener = it
        }
        try {
            mHelper?.launchPurchaseFlow(
                context as Activity,
                sku,
                requestCode,
                mPurchaseFinishedListener,
                extraData
            )
        } catch (e: Exception) {
            purchaseListener?.onFailure?.invoke(
                IabException(
                    IabHelper.IABHELPER_UNKNOWN_ERROR, e.message
                )
            )
        }
    }

    override suspend fun launchPurchase(
        context: Context,
        sku: String,
        requestCode: Int,
        extraData: String?
    ): Purchase {
        return suspendCoroutine { cont ->
            launchPurchase(context, sku, requestCode, extraData) {
                onSuccess = {
                    cont.resume(it)
                }
                onFailure = {
                    cont.resumeWithException(it)
                }
            }
        }
    }

    override fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        return mHelper?.handleActivityResult(requestCode, resultCode, data) ?: false
    }

    override fun consume(purchase: Purchase, listener: MarketConsumeListener.() -> Unit) {
        if (!enable) {
            throw Throwable("iab service is not enabled. first call initService.")
        }
        MarketConsumeListener().also {
            listener(it)
            consumeListener = it
        }
        try {
            mHelper?.consumeAsync(purchase, mConsumeFinishedListener)
        } catch (e: Exception) {
            consumeListener?.onFailure?.invoke(
                IabException(
                    IabHelper.IABHELPER_UNKNOWN_ERROR, e.message
                )
            )
        }
    }

    override suspend fun consume(purchase: Purchase): Purchase {
        return suspendCoroutine { cont ->
            consume(purchase) {
                onSuccess = {
                    cont.resume(it)
                }
                onFailure = {
                    cont.resumeWithException(it)
                }
            }
        }
    }

}