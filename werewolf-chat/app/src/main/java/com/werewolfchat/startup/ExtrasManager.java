package com.werewolfchat.startup;

import android.content.Context;
import android.content.Intent;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;

public class ExtrasManager {


    public void copyOverExtrasAndChangeClassToPrepareToStartNewActivity(Context context, Class classToStart)
    {
        this.setIntent(this.copyExtrasToNewIntent(new Intent(context, classToStart)));
    }

    public void setExtraIfInputIsNotNull(String extraFieldToSet, Object valueToSet)
    {
        if(valueToSet == null)
            return;
        switch (extraFieldToSet)
        {
            case "private_server_url":
                setPrivateServerURL((String) valueToSet);
                break;
            case "dest_end_id":
                setDestEndID((String) valueToSet);
                break;
            case "dest_end_key":
                setDestKey((byte[]) valueToSet);
                break;
            case "enckp_pub":
                setPubKey((byte[]) valueToSet);
                break;
            case "enckp_priv":
                setPrivKey((byte[]) valueToSet);
                break;
            case "chat_id":
                setChatID((String) valueToSet);
                break;
            case "firebase_uid":
                setFirebaseUID((String) valueToSet);
                break;
            case "firebase_email":
                setFirebaseEmail((String) valueToSet);
                break;
            case "proxy_port":
                if((Integer) valueToSet > 0)
                    setProxyPort((Integer) valueToSet);
                break;
            case "time_out":
                if((Integer) valueToSet > 0)
                    setTimeOut((Integer) valueToSet);
                break;
            case "use_self_signed_cert":
                setUsingSelfSignedTLSCert((boolean) valueToSet);
                break;
        }
    }

        public Intent getIntent() {
            return intent;
        }

        public void setUsingSelfSignedTLSCert(boolean input)
        {
            this.intent.putExtra(USING_SELF_SIGNED_TLS_CERT_EXTRA, input);
            this.usingselfSignedCert = input;
        }

        public void setIntent(Intent intent) {
            this.intent = intent;
        }

        public int getProxyPort() {
            if(this.hasProxyPort)
                return proxyPort;
            return -1;
        }

        public void setProxyPort(int proxyPort) {
            this.proxyPort = proxyPort;
            this.hasProxyPort = true;
            this.intent.putExtra(PROXY_PORT_EXTRA, proxyPort);
        }

        public int getTimeOut() {
        if(hasTimeOut)
            return timeOut;
        timeOut = 2000;
        hasTimeOut = true;
      return timeOut;
        }


        public void setTimeOut(int timeOut) {
            this.timeOut = timeOut;
            this.hasTimeOut = true;
            this.intent.putExtra(TIME_OUT_EXTRA, timeOut);

        }

        public ArrayList<String> getExtraStrings() {
            return extraStrings;
        }

        public void setExtraStrings(ArrayList<String> extraStrings) {
            this.extraStrings = extraStrings;
        }

        public String getPrivateServerURL() {
            return privateServerURL;
        }

        public void setPrivateServerURL(String privateServerURL) {
            this.privateServerURL = privateServerURL;
            this.hasPrivateServerURL = true;
            this.intent.putExtra(PRIVATE_SERVER_URL_EXTRA, privateServerURL);
        }

        public String getChatID() {
            return chatID;
        }

        public void setChatID(String chatID) {
            this.chatID = chatID;
            this.hasChatID = true;
            this.intent.putExtra(CHAT_ID_EXTRA, chatID);
        }

        public String getDestEndID() {
            return destEndID;
        }

        public void setDestEndID(String destEndID) {
            this.destEndID = destEndID;
            this.hasDistEndID = true;
            this.intent.putExtra(DIST_END_ID_EXTRA, destEndID);

        }

        public String getFirebaseUID() {
            return firebaseUID;
        }

        public void setFirebaseUID(String firebaseUID) {
            this.firebaseUID = firebaseUID;
            this.hasFirebaseUID = true;
            this.intent.putExtra(FIREBASE_UID_EXTRA, firebaseUID);

        }

