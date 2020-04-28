package com.werewolfchat.startup;

import android.content.Context;
import android.content.Intent;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.werewolfchat.startup.ntru.encrypt.EncryptionKeyPair;
import com.werewolfchat.startup.ntru.encrypt.EncryptionPublicKey;
import com.werewolfchat.startup.ntru.encrypt.NtruEncrypt;

import java.util.ArrayList;

import static com.werewolfchat.startup.Utility.ENCRYPTION_PARAMS;
import static com.werewolfchat.startup.Utility.dumb_debugging;

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
            case PRIVATE_SERVER_URL_EXTRA:
                setPrivateServerURL((String) valueToSet);
                break;
            case DIST_END_ID_EXTRA:
                setDestEndID((String) valueToSet);
                break;
            case DIST_END_KEY_EXTRA:
                setDestKey((byte[]) valueToSet);
                break;
            case PUBLIC_KEY_BYTE_ARRAY_EXTRA:
                setPubKey((byte[]) valueToSet);
                break;
            case PRIVATE_KEY_BYTE_ARRAY_EXTRA:
                setPrivKey((byte[]) valueToSet);
                break;
            case CHAT_ID_EXTRA:
                setChatID((String) valueToSet);
                break;
            case FIREBASE_UID_EXTRA:
                setFirebaseUID((String) valueToSet);
                break;
            case FIREBASE_EMAIL_EXTRA:
                setFirebaseEmail((String) valueToSet);
                break;
            case PROXY_INFO:
                if(((String[]) valueToSet).length == 3)
                    setProxyInfo((String[]) valueToSet);
                break;
            case TIME_OUT_EXTRA:
                if((Integer) valueToSet > 0)
                    setTimeOut((Integer) valueToSet);
                break;
            case USING_SELF_SIGNED_TLS_CERT_EXTRA:
                setUsingSelfSignedTLSCert((boolean) valueToSet);
                break;
            case TOKEN_STR:
                setTokenString((String) valueToSet);
                break;
            case AUTO_PUB_STR:
                setAutoPub((boolean) valueToSet);
        }
    }

    public void setAutoPub(boolean input) {
        this.autoPub = input;
        this.intent.putExtra(AUTO_PUB_STR, input);
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

        public void setProxyInfo(String[] input) {
            this.proxyPort = Integer.parseInt(input[1]);
            this.proxy_info = input;
            this.hasProxyPort = true;
            this.intent.putExtra(PROXY_INFO, input);
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

    public String getTokenString() {
        return this.tokenString;
    }

    public void loadKeyPair(EncryptionKeyPair ekp) {
        this.setPubKey(ekp.getPublic().getEncoded());
        this.setPrivKey(ekp.getPrivate().getEncoded());
    }

    public EncryptionKeyPair getKeyPair() {
        return new EncryptionKeyPair(this.getPubKey(), this.getPrivKey());
    }
        public void setPrivKey(byte[] privKey) {
            this.privKey = privKey;
            this.hasPrivateKey = true;
            this.intent.putExtra(PRIVATE_KEY_BYTE_ARRAY_EXTRA, privKey);
        }

        public byte[] getDestKey() {
            return destKey;
        }

    public EncryptionPublicKey getDestKeyAsEncryptionPublicKey() {
        return new EncryptionPublicKey(this.getDestKey());
    }

        public void setDestKey(byte[] destKey) {
            this.destKey = destKey;
            this.hasDistEndKey = true;
            this.intent.putExtra(DIST_END_KEY_EXTRA, destKey);

        }

    public TokenManager makeNewTokenManager() {
        return new TokenManager(this.getChatID(), this.getPrivateServerURL(), this.getKeyPair(), new NtruEncrypt(ENCRYPTION_PARAMS));
    }

    public TokenManager makeNewTokenManager(String newStr) {

        TokenManager tm = new TokenManager(this.getChatID(), this.getPrivateServerURL(), this.getKeyPair(), new NtruEncrypt(ENCRYPTION_PARAMS));
        tm.setTokenString(newStr);
        return tm;
    }

    public void setTokenString(String inputTokenStr) {
        this.tokenString = inputTokenStr;
        this.hasTokenString = true;
        this.intent.putExtra(TOKEN_STR, tokenString);

    }

        public Intent intent;
        public int proxyPort, timeOut;
        public ArrayList<String> extraStrings;
    public String privateServerURL, chatID, destEndID, firebaseUID, firebaseEmail, tokenString;
        public byte[] pubKey, privKey, destKey;

        public static final String PRIVATE_SERVER_URL_EXTRA = "private_server_url";
        public static final String DIST_END_ID_EXTRA = "dest_end_id";
        public static final String DIST_END_KEY_EXTRA = "dest_end_key";
        public static final String PUBLIC_KEY_BYTE_ARRAY_EXTRA = "enckp_pub";
        public static final String PRIVATE_KEY_BYTE_ARRAY_EXTRA = "enckp_priv";
        public static final String CHAT_ID_EXTRA = "chat_id";
        public static final String FIREBASE_UID_EXTRA = "firebase_uid";
        public static final String FIREBASE_EMAIL_EXTRA = "firebase_email";
        public static final String PROXY_INFO = "proxy_info";

        public static final String TIME_OUT_EXTRA = "time_out";
        public static final String USING_SELF_SIGNED_TLS_CERT_EXTRA = "use_self_signed_cert";
        public static final String TOKEN_STR = "token_str";
        public static final String AUTO_PUB_STR = "auto_pub";

        public boolean hasPrivateServerURL, hasDistEndID, hasDistEndKey, hasPublicKey, hasPrivateKey;
        public boolean hasChatID, hasFirebaseUID, hasFirebaseEmail, hasProxyPort, hasTimeOut, usingselfSignedCert;
        public boolean hasTokenString, autoPub;
        public String[] proxy_info; // hostname:port:type


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
            this.hasTokenString = false;
            this.autoPub = false;
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
            if(this.intent.hasExtra(PROXY_INFO))
                this.setProxyInfo(this.intent.getStringArrayExtra(PROXY_INFO));
            if(this.intent.hasExtra(TIME_OUT_EXTRA))
                this.setTimeOut(this.intent.getIntExtra(TIME_OUT_EXTRA, -1));
            if(this.intent.hasExtra(USING_SELF_SIGNED_TLS_CERT_EXTRA))
                this.usingselfSignedCert = true;
            if (this.intent.hasExtra(TOKEN_STR))
                this.setTokenString(this.intent.getStringExtra(TOKEN_STR));
            if (this.intent.hasExtra(AUTO_PUB_STR))
                this.autoPub = this.intent.getBooleanExtra(AUTO_PUB_STR, false);
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
            this.extraStrings.add(PROXY_INFO);
            this.extraStrings.add(TIME_OUT_EXTRA);
            this.extraStrings.add(USING_SELF_SIGNED_TLS_CERT_EXTRA);
            this.extraStrings.add(TOKEN_STR);
            this.extraStrings.add(AUTO_PUB_STR);
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
            int tempTimeout = 4000;
            if(this.timeOut > 0)
                tempTimeout = this.timeOut;
            if(this.hasProxyPort && proxyPort > 0) {
                dumb_debugging("the timeout is "+Integer.toString(tempTimeout));
                    volleyToReturn = Volley.newRequestQueue(context, new ProxiedHurlStack(this.proxy_info[0],Integer.parseInt(this.proxy_info[1]) , tempTimeout, this.proxy_info[2]));
            }
            else {
                volleyToReturn = Volley.newRequestQueue(context);
            }


            return volleyToReturn;
        }


    public  boolean areWeOnAPrivateServer() {
            return this.hasPrivateServerURL;
    }



    }

