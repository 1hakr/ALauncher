package dev.dworks.apps.alauncher;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.Purchase;
import com.android.launcher3.BuildConfig;
import com.android.launcher3.R;

import java.util.ArrayList;
import java.util.List;

import amirz.App;
import amirz.helpers.Settings;
import dev.dworks.apps.alauncher.misc.BillingHelper;

/**
 * Created by HaKr on 16/05/17.
 */

public abstract class AppFlavour extends Application implements BillingHelper.BillingListener {
	private static final String PURCHASE_PRODUCT_ID = "purchase_product_id";
    public static final String PURCH_ID = "dev.dworks.apps.alauncher.purch";
    public static final String PURCHASED = "purchased";
    private static final int IAP_ID_CODE = 1;
	public static final String BILLING_ACTION = "BillingInitialized";

	private String currentProductId = "";
	private BillingHelper billingHelper;

	@Override
	public void onCreate() {
		super.onCreate();
	}

	public static boolean isPurchased() {
		return Settings.isProVersion() || PreferenceUtils.getBooleanPrefs(PURCHASED)
				|| BuildConfig.DEBUG;
	}

	public void initializeBilling(Activity activity) {
		List<String> skuList = new ArrayList<>();
		skuList.add(getPurchaseId());
		billingHelper = new BillingHelper(this, this);
		billingHelper.setSkuInAppList(skuList);
		billingHelper.setCurrentActivity(activity);
		billingHelper.initialize();
	}

	public void releaseBilling() {
		if(null == billingHelper){
			return;
		}
		billingHelper.endConnection();
	}

	public void loadPurchaseItems(Activity activity){
		billingHelper.getOwnedItems();
	}

	public String getPurchasePrice(String productId){
		return billingHelper.getSkuPrice(productId);
	}

	@Override
	public void onSkuListResponse(ArrayMap<String, com.android.billingclient.api.SkuDetails> skuDetailsMap) {
		LocalBroadcastManager.getInstance(getApplicationContext())
				.sendBroadcast(new Intent(BILLING_ACTION));
	}

	@Override
	public void onPurchaseHistoryResponse(List<Purchase> purchasedList) {
		boolean isPurchased = false;
		String currentId = getPurchasedProductId();
		for (Purchase purchase: purchasedList) {
			if(currentId.equals(purchase.getSku())){
				isPurchased  = true;
				break;
			}
		}
		PreferenceUtils.set(PURCHASED, isPurchased);
		LocalBroadcastManager.getInstance(getApplicationContext())
				.sendBroadcast(new Intent(BILLING_ACTION));
	}

	@Override
	public void onPurchaseCompleted(Activity activity, Purchase purchaseItem) {
		Settings.showSnackBar(activity, R.string.thank_you);
		PreferenceUtils.set(PURCHASE_PRODUCT_ID, purchaseItem.getSku());
		PreferenceUtils.set(PURCHASED, true);
	}

	@Override
	public void onPurchaseError(Activity activity, int errorCode) {
		String message = "";
		String action = "";
		switch (errorCode){
			case BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED:
				if(!TextUtils.isEmpty(currentProductId)) {
					PreferenceUtils.set(PURCHASE_PRODUCT_ID, currentProductId);
				}
				message = "Purchase restored";
				break;
			case BillingClient.BillingResponseCode.USER_CANCELED:
				message = "Payment flow cancelled";
				break;
			case BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE:
			case BillingClient.BillingResponseCode.BILLING_UNAVAILABLE:
				message = "Billing not available. Contact Developer";
				action = "Contact";
				break;
			case BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED:
				message = "In App Purchase not available. Buy Pro App directly.";
				action = "Buy";
				break;
			default:
				message = "Something went wrong! error code="+ errorCode+". Contact Developer";
				action = "Contact";
				break;
		}
		if(null != activity) {
			if(TextUtils.isEmpty(action)) {
				Settings.showSnackBar(activity, message);
			} else {
				Settings.showSnackBar(activity, message, action, new Runnable() {
					@Override
					public void run() {
						billingErrorAction(activity, errorCode);
					}
				});
			}
		} else {
			try {
				Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
			} catch (Exception e){ }
		}
	}

	private void billingErrorAction(Activity activity, int errorCode){
		switch (errorCode){
			case BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED:
				Settings.openProAppLink(activity);
				break;
			default:
				Settings.showSnackBar(activity, "Billing error:"+errorCode);
				break;
		}
	}

	public static boolean isBillingSupported() {
		return BillingHelper.isBillingAvailable(App.getInstance().getApplicationContext());
	}

	public static String getPurchaseId(){
		return PURCH_ID+ ".pro" + IAP_ID_CODE;
	}

	public static String getPurchasedProductId(){
		String productId = PreferenceUtils.getStringPrefs(PURCHASE_PRODUCT_ID);
		return !TextUtils.isEmpty(productId) ? productId : getPurchaseId();
	}

	public void purchase(Activity activity, String productId){
		if(isBillingSupported()) {
			currentProductId = productId;
			billingHelper.launchBillingFLow(activity, productId);
		} else {
			Settings.showSnackBar(activity, "Billing not supported");
		}
	}

	public void openPurchaseActivity(Context context){
		context.startActivity(new Intent(context, PurchaseActivity.class));
	}
}