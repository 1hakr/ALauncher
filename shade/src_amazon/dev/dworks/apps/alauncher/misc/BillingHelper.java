package dev.dworks.apps.alauncher.misc;

import android.app.Activity;
import android.content.Context;
import android.util.ArrayMap;

import com.amazon.device.iap.PurchasingListener;
import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.model.FulfillmentResult;
import com.amazon.device.iap.model.Product;
import com.amazon.device.iap.model.ProductDataResponse;
import com.amazon.device.iap.model.ProductType;
import com.amazon.device.iap.model.PurchaseResponse;
import com.amazon.device.iap.model.PurchaseUpdatesResponse;
import com.amazon.device.iap.model.Receipt;
import com.amazon.device.iap.model.UserDataResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.amazon.device.iap.model.PurchaseResponse.RequestStatus.ALREADY_PURCHASED;


public class BillingHelper implements PurchasingListener {

    private Context context;
    private BillingListener mBillingListener;
    private List<String> skuInAppList = new ArrayList<>();
    private List<String> skuSubList = new ArrayList<>();
    private ArrayMap<String, Product> skuDetailsMap = new ArrayMap<String, Product>();
    private ArrayMap<String, Receipt> purchaseDetailsMap = new ArrayMap<String, Receipt>();
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
        PurchasingService.registerListener(this.context, this);
        getOwnedItems();
        getSKUDetails();
    }

    public void setCurrentActivity(Activity activity) {
        this.mCurrentActivity = activity;
    }

    public ArrayMap<String, Receipt> getPurchaseDetailsMap() {
        return purchaseDetailsMap;
    }

    public void getOwnedItems() {
        getPurchasedItems();
    }
    /**
     * Get purchases details for all the items bought within your app.
     */
    public void getPurchasedItems() {
        PurchasingService.getPurchaseUpdates(true);
    }

    /**
     * Get purchases details for all the items bought within your app.
     */
    public void getSubscribedItems() {
        PurchasingService.getPurchaseUpdates(true);
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
        Set<String> keys = new HashSet<>();
        keys.addAll(this.skuInAppList);
        PurchasingService.getProductData(keys);
    }

    /**
     * Perform a network query to get Sub SKU details and return the result asynchronously.
     */
    public void getSKUSubDetails() {
        if(null == skuSubList || skuSubList.isEmpty()){
            return;
        }
        Set<String> keys = new HashSet<>();
        keys.addAll(this.skuSubList);
        PurchasingService.getProductData(keys);
    }

    /**
     * Initiate the billing flow for an in-app purchase or subscription.
     *
     * @param productId product Id of the product to be purchased
     *                   Developer console.
     */
    public void launchBillingFLow(Activity activity, final String productId) {
        synchronized (skuDetailsMap) {
            Product skuDetails = skuDetailsMap.get(productId);
            if(null == skuDetails){
                return;
            }
            mCurrentActivity = activity;
            PurchasingService.purchase(productId);
        }
    }

    //This is for Non-Consumable product
    public void acknowledgePurchase(Receipt receipt, boolean restore) {
        PurchasingService.notifyFulfillment(receipt.getReceiptId(), FulfillmentResult.FULFILLED);
        if (mBillingListener != null) {
            mBillingListener.onPurchaseCompleted(mCurrentActivity, receipt, restore);
        }
    }

    //This is for Consumable product
    public void consumePurchase(Receipt receipt, boolean restore) {
        PurchasingService.notifyFulfillment(receipt.getReceiptId(), FulfillmentResult.FULFILLED);
        if (mBillingListener != null) {
            mBillingListener.onPurchaseCompleted(mCurrentActivity, receipt, restore);
        }
    }

    public String getSkuPrice(String productId){
        String priceValue = "";
        synchronized (skuDetailsMap) {
            Product sku = skuDetailsMap.get(productId);
            if (null != sku) {
                priceValue = sku.getPrice();
            }
        }
        return priceValue;
    }

    public Product getSkuDetails(String productId){
        synchronized (skuDetailsMap) {
            return skuDetailsMap.get(productId);
        }
    }

    public Receipt getPurchaseDetails(String productId){
        synchronized (purchaseDetailsMap) {
            return purchaseDetailsMap.get(productId);
        }
    }

    private boolean isSignatureValid(Receipt purchase) {
        return true;
    }

    public static boolean isBillingAvailable(Context context){
        return true;
    }

    /**
     * Call this method once you are done with this BillingClient reference.
     */
    public void endConnection() {

    }

    @Override
    public void onUserDataResponse(UserDataResponse userDataResponse) {

    }

    @Override
    public void onProductDataResponse(ProductDataResponse response) {
        final ProductDataResponse.RequestStatus status = response.getRequestStatus();

        if (status == ProductDataResponse.RequestStatus.SUCCESSFUL) {
            final Set<String> unavailableSkus = response.getUnavailableSkus();
            Map<String, Product> productData = response.getProductData();
            synchronized (skuDetailsMap) {
                if(null != productData && !productData.isEmpty()) {
                    for (Map.Entry<String, Product> entry : productData.entrySet()) {
                        Product product = productData.get(entry.getKey());
                        skuDetailsMap.put(product.getSku(), product);
                    }
                }
                if (mBillingListener != null) {
                    mBillingListener.onSkuListResponse(skuDetailsMap);
                }
            }
        } else if (mBillingListener != null) {
            mBillingListener.onPurchaseError(mCurrentActivity, PurchaseResponse.RequestStatus.NOT_SUPPORTED);
        }
    }

    @Override
    public void onPurchaseResponse(PurchaseResponse response) {
        final PurchaseResponse.RequestStatus status = response.getRequestStatus();
        final Receipt receipt;
        if (status == PurchaseResponse.RequestStatus.SUCCESSFUL) {
            //here when purchase completed
            receipt = response.getReceipt();
            if (isSignatureValid(receipt)) {
                processPurchase(receipt, false);
            }
        } else if (status == ALREADY_PURCHASED) {
            getOwnedItems();
        } else if (mBillingListener != null) {
            mBillingListener.onPurchaseError(mCurrentActivity, status);
        }
    }

    private void processPurchase(Receipt receipt, boolean restore){
        if (receipt.getProductType() == ProductType.SUBSCRIPTION) {
            consumePurchase(receipt, restore);
        } else {
            acknowledgePurchase(receipt, restore);
        }
    }

    @Override
    public void onPurchaseUpdatesResponse(PurchaseUpdatesResponse response) {
        if (response == null) {
            return;
        }
        final PurchaseUpdatesResponse.RequestStatus status = response.getRequestStatus();
        if (status == PurchaseUpdatesResponse.RequestStatus.SUCCESSFUL) {
            Receipt[] receiptsList = response.getReceipts().toArray(new Receipt[0]);
            synchronized (purchaseDetailsMap) {
                if(null != receiptsList && receiptsList.length != 0) {
                    for (Receipt receipt : receiptsList) {
                        if (receipt != null && !receipt.isCanceled()) {
                            purchaseDetailsMap.put(receipt.getSku(), receipt);
                            processPurchase(receipt, true);
                        }
                    }
                }
                if (mBillingListener != null) {
                    mBillingListener.onPurchaseHistoryResponse(Arrays.asList(receiptsList));
                }
            }
        } else if (mBillingListener != null) {
            mBillingListener.onPurchaseError(mCurrentActivity, PurchaseResponse.RequestStatus.NOT_SUPPORTED);
        }
    }

    /**
     * Listener interface for handling the various responses of the Purchase helper util
     */
    public interface BillingListener {
        void onSkuListResponse(ArrayMap<String, Product> skuDetailsMap);
        void onPurchaseHistoryResponse(List<Receipt> purchasedList);
        void onPurchaseCompleted(Activity activity, Receipt purchaseItem, boolean restore);
        void onPurchaseError(Activity activity, PurchaseResponse.RequestStatus errorCode);
    }
}