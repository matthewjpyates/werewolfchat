/**
 * Copyright Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.werewolfchat.startup;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.werewolfchat.startup.ntru.encrypt.EncryptionKeyPair;
import com.werewolfchat.startup.ntru.encrypt.EncryptionPublicKey;
import com.werewolfchat.startup.ntru.encrypt.NtruEncrypt;
import com.werewolfchat.startup.ntru.sign.NtruSign;
import com.werewolfchat.startup.ntru.sign.SignatureKeyPair;
import com.werewolfchat.startup.ntru.sign.SignatureParameters;
import com.werewolfchat.startup.ntru.util.ArrayEncoder;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.werewolfchat.startup.Utility.ENCRYPTION_PARAMS;
import static com.werewolfchat.startup.Utility.dumb_debugging;
import static com.werewolfchat.startup.Utility.makeGetStringForPublishingMessages;
import static com.werewolfchat.startup.Utility.makeGetStringForPullingMessagesAfterTime;


public class MainActivity extends AppCompatActivity {
    private static final SignatureParameters SIGNATURE_PARAMS = SignatureParameters.APR2011_439_PROD;
    NtruEncrypt ntruEnc;
    NtruSign ntruSig;

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        ImageView messageImageView;
        TextView messengerTextView;
        CircleImageView messengerImageView;

        public MessageViewHolder(View v) {
            super(v);
            messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
            messageImageView = (ImageView) itemView.findViewById(R.id.messageImageView);
            messengerTextView = (TextView) itemView.findViewById(R.id.messengerTextView);
            messengerImageView = (CircleImageView) itemView.findViewById(R.id.messengerImageView);
        }

        public void bindData(WerewolfMessage input) {
            this.messageTextView.setText(input.getText());
            this.messengerTextView.setText(input.getSenderId());

        }
    }

    private static final String TAG = "MainActivity";
    public static final String MESSAGES_CHILD = "messages";
    private static final int REQUEST_INVITE = 1;
    private static final int REQUEST_IMAGE = 2;
    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 10;
    public static final String ANONYMOUS = "anonymous";
    private static final String MESSAGE_SENT_EVENT = "message_sent";
    private String mUsername;
    private String mPhotoUrl;
    private SharedPreferences mSharedPreferences;
    private GoogleApiClient mGoogleApiClient;

    private Button mSendButton;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;
    private EditText mMessageEditText;
    private ImageView mAddMessageImageView;


    // needed to stop the runnable from calling pull messages
    public volatile boolean doneCheckingforMessages;


    // encryption values
    private File priavteKeyStorage;
    private static final String PATH = "werewolfchatkeys/";
    private EncryptionKeyPair encKP;
    private SignatureKeyPair sigKP;
    private String werewolfChatId;
    private String destinationID;
    private EncryptionPublicKey destinationKey;

    // add stuff
    private AdView mAdView;
    private AdLoader nativeAdLoader;


    // buttons showing to and from
    private Button toButton;
    private Button asButton;


    // what server framework to use
    private String serverUrl;
    private RequestQueue queue;
    private long lastPullTime;
    private Context context;
    private PrivateServerAdapter privateServerAdapter;
    private List<WerewolfMessage> privateMessages;
    private Handler handler;
    private Runnable queryCaller;
    private ExtrasManager extrasManager;
    private TokenManager tokenManager;



    public class PrivateServerAdapter extends RecyclerView.Adapter {


        private List<WerewolfMessage> messageList = new ArrayList<>();
        private Context context;


        public PrivateServerAdapter(Context inContext, List<WerewolfMessage> messages) {
            this.messageList = messages;
            this.context = inContext;
        }


        @Override
        public MessageViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
            return new MessageViewHolder(inflater.inflate(R.layout.item_message, viewGroup, false));
        }


        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            // WerewolfMessage message = messageList.get(position);

            ((MessageViewHolder) holder).bindData(messageList.get(position));
        }


        @Override
        public int getItemCount() {
            return this.messageList.size();
        }

        @Override
        public int getItemViewType(final int position) {
            return R.layout.item_message;
        }


    }

    public void startPullingMessages() {
        doneCheckingforMessages = false;
        this.pullAllMessages();

        // Create the Handler object (on the main thread by default)
        handler = new Handler();
// Define the code block to be executed
        queryCaller = new Runnable() {
            @Override
            public void run() {

                if (doneCheckingforMessages) {
                    dumb_debugging("stopping the messages from being pulled");
                    return;
                }
                pullAllMessagesAfterTime(lastPullTime);


                if (extrasManager.getTimeOut() > 0) {
                    // Repeat for half a second longer than the timeout
                    handler.postDelayed(this, extrasManager.getTimeOut() + 500);
                } else {
                    // default will be 4 seconds
                    handler.postDelayed(this, 4000);
                }

            }
        };
// Start the initial runnable task by posting through the handler
        handler.post(queryCaller);
    }


    public void load_prserverUrlivate_server_details() {
        this.serverUrl = this.getIntent().getStringExtra("private_server_url");
    }

    public class GoodMessageSend implements Utility.Command {
        public void execute(String data) {

            if (data.equals("success")) {
                mSendButton.setText("Send");
                return;
            } else {
                mSendButton.setText("Error on last send");

            }

        }
    }


    public class BadMessageSend implements Utility.Command {
        public void execute(String data) {
            mSendButton.setText("Error on last send");
            dumb_debugging("could not publish because " + data);
        }
    }

    public void sendMessageWithPrivateServer(String toid, String fromid, String message) {

        String queryStr = makeGetStringForPublishingMessages(this.serverUrl, toid, fromid, message, tokenManager.getTokenString());
        //String queryStr = Utility.makeGetStringForPublishingMessages(extrasManager.getPrivateServerURL(), extrasManager.getChatID(), tokenManager.getTokenString());
        dumb_debugging("about to try and send a message with this string\n" + queryStr);
        //String queryStr = makeGetStringForPublishingMessages(this.serverUrl, toid, fromid, message);
        Utility.queryURL(queryStr, this.queue, new GoodMessageSend(), new BadMessageSend());
    }


    public class PullMessageGoodResults implements Utility.Command {
        public void execute(String data) {
            ArrayList<JSONObject> jsonResults = Utility.cleanServerResults(data);
            final NestedScrollView tempNestedScrollView = (NestedScrollView) findViewById(R.id.dest_end_scroller);

            final LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(LinearLayout.VERTICAL);


            if (jsonResults.size() == 0) {
                dumb_debugging("No Messages found for " + werewolfChatId);
                return;
            } else {
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                ArrayList<WerewolfMessage> chatArray = new ArrayList<>();


                for (JSONObject jobj : jsonResults) {

                    try {
                        chatArray.add(new WerewolfMessage(jobj.getString("toid"), jobj.getString("fromid"), jobj.getString("encmessagehexstr")));
                    } catch (Exception e) {
                        Utility.dumb_debugging("something was bad with the json for " + jobj.toString() + "\n" + e.toString());
                    }

                }


// Add Buttons
                for (WerewolfMessage chat : chatArray) {

                    chat.convertEncryptedHexStringToPlainTextString(encKP, ntruEnc);

                }
                privateMessages.addAll(chatArray);
                privateServerAdapter.notifyDataSetChanged();
            }
        }
    }

    public class PullMessageBadResults implements Utility.Command {
        public void execute(String data) {
            Utility.dumb_debugging("hit error in pull messages " + data);
        }
    }


    public void pullAllMessages() {
        //String queryStr = Utility.makeGetStringForPullingMessages(this.serverUrl, this.werewolfChatId);
        String queryStr = Utility.makeGetStringForPullingMessagesWithToken(extrasManager.getPrivateServerURL(), extrasManager.getChatID(), extrasManager.tokenString);

        Utility.queryURL(queryStr, this.queue, new PullMessageGoodResults(), new PullMessageBadResults());
        lastPullTime = System.currentTimeMillis();

    }


    public void pullAllMessagesAfterTime(long time) {
        //String queryStr = Utility.makeGetStringForPullingMessagesAfterTime(this.serverUrl, this.werewolfChatId, time);
        String queryStr = makeGetStringForPullingMessagesAfterTime(extrasManager.getPrivateServerURL(), extrasManager.getChatID(), time, extrasManager.tokenString);
        dumb_debugging(queryStr);
        Utility.queryURL(queryStr, this.queue, new PullMessageGoodResults(), new PullMessageBadResults());
        lastPullTime = System.currentTimeMillis();

    }


    public void initlize_chat() {


        if (!this.extrasManager.areAllTheLocalPKIExtrasSet()) {
            // at least one the extra stings for pki stuff are not set
            return;
        } else {
            Utility.dumb_debugging("in load_intent about to make a new enc KP from extras");
            this.encKP = this.extrasManager.getKeyPair();
            Utility.dumb_debugging("about to set chat id from extras");
            this.werewolfChatId = this.extrasManager.getChatID();
            String asButtonText = "As: " + this.werewolfChatId;
            this.asButton.setText(asButtonText);
            this.serverUrl = this.extrasManager.getPrivateServerURL();
        }

        // if the distant end is configured, set up the key
        if (this.extrasManager.areAllTheDistEndExtrasSet()) {
            this.destinationID = this.extrasManager.getDestEndID();
            this.destinationKey = this.extrasManager.getDestKeyAsEncryptionPublicKey();
            String toButtonText = "To: " + this.destinationID;
            this.toButton.setText(toButtonText);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // needed to allow volly
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        context = this;
        this.extrasManager = new ExtrasManager(this.getIntent());
        this.extrasManager.loadIntent();

        this.queue = this.extrasManager.makeVolley(this);



        /*for real adloader info ca-app-pub-7947382408011370/3809869453 */


        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);


        // Set default username is anonymous.
        mUsername = ANONYMOUS;


        this.toButton = (Button) findViewById(R.id.toButton);
        this.asButton = (Button) findViewById(R.id.asButton);

        this.ntruEnc = new NtruEncrypt(ENCRYPTION_PARAMS);


        if (!this.extrasManager.hasPrivateServerURL) {
            // No server has been set, launch the Sign In activity
            extrasManager.setAutoPub(true);
            startActivity(extrasManager.copyExtrasToNewIntent(new Intent(this, SignInActivity.class)));
            finish();
            return;
        } else if (!this.extrasManager.areAllTheLocalPKIExtrasSet()) {
            // if any of the pki extras are not set and if any of the pki varibles are not set
            // then we need to call the key management activity


            extrasManager.setAutoPub(true);
            extrasManager.copyOverExtrasAndChangeClassToPrepareToStartNewActivity(MainActivity.this,
                    ConfigureKeysAndIDActivity.class);

            startActivity(extrasManager.getIntent());
            finish();
            return;
        } else {
            //we are using a private server


            //see if we can get the add bar to load
            MobileAds.initialize(this, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {
                }

            });

            mAdView = (AdView) findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);





            mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

            initlize_chat();


            // Initialize ProgressBar and RecyclerView.
            privateMessages = new ArrayList<WerewolfMessage>();
            mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
            mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
            mLinearLayoutManager = new LinearLayoutManager(this);
            mLinearLayoutManager.setStackFromEnd(true);
            mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
            privateServerAdapter = new PrivateServerAdapter(this, privateMessages);

            privateServerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    super.onItemRangeInserted(positionStart, itemCount);
                    int friendlyMessageCount = privateServerAdapter.getItemCount();
                    int lastVisiblePosition =
                            mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                    // If the recycler view is initially being loaded or the
                    // user is at the bottom of the list, scroll to the bottom
                    // of the list to show the newly added message.
                    if (lastVisiblePosition == -1 ||
                            (positionStart >= (friendlyMessageCount - 1) &&
                                    lastVisiblePosition == (positionStart - 1))) {
                        mMessageRecyclerView.scrollToPosition(positionStart);
                    }
                }
            });

            mMessageRecyclerView.setAdapter(privateServerAdapter);


        }


        if (this.destinationID != null) {
            String toText = "To: " + this.destinationID;
            this.toButton.setText(toText);
        }


        // pressing the to button changes the distant end
        this.toButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                extrasManager.copyOverExtrasAndChangeClassToPrepareToStartNewActivity(MainActivity.this,
                        DistEndPickerActivity.class);
                startActivity(extrasManager.getIntent());
                finish();
                return;
            }
        });

        // pressing the as buttton changes your id and key
        this.asButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                extrasManager.setAutoPub(false);
                extrasManager.copyOverExtrasAndChangeClassToPrepareToStartNewActivity(MainActivity.this,
                        ConfigureKeysAndIDActivity.class);
                startActivity(extrasManager.getIntent());
                finish();
                return;
            }
        });

        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mSendButton = (Button) findViewById(R.id.sendButton);

        //EncryptionPublicKey temp =  this.destinationKey;

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (destinationKey == null || destinationID == null) {
                    return;
                }
                WerewolfMessage werewolfMessage = new
                        WerewolfMessage(mMessageEditText.getText().toString(),
                        mUsername,
                        mPhotoUrl,
                        null /* no image */,
                        werewolfChatId,
                        destinationID);
                Utility.dumb_debugging("about to send this text \n" + werewolfMessage.getText() +
                        "\nwith this key \n" + ArrayEncoder.bytesToHex(destinationKey.getEncoded()));
                werewolfMessage.convertPlainTextStringToEncryptedHexString(destinationKey, ntruEnc);
                Utility.dumb_debugging("about to send this text \n" + werewolfMessage.getText());


                sendMessageWithPrivateServer(destinationID, werewolfChatId, werewolfMessage.getText());

                mMessageEditText.setText("");
            }
        });

        mAddMessageImageView = (ImageView) findViewById(R.id.addMessageImageView);
        mAddMessageImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_IMAGE);
            }
        });

        this.tokenManager = this.extrasManager.makeNewTokenManager(this.extrasManager.getTokenString());

        // registers the runable to start the volleys
        startPullingMessages();

    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in.
        // TODO: Add code to check if user is signed in.
    }

    @Override
    public void onPause() {

        super.onPause();
        //kill the volley
        this.queue.stop();
        this.handler.removeCallbacks(this.queryCaller);
        doneCheckingforMessages = true;


    }

    @Override
    public void onResume() {
        super.onResume();
        doneCheckingforMessages = false;

        if (this.queue != null)
            startPullingMessages();


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //kill the volley
        //this.queue.stop();
        //this.handler.removeCallbacks(this.queryCaller);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:

                mUsername = ANONYMOUS;

                //kill the volley
                doneCheckingforMessages = true;
                this.queue.stop();
                this.queue.cancelAll(new RequestQueue.RequestFilter() {
                    @Override
                    public boolean apply(Request<?> request) {
                        return true;
                    }
                });
                this.handler.removeCallbacks(this.queryCaller);
                // queryCaller.


                Intent signInIntent = new Intent(this, SignInActivity.class);
                this.extrasManager.setIntent(signInIntent);
                this.extrasManager.wipeIntent();
                startActivity(this.extrasManager.getIntent());
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
