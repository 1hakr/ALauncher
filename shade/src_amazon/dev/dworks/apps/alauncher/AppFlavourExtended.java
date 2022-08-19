package dev.dworks.apps.alauncher;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.amazon.device.iap.model.Product;
import com.amazon.device.iap.model.PurchaseResponse;
import com.amazon.device.iap.model.Receipt;
import com.android.launcher3.BuildConfig;
import com.android.launcher3.R;

import java.util.ArrayList;
import java.util.List;

import amirz.App;
import amirz.helpers.Settings;
import dev.dworks.apps.alauncher.misc.BillingHelper;
import needle.Needle;
import needle.UiRelatedTask;

/**
 * Created by HaKr on 16/05/17.
 */

public abstract class AppFlavourExtended extends Application implements BillingHelper.BillingListener {
	private static final String PURCHASE_PRODUCT_ID = "purchase_product_id";
	public static final String PURCH_ID = BuildConfig.APPLICATION_ID + ".purch";
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
		return Settings.isProVersion() || PreferenceUtils.getBooleanPrefs(PURCHASED);
	}

	public void initializeBilling(Activity activity) {
		List<String> skuList = new ArrayList<>();
		skuList.add(getPurchaseId());
		billingHelper = new BillingHelper(this, this);
		billingHelper.setProductInAppList(skuList);
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
		Needle.onBackgroundThread().execute(new UiRelatedTask<Boolean>(){
			@Override
			protected Boolean doWork() {
				billingHelper.getOwnedItems();
				return true;
			}

			@Override
			protected void thenDoUiRelatedWork(Boolean aBoolean) {
				if (App.isPurchased()) {
					Settings.showSnackBar(activity, R.string.restored_previous_purchase_please_restart);
					finishDelayed(activity);
				} else {
					Settings.showSnackBar(activity, R.string.could_not_restore_purchase);
				}
			}
		});
	}

	public String getPurchasePrice(String productId){
		return billingHelper.getProductPrice(productId);
	}

	@Override
	public void onProductListResponse(ArrayMap<String, Product> productDetailsMap) {
		LocalBroadcastManager.getInstance(getApplicationContext())
				.sendBroadcast(new Intent(BILLING_ACTION));
	}

	@Override
	public void onPurchaseHistoryResponse(List<Receipt> purchasedList) {
		boolean isPurchased = false;
		String currentId = getPurchasedProductId();
		for (Receipt purchase: purchasedList) {
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
	public void onPurchaseCompleted(Activity activity, Receipt purchaseItem, boolean restore) {
		Settings.showSnackBar(activity, R.string.thank_you);
		PreferenceUtils.set(PURCHASE_PRODUCT_ID, purchaseItem.getSku());
		PreferenceUtils.set(PURCHASED, true);
		finishDelayed(activity);
	}

	@Override
	public void onPurchaseError(Activity activity, PurchaseResponse.RequestStatus errorCode) {
		String message = "";
		String action = "";
		switch (errorCode){
			case ALREADY_PURCHASED:
				if(!TextUtils.isEmpty(currentProductId)) {
					PreferenceUtils.set(PURCHASE_PRODUCT_ID, currentProductId);
				}
				message = "Purchase restored";
				break;
			case FAILED:
				message = "Payment flow cancelled";
				break;
			case INVALID_SKU:
				message = "Billing not available. Contact Developer";
				action = "Contact";
				break;
			case NOT_SUPPORTED:
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
				String finalMessage = message;
				Settings.showSnackBar(activity, message, action, new Runnable() {
					@Override
					public void run() {
						billingErrorAction(activity, finalMessage, errorCode);
					}
				});
			}
		} else {
			try {
				Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
			} catch (Exception e){ }
		}
	}

	private void billingErrorAction(Activity activity, String message, PurchaseResponse.RequestStatus errorCode){
		switch (errorCode){
			case NOT_SUPPORTED:
				Settings.openProAppLink(activity);
				break;
			default:
				String details = "Error:  \n";
				details	+= message+"\n";
				details += "Billing error code:"+errorCode;
				Settings.sendError(activity, details);
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

	protected static void finishDelayed(Activity activity){
		if(!Settings.isActivityAlive(activity)){
			return;
		}
		new Handler().postDelayed(new Runnable(){
			@Override
			public void run() {
				activity.finish();
			}
		}, 2000);
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