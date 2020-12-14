@file:Suppress("ConstantConditionIf")

package com.codegames.pasudemo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.codegames.pasu.MarketUtil
import com.codegames.pasu.util.Purchase
import com.codegames.pasu.util.SkuDetails
import com.codegames.simplelist.adapter.SimpleAdapter
import com.codegames.simplelist.simple
import com.google.android.gms.ads.*
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_view.view.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext


class MainActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val skuList = listOf("coin1", "coin2")
    private val market = MarketUtil.MARKET_BAZAAR
    private val publicKey =
        ""

    private var itemList = mutableListOf<SkuDetails>()
    private var itemAdapter: SimpleAdapter<SkuDetails>? =
        null
    private var purchaseList = mutableListOf<Purchase>()
    private var purchaseAdapter: SimpleAdapter<Purchase>? =
        null

    companion object {
        const val RC_PURCHASE = 10
        const val RC_SIGN_IN = 11
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        job = Job()

        MarketUtil.setTargetMarket(market)
        MarketUtil.targetMarket?.enableDebugLogging = true

        setupList()

        progressBar.isVisible = false
        getInventory(false)

        bottomImage.setOnClickListener {
            getInventory(false)
        }

        bottomImage.setOnClickListener {
            getInventory(true)
        }

        if (market == MarketUtil.MARKET_BAZAAR) {
            bottomImage.setBackgroundResource(R.color.colorGreen)
            bottomImage.setImageResource(R.drawable.bazaar)
        } else if (market == MarketUtil.MARKET_MYKET) {
            bottomImage.setBackgroundResource(R.color.colorBlue)
            bottomImage.setImageResource(R.drawable.myket)
        }

    }

    private val exceptionHandler = CoroutineExceptionHandler { _, e ->
        Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
        progressBar.isVisible = false
        job = Job()
    }

    private fun getInventory(consume: Boolean) {
        if (progressBar.isVisible) return
        progressBar.isVisible = true

        launch(exceptionHandler) {
            val iab = MarketUtil.iab!!

            if (!iab.isEnable())
                iab.initService(this@MainActivity, publicKey)

            val inventory = iab.getInventory(skuList)

            recyclerView.post {
                itemList.clear()
                itemList.addAll(inventory.skuList)
                itemAdapter?.notifyDataSetChanged()
                recyclerView2.post {
                    purchaseList.clear()
                    purchaseList.addAll(inventory.purchaseList)
                    purchaseAdapter?.notifyDataSetChanged()
                }
            }

            Toast.makeText(this@MainActivity, "updated", Toast.LENGTH_SHORT).show()

            if (consume) {
                for (purchase in inventory.purchaseList) {
                    iab.consume(purchase)
                }
                Toast.makeText(this@MainActivity, "consumed all", Toast.LENGTH_SHORT).show()
            }


        }.invokeOnCompletion {
            progressBar.isVisible = false
        }
    }

    private fun purchaseItem(sku: String) {
        if (progressBar.isVisible) return
        progressBar.isVisible = true
        launch(exceptionHandler) {
            Log.d("s", "click")
            val iab = MarketUtil.iab!!

            val purchase = iab.launchPurchase(
                this@MainActivity, sku, RC_PURCHASE, System.currentTimeMillis().toString()
            )

            Log.d("s", purchase.toString())

            recyclerView2.post {
                if (purchaseList.find { p -> p.token == purchase.token } == null) {
                    purchaseList.add(purchase)
                    purchaseAdapter?.notifyItemInserted(purchaseList.lastIndex)
                }
            }

            Toast.makeText(this@MainActivity, "purchased", Toast.LENGTH_SHORT).show()

        }.invokeOnCompletion {
            progressBar.isVisible = false
        }
    }

    private fun consumeItem(purchase: Purchase) {
        if (progressBar.isVisible) return
        progressBar.isVisible = true
        launch(exceptionHandler) {
            val iab = MarketUtil.iab!!

            iab.consume(purchase)

            purchaseList.indexOfFirst { p -> p.sku === purchase.sku }.also {
                purchaseAdapter?.removeItem(it)
            }

            Toast.makeText(this@MainActivity, "consumed", Toast.LENGTH_SHORT).show()

        }.invokeOnCompletion {
            progressBar.isVisible = false
        }
    }

    private fun setupList() {

        recyclerView.simple(itemList) {
            itemAdapter = adapter
            rows = 1
            itemMargin(16)

            itemHolder(R.layout.item_view) {

                itemView.setOnClickListener {
                    purchaseItem(item.sku)
                }

                bind { view, item, position ->
                    view.iv_title.text = item.title
                    view.iv_price.text = item.price
                }

            }
        }

        recyclerView2.simple(purchaseList) {
            purchaseAdapter = adapter
            rows = 1
            itemMargin(16)

            itemHolder(R.layout.item_view) {

                itemView.setOnClickListener {
                    consumeItem(item)
                }

                bind { view, item, _ ->
                    val i = itemList.find { i -> i.sku == item.sku }
                    view.iv_title.text = i?.title
                    view.iv_price.text = i?.price
                }

            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (MarketUtil.iab?.handleActivityResult(requestCode, resultCode, data) == true) return

        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d("s", account.idToken)
                Toast.makeText(this, account.email, Toast.LENGTH_SHORT).show()
            } catch (e: ApiException) {
                Toast.makeText(
                    this,
                    GoogleSignInStatusCodes.getStatusCodeString(e.statusCode),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        MarketUtil.targetMarket?.updater?.releaseService(this)
        MarketUtil.targetMarket?.iab?.releaseService(this)
    }

}