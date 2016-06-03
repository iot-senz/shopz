package com.score.shopz.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.score.senzc.pojos.Senz;
import com.score.shopz.R;
import com.score.shopz.pojos.Matm;
import com.score.shopz.utils.ActivityUtils;
import com.score.shopz.utils.SenzParser;

public class QrCodeActivity extends Activity {

    private static final String TAG = QrCodeActivity.class.getName();

    private BroadcastReceiver senzMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Got message from Senz service");
            handleMessage(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_code_generate_activity);

        initActionBar();
        initQrCodeContent();

        // register broadcast receiver
        registerReceiver(senzMessageReceiver, new IntentFilter("com.score.shopz.DATA_SENZ"));
    }

    @Override
    protected void onResume() {
        super.onResume();

        // register broadcast receiver
        registerReceiver(senzMessageReceiver, new IntentFilter("com.score.shopz.DATA_SENZ"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(senzMessageReceiver);
    }

    private void initQrCodeContent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String qrCodeContent = bundle.getString("EXTRA");
            generateQrCode(qrCodeContent);
        }
    }

    private void generateQrCode(String qrCodeContent) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(qrCodeContent, BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            ((ImageView) findViewById(R.id.qr_code)).setImageBitmap(bmp);
        } catch (WriterException e) {
            e.printStackTrace();
        }
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

    /**
     * Handle broadcast message receives
     * Need to handle registration success failure here
     *
     * @param intent intent
     */
    private void handleMessage(Intent intent) {
        String action = intent.getAction();

        if (action.equals("com.score.shopz.DATA_SENZ")) {
            Senz senz = intent.getExtras().getParcelable("SENZ");

            if (senz.getAttributes().containsKey("tid") && senz.getAttributes().containsKey("key")) {
                // Matm response received
                ActivityUtils.cancelProgressDialog();

                // create Matm object from senz
                Matm matm = SenzParser.getMatm(senz);

                // launch Matm activity
                Intent mapIntent = new Intent(this, ShopzScannerActivity.class);
                mapIntent.putExtra("EXTRA", matm);
                startActivity(mapIntent);
                this.finish();
                overridePendingTransition(R.anim.stay_in, R.anim.right_in);
            }
        }
    }

}
