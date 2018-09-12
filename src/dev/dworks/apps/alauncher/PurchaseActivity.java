package dev.dworks.apps.alauncher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toolbar;

import com.android.launcher3.R;

import androidx.core.content.ContextCompat;
import dev.dworks.apps.alauncher.helpers.Utils;
import needle.Needle;
import needle.UiRelatedTask;

public class PurchaseActivity extends Activity {

    public static final String TAG = PurchaseActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitleTextAppearance(this, android.R.style.TextAppearance_Material_Widget_ActionBar_Title);
        int color = ContextCompat.getColor(this, R.color.colorAccent);
        mToolbar.setBackgroundColor(color);
        setActionBar(mToolbar);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(getString(R.string.support_app));
        getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccentSecondary));
        initControls();
        App.getInstance().initializeBilling();
    }

    private void initControls() {

        Button restoreButton = (Button) findViewById(R.id.restore_button);
        Button purchaseButton = (Button) findViewById(R.id.purchase_button);
        restoreButton.setEnabled(true);
        purchaseButton.setEnabled(true);

        restoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restorePurchase();
            }
        });

        purchaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(App.isPurchased()){
                    Utils.showSnackBar(PurchaseActivity.this, R.string.thank_you);
                } else {
                    App.getInstance().purchase(PurchaseActivity.this, App.getPurchaseId());
                }
            }
        });
    }

    private void restorePurchase() {
        Utils.showSnackBar(this, R.string.restoring_purchase);
        Needle.onBackgroundThread().execute(new UiRelatedTask<Boolean>(){
            @Override
            protected Boolean doWork() {
                App.getInstance().loadOwnedPurchases();
                App.getInstance().onPurchaseHistoryRestored();
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
            Utils.showSnackBar(this, R.string.restored_previous_purchase_please_restart);
        } else {
            Utils.showSnackBar(this, R.string.could_not_restore_purchase);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!App.getInstance().handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
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
        //App.getInstance().releaseBillingProcessor();
        super.onDestroy();
    }
}