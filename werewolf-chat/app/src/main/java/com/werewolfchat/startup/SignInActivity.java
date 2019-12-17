/**
 * Copyright Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.werewolfchat.startup;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import static com.werewolfchat.startup.Utility.convertStringToPortNumberOrReturnNegitiveOneIfNotGoodInput;
import static com.werewolfchat.startup.Utility.dumb_debugging;
import static com.werewolfchat.startup.Utility.isServerReachable;

// fixing this is so frustrating
//import android.support.v7.app.AppCompatActivity;


public class SignInActivity extends AppCompatActivity implements View.OnClickListener {

    private ExtrasManager extrasManager;
    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;

    private static final int HTTPS_SIGN_IN = 1;
    private static final int TOR_SIGN_IN = 2;
    private static final int I2P_SIGN_IN = 3;




    private Button privateServerButton;

    private Button httpsServerButton;
    private Button torServerButton;
    private Button i2pServerButton;


    private EditText privateServerInput;
    private EditText privateServerPortInput;
    private EditText privateServerPasscodeInput;


    private EditText httpsProxyPortTextField;
    private EditText torProxyPortTextField;
    private EditText i2pProxyPortTextField;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);


        this.extrasManager = new ExtrasManager(this.getIntent());


        // add private server button
        privateServerButton = (Button) findViewById(R.id.private_sign_in_button);

        // add edit text for the private server url
        privateServerInput = (EditText) findViewById(R.id.private_server_url_input);

        privateServerPortInput = (EditText) findViewById(R.id.private_server_port_input);

        privateServerPasscodeInput = (EditText) findViewById(R.id.private_server_passcode_input);


        httpsProxyPortTextField = (EditText) findViewById(R.id.https_edit_text);
        torProxyPortTextField = (EditText) findViewById(R.id.tor_edit_text);
        i2pProxyPortTextField = (EditText) findViewById(R.id.i2p_edit_text);

        httpsServerButton = (Button) findViewById(R.id.https_sign_in_button);
        torServerButton = (Button) findViewById(R.id.tor_sign_in_button);
        i2pServerButton = (Button) findViewById(R.id.i2p_sign_in_button);

        // Set click listeners
        httpsServerButton.setOnClickListener(this);
        torServerButton.setOnClickListener(this);
        i2pServerButton.setOnClickListener(this);


        // set the click listener fir the annon button
        privateServerButton.setOnClickListener(this);


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.https_sign_in_button:
                httpsServerSignIn();
                break;
            case R.id.tor_sign_in_button:
                torServerSignIn();
                break;

            case R.id.i2p_sign_in_button:
                i2pServerSignIn();
                break;

            case R.id.private_sign_in_button:
                privateServerSignIn();
                break;
        }
    }


    // type is the server type
    // 1 for https
    // 2 for tor
    // 3 for i2p
    private void serverSignInWorker(int type) {

        if (!(type >= 1 && type <= 3)) {
            dumb_debugging("the wrong op code was sent: " + type);
            return;
        }

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        String privateServerURL = "dumb";
        String proxyText = "dumb";
        boolean serverTestResult = false;


        int proxyPort = -1;

        switch (type) {
            case HTTPS_SIGN_IN: // https
                privateServerURL = "https://" + Utility.httpsAddress + "/api/";
                proxyText = httpsProxyPortTextField.getText().toString();
                proxyPort = convertStringToPortNumberOrReturnNegitiveOneIfNotGoodInput(proxyText);
                httpsServerButton.setText("checking server");

                serverTestResult = Utility.check_HTTPS_server(this, proxyText);
                break;
            case TOR_SIGN_IN: //tor
                privateServerURL = "http://" + Utility.torAddress + "/api/";
                proxyText = torProxyPortTextField.getText().toString();
                proxyPort = convertStringToPortNumberOrReturnNegitiveOneIfNotGoodInput(proxyText);
                torServerButton.setText("checking server");

                serverTestResult = Utility.check_TOR_server(this, proxyText);
                break;
            case I2P_SIGN_IN: //i2p
                privateServerURL = "http://" + Utility.i2pAddress + "/api/";
                proxyText = i2pProxyPortTextField.getText().toString();
                proxyPort = convertStringToPortNumberOrReturnNegitiveOneIfNotGoodInput(proxyText);
                i2pServerButton.setText("checking server");
                serverTestResult = Utility.check_I2P_server(this, proxyText);
                break;
        }

        dumb_debugging("proxy text is " + proxyText);

        if (serverTestResult) {
            extrasManager.setIntent(new Intent(SignInActivity.this,
                    ConfigureKeysAndIDActivity.class));
            extrasManager.setPrivateServerURL(privateServerURL);


            // set the timeouts depending on connection type
            //TODO actually come up with good numbers for this
            switch (type)
            {
                case HTTPS_SIGN_IN:
                    extrasManager.setTimeOut(2000); // TODO say I am sorry to those that use GEO stationary satcomm this will probably fail
                    break;
                case TOR_SIGN_IN:
                    extrasManager.setTimeOut(3000);
                    break;
                case I2P_SIGN_IN:
                    extrasManager.setTimeOut(4000);
                    break;

            }



            if (proxyPort > 0) {
                extrasManager.setProxyPort(proxyPort);
            }
            startActivity(extrasManager.getIntent());
            finish();
        } else {
            String proxyStatusText = "";
            if (proxyPort != -1)
                proxyStatusText = " with proxy on " + proxyPort;


            switch (type) {
                case HTTPS_SIGN_IN: // https
                    httpsServerButton.setText("https failed" + proxyStatusText);
                    break;
                case TOR_SIGN_IN: //tor
                    torServerButton.setText("tor failed" + proxyStatusText);
                    break;
                case I2P_SIGN_IN: //i2p
                    i2pServerButton.setText("i2p failed" + proxyStatusText);
                    break;
            }


        }

    }


    private void httpsServerSignIn() {
        // call the worker for https
        serverSignInWorker(HTTPS_SIGN_IN);
    }


    private void torServerSignIn() {
        // call the worker for tor
        serverSignInWorker(TOR_SIGN_IN);

    }


    private void i2pServerSignIn() {
        // call the worker for i2p
        serverSignInWorker(I2P_SIGN_IN);
    }


    private void privateServerSignIn() {
        String hostname = privateServerInput.getText().toString();
        // no more ssl nuking for now
        // TODO figure out how to get run time shady certs the the user might need to work without nuking all the good certs
        //Utility.nuke();
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);


        if (hostname.isEmpty()) {
            privateServerButton.setText("URL not set");
            return;
        }
        int portNum = 443;
        if (!privateServerPortInput.getText().toString().isEmpty()) {
            portNum = Integer.parseInt(privateServerPortInput.getText().toString());
        }
        String passcode = privateServerPasscodeInput.getText().toString();

        if (!passcode.isEmpty()) {
            passcode = passcode + "/";
        }

        String privateServerURL = "https://" + hostname + ":" + Integer.toString(portNum) + "/" + passcode;

        Utility.dumb_debugging("Checking this server " + privateServerURL);

        privateServerButton.setText("checking server " + privateServerURL);
        if (isServerReachable(this, privateServerURL + "pubkeys")) {
            extrasManager.copyOverExtrasAndChangeClassToPrepareToStartNewActivity(SignInActivity.this,
                    ConfigureKeysAndIDActivity.class);
            extrasManager.setPrivateServerURL(privateServerURL);
            startActivity(extrasManager.getIntent());
            finish();
        } else {
            privateServerButton.setText("server test failed, please try again");
        }
    }
}
