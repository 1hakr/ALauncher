package dev.dworks.apps.alauncher;

import android.content.Context;
import android.content.Intent;

/**
 * Created by HaKr on 16/05/17.
 */

public abstract class AppFlavour extends AppFlavourExtended {

	@Override
	public void onCreate() {
		super.onCreate();
	}

	public void openPurchaseActivity(Context context){
		context.startActivity(new Intent(context, PurchaseActivity.class));
	}
}