        public String getFirebaseEmail() {
            return firebaseEmail;
        }

        public void setFirebaseEmail(String firebaseEmail) {
            this.firebaseEmail = firebaseEmail;
            this.hasFirebaseEmail = true;
            this.intent.putExtra(FIREBASE_EMAIL_EXTRA, firebaseEmail);

        }

        public byte[] getPubKey() {
            return pubKey;
        }

        public void setPubKey(byte[] pubKey) {
            this.pubKey = pubKey;
            this.hasPublicKey = true;
            this.intent.putExtra(PUBLIC_KEY_BYTE_ARRAY_EXTRA, pubKey);

        }

        public byte[] getPrivKey() {
            return privKey;

        }

        public void setPrivKey(byte[] privKey) {
            this.privKey = privKey;
            this.hasPrivateKey = true;
            this.intent.putExtra(PRIVATE_KEY_BYTE_ARRAY_EXTRA, privKey);
        }

        public byte[] getDestKey() {
            return destKey;
        }

        public void setDestKey(byte[] destKey) {
            this.destKey = destKey;
            this.hasDistEndKey = true;
            this.intent.putExtra(DIST_END_KEY_EXTRA, destKey);

        }

        public Intent intent;
        public int proxyPort, timeOut;
        public ArrayList<String> extraStrings;
        public String privateServerURL, chatID, destEndID, firebaseUID, firebaseEmail;
        public byte[] pubKey, privKey, destKey;

        public static final String PRIVATE_SERVER_URL_EXTRA = "private_server_url";
        public static final String DIST_END_ID_EXTRA = "dest_end_id";
        public static final String DIST_END_KEY_EXTRA = "dest_end_key";
        public static final String PUBLIC_KEY_BYTE_ARRAY_EXTRA = "enckp_pub";
        public static final String PRIVATE_KEY_BYTE_ARRAY_EXTRA = "enckp_priv";
        public static final String CHAT_ID_EXTRA = "chat_id";
        public static final String FIREBASE_UID_EXTRA = "firebase_uid";
        public static final String FIREBASE_EMAIL_EXTRA = "firebase_email";
        public static final String PROXY_PORT_EXTRA = "proxy_port";
        public static final String TIME_OUT_EXTRA = "time_out";
        public static final String USING_SELF_SIGNED_TLS_CERT_EXTRA = "use_self_signed_cert";

        public boolean hasPrivateServerURL, hasDistEndID, hasDistEndKey, hasPublicKey, hasPrivateKey;
        public boolean hasChatID, hasFirebaseUID, hasFirebaseEmail, hasProxyPort, hasTimeOut, usingselfSignedCert;


        public void setBoolsToFalse()
        {
            this.hasPrivateServerURL = false;
            this.hasDistEndID = false;
            this.hasDistEndKey = false;
            this.hasPublicKey = false;
            this.hasPrivateKey = false;
            this.hasChatID = false;
            this.hasFirebaseUID = false;
            this.hasFirebaseEmail = false;
            this.hasProxyPort = false;
            this.hasTimeOut = false;
            this.usingselfSignedCert = false;
        }



