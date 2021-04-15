package dev.dworks.apps.alauncher.misc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.ArrayMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.List;


public class BillingHelper implements BillingClientStateListener, SkuDetailsResponseListener,
        PurchasesUpdatedListener {

    private Context context;
    private BillingClient mBillingClient;
    private BillingListener mBillingListener;
    private List<String> skuInAppList = new ArrayList<>();
    private List<String> skuSubList = new ArrayList<>();
    private ArrayMap<String, SkuDetails> skuDetailsMap = new ArrayMap<>();
    private ArrayMap<String, Purchase> purchaseDetailsMap = new ArrayMap<>();
    private boolean mSubscriptionSupported = false;
    private Activity mCurrentActivity;

    /**
     * To instantiate the object
     *  @param context           It will be used to get an application context to bind to the in-app billing service.
     * @param listener          Your listener to get the response for your query.
     */
    public BillingHelper(Context context, BillingListener listener) {
        this.context = context;
        this.mBillingListener = listener;
    }

    public void setSkuInAppList(List<String> skuInAppList) {
        this.skuInAppList = skuInAppList;
    }

    public void setSkuSubList(List<String> skuSubList) {
        this.skuSubList = skuSubList;
        this.mSubscriptionSupported = true;
    }

    public void initialize(){
        this.mBillingClient = BillingClient.newBuilder(context)
                .enablePendingPurchases()
                .setListener(this)
                .build();
        if (!mBillingClient.isReady()) {
            startConnection();
        }
    }

    public void setCurrentActivity(Activity activity) {
        this.mCurrentActivity = activity;
    }

    public ArrayMap<String, Purchase> getPurchaseDetailsMap() {
        return purchaseDetailsMap;
    }

    @Override
    public void onBillingSetupFinished(BillingResult billingResult) {
        int billingResponseCode = billingResult.getResponseCode();
        if (billingResponseCode == BillingClient.BillingResponseCode.OK) {
            getOwnedItems();
            getSKUDetails();
        } else if (mBillingListener != null) {
            mBillingListener.onPurchaseError(mCurrentActivity, billingResponseCode);
        }
    }

    @Override
    public void onBillingServiceDisconnected() {

    }

    @Override
    public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {
        int responseCode = billingResult.getResponseCode();
        if (responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
            synchronized (skuDetailsMap) {
                if(null != skuDetailsList && !skuDetailsList.isEmpty()) {
                    for (SkuDetails skuDetails : skuDetailsList) {
                        skuDetailsMap.put(skuDetails.getSku(), skuDetails);
                    }
                }
                if (mBillingListener != null) {
                    mBillingListener.onSkuListResponse(skuDetailsMap);
                }
            }
        } else if (mBillingListener != null) {
            mBillingListener.onPurchaseError(mCurrentActivity, responseCode);
        }
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
        int responseCode = billingResult.getResponseCode();
        if (responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            //here when purchase completed
            processPurchases(purchases, false);
        } else if (responseCode == BillingClient.BillingResponseCode.SERVICE_DISCONNECTED) {
            startConnection();
        } else if (responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            getOwnedItems();
        } else if (mBillingListener != null) {
            mBillingListener.onPurchaseError(mCurrentActivity, responseCode);
        }
    }

    private void processPurchases(List<Purchase> purchases, boolean restore){
        if(null != purchases && !purchases.isEmpty()) {
            for (Purchase purchase : purchases) {
                String type = purchase.getSku();
                if (isValidPurchase(purchase)) {
                    if (skuInAppList.contains(type)) {
                        //consumePurchase(purchase, restore);
                    }

                    if(!purchase.isAcknowledged()) {
                        acknowledgePurchase(purchase, restore);
                    }
                }
            }
        }
    }

    /**
     * To establish the connection with play library
     * It will be used to notify that setup is complete and the billing
     * client is ready. You can query whatever you want.
     */
    private void startConnection() {
        mBillingClient.startConnection(this);
    }

    public void getOwnedItems() {
        getPurchasedItems();
        if(mSubscriptionSupported) {
            getSubscribedItems();
        }
    }
    /**
     * Get purchases details for all the items bought within your app.
     */
    public void getPurchasedItems() {
        Purchase.PurchasesResult purchasesResult = mBillingClient.queryPurchases(BillingClient.SkuType.INAPP);
        List<Purchase> purchaseList = purchasesResult.getPurchasesList();
        synchronized (purchaseDetailsMap) {
            if(null != purchaseList && !purchaseList.isEmpty()) {
                for (Purchase purchase : purchaseList) {
                    purchaseDetailsMap.put(purchase.getSku(), purchase);
                }
            }
            if (mBillingListener != null) {
                processPurchases(purchaseList, true);
                mBillingListener.onPurchaseHistoryResponse(purchaseList);
            }
        }
    }

    /**
     * Get purchases details for all the items bought within your app.
     */
    public void getSubscribedItems() {
        Purchase.PurchasesResult purchasesResult = mBillingClient.queryPurchases(BillingClient.SkuType.SUBS);
        List<Purchase> purchaseList = purchasesResult.getPurchasesList();
        synchronized (purchaseDetailsMap) {
            if(null != purchaseList && !purchaseList.isEmpty()) {
                for (Purchase purchase : purchaseList) {
                    purchaseDetailsMap.put(purchase.getSku(), purchase);
                }
            }
            if (mBillingListener != null && !purchaseList.isEmpty()) {
                processPurchases(purchaseList, true);
                mBillingListener.onPurchaseHistoryResponse(purchaseList);
            }
        }
    }

    /**x
     * Perform a network query to get SKU details and return the result asynchronously.
     */
    public void getSKUDetails() {
        getSKUInAppDetails();
        getSKUSubDetails();
    }

    /**
     * Perform a network query to get In App SKU details and return the result asynchronously.
     */
    public void getSKUInAppDetails() {
        if(null == skuInAppList || skuInAppList.isEmpty()){
            return;
        }
        SkuDetailsParams skuParams = SkuDetailsParams.newBuilder()
                .setType(BillingClient.SkuType.INAPP)
                .setSkusList(skuInAppList)
                .build();
        mBillingClient.querySkuDetailsAsync(skuParams, this);
    }

    /**
     * Perform a network query to get Sub SKU details and return the result asynchronously.
     */
    public void getSKUSubDetails() {
        if(null == skuSubList || skuSubList.isEmpty()){
            return;
        }
        SkuDetailsParams skuParams = SkuDetailsParams.newBuilder()
                .setType(BillingClient.SkuType.SUBS)
                .setSkusList(skuSubList)
                .build();
        mBillingClient.querySkuDetailsAsync(skuParams, this);
    }

    /**
     * Initiate the billing flow for an in-app purchase or subscription.
     *
     * @param productId product Id of the product to be purchased
     *                   Developer console.
     */
    public void launchBillingFLow(Activity activity, final String productId) {
        synchronized (skuDetailsMap) {
            SkuDetails skuDetails = skuDetailsMap.get(productId);
            if(null == skuDetails){
                return;
            }
            if (mBillingClient.isReady()) {
                BillingFlowParams mBillingFlowParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(skuDetails)
                        .build();
                mCurrentActivity = activity;
                mBillingClient.launchBillingFlow(activity, mBillingFlowParams);
            }
        }
    }

    //This is for Non-Consumable product
    public void acknowledgePurchase(Purchase purchase, boolean restore) {
        AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();
        mBillingClient.acknowledgePurchase(acknowledgePurchaseParams, new AcknowledgePurchaseResponseListener() {
            @Override
            public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
                int responseCode = billingResult.getResponseCode();
                if(responseCode == BillingClient.BillingResponseCode.OK){

                } else if (mBillingListener != null) {
                    mBillingListener.onPurchaseError(mCurrentActivity, responseCode);
                }
            }
        });

        if (mBillingListener != null) {
            mBillingListener.onPurchaseCompleted(mCurrentActivity, purchase, restore);
        }
    }

    //This is for Consumable product
    public void consumePurchase(Purchase purchase, boolean restore) {
        ConsumeParams consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();
        mBillingClient.consumeAsync(consumeParams, new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
                int responseCode = billingResult.getResponseCode();
                if(responseCode == BillingClient.BillingResponseCode.OK){

                } else if (mBillingListener != null) {
                    mBillingListener.onPurchaseError(mCurrentActivity, responseCode);
                }
            }
        });

        if (mBillingListener != null) {
            mBillingListener.onPurchaseCompleted(mCurrentActivity, purchase, restore);
        }
    }

    public String getSkuPrice(String productId){
        String priceValue = "";
        synchronized (skuDetailsMap) {
            SkuDetails sku = skuDetailsMap.get(productId);
            if (null != sku) {
                priceValue = sku.getPrice();
            }
        }
        return priceValue;
    }

    public SkuDetails getSkuDetails(String productId){
        synchronized (skuDetailsMap) {
            return skuDetailsMap.get(productId);
        }
    }

    public Purchase getPurchaseDetails(String productId){
        synchronized (purchaseDetailsMap) {
            return purchaseDetailsMap.get(productId);
        }
    }

    private static boolean isSignatureValid(Purchase purchase) {
        return BillingSecurity.verifyPurchase(BillingSecurity.BASE_64_ENCODED_PUBLIC_KEY,
                purchase.getOriginalJson(), purchase.getSignature()) && BillingSecurity.verifyMerchant(purchase);
    }

    private static Intent getBindServiceIntent() {
        Intent intent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        intent.setPackage("com.android.vending");
        return intent;
    }

    public static boolean isBillingAvailable(Context context){
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentServices(getBindServiceIntent(), 0);
        return list != null && list.size() > 0;
    }

    public static boolean isValidPurchase(Purchase purchase){
        return purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED
                && isSignatureValid(purchase);
    }

    /**
     * Call this method once you are done with this BillingClient reference.
     */
    public void endConnection() {
        if (mBillingClient != null && mBillingClient.isReady()) {
            mBillingClient.endConnection();
            mBillingClient = null;
        }
    }

    /**
     * Listener interface for handling the various responses of the Purchase helper util
     */
    public interface BillingListener {
        void onSkuListResponse(ArrayMap<String, SkuDetails> skuDetailsMap);
        void onPurchaseHistoryResponse(List<Purchase> purchasedList);
        void onPurchaseCompleted(Activity activity, Purchase purchaseItem, boolean restore);
        void onPurchaseError(Activity activity, int errorCode);
    }
}