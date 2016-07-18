package com.score.shopz.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.score.senzc.pojos.Senz;
import com.score.shopz.R;
import com.score.shopz.pojos.Bill;
import com.score.shopz.pojos.Matm;
import com.score.shopz.utils.ActivityUtils;
import com.score.shopz.utils.JSONUtils;
import com.score.shopz.utils.SenzParser;

import org.json.JSONException;


public class BillActivity extends Activity implements NfcAdapter.CreateNdefMessageCallback, NfcAdapter.OnNdefPushCompleteCallback {

    private static final String TAG = BillActivity.class.getName();

    // deals with NFC
    private NfcAdapter nfcAdapter;

    // custom type face
    private Typeface typeface;

    // UI components
    private TextView billNoText;
    private TextView billAccountText;
    private TextView billAmountText;

    private EditText billNoEditText;
    private EditText billAccountEditText;
    private EditText billAmountEditText;

    // Activity deals with a bill
    private Bill bill;

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
        setContentView(R.layout.bill_layout);

        initNfc();
        initUi();
        initActionBar();
        initBill();

        // register broadcast receiver
        registerReceiver(senzMessageReceiver, new IntentFilter("com.score.shopz.DATA_SENZ"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(senzMessageReceiver);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NdefMessage createNdefMessage(NfcEvent nfcEvent) {
        // create JSON message from TopUp
        if (bill != null) try {
            // populate bill with amount first
            // then create JSON string to send via NFC
            String billAmount = billAmountEditText.getText().toString().trim();

            if (!billAmount.isEmpty()) {
                bill.setAmount(billAmount);
                String message = JSONUtils.getBillJson(bill);

                NdefRecord ndefRecord = NdefRecord.createMime("text/plain", message.getBytes());

                return new NdefMessage(ndefRecord);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNdefPushComplete(NfcEvent event) {
        // start progress dialog
        //ActivityUtils.showProgressDialog(this, "Please wait...");

        // need to run on UI thread since beam touch involved here
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // toast to notify wait
                Toast.makeText(BillActivity.this, "Please wait", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Initialize UI components
     */
    private void initUi() {
        typeface = Typeface.createFromAsset(getAssets(), "fonts/vegur_2.otf");

        billNoText = (TextView) findViewById(R.id.bill_no);
        billAccountText = (TextView) findViewById(R.id.bill_account);
        billAmountText = (TextView) findViewById(R.id.bill_amount);

        billNoEditText = (EditText) findViewById(R.id.bill_layout_number_text);
        billAccountEditText = (EditText) findViewById(R.id.bill_layout_account_text);
        billAmountEditText = (EditText) findViewById(R.id.bill_layout_amount_text);

        billNoText.setTypeface(typeface, Typeface.NORMAL);
        billAccountText.setTypeface(typeface, Typeface.NORMAL);
        billAmountText.setTypeface(typeface, Typeface.NORMAL);

        billNoEditText.setTypeface(typeface, Typeface.BOLD);
        billAccountEditText.setTypeface(typeface, Typeface.BOLD);
        billAmountEditText.setTypeface(typeface, Typeface.BOLD);
    }

    /**
     * Initialize action bar from here
     */
    private void initActionBar() {
        // Set up action bar.
        // Specify that the Home button should show an "Up" caret, indicating that touching the
        // button will take the user one step up in the application's hierarchy.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Bill");
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
     * Initialize NFC components
     */
    private void initNfc() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            Toast.makeText(this, "[ERROR] No NFC supported", Toast.LENGTH_LONG).show();
        } else {
            nfcAdapter.setNdefPushMessageCallback(this, this);
            nfcAdapter.setOnNdefPushCompleteCallback(this, this);
        }
    }

    private void initBill() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            bill = bundle.getParcelable("EXTRA");

            if (bill != null) {
                billNoEditText.setText(bill.getBillNo());
                billAccountEditText.setText(bill.getAccount());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.overridePendingTransition(R.anim.stay_in, R.anim.right_out);
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
                Intent mapIntent = new Intent(this, MatmActivity.class);
                mapIntent.putExtra("EXTRA_MATM", matm);
                mapIntent.putExtra("EXTRA_BILL", bill);
                startActivity(mapIntent);
                this.finish();
                overridePendingTransition(R.anim.stay_in, R.anim.right_in);
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
