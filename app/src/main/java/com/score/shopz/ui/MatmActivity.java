package com.score.shopz.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.score.senz.ISenzService;
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;
import com.score.senzc.pojos.User;
import com.score.shopz.R;
import com.score.shopz.pojos.Matm;
import com.score.shopz.utils.ActivityUtils;

import java.util.HashMap;

public class MatmActivity extends Activity implements NfcAdapter.CreateNdefMessageCallback, NfcAdapter.OnNdefPushCompleteCallback {

    private static final String TAG = MatmActivity.class.getName();

    // custom type face
    private Typeface typeface;

    // UI components
    private TextView textViewAppIcon;
    private TextView clickToPay;
    private TextView payAmountText;

    // use to track registration timeout
    private SenzCountDownTimer senzCountDownTimer;
    private boolean isResponseReceived;

    // service interface
    private ISenzService senzService;
    private boolean isServiceBound;

    // deals with NFC
    private NfcAdapter nfcAdapter;
    private PendingIntent nfcPendingIntent;
    private IntentFilter[] nfcIntentFilters;
    private String[][] nfcTechLists;


    // activity deal with matm
    private Matm thisMatm;
    private String receivedKey;

    // service connection
    private ServiceConnection senzServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d("TAG", "Connected with senz service");
            isServiceBound = true;
            senzService = ISenzService.Stub.asInterface(service);
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.d("TAG", "Disconnected from senz service");

