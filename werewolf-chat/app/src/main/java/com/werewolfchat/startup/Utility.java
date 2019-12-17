package com.werewolfchat.startup;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Random;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

//import android.support.v4.widget.NestedScrollView;


//import static android.support.constraint.Constraints.TAG;

public class Utility {


    public static final String torAddress = "pmbldnf4zyeb4esolfmfv2uzfzdcusgzvxwkg5r6fnm7vbunjk5gvfyd.onion";

    //public static final String  torHttpsAddress = "khskqy4zzbymmoiczgwwnwfi7y6hmgefca2vkluleemp2j27r4qdwoid.onion";

    public static final String i2pAddress = "tajaf6gpja4kdm4jxi3vsbltep5vtslbyrmxru5zw5yl5kymtzsq.b32.i2p";

    public static final String httpsAddress = "werewolfchat.com";



    public static boolean isInteger(String str) {
       return str.matches("\\d+");
    }

    public static boolean isGoodPortNumber(int numToCheck)
    {
        return numToCheck >= 0 && numToCheck <= 65535;
    }




    // semantically meaningful method names
    // probably bad coupling
    public static int convertStringToPortNumberOrReturnNegitiveOneIfNotGoodInput(String inputStr)
    {
        if(isInteger(inputStr))
        {
            int convertedIntFromStr = Integer.parseInt(inputStr);
            if(isGoodPortNumber(convertedIntFromStr))
            {
                return convertedIntFromStr;
            }
        }
        return -1;
    }

    public static Intent addProxyToIntent(Intent intent, int proxy)
    {
        if(proxy >=0) {
            intent = intent.putExtra("proxy_port", proxy);
        }
        return  intent;
    }


    public static Intent addProxyToIntent(Intent intent, String proxyStr)
    {

        int proxy = convertStringToPortNumberOrReturnNegitiveOneIfNotGoodInput(proxyStr);
        if(proxy >=0) {
            intent = intent.putExtra("proxy_port", proxy);
        }
        return  intent;
    }

    public static  boolean isTheProxyPortExtraNull(Intent intent)
    {
        int valueFromIntent = intent.getIntExtra("proxy_port",-1);
        if(isGoodPortNumber(valueFromIntent))
            return false;
        else
            return true;
    }

    public static int getProxyPortFromIntent(Intent intent)
    {
        return intent.getIntExtra("proxy_port",-1);
    }


    public static boolean check_HTTPS_server(Context context, String proxyStr)
    {
        int proxyPort = convertStringToPortNumberOrReturnNegitiveOneIfNotGoodInput(proxyStr);
        String serverurl = "https://" + httpsAddress + "/api/pubkeys";
        if(proxyPort>=0)
        {
            dumb_debugging("checking with proxy");
            return isServerReachable(context, serverurl, proxyPort);
        }
        else
        {
            dumb_debugging("checking without proxy");
            return isServerReachable(context, serverurl);
        }
    }

    public static boolean check_TOR_server(Context context, String proxyStr)
    {
        //TODO figureout how to get a properly signed https cert for a .onion domain so I can get rid of the cert nuke
        //nuke();
        int proxyPort = convertStringToPortNumberOrReturnNegitiveOneIfNotGoodInput(proxyStr);
        String addresToCheck = "http://"+torAddress+"/api/pubkeys";
        if(proxyPort>=0)
        {
            return isServerReachable(context, addresToCheck, proxyPort);
            //return isServerReachable(context, addresToCheck, proxyPort);
        }
        else
        {
            return isServerReachable(context, addresToCheck);
        }
    }

    public static boolean check_I2P_server(Context context, String proxyStr)
    {

        int proxyPort = convertStringToPortNumberOrReturnNegitiveOneIfNotGoodInput(proxyStr);

        dumb_debugging("the proxy port is " + proxyPort);

        String addresToCheck = "http://"+i2pAddress+"/api/pubkeys";

        if(proxyPort>=0)
        {
            return isServerReachable(context, addresToCheck, proxyPort);
        }
        else
        {
            return isServerReachable(context, addresToCheck);
        }
    }



    // https://stackoverflow.com/questions/14376807/how-to-read-write-string-from-a-file-in-android
    public static void writeToFile(String data, Context context, String filePath) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(filePath, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }


