package com.score.shopz.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;
import com.score.shopz.R;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ShopzScannerActivity extends Activity implements ZXingScannerView.ResultHandler {

    private static final String TAG = ShopzScannerActivity.class.getName();

    private ZXingScannerView mScannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZXingScannerView(this);
        setContentView(mScannerView);

        initActionBar();
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result result) {
        Log.v(TAG, "Scan result " + result.getText());
        Log.v(TAG, "Scan barcode format " + result.getBarcodeFormat().toString());

        // create Payz object
        this.finish();
        Toast.makeText(ShopzScannerActivity.this, "Payment done", Toast.LENGTH_LONG).show();
    }

    /**
     * Initialize action bar
     */
    private void initActionBar() {
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/vegur_2.otf");

        // Set up action bar.
        // Specify that the Home button should show an "Up" caret, indicating that touching the
        // button will take the user one step up in the application's hierarchy.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("QR Code");
        getActionBar().setBackgroundDrawable(new ColorDrawable(0xff666666));

        // set custom font for
        //  1. action bar title
        //  2. other ui texts
        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView actionBarTitle = (TextView) (findViewById(titleId));
        actionBarTitle.setTextColor(getResources().getColor(R.color.white));
        actionBarTitle.setTypeface(typeface);
    }

}