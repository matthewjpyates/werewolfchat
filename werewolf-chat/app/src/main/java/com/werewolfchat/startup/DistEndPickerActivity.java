package com.werewolfchat.startup;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;

import com.android.volley.RequestQueue;
import com.werewolfchat.startup.ntru.util.ArrayEncoder;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;



public class DistEndPickerActivity extends AppCompatActivity {
    NestedScrollView nestedScrollView;
    Intent currentInent;

    private RequestQueue queue;
    private Context context;
    private ExtrasManager extrasManager;
    private LinearLayout privServerlinearLayout;
    private boolean vollyYouCanStopNow;


    public class PubKeyPullGoodResultCommand implements Utility.Command {
        public void execute(String data) {
            if (vollyYouCanStopNow)
                return;
            Utility.dumb_debugging("I am running in dis end picker PubKeyPullGoodResultCommand with this data " + data);
            vollyYouCanStopNow = true;

            ArrayList<JSONObject> jsonResults = Utility.cleanServerResults(data);




            if (jsonResults.size() == 0) {
                Button button = new Button(context);
                button.setText("No public users found on the database, press to go back");

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        extrasManager.copyOverExtrasAndChangeClassToPrepareToStartNewActivity(DistEndPickerActivity.this, MainActivity.class);
                        startActivity(extrasManager.getIntent());
                        finish();
                        return;
                    }
                });
                privServerlinearLayout.addView(button);
            } else
                {

                Map<String, WerewolfChatPublicKey> pubKeys = new HashMap<>();
                ArrayList<WerewolfChatPublicKey> pubKeyArray = new ArrayList<>();


                for (JSONObject jobj : jsonResults) {
                    //pubKeyArray.add((WerewolfChatPublicKey)row.getValue());
                    try {
                        pubKeyArray.add(new WerewolfChatPublicKey(jobj.getString("chatid"), jobj.getString("pubkeyhexstr")));
                    } catch (Exception e) {
                        Utility.dumb_debugging("something was bad with the json for " + jobj.toString() + "\n" + e.toString());
                    }

                }

                Utility.dumb_debugging("going to make " + Integer.toString(pubKeyArray.size()) + "  buttons");

// Add Buttons
                for (WerewolfChatPublicKey pubKey : pubKeyArray) {
                    Utility.dumb_debugging("looping for " + pubKey.chat_id);

                    Button button = new Button(context);
                    final String chatID = pubKey.chat_id;
                    final String buttonText = "Chat ID: " + pubKey.chat_id;
                    Utility.dumb_debugging("about to make this a byte array " + pubKey.public_key_hexstring);
                    final byte[] distEndKey = ArrayEncoder.hexStringToByteArray(pubKey.public_key_hexstring);
                    button.setText(buttonText);

                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // launch the change key and id activity
                            Utility.dumb_debugging("to button clicked for " + buttonText);


                            extrasManager.copyOverExtrasAndChangeClassToPrepareToStartNewActivity(DistEndPickerActivity.this, MainActivity.class);
                            extrasManager.setExtraIfInputIsNotNull(ExtrasManager.DIST_END_ID_EXTRA,chatID);
                            extrasManager.setExtraIfInputIsNotNull(ExtrasManager.DIST_END_KEY_EXTRA,distEndKey);

                            startActivity(extrasManager.getIntent());

                            finish();
                            return;
                        }
                    });

                    privServerlinearLayout.addView(button);

                }


            }
        }
    }

    public class PubKeyPullBadResultCommand implements Utility.Command {
        public void execute(String data) {
            Utility.dumb_debugging("I am running in dis end picker PubKeyPullBadResultCommand with this data " + data);

            extrasManager.copyOverExtrasAndChangeClassToPrepareToStartNewActivity(DistEndPickerActivity.this, MainActivity.class);


            startActivity(extrasManager.getIntent());

            finish();
            return;



        }


    }




    public void getPubKeysFromPrivateServer() {

        vollyYouCanStopNow = false;
        String queryStr = Utility.makeGetStringForPullingKeys(this.extrasManager.getPrivateServerURL());
        Utility.queryURL(queryStr, this.queue, new PubKeyPullGoodResultCommand(), new PubKeyPullBadResultCommand());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dist_end_picker);

        context = this;
        extrasManager = new ExtrasManager(this.getIntent());

        // Find the ScrollView
        this.nestedScrollView = (NestedScrollView) findViewById(R.id.dest_end_scroller);

// Create a LinearLayout element
        privServerlinearLayout = new LinearLayout(this);
        privServerlinearLayout.setOrientation(LinearLayout.VERTICAL);
        nestedScrollView.addView(privServerlinearLayout);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        currentInent = this.getIntent();

        //with this
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        this.queue = this.extrasManager.makeVolley(this);
        getPubKeysFromPrivateServer();
    }

}