    public static String convertStreamToString(InputStream is) throws IOException {
        // http://www.java2s.com/Code/Java/File-Input-Output/ConvertInputStreamtoString.htm
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        Boolean firstLine = true;
        while ((line = reader.readLine()) != null) {
            if (firstLine) {
                sb.append(line);
                firstLine = false;
            } else {
                sb.append("\n").append(line);
            }
        }
        reader.close();
        return sb.toString();
    }


    public static String getStringFromFile(String filePath) throws IOException {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = Utility.convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return ret;
    }

    public static boolean areEitherOfTheFirebaseExtrasNull(Intent intentToCheck) {
        return intentToCheck.getStringExtra("firebase_uid") == null ||
                intentToCheck.getStringExtra("firebase_email") == null;
    }

    public static Intent addEmailAndUIDToIntent(Intent intentPassedIn, String email, String uid) {

        intentPassedIn = intentPassedIn.putExtra("firebase_uid", uid);
        intentPassedIn = intentPassedIn.putExtra("firebase_email", email);

        return intentPassedIn;
    }

    // the one true way to debug, debugging with print statements
    public static void dumb_debugging(String text) {
        System.out.println(text);
        Log.d("Debuggin_via_statements",text);
    }

    // sees if either of the key pairs extras or the chat id extra are null
    // returns true if any are null
    // returns false if all are set
    public static boolean areAnyOfThePKIExtrasNull(Intent intent) {
        return intent.getByteArrayExtra("enckp_pub") == null ||
                intent.getByteArrayExtra("enckp_priv") == null ||
                intent.getStringExtra("chat_id") == null;

    }

    // sees if either of the dest id extra or the dest end kay extra are null
    // returns true if any are null
    // returns false if all are set
    public static boolean areTheDistantEndExtrasNull(Intent intent) {
        return intent.getStringExtra("dest_end_id") == null ||
                intent.getByteArrayExtra("dest_end_key") == null;
    }

    // adds the distant end info to the extras
    public static Intent addDistEndAndChatIDtoIntent(Intent intentPassedIn, String dest_id, byte[] dest_key) {
        intentPassedIn = intentPassedIn.putExtra("dest_end_id", dest_id);
        intentPassedIn = intentPassedIn.putExtra("dest_end_key", dest_key);
        return intentPassedIn;
    }

    // adds the distant end info to the extras
    public static Intent addLocalPKIExtras(Intent intentPassedIn, byte[] pub_key, byte[] priv_key, String chat_id) {
        intentPassedIn = intentPassedIn.putExtra("enckp_pub", pub_key);
        intentPassedIn = intentPassedIn.putExtra("enckp_priv", priv_key);
        intentPassedIn = intentPassedIn.putExtra("chat_id", chat_id);
        return intentPassedIn;
    }

    public static void debug_intent(Intent data) {
        Bundle bundle = data.getExtras();
        if (bundle != null) {
            for (String key : bundle.keySet()) {
                Object value = bundle.get(key);
              dumb_debugging(String.format("%s %s (%s)", key,
                        value.toString(), value.getClass().getName()));
            }
        }
    }

    public static String makeRandomString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890abcdefghijklmnopqrstuvwxyz";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 16) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }

