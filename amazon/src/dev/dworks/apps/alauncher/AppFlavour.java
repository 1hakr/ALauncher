package dev.dworks.apps.alauncher;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import com.amazon.device.iap.PurchasingService;
import com.android.launcher3.BuildConfig;
import com.android.launcher3.R;
import com.billing.PurchaseServiceListener;
import com.eggheadgames.inapppayments.IAPManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dev.dworks.apps.alauncher.helpers.PreferenceUtils;

/**
 * Created by HaKr on 16/05/17.
 */

public class AppFlavour extends Application implements PurchaseServiceListener {

	private static final String PURCHASE_PRODUCT_ID = "purchase_product_id";
	public static final String PURCH_ID = "dev.dworks.apps.alauncher" + ".purch";
	public static final String PURCHASED = "purchased";
	private static final int IAP_ID_CODE = 1;

	private List<String> skuList;
	private String price;
	private String productId;


	@Override
	public void onCreate() {
		super.onCreate();
	}

	public void initializeBilling() {
		skuList = new ArrayList<>();
		skuList.add(getPurchaseId());
		IAPManager.build(getApplicationContext(), IAPManager.BUILD_TARGET_AMAZON, skuList);
		IAPManager.addPurchaseListener(this);
		IAPManager.init("", BuildConfig.DEBUG);
	}

	public static boolean isPurchased() {
		return BuildConfig.DEBUG || PreferenceUtils.getBooleanPrefs(PURCHASED);
	}

	public static String getPurchaseId(){
		return PURCH_ID + ".pro" + IAP_ID_CODE;
	}

	public static String getPurchasedProductId(){
		String productId = PreferenceUtils.getStringPrefs(PURCHASE_PRODUCT_ID);
		return !TextUtils.isEmpty(productId) ? productId : getPurchaseId();
	}

	public boolean handleActivityResult(int requestCode, int resultCode, Intent data){
		return false;
	}

	public void releaseBillingProcessor() {
		IAPManager.destroy();
	}

	public boolean isBillingSupported() {
		return null != IAPManager.getBillingService();
	}

	public void loadOwnedPurchases() {
		if(!isBillingSupported()){
			return;
		}
		PurchasingService.getPurchaseUpdates(true);
	}

	public void reloadPurchase() {
		if(!isBillingSupported()){
			return;
		}
		PurchasingService.getPurchaseUpdates(true);
	}

	public void onPurchaseHistoryRestored() {
		reloadPurchase();
	}

	public void purchase(Activity activity, String productId){
		if(isBillingSupported()) {
			IAPManager.buy(activity, productId, 0);
		} else {
			Toast.makeText(activity, "Billing not supported", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onPricesUpdated(Map<String, String> map) {
		price = map.get(getPurchaseId());
	}

	@Override
	public void onProductPurchased(String productId) {
		boolean isPurchased = !TextUtils.isEmpty(productId);
		Toast.makeText(getApplicationContext(), R.string.thank_you, Toast.LENGTH_SHORT).show();
		PreferenceUtils.set(PURCHASE_PRODUCT_ID, productId);
		PreferenceUtils.set(PURCHASED, isPurchased);
	}

	@Override
	public void onProductRestored(String productId) {
		boolean isPurchased = !TextUtils.isEmpty(productId);
		PreferenceUtils.set(PURCHASE_PRODUCT_ID, productId);
		PreferenceUtils.set(PURCHASED, isPurchased);
	}
}