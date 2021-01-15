package dev.dworks.apps.alauncher;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;

/**
 * Created by HaKr on 16/05/17.
 */

public abstract class AppFlavour extends Application {

	public void initializeBilling() {

	}

	public static boolean isPurchased() {
		return true;
	}

	public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
		return true;
	}

	public void releaseBillingProcessor() {

	}

	public void loadOwnedPurchases() {

	}

	public void onPurchaseHistoryRestored() {

	}

	public static String getPurchaseId(){
		return "";
	}

	public void purchase(Activity activity, String productId){

	}

	public void openPurchaseActivity(Context context){

	}
}