    public static String convertWebStreamToString(InputStream inputStream) throws IOException {
        if (inputStream != null) {
            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 1024);
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                inputStream.close();
            }
            return writer.toString();
        } else {
            return "";
        }
    }

    public static JSONObject jsonFromStr(String input) {

        try {
            return new JSONObject(input);
        } catch (Exception e) {
            dumb_debugging(e.getMessage());

            dumb_debugging("must have bad JSON format in this: " + input);

        }
        return new JSONObject();
    }

    public static Intent addPrivateServerExtras(Intent intentPassedIn, String serverURL) {
        intentPassedIn = intentPassedIn.putExtra("private_server_url", serverURL);

        return intentPassedIn;
    }

    public static boolean check_if_on_private_server(Intent intentPassedIn) {
        if (intentPassedIn.getStringExtra("private_server_url") != null) {
            Utility.dumb_debugging("we are talking to a private server");
            //usePrivateServer = true;
            //serverUrl = this.getIntent().getStringExtra("private_server_url");
            return true;
        } else {
            Utility.dumb_debugging("we are not talking to a private server");
            //usePrivateServer = false;
            return false;

        }
    }

    public static Intent wipeExtras(Intent intentPassedIn) {
        if (intentPassedIn.getStringExtra("private_server_url") != null) {
            intentPassedIn.removeExtra("private_server_url");
        }


        if (intentPassedIn.getStringExtra("dest_end_id") != null) {
            intentPassedIn.removeExtra("dest_end_id");
        }

        if (intentPassedIn.getStringExtra("dest_end_key") != null) {
            intentPassedIn.removeExtra("dest_end_key");
        }

        if (intentPassedIn.getStringExtra("enckp_pub") != null) {
            intentPassedIn.removeExtra("enckp_pub");
        }

        if (intentPassedIn.getStringExtra("enckp_priv") != null) {
            intentPassedIn.removeExtra("enckp_priv");
        }

        if (intentPassedIn.getStringExtra("chat_id") != null) {
            intentPassedIn.removeExtra("chat_id");
        }

        if (intentPassedIn.getStringExtra("firebase_uid") != null) {
            intentPassedIn.removeExtra("firebase_uid");
        }

        if (intentPassedIn.getStringExtra("firebase_email") != null) {
            intentPassedIn.removeExtra("firebase_email");
        }

        if (intentPassedIn.getIntExtra("proxy_port",-99) != -99) {
            intentPassedIn.removeExtra("proxy_port");
        }

        if (intentPassedIn.getStringExtra("time_out") != null) {
            intentPassedIn.removeExtra("time_out");
        }


        return intentPassedIn;

    }

    public static  Intent addTimeOutToIntent(Intent intent, int timeOut)
    {
        intent = intent.putExtra("time_out", timeOut);
        return intent;
    }

    public static int getTimeOutFromIntnet(Intent intent)
    {
        return  intent.getIntExtra("time_out", -1);
    }

    public static boolean isTimeOutSetInIntent(Intent intent)
    {

        int timeout = intent.getIntExtra("time_out", -1);
        return  timeout >0;
    }


    public static boolean isServerReachable(Context context, String urlToTest) {



        ConnectivityManager connMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connMan.getActiveNetworkInfo();
        dumb_debugging("about to check this URL " + urlToTest );
        if (netInfo != null && netInfo.isConnected()) {
            try {
                URL urlServer = new URL(urlToTest);
                //Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8118));

                //HttpURLConnection urlConn = (HttpURLConnection) urlServer.openConnection(proxy);
                HttpURLConnection urlConn = (HttpURLConnection) urlServer.openConnection();

                urlConn.setConnectTimeout(5000); //<- 5 Seconds Timeout
                dumb_debugging("trying the test to " + urlToTest);

                urlConn.connect();
                if (urlConn.getResponseCode() == 200) {
                    dumb_debugging("it worked");

                    return true;
                } else {
                    dumb_debugging("it failed");

                    return false;
                }
            } catch (MalformedURLException e1) {
                dumb_debugging("this is a malformed url " + urlToTest);


                return false;
            } catch (IOException e) {
                dumb_debugging(e.getMessage());

                return false;
            }
        }
        return false;
    }

    public static boolean isServerReachableSocksProxy(Context context, String urlToTest, int proxyPort) {



        ConnectivityManager connMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connMan.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            try {
                URL urlServer = new URL(urlToTest);
                Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", proxyPort));

                HttpURLConnection urlConn = (HttpURLConnection) urlServer.openConnection(proxy);
                //HttpURLConnection urlConn = (HttpURLConnection) urlServer.openConnection();

                urlConn.setConnectTimeout(5000); //<- 5 Seconds Timeout
                dumb_debugging("trying the test to " + urlToTest);

                urlConn.connect();
                if (urlConn.getResponseCode() == 200) {
                    dumb_debugging("it worked");

                    return true;
                } else {
                    dumb_debugging("it failed");

                    return false;
                }
            } catch (MalformedURLException e1) {
                dumb_debugging("this is a malformed url " + urlToTest);


                return false;
            } catch (IOException e) {
                dumb_debugging(e.getMessage());

                return false;
            }
        }
        return false;
    }


    public static boolean isServerReachable(Context context, String urlToTest, int proxyPort) {



        ConnectivityManager connMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connMan.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            try {
                URL urlServer = new URL(urlToTest);
                Proxy proxy;

                if(proxyPort == 9050)https://www.reddit.com/r/werewolf_chat/
                    proxy =  new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", proxyPort));
                else
                    proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", proxyPort));

                HttpURLConnection urlConn = (HttpURLConnection) urlServer.openConnection(proxy);
                //HttpURLConnection urlConn = (HttpURLConnection) urlServer.openConnection();

                urlConn.setConnectTimeout(5000); //<- 5 Seconds Timeout
                dumb_debugging("trying the test to " + urlToTest);

                urlConn.connect();
                if (urlConn.getResponseCode() == 200) {
                    dumb_debugging("it worked");

                    return true;
                } else {
                    dumb_debugging("it failed");

                    return false;
                }
            } catch (MalformedURLException e1) {
                dumb_debugging("this is a malformed url " + urlToTest);


                return false;
            } catch (IOException e) {
                dumb_debugging(e.getMessage());

                return false;
            }
        }
        return false;
    }


    public static boolean isTorServerReachable(Context context, String urlToTest, int proxyPort) {



        ConnectivityManager connMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connMan.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            try {
                URL urlServer = new URL(urlToTest);
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", proxyPort));

                HttpURLConnection urlConn = (HttpURLConnection) urlServer.openConnection(proxy);
                //HttpURLConnection urlConn = (HttpURLConnection) urlServer.openConnection();

                urlConn.setConnectTimeout(5000); //<- 5 Seconds Timeout
                dumb_debugging("trying the test to " + urlToTest);

                urlConn.connect();
                if (urlConn.getResponseCode() == 200) {
                    dumb_debugging("it worked");

                    return true;
                } else {
                    dumb_debugging("it failed");

                    return false;
                }
            } catch (MalformedURLException e1) {
                dumb_debugging("this is a malformed url " + urlToTest);


                return false;
            } catch (IOException e) {
                dumb_debugging(e.getMessage());

                return false;
            }
        }
        return false;
    }





    // /sendmessage/:tochatid/:fromchatid/:messagetosend
    public static String makeGetStringForPublishingMessages(String serverUrl, String toid, String fromid, String message) {
        return serverUrl + "sendmessage/" + toid + "/" + fromid + "/" + message;
    }


    // /publishpubkey/:chatid/:pubkeystring
    public static String makeGetStringForPublishingKey(String serverUrl, String user, String key) {
        return serverUrl + "publishpubkey/" + user + "/" + key;
    }

    // /pubkeys
    public static String makeGetStringForPullingKeys(String serverUrl) {
        return serverUrl + "pubkeys";
    }

    // /messages/:chatid
    public static String makeGetStringForPullingMessages(String serverUrl, String user) {
        return serverUrl + "messages/" + user;
    }

    // /messagesaftertime/:chatid/:time
    public static String makeGetStringForPullingMessagesAfterTime(String serverUrl, String user, long time) {
        return serverUrl + "messagesaftertime/" + user + "/" + Long.toString(time);
    }

    // credit goes to https://stackoverflow.com/questions/2186931/java-pass-method-as-parameter
    // for this lamda function like hack
    public interface Command {
        public void execute(String data);
    }


    public static void queryURL(String url, RequestQueue passedInQueue, Command inputWorkerForGood, Command inputWorkerForBad) {
        final Command worker_good = inputWorkerForGood;
        final Command worker_bad = inputWorkerForBad;
// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        worker_good.execute(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                worker_bad.execute(error.toString());
            }
        });

// Add the request to the RequestQueue.
        passedInQueue.add(stringRequest);
    }


    // this is kind of a hack, but it was getting frustrating trying to work with collections
    public static ArrayList<JSONObject> cleanServerResults(String inputStr) {
        ArrayList<JSONObject> output = new ArrayList<JSONObject>();
        String[] splitStrings = inputStr.split("[\\{||\\}]");
        for (int ii = 0; ii < splitStrings.length; ii++) {
            if (!(splitStrings[ii].equals(",") || splitStrings[ii].equals("]") || splitStrings[ii].equals("[")
                    || splitStrings[ii].equals("[]") || splitStrings[ii].equals("{}") || splitStrings[ii].equals("{[]}") ||
                    splitStrings[ii].equals("[{}]"))) {
                output.add(jsonFromStr("{" + splitStrings[ii] + "}"));
            }
        }


        return output;
    }



}
