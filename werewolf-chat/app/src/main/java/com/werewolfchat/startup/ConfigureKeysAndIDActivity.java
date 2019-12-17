package com.werewolfchat.startup;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.werewolfchat.startup.ntru.encrypt.EncryptionKeyPair;
import com.werewolfchat.startup.ntru.encrypt.EncryptionParameters;
import com.werewolfchat.startup.ntru.encrypt.NtruEncrypt;
import com.werewolfchat.startup.ntru.sign.NtruSign;
import com.werewolfchat.startup.ntru.sign.SignatureKeyPair;
import com.werewolfchat.startup.ntru.sign.SignatureParameters;
import com.werewolfchat.startup.ntru.util.ArrayEncoder;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.werewolfchat.startup.Utility.addProxyToIntent;
import static com.werewolfchat.startup.Utility.addTimeOutToIntent;
import static com.werewolfchat.startup.Utility.dumb_debugging;
import static com.werewolfchat.startup.Utility.getProxyPortFromIntent;
import static com.werewolfchat.startup.Utility.getTimeOutFromIntnet;
import static com.werewolfchat.startup.Utility.isTheProxyPortExtraNull;
import static com.werewolfchat.startup.ntru.util.ArrayEncoder.bytesToHex;

//import android.support.v7.app.AppCompatActivity;

//import com.google.firebase.database.DatabaseReference;

public class ConfigureKeysAndIDActivity extends AppCompatActivity implements View.OnClickListener {

    private static final SignatureParameters SIGNATURE_PARAMS = SignatureParameters.APR2011_439_PROD;
    private static final EncryptionParameters ENCRYPTION_PARAMS = EncryptionParameters.APR2011_439_FAST;
    private static final String publishButtonText = "Continue by Publishing ID and Public Key";
    public static final String PUBLIC_KEYS_CHILD = "public_keys";
    NtruEncrypt ntruEnc;
    NtruSign ntruSig;
    private File priavteKeyStorage;
    private static final String PATH = "werewolfchatkeys/";
    private EncryptionKeyPair encKP;
    private SignatureKeyPair sigKP;
    private String werewolfChatId;
    private Button set_id_button;  //= (Button) update_idfindViewById(R.id.button_to_set_id);
    private Button publish_button; // = (Button) findViewById(R.id.button_to_publish_info);
    private Button dont_publish_button; // = (Button) findViewById(R.id.button_to_set_id);
    private String passed_in_uid;
    private Button wipe_local_keys_button;
    private String passed_in_email;
    private TextView loadingView;
    private TextView chatIDView;
    private ImageView loadingImg;
    private EditText idInput;
    private boolean isTheIdGood;
    private boolean usingPrivateServer;
    private String privateServerURL;
    private RequestQueue queue;
    private ExtrasManager extrasManager;



    //CountDownLatch latch;


    public void lookupOrGenerateKeys() {
        //see if they have a key pair file on file for this firebase uuid
        String localPath = this.getFilesDir() + "/chat.key";
        File internalStorage = this.getFilesDir();
        priavteKeyStorage = new File(internalStorage, "chat.key");
        if (priavteKeyStorage.exists()) {
            String fileOutput = "foo";
            this.loadingView.setText("Keyfile found, loading");

            // file exists so read keys and id
            //TODO have a more elegant soultion
            try {
                fileOutput = Utility.getStringFromFile(localPath);
            } catch (IOException e) {
                System.out.println(e.toString());
                setError(e.toString());

            }
            TextView loadingview = (TextView) findViewById(R.id.Loading_status);


            //System.out.println(fileOutput);

            String[] parts = fileOutput.split(",");

            this.encKP = new EncryptionKeyPair(parts[0], parts[1]);
            //this.sigKP = new SignatureKeyPair(parts[2],parts[3]);
            this.chatIDView.setText(parts[2]);
            this.werewolfChatId = parts[2];
            this.setGoodNews("Keyfile loaded");
            this.isTheIdGood = true;
        } else {


            // file dose not exist, so make new keys and save to file
            // writen in the format encypt pub ### encrypt priv ### sig pub
            //                                                         ### sig priv ### id
            this.loadingView.setText("No keyfile found, generating");
            this.ntruEnc = new NtruEncrypt(ENCRYPTION_PARAMS);
            //this.ntruSig = new NtruSign(SIGNATURE_PARAMS);
            this.encKP = ntruEnc.generateKeyPair();
            //this.sigKP = ntruSig.generateKeyPair();
            update_id(Utility.makeRandomString());

            this.setGoodNews("Keys generated");


        }
    }

    public void setError(String errMesg) {
        this.loadingView.setText(errMesg);
        this.loadingImg.setImageResource(R.drawable.bad_key);
        return;
    }

