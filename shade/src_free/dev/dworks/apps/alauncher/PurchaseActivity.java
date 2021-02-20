package dev.dworks.apps.alauncher;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.launcher3.R;

import amirz.App;
import amirz.helpers.Settings;
import amirz.shade.ShadeFont;
import amirz.shade.customization.ShadeStyle;
import needle.Needle;
import needle.UiRelatedTask;

import static dev.dworks.apps.alauncher.AppFlavour.BILLING_ACTION;

public class PurchaseActivity extends Activity {

    public static final String TAG = PurchaseActivity.class.getSimpleName();
    private Button purchaseButton;
    private String purchaseText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ShadeFont.override(this);
        ShadeStyle.override(this);
        ShadeStyle.overrideTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);

        int color = ShadeStyle.getPrimaryColor(this);
        ColorDrawable colorDrawable = new ColorDrawable(color);
        getActionBar().setBackgroundDrawable(colorDrawable);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(getString(R.string.support_app));
        getActionBar().setElevation(0);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setStatusBarColor(getStatusBarColor(color));

        View background = findViewById(R.id.app_bar);
        background.setBackgroundColor(color);
        purchaseButton = (Button) findViewById(R.id.purchase_button);
        purchaseButton.setTextColor(color);
        purchaseText = getString(R.string.purchase);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        updatePrice();
                    }
                },  new IntentFilter(BILLING_ACTION));
        App.getInstance().initializeBilling(this);
        initControls();
    }

    private void initControls() {

        Button restoreButton = (Button) findViewById(R.id.restore_button);
        restoreButton.setEnabled(true);
        purchaseButton.setEnabled(true);

        restoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restorePurchase();
            }
        });

        updatePrice();
        purchaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(App.isPurchased()){
                    Settings.showSnackBar(PurchaseActivity.this, R.string.thank_you);
                } else {
                    App.getInstance().purchase(PurchaseActivity.this, App.getPurchaseId());
                }
            }
        });
    }

    private void updatePrice() {
        if(!AppFlavour.isBillingSupported()){
            return;
        }
        String purchaseId = AppFlavour.getPurchaseId();
        String price = App.getInstance().getPurchasePrice(purchaseId);
        if (!TextUtils.isEmpty(price)) {
            purchaseButton.setText(purchaseText + " with " + price);
        }
    }

    private void restorePurchase() {
        Needle.onBackgroundThread().execute(new UiRelatedTask<Boolean>(){
            @Override
            protected Boolean doWork() {
                App.getInstance().loadPurchaseItems(PurchaseActivity.this);
                return true;
            }

            @Override
            protected void thenDoUiRelatedWork(Boolean aBoolean) {
                onPurchaseRestored();
            }
        });
    }

    public void onPurchaseRestored(){
        if (App.isPurchased()) {
            Settings.showSnackBar(this, R.string.restored_previous_purchase_please_restart);
        } else {
            Settings.showSnackBar(this, R.string.could_not_restore_purchase);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        App.getInstance().releaseBilling();
        super.onDestroy();
    }

    public static int getStatusBarColor(int color1) {
        int color2 = Color.parseColor("#000000");
        return blendColors(color1, color2, 0.9f);
    }

    public static int blendColors(int color1, int color2, float ratio) {
        final float inverseRation = 1f - ratio;
        float r = (Color.red(color1) * ratio) + (Color.red(color2) * inverseRation);
        float g = (Color.green(color1) * ratio) + (Color.green(color2) * inverseRation);
        float b = (Color.blue(color1) * ratio) + (Color.blue(color2) * inverseRation);
        return Color.rgb((int) r, (int) g, (int) b);
    }
}