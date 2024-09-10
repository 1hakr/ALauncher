package dev.dworks.apps.alauncher;

import android.app.Application;
import android.content.Context;

/**
 * Created by HaKr on 16/05/17.
 */

public abstract class AppFlavour extends Application {

	public static boolean isPurchased() {
		return true;
	}

	public static String getPurchaseId(){
		return "";
	}

	public void openPurchaseActivity(Context context){

	}
}