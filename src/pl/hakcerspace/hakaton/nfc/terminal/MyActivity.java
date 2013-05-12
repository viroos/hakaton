package pl.hakcerspace.hakaton.nfc.terminal;

import org.json.JSONException;
import org.json.JSONObject;
import pl.upaid.api.android.BuyInterface;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Base64;
import android.util.Log;

import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.BasicHttpClient;
import com.turbomanage.httpclient.HttpResponse;
import com.turbomanage.httpclient.ParameterMap;
import com.turbomanage.httpclient.android.AndroidHttpClient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Arrays;

public class MyActivity extends Activity {


    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;

    private static final int REQUEST_CODE_BUY = 0;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mAdapter = NfcAdapter.getDefaultAdapter(this);

        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);

        mFilters = new IntentFilter[] {
                ndef,
        };

        // Setup a tech list for all NfcF tags
        mTechLists = new String[][] { new String[] { MifareClassic.class.getName() } };









    }

    private void sendID(String id){

        URL uri = null;
        try {
            uri = new URL("http://192.168.100.182.xip.io:7000/");





        AndroidHttpClient httpClient = new AndroidHttpClient(uri.toString());
        httpClient.setMaxRetries(5);
        ParameterMap params = httpClient.newParams();

        httpClient.get("nfc/"+id, params, new AsyncCallback() {
            @Override
            public void onComplete(HttpResponse httpResponse) {
                String response = httpResponse.getBodyAsString();
                Log.i("skasowano",response) ;
                JSONObject jsonResponse;
                try {
                    jsonResponse = new JSONObject(response);
                    int total = (int)(jsonResponse.getDouble("total")*100);
                    paid(total);


                } catch (JSONException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

            }
            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
        }
        catch (Exception e){
          Log.e("fuck",e.toString());
        }
    }


    private void paid(int amount){
        //upaid

        BuyInterface inter = new BuyInterface(true, 34,"PartnerName", "merchant","6", amount);
        // inter.setPhone("48123456789");
        inter.setRegulationsLinkAndService("http://google.pl", "Google");
        inter.setData("some additional data");
        Intent intent = inter.getBuyIntent(this);
        startActivityForResult(intent, REQUEST_CODE_BUY);

    }





    @Override
    public void onResume() {
        super.onResume();
        if (mAdapter != null) mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters,
                mTechLists);
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.i("Foreground dispatch", "Discovered tag with intent: " + intent);
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            byte[] tagId = tag.getId();
            String stringId = Base64.encodeToString(tagId, Base64.DEFAULT);
            sendID(stringId);

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAdapter != null) mAdapter.disableForegroundDispatch(this);
    }
}