        public void loadIntent()
        {
            if(this.intent.hasExtra(PRIVATE_SERVER_URL_EXTRA))
                this.setPrivateServerURL(this.intent.getStringExtra(PRIVATE_SERVER_URL_EXTRA));
            if(this.intent.hasExtra(DIST_END_ID_EXTRA))
                this.setDestEndID(this.intent.getStringExtra(DIST_END_ID_EXTRA));
            if(this.intent.hasExtra(DIST_END_KEY_EXTRA))
                this.setDestKey(this.intent.getByteArrayExtra(DIST_END_KEY_EXTRA));
            if(this.intent.hasExtra(PUBLIC_KEY_BYTE_ARRAY_EXTRA))
                this.setPubKey(this.intent.getByteArrayExtra(PUBLIC_KEY_BYTE_ARRAY_EXTRA));
            if(this.intent.hasExtra(PRIVATE_KEY_BYTE_ARRAY_EXTRA))
                this.setPrivKey(this.intent.getByteArrayExtra(PRIVATE_KEY_BYTE_ARRAY_EXTRA));
            if(this.intent.hasExtra(CHAT_ID_EXTRA))
                this.setChatID(this.intent.getStringExtra(CHAT_ID_EXTRA));
            if(this.intent.hasExtra(FIREBASE_UID_EXTRA))
                this.setFirebaseUID(this.intent.getStringExtra(FIREBASE_UID_EXTRA));
            if(this.intent.hasExtra(FIREBASE_EMAIL_EXTRA))
                this.setFirebaseEmail(this.intent.getStringExtra(FIREBASE_EMAIL_EXTRA));
            if(this.intent.hasExtra(PROXY_PORT_EXTRA))
                this.setProxyPort(this.intent.getIntExtra(PROXY_PORT_EXTRA, -1));
            if(this.intent.hasExtra(TIME_OUT_EXTRA))
                this.setTimeOut(this.intent.getIntExtra(TIME_OUT_EXTRA, -1));
            if(this.intent.hasExtra(USING_SELF_SIGNED_TLS_CERT_EXTRA))
                this.usingselfSignedCert = true;

        }









        public void makeExtraStringList()
        {
            this.extraStrings = new ArrayList<String>();
            this.extraStrings.add(PRIVATE_SERVER_URL_EXTRA);
            this.extraStrings.add(DIST_END_ID_EXTRA);
            this.extraStrings.add(DIST_END_KEY_EXTRA);
            this.extraStrings.add(PUBLIC_KEY_BYTE_ARRAY_EXTRA);
            this.extraStrings.add(PRIVATE_KEY_BYTE_ARRAY_EXTRA);
            this.extraStrings.add(CHAT_ID_EXTRA);
            this.extraStrings.add(FIREBASE_UID_EXTRA);
            this.extraStrings.add(FIREBASE_EMAIL_EXTRA);
            this.extraStrings.add(PROXY_PORT_EXTRA);
            this.extraStrings.add(TIME_OUT_EXTRA);
            this.extraStrings.add(USING_SELF_SIGNED_TLS_CERT_EXTRA);
        }

        public void  wipeIntent()
        {
            for(String extra : this.extraStrings)
            {
                this.intent.removeExtra(extra);
            }
        }

        public Intent copyExtrasToNewIntent(Intent newIntent)
        {
            if(this.intent.getExtras() == null)
                return newIntent;

            newIntent = newIntent.putExtras(this.intent.getExtras());
            return  newIntent;
        }

        public ExtrasManager(Intent inputIntent)
        {
            this.intent = inputIntent;
            this.makeExtraStringList();
            this.setBoolsToFalse();
            this.loadIntent();
        }

        public boolean areAllTheDistEndExtrasSet()
        {
            return this.hasDistEndID && this.hasDistEndKey;
        }

        public boolean areAllTheLocalPKIExtrasSet()
        {
            return this.hasChatID && this.hasPublicKey && this.hasPrivateKey;
        }

        public boolean areAllTheFireBaseExtrasSet()
        {
            return  this.hasFirebaseUID && this.hasFirebaseEmail;
        }

        public RequestQueue makeVolley(Context context)
        {
            RequestQueue volleyToReturn;
            if(this.hasProxyPort && proxyPort > 0) {
                if(this.timeOut > 0)
                    volleyToReturn = Volley.newRequestQueue(context, new ProxiedHurlStack(this.proxyPort,this.timeOut));
                else
                    volleyToReturn = Volley.newRequestQueue(context, new ProxiedHurlStack(this.proxyPort, 4000));
            }
            else
                volleyToReturn = Volley.newRequestQueue(context);



            return volleyToReturn;
        }


    public  boolean areWeOnAPrivateServer() {
            return this.hasPrivateServerURL;
    }



    }

