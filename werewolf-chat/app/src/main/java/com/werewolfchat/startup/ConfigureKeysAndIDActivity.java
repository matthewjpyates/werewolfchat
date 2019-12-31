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
import com.werewolfchat.startup.ntru.encrypt.EncryptionKeyPair;
import com.werewolfchat.startup.ntru.encrypt.EncryptionParameters;
import com.werewolfchat.startup.ntru.encrypt.NtruEncrypt;
import com.werewolfchat.startup.ntru.sign.NtruSign;
import com.werewolfchat.startup.ntru.sign.SignatureKeyPair;
import com.werewolfchat.startup.ntru.sign.SignatureParameters;
import com.werewolfchat.startup.ntru.util.ArrayEncoder;

import java.io.File;
import java.io.IOException;

import static com.werewolfchat.startup.Utility.dumb_debugging;
import static com.werewolfchat.startup.Utility.queryURL;

/*import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;*/

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
    private TokenManager tokenManager;
    private boolean keyWasFound;


    public class FeedbackWoker implements Utility.Command {
        public void execute(String data) {
            if (data.startsWith("good:")) {
                setGoodNews("token pull is working");
            } else {
                setError(data);
            }
        }
    }


    public class VerifyKeysAfterGettingThemWorker implements Utility.Command {


        public void execute(String data) {
            dumb_debugging("At 1stposition");
            extrasManager.setPubKey(encKP.getPublic().getEncoded());
            extrasManager.setPrivKey(encKP.getPrivate().getEncoded());
            extrasManager.setPrivateServerURL(privateServerURL);
            extrasManager.setChatID(werewolfChatId);
            verifyKeys();
        }
    }


    //CountDownLatch latch;


    public void lookupOrGenerateKeys() {
        dumb_debugging("3 autopub is " + (this.extrasManager.autoPub ? "true" : "false"));

        keyWasFound = false;
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
            this.extrasManager.setChatID(this.werewolfChatId);
            this.extrasManager.loadKeyPair(encKP);
            this.tokenManager = this.extrasManager.makeNewTokenManager();
            //this.tokenManager.getNewToken(this.queue);
            if (this.extrasManager.autoPub) {
                dumb_debugging("At 2ndposition in lookup keys with autopub set");
                this.extrasManager.setAutoPub(false);
                this.tokenManager.getNewTokenThenExecuteCommand(this.queue, new VerifyKeysAfterGettingThemWorker());

            }

            //this.setError("unable to get auth token for keyfile");
            keyWasFound = true;
        } else {


            // file dose not exist, so make new keys and save to file
            // writen in the format encypt pub ### encrypt priv ### sig pub
            //                                                         ### sig priv ### id
            this.loadingView.setText("No keyfile found, generating");
            this.ntruEnc = new NtruEncrypt(ENCRYPTION_PARAMS);
            //this.ntruSig = new NtruSign(SIGNATURE_PARAMS);
            this.encKP = ntruEnc.generateKeyPair();
            //this.sigKP = ntruSig.generateKeyPair();
            this.extrasManager.setPrivKey(this.encKP.getPrivate().getEncoded());
            this.extrasManager.setPubKey(this.encKP.getPublic().getEncoded());
            this.extrasManager.setChatID(Utility.makeRandomString());
            //update_id(Utility.makeRandomString());
            update_id_field_without_publishing(this.extrasManager.getChatID());
            this.setGoodNews("Keys generated");
            if (this.extrasManager.autoPub) {
                dumb_debugging("At 2ndposition_other in lookup keys with autopub set");
                this.setGoodNews("Publishing new keys");
                this.extrasManager.setAutoPub(false);
                publish_private_server_info();
                //this.tokenManager.getNewTokenThenExecuteCommand(this.queue, new VerifyKeysAfterGettingThemWorker());

            }



        }
    }

    public void update_id_field_without_publishing(String newID) {
        this.chatIDView.setText(newID);
        this.extrasManager.setChatID(newID);
        this.werewolfChatId = newID;
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

        this.loadingView.setText("Generating new keys");

        this.ntruEnc = new NtruEncrypt(ENCRYPTION_PARAMS);
        //this.ntruSig = new NtruSign(SIGNATURE_PARAMS);
        this.encKP = ntruEnc.generateKeyPair();
        //this.sigKP = ntruSig.generateKeyPair();
        this.extrasManager.setPrivKey(this.encKP.getPrivate().getEncoded());
        this.extrasManager.setPubKey(this.encKP.getPublic().getEncoded());
        this.extrasManager.setChatID(Utility.makeRandomString());
        update_id_field_without_publishing(this.extrasManager.getChatID());
        //update_id(Utility.makeRandomString());
        this.setGoodNews("Keys generated");

    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_name_prompt);

        getIntent().getPackage();
        //this.latch = new CountDownLatch(1);
        this.isTheIdGood = false;
        //this.queue = Volley.newRequestQueue(this);
        keyWasFound = false;

        Intent theIntent = this.getIntent();
        this.extrasManager = new ExtrasManager( theIntent);


        dumb_debugging("1 autopub is " + (this.extrasManager.autoPub ? "true" : "false"));

        this.usingPrivateServer = true;
        privateServerURL = this.extrasManager.getPrivateServerURL();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        this.queue = this.extrasManager.makeVolley(this);


        wipe_local_keys_button = (Button) findViewById(R.id.generate_new_keys);
        wipe_local_keys_button.setOnClickListener(this);
        set_id_button = (Button) findViewById(R.id.button_to_set_id);
        publish_button = (Button) findViewById(R.id.button_to_publish_info);
        //dont_publish_button = (Button) findViewById(R.id.button_to_not_publish);
        set_id_button.setOnClickListener(this);
        publish_button.setOnClickListener(this);
        //dont_publish_button.setOnClickListener(this);
        this.loadingView = (TextView) findViewById(R.id.Loading_status);
        this.loadingImg = (ImageView) findViewById(R.id.loading_img);
        this.chatIDView = (TextView) findViewById(R.id.generated_ID);
        this.idInput = (EditText) findViewById(R.id.editTextDialogUserInput);


        dumb_debugging("2 autopub is " + (this.extrasManager.autoPub ? "true" : "false"));

        lookupOrGenerateKeys();

        /*if(this.extrasManager.autoPub)
        {
            dumb_debugging("auto pub is set");
            this.publishKeyWrapper();
        }*/

    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_to_set_id:
                update_id_field_without_publishing(this.idInput.getText().toString());
                this.update_id(this.extrasManager.getChatID());
                break;
            case R.id.button_to_publish_info:
                this.publishKeyWrapper();
                break;

            case R.id.generate_new_keys:
                makeNewKeys();
                break;

            /*case R.id.button_to_not_publish:
                this.save_key_info_without_publishing();
                this.setkeyStringsFile();
                startActivity(makeIntentWithKeys());
                finish();
                return;*/
        }
    }


    //generates the intent to return to the Main Activity
    public Intent makeIntentWithKeys() {
        Utility.dumb_debugging("In makeIntentWithKeys");

        extrasManager.copyOverExtrasAndChangeClassToPrepareToStartNewActivity(ConfigureKeysAndIDActivity.this, MainActivity.class);
        extrasManager.setPubKey(this.encKP.getPublic().getEncoded());
        extrasManager.setPrivKey(this.encKP.getPrivate().getEncoded());
        extrasManager.setChatID(this.werewolfChatId);
        extrasManager.setTokenString(this.tokenManager.getTokenString());



        return extrasManager.getIntent();
    }

    public void setkeyStringsFile() {
        Utility.writeToFile(this.encKP.getHexStringForOfflineStorage() + "," +
                this.werewolfChatId, this, "chat.key");
        return;
    }


    public void publishKeyWrapper() {
            this.publish_private_server_info();


    }


    public class ChangeIDGoodResult implements Utility.Command {

        public String newID;

        public ChangeIDGoodResult(String id) {
            this.newID = id;
        }

        public void execute(String data) {
            if (data.equals("token failed")) {
                publish_button.setText("bad server token");
                dumb_debugging("got the token fail result");
                return;
            } else if (data.equals("chatid changed")) {
                werewolfChatId = this.newID;
                dumb_debugging("changing chatid after server");

                return;
            } else {
                publish_button.setText("server error");
                dumb_debugging("did not get a valid response from the server");
            }
        }
    }

    public class ChangeIDFail implements Utility.Command {
        public void execute(String data) {

            publish_button.setText("Could not update ID");
            dumb_debugging("failed to update chat id:\n" + data);

        }
    }

    public class VerifyPubKeyGood implements Utility.Command {
        public void execute(String data) {
            dumb_debugging("in VerifyPubKeyGood with this value: " + data);

            if (data.startsWith("fail:")) {
                dumb_debugging("in VerifyPubKeyGood with this value: ");
                publish_button.setText("Key Verification Failed");
                return;
            }
            setkeyStringsFile();
            startActivity(makeIntentWithKeys());
            finish();
            return;

        }
    }


    public void verifyKeys() {
        isTheIdGood = true;
        publish_button.setText("Verifying Key File");
        dumb_debugging("about to try to verify the key file");
        dumb_debugging("going to use this token " + tokenManager.getTokenString());

        Utility.queryURL(Utility.makeVerifyKeyURL(extrasManager.getPrivateServerURL(),
                extrasManager.getChatID(), tokenManager.getTokenString()),
                queue, new VerifyPubKeyGood(), new VerifyPubKeyFail());
        return;
    }

    public class VerifyPubKeyFail implements Utility.Command {
        public void execute(String data) {
            publish_button.setText("Key Verification Failed");
            dumb_debugging(data);
        }
    }


    public class PubKeyPublishGoodResultCommand implements Utility.Command {
        public void execute(String data) {
            if (data.equals("fail:chatidtaken") && !isTheIdGood) {
                publish_button.setText("ID taken, can't publish, choose new id");
                return;
            } else if (data.startsWith("fail:")) {
                publish_button.setText("Failed to publish");
                return;
            } else if (data.startsWith("good:")) {
                String encryptedtoken = data.split(":")[1].trim();
                dumb_debugging("the encrypted token is:" + encryptedtoken);
                tokenManager = extrasManager.makeNewTokenManager();
                tokenManager.loadTokenOffline(encryptedtoken);

                if (tokenManager.isTokenSet()) {

                    verifyKeys();
                } else {
                    publish_button.setText("Received a bad token");
                    return;

                }
            }
            isTheIdGood = false;
            publish_button.setText("Failed to publish");
        }
    }

    public class PubKeyPublishBadResultCommand implements Utility.Command {
        public void execute(String data) {

            isTheIdGood = false;
            dumb_debugging(data);
            publish_button.setText("Could not contact server");

        }
    }


    public void publish_private_server_info() {
        String keystr = ArrayEncoder.bytesToHex(this.encKP.getPublic().getEncoded());
        String queryStr = Utility.makeGetStringForPublishingKey(this.privateServerURL, this.werewolfChatId, keystr);
        extrasManager.setPubKey(this.encKP.getPublic().getEncoded());
        extrasManager.setPrivKey(this.encKP.getPrivate().getEncoded());
        extrasManager.setPrivateServerURL(this.privateServerURL);
        extrasManager.setChatID(this.werewolfChatId);
        if (keyWasFound && tokenManager.isTokenSet()) {
            dumb_debugging("about to try to verfiy keys");
            verifyKeys();
        } else {
            Utility.queryURL(queryStr, this.queue, new PubKeyPublishGoodResultCommand(), new PubKeyPublishBadResultCommand());
        }
        return;
    }


    public class TaskToRunIfNoPubKeyWasFoundWhenTryingToChangeId implements Utility.Command {

        public String newID;

        public TaskToRunIfNoPubKeyWasFoundWhenTryingToChangeId(String newIDinput) {
            newID = newIDinput;
        }

        // if no public key was found on the server, then it has never been published so we can just publish with the new id
        public void execute(String data) {
            werewolfChatId = newID;
            extrasManager.setChatID(newID);
            publish_private_server_info();
        }
    }

    public class ChangeIDTasker implements Utility.Command {
        public String newID;

        public ChangeIDTasker(String newIDinput) {
            newID = newIDinput;
        }

        public void execute(String data) {
            String queryStr = Utility.makeGetStringForChangingChatID(extrasManager.getPrivateServerURL(), werewolfChatId, newID, tokenManager.getTokenString());
            dumb_debugging("about to query the  server with this string:\n" + queryStr);
            queryURL(queryStr, queue, new ChangeIDGoodResult(newID), new ChangeIDFail());
        }
    }

    public void update_id(String chatId) {

            this.extrasManager.setChatID(this.werewolfChatId);
            this.tokenManager = this.extrasManager.makeNewTokenManager();
        this.tokenManager.getNewTokenThenExecuteCommandWithPrepossingCommandIfStringMatches(this.queue, new ChangeIDTasker(chatId),
                "fail:public_key_not_found", new TaskToRunIfNoPubKeyWasFoundWhenTryingToChangeId(chatId));
        this.setGoodNews("trying to change ID");

    }

    /*public void update_id(String newID) {


            Utility.dumb_debugging("checking private server id");
            update_id_private_server(newID);

    }*/

}