    public void setGoodNews(String goodMesg) {
        this.loadingImg.setImageResource(R.drawable.good_key);
        this.loadingView.setText(goodMesg);

    }

    public void makeNewKeys() {

        File internalStorage = this.getFilesDir();
        priavteKeyStorage = new File(internalStorage, "chat.key");
        if (priavteKeyStorage.exists()) {
            Utility.dumb_debugging("wiping old key file");
            priavteKeyStorage.delete();
            Utility.dumb_debugging("old key file wipped");
        }

        this.loadingView.setText("Making new keys");
        this.ntruEnc = new NtruEncrypt(ENCRYPTION_PARAMS);
        //this.ntruSig = new NtruSign(SIGNATURE_PARAMS);
        this.encKP = ntruEnc.generateKeyPair();
        //this.sigKP = ntruSig.generateKeyPair();
        update_id(Utility.makeRandomString());
        this.setGoodNews("Keys generated");

    }



    //TODO make a more OOP way to handle proxies so I dont have identical code in multiple classes
    //proxy methods

    private boolean isUsingProxy;
    private int proxy_port;
    private int timeOut;
    private void loadProxySettings(Intent intent)
    {
        if(isTheProxyPortExtraNull(intent))
        {
            proxy_port = -1;
            isUsingProxy = false;
            dumb_debugging("no proxy ports");
        }
        else
        {
            proxy_port = getProxyPortFromIntent(intent);
            isUsingProxy = true;
            timeOut = getTimeOutFromIntnet(intent);

            dumb_debugging("loading proxy port "+proxy_port+" in main activity");
        }
    }

    public Intent addProxyToIntentIfInUse(Intent intent)
    {
        if(this.isUsingProxy) {
            intent = addProxyToIntent(intent, this.proxy_port);
            if(this.timeOut>0)
                intent = addTimeOutToIntent(intent, this.timeOut);
        }

        return intent;
    }

    public void makeVolley()
    {
        loadProxySettings(this.getIntent());
        if(this.isUsingProxy)
            this.queue = Volley.newRequestQueue(this, new ProxiedHurlStack(this.proxy_port));
        else
            this.queue = Volley.newRequestQueue(this);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_name_prompt);

        getIntent().getPackage();
        //this.latch = new CountDownLatch(1);
        this.isTheIdGood = false;
        //this.queue = Volley.newRequestQueue(this);

        Intent theIntent = this.getIntent();
        this.extrasManager = new ExtrasManager( theIntent);

        this.usingPrivateServer = true;
        privateServerURL = this.extrasManager.getPrivateServerURL();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        this.queue = this.extrasManager.makeVolley(this);


        wipe_local_keys_button = (Button) findViewById(R.id.generate_new_keys);
        wipe_local_keys_button.setOnClickListener(this);
        set_id_button = (Button) findViewById(R.id.button_to_set_id);
        publish_button = (Button) findViewById(R.id.button_to_publish_info);
        dont_publish_button = (Button) findViewById(R.id.button_to_not_publish);
        set_id_button.setOnClickListener(this);
        publish_button.setOnClickListener(this);
        dont_publish_button.setOnClickListener(this);
        this.loadingView = (TextView) findViewById(R.id.Loading_status);
        this.loadingImg = (ImageView) findViewById(R.id.loading_img);
        this.chatIDView = (TextView) findViewById(R.id.generated_ID);
        this.idInput = (EditText) findViewById(R.id.editTextDialogUserInput);






        lookupOrGenerateKeys();

    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_to_set_id:
                this.update_id(this.idInput.getText().toString());
                break;
            case R.id.button_to_publish_info:
                this.publishKeyWrapper();
                break;

            case R.id.generate_new_keys:
                makeNewKeys();
                break;

