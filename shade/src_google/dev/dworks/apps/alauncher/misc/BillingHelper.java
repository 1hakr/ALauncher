package dev.dworks.apps.alauncher.misc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.Looper;
import android.util.ArrayMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;

import java.util.ArrayList;
import java.util.List;

public class BillingHelper implements BillingClientStateListener, ProductDetailsResponseListener,
        PurchasesUpdatedListener {

    private static final long RECONNECT_TIMER_START_MILLISECONDS = 1L * 1000L;
    private static final long RECONNECT_TIMER_MAX_TIME_MILLISECONDS = 1000L * 60L * 15L; // 15 mins
    private static final Handler handler = new Handler(Looper.getMainLooper());
    private long reconnectMilliseconds = RECONNECT_TIMER_START_MILLISECONDS;

    private final Context context;
    private BillingClient mBillingClient;
    private final BillingListener mBillingListener;
    private List<QueryProductDetailsParams.Product> productInAppList = new ArrayList<>();
    private List<QueryProductDetailsParams.Product> productSubList = new ArrayList<>();
    private final ArrayMap<String, ProductDetails> productDetailsMap = new ArrayMap<>();
    private final ArrayMap<String, Purchase> purchaseDetailsMap = new ArrayMap<>();
    private boolean mSubscriptionSupported = false;
    private Activity mCurrentActivity;

    /**
     * To instantiate the object
     *
     * @param context  It will be used to get an application context to bind to the in-app billing service.
     * @param listener Your listener to get the response for your query.
     */
    public BillingHelper(Context context, BillingListener listener) {
        this.context = context;
        this.mBillingListener = listener;
    }

    public void setProductInAppList(List<String> productInAppList) {
        this.productInAppList = getProductList(productInAppList, BillingClient.ProductType.INAPP);

    }

    public void setProductSubList(List<String> productSubList) {
        this.productSubList = getProductList(productSubList, BillingClient.ProductType.SUBS);
        this.mSubscriptionSupported = true;
    }

    private List<QueryProductDetailsParams.Product> getProductList(List<String> productList, String productType) {
        List<QueryProductDetailsParams.Product> list = new ArrayList<>();
        for (String productId : productList) {
            QueryProductDetailsParams.Product product = QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productId)
                    .setProductType(productType)
                    .build();
            list.add(product);
        }
        return list;
    }

    public void initialize() {
        this.mBillingClient = BillingClient.newBuilder(context)
                .enablePendingPurchases()
                .setListener(this)
                .build();
        startConnection();
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
        String debugMessage = billingResult.getDebugMessage();
        if (billingResponseCode == BillingClient.BillingResponseCode.OK) {
            //getOwnedItems();
            getProductDetails();
        } else if (mBillingListener != null) {
            if (billingResponseCode != BillingClient.BillingResponseCode.BILLING_UNAVAILABLE) {
                retryBillingServiceConnectionWithExponentialBackoff();
            }
            mBillingListener.onPurchaseError(mCurrentActivity, billingResponseCode, debugMessage);
        }
    }

    @Override
    public void onBillingServiceDisconnected() {
        retryBillingServiceConnectionWithExponentialBackoff();
    }

    /**
     * Retries the billing service connection with exponential backoff, maxing out at the time
     * specified by RECONNECT_TIMER_MAX_TIME_MILLISECONDS.
     */
    private void retryBillingServiceConnectionWithExponentialBackoff() {
        handler.postDelayed(() -> startConnection(),
                reconnectMilliseconds);
        reconnectMilliseconds = Math.min(reconnectMilliseconds * 2,
                RECONNECT_TIMER_MAX_TIME_MILLISECONDS);
    }

    @Override
    public void onProductDetailsResponse(@NonNull BillingResult billingResult, @NonNull List<ProductDetails> list) {
        int responseCode = billingResult.getResponseCode();
        String debugMessage = billingResult.getDebugMessage();
        if (responseCode == BillingClient.BillingResponseCode.OK && list != null) {
            synchronized (productDetailsMap) {
                if (null != list && !list.isEmpty()) {
                    for (ProductDetails productDetails : list) {
                        productDetailsMap.put(productDetails.getProductId(), productDetails);
                    }
                }
                if (mBillingListener != null) {
                    mBillingListener.onProductListResponse(productDetailsMap);
                }
            }
        } else if (mBillingListener != null) {
            mBillingListener.onPurchaseError(mCurrentActivity, responseCode, debugMessage);
        }
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
        int responseCode = billingResult.getResponseCode();
        String debugMessage = billingResult.getDebugMessage();
        if (responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            //here when purchase completed
            processPurchases(purchases, false);
        } else if (responseCode == BillingClient.BillingResponseCode.SERVICE_DISCONNECTED) {
            retryBillingServiceConnectionWithExponentialBackoff();
        } else if (responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            getOwnedItems();
        } else if (mBillingListener != null) {
            mBillingListener.onPurchaseError(mCurrentActivity, responseCode, debugMessage);
        }
    }

    private void processPurchases(List<Purchase> purchases, boolean restore) {
        if (null != purchases && !purchases.isEmpty()) {
            for (Purchase purchase : purchases) {
                String type = purchase.getProducts().get(0);
                if (isValidPurchase(purchase)) {
                    if (productInAppList.contains(type)) {
                        //consumePurchase(purchase, restore);
                    }

                    if (!purchase.isAcknowledged()) {
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
        if (mBillingClient != null && !mBillingClient.isReady()) {
            mBillingClient.startConnection(this);
        }
    }

    public void getOwnedItems() {
        getPurchasedItems();
        if (mSubscriptionSupported) {
            getSubscribedItems();
        }
    }

    /**
     * Get purchases details for all the items bought within your app.
     */
    public void getPurchasedItems() {
        if (null == mBillingClient) {
            return;
        }
        mBillingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(), new PurchasesResponseListener() {
            @Override
            public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> purchaseList) {
                synchronized (purchaseDetailsMap) {
                    if (null != purchaseList && !purchaseList.isEmpty()) {
                        for (Purchase purchase : purchaseList) {
                            purchaseDetailsMap.put(purchase.getProducts().get(0), purchase);
                        }
                    }
                    if (mBillingListener != null) {
                        processPurchases(purchaseList, true);
                        mBillingListener.onPurchaseHistoryResponse(purchaseList);
                    }
                }
            }
        });
    }

    /**
     * Get purchases details for all the   items bought within your app.
     */
    public void getSubscribedItems() {
        if (null == mBillingClient) {
            return;
        }
        mBillingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build(), new PurchasesResponseListener() {
            @Override
            public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> purchaseList) {
                synchronized (purchaseDetailsMap) {
                    if (null != purchaseList && !purchaseList.isEmpty()) {
                        for (Purchase purchase : purchaseList) {
                            purchaseDetailsMap.put(purchase.getProducts().get(0), purchase);
                        }
                    }
                    if (mBillingListener != null) {
                        processPurchases(purchaseList, true);
                        mBillingListener.onPurchaseHistoryResponse(purchaseList);
                    }
                }
            }
        });
    }

    /**
     * x
     * Perform a network query to get Product details and return the result asynchronously.
     */
    public void getProductDetails() {
        getProductInAppDetails();
        getProductSubDetails();
    }

    /**
     * Perform a network query to get In App Product details and return the result asynchronously.
     */
    public void getProductInAppDetails() {
        if (null == productInAppList || productInAppList.isEmpty()) {
            return;
        }
        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productInAppList)
                .build();
        mBillingClient.queryProductDetailsAsync(params, this);
    }

    /**
     * Perform a network query to get Sub Product details and return the result asynchronously.
     */
    public void getProductSubDetails() {
        if (null == productSubList || productSubList.isEmpty()) {
            return;
        }
        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productSubList)
                .build();
        mBillingClient.queryProductDetailsAsync(params, this);
    }

    /**
     * Initiate the billing flow for an in-app purchase or subscription.
     *
     * @param productId product Id of the product to be purchased
     *                  Developer console.
     */
    public void launchBillingFLow(Activity activity, final String productId) {
        if (null == mBillingClient) {
            return;
        }
        synchronized (productDetailsMap) {
            ProductDetails productDetails = productDetailsMap.get(productId);
            if (null == productDetails) {
                return;
            }
            if (mBillingClient.isReady()) {
                BillingFlowParams.ProductDetailsParams.Builder paramsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails);
                if (productDetails.getProductType().equals(BillingClient.ProductType.SUBS)) {
                    String offerToken = productDetails
                            .getSubscriptionOfferDetails().get(0)
                            .getOfferToken();
                    paramsBuilder.setOfferToken(offerToken);
                }
                BillingFlowParams.ProductDetailsParams params = paramsBuilder.build();
                List<BillingFlowParams.ProductDetailsParams> list = new ArrayList<>();
                list.add(params);
                BillingFlowParams mBillingFlowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(list)
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
                String debugMessage = billingResult.getDebugMessage();
                if (responseCode == BillingClient.BillingResponseCode.OK) {

                } else if (mBillingListener != null) {
                    mBillingListener.onPurchaseError(mCurrentActivity, responseCode, debugMessage);
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
                String debugMessage = billingResult.getDebugMessage();
                if (responseCode == BillingClient.BillingResponseCode.OK) {

                } else if (mBillingListener != null) {
                    mBillingListener.onPurchaseError(mCurrentActivity, responseCode, debugMessage);
                }
            }
        });

        if (mBillingListener != null) {
            mBillingListener.onPurchaseCompleted(mCurrentActivity, purchase, restore);
        }
    }

    public BillingPricing getPurchasePricingDetails(String productId) {
        BillingPricing billingPricing = null;
        synchronized (productDetailsMap) {
            ProductDetails productDetails = productDetailsMap.get(productId);
            if (null != productDetails) {
                ProductDetails.OneTimePurchaseOfferDetails purchaseOfferDetails = productDetails.getOneTimePurchaseOfferDetails();
                if (null != purchaseOfferDetails) {
                    billingPricing = new BillingPricing(purchaseOfferDetails);
                }
            }
        }
        return billingPricing;
    }

    public BillingPricing getSubscriptionPricingDetails(String productId) {
        BillingPricing billingPricing = null;
        List<ProductDetails.SubscriptionOfferDetails> purchaseOfferDetails;
        synchronized (productDetailsMap) {
            ProductDetails productDetails = productDetailsMap.get(productId);
            if (null != productDetails) {
                purchaseOfferDetails = productDetails.getSubscriptionOfferDetails();
                if (null != purchaseOfferDetails) {
                    List<ProductDetails.PricingPhase> list = purchaseOfferDetails.get(0).getPricingPhases().getPricingPhaseList();
                    if (null != list) {
                        ProductDetails.PricingPhase pricingPhase = list.get(0);
                        if (null != pricingPhase) {
                            billingPricing = new BillingPricing(pricingPhase);
                        }
                    }
                }
            }
        }
        return billingPricing;
    }


    public ProductDetails getProductDetails(String productId) {
        synchronized (productDetailsMap) {
            return productDetailsMap.get(productId);
        }
    }

    public Purchase getPurchaseDetails(String productId) {
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

    public static boolean isBillingAvailable(Context context) {
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentServices(getBindServiceIntent(), 0);
        return list != null && list.size() > 0;
    }

    public static boolean isValidPurchase(Purchase purchase) {
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

    public static final class BillingPricing {
        private final String formattedPrice;
        private final long priceAmountMicros;
        private final String priceCurrencyCode;
        private final String billingPeriod;
        private final int billingCycleCount;
        private final int recurrenceMode;

        public int getBillingCycleCount() {
            return this.billingCycleCount;
        }

        public int getRecurrenceMode() {
            return this.recurrenceMode;
        }

        public long getPriceAmountMicros() {
            return this.priceAmountMicros;
        }

        @NonNull
        public String getBillingPeriod() {
            return this.billingPeriod;
        }

        @NonNull
        public String getFormattedPrice() {
            return this.formattedPrice;
        }

        @NonNull
        public String getPriceCurrencyCode() {
            return this.priceCurrencyCode;
        }

        BillingPricing(ProductDetails.PricingPhase pricing) {
            this.billingPeriod = pricing.getBillingPeriod();
            this.priceCurrencyCode = pricing.getPriceCurrencyCode();
            this.formattedPrice = pricing.getFormattedPrice();
            this.priceAmountMicros = pricing.getPriceAmountMicros();
            this.recurrenceMode = pricing.getRecurrenceMode();
            this.billingCycleCount = pricing.getBillingCycleCount();
        }

        BillingPricing(ProductDetails.OneTimePurchaseOfferDetails pricing) {
            this.billingPeriod = "";
            this.priceCurrencyCode = pricing.getPriceCurrencyCode();
            this.formattedPrice = pricing.getFormattedPrice();
            this.priceAmountMicros = pricing.getPriceAmountMicros();
            this.recurrenceMode = ProductDetails.RecurrenceMode.FINITE_RECURRING;
            this.billingCycleCount = 0;
        }
    }

    /**
     * Listener interface for handling the various responses of the Purchase helper util
     */
    public interface BillingListener {
        void onProductListResponse(ArrayMap<String, ProductDetails> productDetailsMap);

        void onPurchaseHistoryResponse(List<Purchase> purchasedList);

        void onPurchaseCompleted(Activity activity, Purchase purchaseItem, boolean restore);

        void onPurchaseError(Activity activity, int errorCode, String errorMessage);
    }
}