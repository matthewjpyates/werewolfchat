package com.werewolfchat.startup;


import android.app.Activity;
import android.icu.text.SymbolTable;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.werewolfchat.startup.ntru.encrypt.EncryptionKeyPair;
import com.werewolfchat.startup.ntru.encrypt.EncryptionParameters;
import com.werewolfchat.startup.ntru.encrypt.EncryptionPublicKey;
import com.werewolfchat.startup.ntru.encrypt.NtruEncrypt;

import static com.werewolfchat.startup.Utility.ENCRYPTION_PARAMS;
import static com.werewolfchat.startup.Utility.dumb_debugging;
import static com.werewolfchat.startup.ntru.util.ArrayEncoder.bytesToHex;
import static com.werewolfchat.startup.ntru.util.ArrayEncoder.hexStringToByteArray;



import okhttp3.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class CryptoWorker
{



    private NtruEncrypt ntruEnc;

    public CryptoWorker()
    {
        this.ntruEnc = new NtruEncrypt(ENCRYPTION_PARAMS);
    }


    public String convertPlainTextStringToEncryptedHexString(EncryptionPublicKey pubKey, String plainText) {
        return bytesToHex(this.ntruEnc.encrypt(plainText.getBytes(), pubKey));
    }

    public String convertEncryptedHexStringToPlainTextString(EncryptionKeyPair key_pair, String cypherText) {
        return new String(this.ntruEnc.decrypt(hexStringToByteArray(cypherText), key_pair));
    }



    public static void printHelp()
    {

        System.out.println(
                "Cryptoworker.jar Version " +returnVersionStr() + "\n"+
                        "to print this help message -h or --help\n"+
                        "to encrypt: -e key_in_hex string_to_encrypt\n"+
                        "to encrypt: --encrypt key_in_hex string_to_encrypt\n"+
                        "to print the version: -v\n"+
                        "to start interactive mode: -i\n"+
                        "to start interactive mode with a key file: -i --File=/path/to/your/key/file\n"+
                        "to start interactive mode with an output directory: -i --Output_Dir=/path/to/output/directory/\n\n"+
                        "in interactive mode over https with no proxy to the public server is assumed unless you specify a different type\n"+
                        "the poxy is assumed to be localhost unless otherwise specified by --Proxy_Server=my.proxy.server.url\n"+
                        "TOR:\n"+
                        "to start interactive mode over TOR without a proxy: -i --TOR_No_Proxy\n"+
                        "to start interactive mode over TOR: -i --TOR_Proxy_SOCKS=9050\n"+
                        "to start interactive mode over TOR: -i --TOR_Proxy_HTTP=8118\n"+
                        "I2P:\n"+
                        "to start interactive mode over I2P without a proxy: -i --I2P_No_Proxy\n"+
                        "to start interactive mode over I2P: -i --I2P_Proxy_SOCKS=4444\n"+
                        "to start interactive mode over I2P: -i --I2P_Proxy_HTTP=4444\n"+
                        "HTTPS with a proxy:\n"+
                        "to start interactive mode over HTTPS with a SOCKS proxy -i --HTTPS_Proxy_SOCKS=8118\n"+
                        "to start interactive mode over HTTPS with a HTTP proxy -i --HTTPS_Proxy_HTTP=8118\n"+
                        "A private server:\n"+
                        "to start interactive mode with private server: -i --Private_Server_Url=https://something.com\n"+
                        "to start interactive mode with private server with an SOCKS proxy: -i --Private_Server_Url=https://something.com --Private_Server_Proxy_SOCKS=9050\n"+
                        "to start interactive mode with private server with an HTTP proxy: -i --Private_Server_Url=https://something.com --Private_Server_Proxy_HTTP=9050\n"+
                        "you can't combine the the TOR, I2P, and private_server flags with each other\n"+
                        "all other interactive mode flags can be combined as long as the first flag is an -i");


    }

    public static String returnVersionStr()
    {
        return "0.2";
    }

    public static void printVersion()
    {
        System.out.println(returnVersionStr());
    }



    public String encryptStr(String key, String thingToEncrypt)
    {

        EncryptionPublicKey epk = new EncryptionPublicKey(hexStringToByteArray(key));
        return convertPlainTextStringToEncryptedHexString(epk, thingToEncrypt);
    }

    public static OkHttpClient makeHTTPClientWithSocksProxy(int proxyPort)
    {
        Proxy proxy = new Proxy( Proxy.Type.SOCKS,
        new InetSocketAddress("localhost", proxyPort));

        return new OkHttpClient.Builder()
                .proxy(proxy)
                .build();

    }


    public static OkHttpClient makeHTTPClientWithSocksProxy(int proxyPort, String hostname)
    {
        Proxy proxy = new Proxy( Proxy.Type.SOCKS,
                new InetSocketAddress(hostname, proxyPort));

        return new OkHttpClient.Builder()
                .proxy(proxy)
                .build();

    }


    public static OkHttpClient makeHTTPClientWithHttpProxy(int proxyPort)
    {
        Proxy proxy = new Proxy( Proxy.Type.HTTP,
                new InetSocketAddress("localhost", proxyPort));

        return new OkHttpClient.Builder()
                .proxy(proxy)
                .build();

    }

    public static OkHttpClient makeHTTPClientWithHttpProxy(int proxyPort, String hostname)
    {
        Proxy proxy = new Proxy( Proxy.Type.HTTP,
                new InetSocketAddress(hostname, proxyPort));

        return new OkHttpClient.Builder().proxy(proxy)
                .build();

    }

    //  https://howtodoinjava.com/java/io/java-read-file-to-string-examples/

    public static String fileToString(String filePath)
    {
        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath)))
        {

            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null)
            {
                contentBuilder.append(sCurrentLine).append("\n");
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return contentBuilder.toString();
    }

    private static final int PROXY_TYPE_NONE = 0;
    private static final int PROXY_TYPE_SOCKS = 1;
    private static final int PROXY_TYPE_HTTP = 2;
    public class InteractiveModeWorker{
        private String serverUrl;
        private TokenManager tokenManager;
        private RequestQueue queue;
        private long lastPullTime;
        private String chatID;
        private EncryptionKeyPair encKP;
        private OkHttpClient httpClient;
        private String tokenStr;
        private String outputDir;
        private String proxyURL;
        private int proxyPort;
        private boolean usingProxy;



        private int proxyType; // 0 = none, 1 = SOCKS, 2 = HTTP


        public int getProxyType() {
            return proxyType;
        }

        public void setProxyType(int intputProxyType) {
            if (intputProxyType == PROXY_TYPE_NONE) { //0
                this.proxyType = PROXY_TYPE_NONE;
                this.setUsingProxy(false);
            }
            else if(intputProxyType == PROXY_TYPE_SOCKS) { // 1
                this.proxyType = PROXY_TYPE_SOCKS;
                this.setUsingProxy(true);
            }
            else if(intputProxyType == PROXY_TYPE_HTTP) { // 2
                this.proxyType = PROXY_TYPE_HTTP;
                this.setUsingProxy(true);
            }
        }



        public int getProxyPort() {
            return proxyPort;
        }

        public void setProxyPort(int intputProxyPort) {
            if(intputProxyPort>0 && intputProxyPort<65536) {
                this.proxyPort = intputProxyPort;
                this.setUsingProxy(true);
            }
            else
            {
                dumb_debugging(intputProxyPort +" is a bad port for a proxy");
            }
        }

        public boolean isUsingProxy() {
            return usingProxy;
        }

        public void setUsingProxy(boolean usingProxy) {
            this.usingProxy = usingProxy;
        }



        public String getProxyURL() {
            return proxyURL;
        }

        public void setProxyURL(String proxyURL) {
            this.setUsingProxy(true);
            this.proxyURL = proxyURL;
        }


        public String getServerUrl() {
            return serverUrl;
        }

        public void setServerUrl(String serverUrl) {
            this.serverUrl = serverUrl;
        }

        public TokenManager getTokenManager() {
            return tokenManager;
        }

        public void setTokenManager(TokenManager tokenManager) {
            this.tokenManager = tokenManager;
        }

        public RequestQueue getQueue() {
            return queue;
        }

        public void setQueue(RequestQueue queue) {
            this.queue = queue;
        }

        public long getLastPullTime() {
            return lastPullTime;
        }

        public void setLastPullTime(long lastPullTime) {
            this.lastPullTime = lastPullTime;
        }

        public String getChatID() {
            return chatID;
        }

        public void setChatID(String chatID) {
            this.chatID = chatID;
        }

        public EncryptionKeyPair getEncKP() {
            return encKP;
        }

        public void setEncKP(EncryptionKeyPair encKP) {
            this.encKP = encKP;
        }

        public OkHttpClient getHttpClient() {
            return httpClient;
        }

        public void setHttpClient(OkHttpClient httpClient) {
            this.httpClient = httpClient;
        }

        public String getTokenStr() {
            return tokenStr;
        }

        public void setTokenStr(String tokenStr) {
            this.tokenStr = tokenStr;
        }

        public String getOutputDir() {
            return outputDir;
        }

        public void setOutputDir(String outputDir) {
            this.outputDir = outputDir;
        }



        //empty constructor assumes all defaults
        public InteractiveModeWorker()
        {
            this.serverUrl = Utility.httpsAddress;
            this.encKP = ntruEnc.generateKeyPair();
            this.lastPullTime = System.currentTimeMillis();
            this.chatID = Utility.makeRandomString();
            this.tokenManager = new TokenManager(chatID, serverUrl, encKP, ntruEnc);
            this.lastPullTime = System.currentTimeMillis();
            //this.queue = Volley.newRequestQueue();
            this.httpClient = new OkHttpClient.Builder().build();
            this.outputDir = System.getProperty("user.home");
            this.proxyURL = "localhost";
        }

        public void makeNewTokenManager()
        {
            this.tokenManager = new TokenManager(chatID, serverUrl, encKP, ntruEnc);
        }



        //            String getNewTokenUrl = Utility.makeGetStringForPullingNewToken(this.serverUrl, this.chatID);


        public Response getResponseFromURL(String queryURL)
        {
            Request request = new Request.Builder().url(queryURL).build();
            Response response = null;
            try {
                response = this.httpClient.newCall(request).execute();
            }
            catch (IOException e)
            {
                dumb_debugging(e.getMessage());
            }
            return response;
        }

    }


    public  void startInteractiveMode(String args[])
    {
        InteractiveModeWorker worker  = new InteractiveModeWorker();
        if(args.length>1){
            System.out.println("invalid arguments");
            return;
        }
        if(args.length == 1 && args[0].equals("-i"))
        {
            //worker = new InteractiveModeWorker();
        }
        else
        {
            System.out.println("invalid arguments");
            return;
        }
        /*
            --file=/path/to/your/key/file\n"+
            --output_dir=/path/to/output/directory/\n\n"+
            --proxy_server=my.proxy.server.url\n"+
            --TOR_Proxy_SOCKS=9050\n"+
            --TOR_Proxy_HTTP=8118\n"+
            --I2P_Proxy_SOCKS=4444\n"+
            --I2P_Proxy_HTTP=4444\n"+
            --HTTPS_Proxy=8118\n"+
            --private_server_url=https://something.com\n"+
            --private_server_proxy=9050\n"+
             --TOR_No_Proxy
             --I2P_No_Proxy
        * */




        for(String value : args )
        {
            if(value.startsWith("--File=")) {
                if (value.split("=").length == 2)
                {
                    String keyfiledata = fileToString(value.split("=")[1]);
                    String[] parts = keyfiledata.split(",");

                    worker.setEncKP( new EncryptionKeyPair(parts[0], parts[1]));
                    //this.sigKP = new SignatureKeyPair(parts[2],parts[3]);
                    worker.setChatID(parts[2]);
                    worker.makeNewTokenManager();
                }else
                {
                    dumb_debugging("improper formatting for file with "+value);
                }


            }
            else if(value.startsWith("--Output_Dir="))
            {
                if (value.split("=").length == 2)
                {
                    worker.setOutputDir(value.split("=")[2]);
                }else
                {
                    dumb_debugging("improper formatting for output dir with "+value);
                }

            }
            else if(value.startsWith("--Proxy_Server="))
            {
                if (value.split("=").length == 2)
                {
                    worker.setProxyURL(value.split("=")[2]);
                }else
                {
                    dumb_debugging("improper formatting for proxy server url with "+value);
                }

            }
            else if(value.startsWith("--TOR_Proxy_SOCKS="))
            {
                if (value.split("=").length == 2)
                {
                    worker.setProxyPort(Integer.parseInt(value.split("=")[2]));
                    worker.setServerUrl(Utility.torAddress);
                    worker.setProxyType(PROXY_TYPE_SOCKS);
                }else
                {
                    dumb_debugging("improper formatting for tor socks proxy with "+value);
                }

            }
            else if(value.startsWith("--TOR_Proxy_HTTP="))
            {
                if (value.split("=").length == 2)
                {
                    worker.setProxyPort(Integer.parseInt(value.split("=")[2]));
                    worker.setServerUrl(Utility.torAddress);
                    worker.setProxyType(PROXY_TYPE_HTTP);
                }else
                {
                    dumb_debugging("improper formatting for tor http proxy with "+value);
                }


            }
            else if(value.startsWith("--I2P_Proxy_SOCKS="))
            {
                if (value.split("=").length == 2)
                {
                    worker.setProxyPort(Integer.parseInt(value.split("=")[2]));
                    worker.setServerUrl(Utility.i2pAddress);
                    worker.setProxyType(PROXY_TYPE_SOCKS);
                }else
                {
                    dumb_debugging("improper formatting for i2p socks proxy with "+value);
                }
            }
            else if(value.startsWith("--I2P_Proxy_HTTP="))
            {
                if (value.split("=").length == 2)
                {
                    worker.setProxyPort(Integer.parseInt(value.split("=")[2]));
                    worker.setServerUrl(Utility.i2pAddress);
                    worker.setProxyType(PROXY_TYPE_HTTP);
                }else
                {
                    dumb_debugging("improper formatting for i2p http proxy with "+value);
                }
            }
            else if(value.startsWith("--HTTPS_Proxy_SOCKS="))
            {
                if (value.split("=").length == 2)
                {
                    worker.setProxyPort(Integer.parseInt(value.split("=")[2]));
                    worker.setServerUrl(Utility.httpsAddress);
                    worker.setProxyType(PROXY_TYPE_SOCKS);
                }else
                {
                    dumb_debugging("improper formatting for https server socks proxy port with "+value);
                }
            }
            else if(value.startsWith("--HTTPS_Proxy_HTTP="))
            {
                if (value.split("=").length == 2)
                {
                    worker.setProxyPort(Integer.parseInt(value.split("=")[2]));
                    worker.setServerUrl(Utility.httpsAddress);
                    worker.setProxyType(PROXY_TYPE_HTTP);
                }else
                {
                    dumb_debugging("improper formatting for https server http proxy port with "+value);
                }
            }
            else if(value.startsWith("--private_server_url="))
            {
                if (value.split("=").length == 2)
                {
                    worker.setServerUrl(value.split("=")[2]);
                }else
                {
                    dumb_debugging("improper formatting for private server url "+value);
                }
            }
            else if(value.startsWith("--Private_Server_Proxy_SOCKS="))
            {
                if (value.split("=").length == 2)
                {
                    worker.setProxyPort(Integer.parseInt(value.split("=")[2]));
                    worker.setProxyType(PROXY_TYPE_SOCKS);
                }else
                {
                    dumb_debugging("improper formatting for private server socks proxy port with "+value);
                }
            }
            else if(value.startsWith("--Private_Server_Proxy_HTTP="))
            {
                if (value.split("=").length == 2)
                {
                    worker.setProxyPort(Integer.parseInt(value.split("=")[2]));
                    worker.setProxyType(PROXY_TYPE_HTTP);
                }else
                {
                    dumb_debugging("improper formatting for private server http proxy port with "+value);
                }
            }
            else if(value.equals("--TOR_No_Proxy"))
            {
                worker.setServerUrl(Utility.torAddress);
                worker.setUsingProxy(false);
            }
            else if(value.equals("--I2P_No_Proxy"))
            {
                worker.setServerUrl(Utility.i2pAddress);
                worker.setUsingProxy(false);
            }
            else if(value.equals("-i"))
            {
                // do nothing
            }
            else
            {
                dumb_debugging("Did not recognise "+value + " as an argument");
            }
            }

        }







    public static void main(String[] args){

//System.out.println(args.length);

        if(args.length ==0)
        {
            System.out.println("no arguments were passed");
            return;
        }

        if(args.length>0 && args[0].equals("-i"))
        {
            CryptoWorker worker = new CryptoWorker();
            worker.startInteractiveMode(args);
            return;
        }

        if(args.length ==1)
        {
            switch (args[0]) {
                case "-h":
                    printHelp();
                    break;
                case "--help":
                    printHelp();
                    break;

                case "--version":
                    printVersion();
                    break;

                case "-v":
                    printVersion();
                    break;



                default:
                    System.out.println("Invalid Args");
                    break;
            }
            return;
        }

        if(args.length ==3)
        {
            CryptoWorker worker = new CryptoWorker();
            switch (args[0]) {

                //encryption stuff
                case "-e":
                    System.out.println(worker.encryptStr(args[1], args[2]));
                    break;

                case "--encrypt":
                    System.out.println(worker.encryptStr(args[1], args[2]));
                    break;

                default:
                    System.out.println("Invalid Args");
                    break;
            }
            return;
        }



        System.out.println("Invalid Args");
        return;
    }
}