            case R.id.button_to_not_publish:
                this.save_key_info_without_publishing();
                this.setkeyStringsFile();
                startActivity(makeIntentWithKeys());
                finish();
                return;
        }
    }


    //generates the intent to return to the Main Activity
    public Intent makeIntentWithKeys() {
        Utility.dumb_debugging("In makeIntentWithKeys");

        extrasManager.copyOverExtrasAndChangeClassToPrepareToStartNewActivity(ConfigureKeysAndIDActivity.this, MainActivity.class);
        extrasManager.setPubKey(this.encKP.getPublic().getEncoded());
        extrasManager.setPrivKey(this.encKP.getPrivate().getEncoded());
        extrasManager.setChatID(this.werewolfChatId);



        return extrasManager.getIntent();
    }

    public void setkeyStringsFile() {
        Utility.writeToFile(this.encKP.getHexStringForOfflineStorage() + "," +
                this.werewolfChatId, this, "chat.key");
        return;
    }


    public void publishKeyWrapper() {
        if (this.usingPrivateServer) {
            this.publish_private_server_info();
        } else {
            this.publish_key_info();

        }

    }

    public class PubKeyPublishGoodResultCommand implements Utility.Command {
        public void execute(String data) {
            if (data.equals("chatidtaken") && !isTheIdGood) {
                publish_button.setText("ID taken, can't publish, choose new id");
                return;
            }
            isTheIdGood = true;
            setkeyStringsFile();
            startActivity(makeIntentWithKeys());
            finish();
            return;

        }
    }

    public class PubKeyPublishBadResultCommand implements Utility.Command {
        public void execute(String data) {

            isTheIdGood = false;
            publish_button.setText("Could not contact server");

        }
    }


    public void publish_private_server_info() {
        String keystr = ArrayEncoder.bytesToHex(this.encKP.getPublic().getEncoded());
        String queryStr = Utility.makeGetStringForPublishingKey(this.privateServerURL, this.werewolfChatId, keystr);
        Utility.queryURL(queryStr, this.queue, new PubKeyPublishGoodResultCommand(), new PubKeyPublishBadResultCommand());
        return;
    }

    public void update_id_private_server(String chatId) {
        this.isTheIdGood = false;
        this.werewolfChatId = chatId;
        this.chatIDView.setText(chatId);
        return;
    }

    public void update_id(String newID) {
        //latch = new CountDownLatch(1);
        if (this.usingPrivateServer) {
            Utility.dumb_debugging("checking private server id");
            update_id_private_server(newID);
            return;
        }

        this.isTheIdGood = false;
        this.isThisIdTaken(newID);

        this.werewolfChatId = newID;
        this.chatIDView.setText(newID);

        return;
    }

    //TODO check to see if chat id taken
    private void publish_key_info() {

        Utility.dumb_debugging("publishing key to firebase");


        Map<String, WerewolfChatPublicKey> mapForUpload = new HashMap<>();
        mapForUpload.put(this.passed_in_uid, new WerewolfChatPublicKey(this.werewolfChatId,
                ArrayEncoder.bytesToHex(this.encKP.getPublic().getEncoded())));

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        Utility.dumb_debugging("public keys child: " + PUBLIC_KEYS_CHILD);
        Utility.dumb_debugging("passed in uuid: " + passed_in_uid);
        DatabaseReference pubKeyRef = ref.child(PUBLIC_KEYS_CHILD).child(this.passed_in_uid);

        pubKeyRef.setValue(new WerewolfChatPublicKey(this.werewolfChatId,
                ArrayEncoder.bytesToHex(this.encKP.getPublic().getEncoded())));


        this.setkeyStringsFile();
        startActivity(makeIntentWithKeys());
        finish();
        return;
    }

    public void save_key_info_without_publishing() {
        String file_location = "Saving Keys to " + this.werewolfChatId + ".pubkey";
        this.loadingView.setText(file_location);
        Utility.writeToFile(bytesToHex(this.encKP.getPublic().getEncoded()), this,
                this.werewolfChatId + ".pubkey");
        file_location = "File Saved at " + this.werewolfChatId + ".pubkey";
        this.loadingView.setText(file_location);
        return;
    }


    public void isThisIdTaken(String idToCheck) {
        isTheIdGood = false;
        publish_button.setText("Checking Id to see if taken");
        DatabaseReference queryRef = FirebaseDatabase.getInstance().getReference().child("public_keys");
        final String finalStringToCheck = idToCheck;


        queryRef.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {


            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getChildrenCount() == 0) {

                    Utility.dumb_debugging("No public users found on the database, so the ide must not be taken");
                    publish_button.setText(publishButtonText);
                    isTheIdGood = true;

                    return;

                } else {
                    Utility.dumb_debugging(Long.toString(dataSnapshot.getChildrenCount()));

                    for (DataSnapshot row : dataSnapshot.getChildren()) {
                        Utility.dumb_debugging(row.getKey());
                        //pubKeyArray.add((WerewolfChatPublicKey)row.getValue());
                        if (finalStringToCheck.equals((String) row.child("chat_id").getValue())) {
                            isTheIdGood = false;
                            setError("the id is taken, change id");
                            publish_button.setText("ID taken, can't publish, choose new id");
                            return;
                        }
                    }
                    isTheIdGood = true;
                    publish_button.setText(publishButtonText);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Utility.dumb_debugging("queryPublicKeys:onCancelled" + databaseError.toString());
                setError("could not contact database to check id, try again");

            }
        });


    }
}