            senzService = null;
            isServiceBound = false;
        }
    };

    // senz message receiver
    private BroadcastReceiver senzMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Got message from Senz service");
            handleSenzMessage(intent);
        }
    };

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.matm_layout);

        initNfc();
        initUi();
        initActionBar();
        initMatm();

        // service
        senzService = null;
        isServiceBound = false;

        // register broadcast receiver
        registerReceiver(senzMessageReceiver, new IntentFilter("com.score.shopz.DATA_SENZ"));

        // bind with senz service
        // bind to service from here as well
        if (!isServiceBound) {
            Intent intent = new Intent();
            intent.setClassName("com.score.shopz", "com.score.shopz.services.RemoteSenzService");
            bindService(intent, senzServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onResume() {
        super.onResume();

        // enable foreground dispatch
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, nfcPendingIntent, nfcIntentFilters, nfcTechLists);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPause() {
        super.onPause();

        // disable foreground dispatch
        if (nfcAdapter != null)
            nfcAdapter.disableForegroundDispatch(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNewIntent(Intent intent) {
        String action = intent.getAction();
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        Log.d(TAG, "New intent action " + action);
        Log.d(TAG, "New intent tag " + tag.toString());

        // parse through all NDEF messages and their records and pick text type only
        // we only send one NDEF message(as a JSON string)
        Parcelable[] data = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (data != null) {
            NdefMessage message = (NdefMessage) data[0];
            receivedKey = new String(message.getRecords()[0].getPayload());
            Log.d(TAG, "NFC Data received, " + receivedKey);

            // process receive data
            onClickPut();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NdefMessage createNdefMessage(NfcEvent nfcEvent) {
        if (thisMatm != null) {
            NdefRecord ndefRecord = NdefRecord.createMime("text/plain", thisMatm.getKey().getBytes());

            return new NdefMessage(ndefRecord);
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNdefPushComplete(NfcEvent event) {
        // need to run on UI thread since beam touch involved here
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // start progress dialog
                //ActivityUtils.showProgressDialog(this, "Please wait...");

                // toast to notify wait
                Toast.makeText(MatmActivity.this, "Please wait", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(senzServiceConnection);
        unregisterReceiver(senzMessageReceiver);
    }

    /**
     * Initialize NFC components
     */
    private void initNfc() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            Toast.makeText(this, "[ERROR] No NFC supported", Toast.LENGTH_LONG).show();
        } else {
            // set listeners to receive data
            nfcAdapter.setNdefPushMessageCallback(this, this);
            nfcAdapter.setOnNdefPushCompleteCallback(this, this);

            // create an intent with tag data and deliver to this activity
            nfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

            // set an intent filter for all MIME data
            IntentFilter nfcDiscoveredIntentFilter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
            try {
                nfcDiscoveredIntentFilter.addDataType("text/plain");
                nfcIntentFilters = new IntentFilter[]{nfcDiscoveredIntentFilter};
            } catch (Exception e) {
                Log.e("TagDispatch", e.toString());
            }

            // tech list
            nfcTechLists = new String[][]{new String[]{NfcF.class.getName()}};
        }
    }

    private void initUi() {
        typeface = Typeface.createFromAsset(getAssets(), "fonts/vegur_2.otf");

        textViewAppIcon = (TextView) findViewById(R.id.app_icon);
        clickToPay = (TextView) findViewById(R.id.click_to_pay);
        payAmountText = (TextView) findViewById(R.id.pay_amount_text);

        textViewAppIcon.setTypeface(typeface, Typeface.BOLD);
        clickToPay.setTypeface(typeface, Typeface.BOLD);
        payAmountText.setTypeface(typeface, Typeface.BOLD);
    }

    private void initActionBar() {
        // Set up action bar.
        // Specify that the Home button should show an "Up" caret, indicating that touching the
        // button will take the user one step up in the application's hierarchy.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setTitle("Pay");
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // set custom font for
        //  1. action bar title
        //  2. other ui texts
        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView actionBarTitle = (TextView) (findViewById(titleId));
        actionBarTitle.setTextColor(getResources().getColor(R.color.white));
        actionBarTitle.setTypeface(typeface);
        actionBar.setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
    }

    private void initMatm() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            thisMatm = bundle.getParcelable("EXTRA");

            if (thisMatm != null) {
                Log.i(TAG, "Matm tid :" + thisMatm.gettId());
                Log.i(TAG, "Matm key :" + thisMatm.getKey());

                payAmountText.setText(thisMatm.getKey());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.overridePendingTransition(R.anim.stay_in, R.anim.bottom_out);
    }

    private void onClickPut() {
        ActivityUtils.showProgressDialog(MatmActivity.this, "Please wait...");

        // start new timer
        isResponseReceived = false;
        senzCountDownTimer = new SenzCountDownTimer(16000, 5000, createPutSenz());
        senzCountDownTimer.start();
    }

    private void doPut(Senz senz) {
        try {
            senzService.send(senz);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private Senz createPutSenz() {
        HashMap<String, String> senzAttributes = new HashMap<>();
        senzAttributes.put("tid", thisMatm.gettId());
        senzAttributes.put("key", receivedKey);
        senzAttributes.put("time", ((Long) (System.currentTimeMillis() / 1000)).toString());

        // new senz
        String id = "_ID";
        String signature = "_SIGNATURE";
        SenzTypeEnum senzType = SenzTypeEnum.PUT;
        User receiver = new User("", "payzbank");

        return new Senz(id, signature, senzType, null, receiver, senzAttributes);
    }

    /**
     * Keep track with share response timeout
     */
    private class SenzCountDownTimer extends CountDownTimer {

        // timer deals with only one senz
        private Senz senz;

        public SenzCountDownTimer(long millisInFuture, long countDownInterval, final Senz senz) {
            super(millisInFuture, countDownInterval);

            this.senz = senz;
        }

        @Override
        public void onTick(long millisUntilFinished) {
            // if response not received yet, resend share
            if (!isResponseReceived) {
                doPut(senz);
                Log.d(TAG, "Response not received yet");
            }
        }

        @Override
        public void onFinish() {
            ActivityUtils.hideSoftKeyboard(MatmActivity.this);
            ActivityUtils.cancelProgressDialog();

            // display message dialog that we couldn't reach the user
            if (!isResponseReceived) {
                String message = "Seems we couldn't complete the payment at this moment";
                displayMessageDialog("#PUT Fail", message);
            }
        }
    }

    /**
     * Handle broadcast message receives
     * Need to handle registration success failure here
     *
     * @param intent intent
     */
    private void handleSenzMessage(Intent intent) {
        String action = intent.getAction();

        if (action.equals("com.score.shopz.DATA_SENZ")) {
            Senz senz = intent.getExtras().getParcelable("SENZ");

            // process response
            if (senz.getAttributes().containsKey("msg")) {
                // msg response received
                ActivityUtils.cancelProgressDialog();
                isResponseReceived = true;
                senzCountDownTimer.cancel();

                String msg = senz.getAttributes().get("msg");
                if (msg != null && msg.equalsIgnoreCase("DONE")) {
                    Toast.makeText(this, "Payment successful", Toast.LENGTH_LONG).show();

                    // exit from activity
                    this.finish();
                    this.overridePendingTransition(R.anim.stay_in, R.anim.bottom_out);
                } else {
                    String informationMessage = "Failed to complete the payment";
                    displayMessageDialog("PUT fail", informationMessage);
                }
            }
        }
    }

    /**
     * Display message dialog
     *
     * @param messageHeader message header
     * @param message       message to be display
     */
    public void displayMessageDialog(String messageHeader, String message) {
        final Dialog dialog = new Dialog(this);

        //set layout for dialog
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.information_message_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);

        // set dialog texts
        TextView messageHeaderTextView = (TextView) dialog.findViewById(R.id.information_message_dialog_layout_message_header_text);
        TextView messageTextView = (TextView) dialog.findViewById(R.id.information_message_dialog_layout_message_text);
        messageHeaderTextView.setText(messageHeader);
        messageTextView.setText(message);

        // set custom font
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/vegur_2.otf");
        messageHeaderTextView.setTypeface(face);
        messageHeaderTextView.setTypeface(null, Typeface.BOLD);
        messageTextView.setTypeface(face);

        //set ok button
        Button okButton = (Button) dialog.findViewById(R.id.information_message_dialog_layout_ok_button);
        okButton.setTypeface(face);
        okButton.setTypeface(null, Typeface.BOLD);
        okButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        dialog.show();
    }

}
