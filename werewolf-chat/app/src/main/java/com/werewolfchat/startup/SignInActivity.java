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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import static com.werewolfchat.startup.Utility.cleanServerResults;
import static com.werewolfchat.startup.Utility.convertStringToPortNumberOrReturnNegitiveOneIfNotGoodInput;
import static com.werewolfchat.startup.Utility.dumb_debugging;
import static com.werewolfchat.startup.Utility.isServerReachable;
import static com.werewolfchat.startup.Utility.isStringAValidHostnameColonPortNumber;

// fixing this is so frustrating
//import android.support.v7.app.AppCompatActivity;


public class SignInActivity extends AppCompatActivity implements View.OnClickListener {

    private ExtrasManager extrasManager;
    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;

    private static final int HTTPS_SIGN_IN = 1;
    private static final int TOR_SIGN_IN = 2;
    private static final int I2P_SIGN_IN = 3;




    private Button connectButton;



    private EditText urlTextField;
    private EditText proxyTextField;

    private Spinner networkSpinner;
    private Spinner proxySpinner;
    private ArrayAdapter<CharSequence> networkAdapter;
    private ArrayAdapter<CharSequence> proxyAdapter;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);


        this.extrasManager = new ExtrasManager(this.getIntent());


        //build spinners
        networkSpinner = (Spinner) findViewById(R.id.network_spinner);
        proxySpinner = (Spinner) findViewById(R.id.proxy_spinner);

        //add values to adapters for spinners
        networkAdapter = ArrayAdapter.createFromResource(this,R.array.network_array, android.R.layout.simple_spinner_item);
        proxyAdapter = ArrayAdapter.createFromResource(this,R.array.proxy_array, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        networkAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        proxyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapters to the spinners
        networkSpinner.setAdapter(networkAdapter);
        proxySpinner.setAdapter(proxyAdapter);

        // set defaults for the spinnners
        networkSpinner.setSelection(networkAdapter.getPosition("HTTPS"));
        proxySpinner.setSelection(proxyAdapter.getPosition("None"));

        // add the class as the on selceted listener
        networkSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String networkType = (String) networkAdapter.getItem(position);
                switch (networkType)
                {
                    case "HTTPS":
                        urlTextField.setText("https://" + Utility.httpsAddress + "/api/");
                        proxySpinner.setSelection(proxyAdapter.getPosition("None"));
                        proxyTextField.setText("");
                        break;
                    case "TOR":
                        urlTextField.setText("http://" + Utility.torAddress + "/api/");
                        proxySpinner.setSelection(proxyAdapter.getPosition("SOCKS"));
                        proxyTextField.setText("localhost:9050");
                        break;
                    case "I2P":
                        urlTextField.setText("http://" + Utility.i2pAddress + "/api/");
                        proxySpinner.setSelection(proxyAdapter.getPosition("HTTP"));
                        proxyTextField.setText("localhost:4444");
                        break;
                    case "Custom":
                        urlTextField.setText("enter your server's url here");
                        proxySpinner.setSelection(proxyAdapter.getPosition("None"));
                        proxyTextField.setText("");
                        break;
                    default:
                        urlTextField.setText("");
                        proxySpinner.setSelection(proxyAdapter.getPosition("None"));
                        proxyTextField.setText("");
                        break;

                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        proxySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String proxyType = (String) proxyAdapter.getItem(position);
                switch (proxyType)
                {
                    case "SOCKS":
                        proxyTextField.setText("localhost:9050");

                        break;
                    case "HTTP":

                        proxyTextField.setText("localhost:4444");
                        break;
                    case "None":
                        proxyTextField.setText("");
                        break;
                    default:
                        proxyTextField.setText("");
                        break;

                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });


        // add private server button
        connectButton = (Button) findViewById(R.id.sign_in_button);
        connectButton.setOnClickListener(this);
        // add edit text for the private server url and proxy url
        urlTextField = (EditText) findViewById(R.id.server_url_field);

        proxyTextField = (EditText) findViewById(R.id.proxy_url_field);



    }




    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                String networkType = (String) networkAdapter.getItem(networkSpinner.getSelectedItemPosition());
                switch (networkType)
                {
                    case "HTTPS":
                        serverSignInWorker(HTTPS_SIGN_IN);
                        break;
                    case "TOR":
                        serverSignInWorker(TOR_SIGN_IN);
                        break;
                    case "I2P":
                        serverSignInWorker(I2P_SIGN_IN);
                        break;
                    case "Custom":
                        serverSignInWorker(I2P_SIGN_IN);
                        break;
                    default:
                        break;

                }

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

        //proxySpinner.setSelection(proxyAdapter.getPosition("None"));
        String proxyType = (String) proxySpinner.getSelectedItem();
        int proxyCode = 0;
        switch (proxyType)
        {
            case "SOCKS":
                proxyCode = 1;
                break;
            case "HTTP":
                proxyCode = 2;
                break;
            case "None":
                proxyCode = 0;
                break;
            default:
                proxyCode = 0;
                break;
        }
        String proxyURL = "";
        int proxyPort = -1;
        if (isStringAValidHostnameColonPortNumber(proxyTextField.getText().toString()))
        {
            String[] parts = proxyTextField.getText().toString().split(":");
            proxyURL = parts[0];
            proxyPort = Integer.parseInt(parts[1]);
            dumb_debugging("this is proxy url "+proxyURL);
            serverTestResult = isServerReachable(this,urlTextField.getText().toString(), proxyPort, proxyURL, proxyCode);

        }
        else
        {
            serverTestResult = isServerReachable(this, urlTextField.getText().toString());
        }


 /*       switch (type) {
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
*/
        dumb_debugging("proxy text is " + proxyText);

        if (serverTestResult) {
            extrasManager.setIntent(new Intent(SignInActivity.this,
                    ConfigureKeysAndIDActivity.class));
            extrasManager.setPrivateServerURL(urlTextField.getText().toString());

            // set the timeouts depending on connection type
            //TODO actually come up with good numbers for this
            /*switch (type)
            {
                case HTTPS_SIGN_IN:
                    extrasManager.setTimeOut(2000); // TODO say I am sorry to those that use GEO stationary satcomm this will probably fail
                    break;
                case TOR_SIGN_IN:
                    extrasManager.setTimeOut(3000);
                    break;
                case I2P_SIGN_IN:
                    extrasManager.setTimeOut(5000);
                    break;

            }*/
            extrasManager.setTimeOut(7500);




            dumb_debugging("Proxy port is " + Integer.toString(proxyPort));
            if (proxyPort > 0) {
                // hostname:port:type
                //extrasManager.setProxyInfo(new String[] {proxyURL,Integer.toString(proxyPort), Integer.toString(proxyCode)});
                extrasManager.setProxyInfo(new String[] {proxyURL,Integer.toString(proxyPort), proxyCode == 1 ? "SOCKS" : "HTTP" });
                dumb_debugging("proxy array is in the extras manager");
            }
            extrasManager.setAutoPub(true);
            startActivity(extrasManager.getIntent());
            finish();
        } else {
            String proxyStatusText = "";
            if (proxyPort != -1)
                proxyStatusText = " with proxy on " + proxyPort;

            connectButton.setText("Sign in failed");



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


    /*
    private void privateServerSignIn() {
        String hostname = privateServerInput.getText().toString();
        hostname = hostname.trim();
        // no more ssl nuking for now
        // TODO figure out how to get run time shady certs the the user might need to work without nuking all the good certs
        //Utility.nuke();
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);


        if (hostname.isEmpty()) {
            privateServerButton.setText("URL not set");
            return;
        }
        int proxyPortNum = -1;
        if (!privateServerProxyPortInput.getText().toString().isEmpty()) {
            proxyPortNum = Integer.parseInt(privateServerProxyPortInput.getText().toString());
            if (proxyPortNum < 1) {
                privateServerButton.setText("Bad Proxy Port");
                return;

            }
        }

        if (!(hostname.startsWith("http://") || hostname.startsWith("https://"))) {
            privateServerButton.setText("assuming http");
            hostname = "http://" + hostname;

        }

        Utility.dumb_debugging("Checking this server " + hostname + " with proxy " + proxyPortNum);
        if ((proxyPortNum > 0) ? isServerReachable(this, hostname, proxyPortNum) : isServerReachable(this, hostname)) {
            extrasManager.copyOverExtrasAndChangeClassToPrepareToStartNewActivity(SignInActivity.this,
                    ConfigureKeysAndIDActivity.class);
            extrasManager.setPrivateServerURL(hostname);
            if (proxyPortNum > 0) {
                extrasManager.setProxyPort(proxyPortNum);
            }
            extrasManager.setPrivateServerURL(hostname);
            extrasManager.setAutoPub(true);
            startActivity(extrasManager.getIntent());
            finish();
        } else {
            privateServerButton.setText("server test failed, please try again");
        }

    }*/


}
