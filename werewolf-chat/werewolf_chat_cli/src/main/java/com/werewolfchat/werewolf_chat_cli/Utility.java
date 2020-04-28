package com.werewolfchat.werewolf_chat_cli;



import com.werewolfchat.werewolf_chat_cli.ntru.encrypt.EncryptionParameters;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
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
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

//import android.support.v4.widget.NestedScrollView;


//import static android.support.constraint.Constraints.TAG;

public class Utility {


    public static final EncryptionParameters ENCRYPTION_PARAMS = EncryptionParameters.APR2011_439_FAST;


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

    public static void appendStrToFile(String fileName,
                                       String str)
    {
        try {

            // Open given file in append mode.
            BufferedWriter out = new BufferedWriter(
                    new FileWriter(fileName, true));
            out.write(str);
            out.close();
        }
        catch (IOException e) {
            System.out.println("exception occoured" + e);
        }
    }


    public static boolean isStrInStrArray(String inputStr, String[] inputArray)
    {
        for(String value : inputArray)
        {
            if(value.equals(inputStr))
                return true;
        }
        return false;
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



    // the one true way to debug, debugging with print statements
    public static void dumb_debugging(String text) {
        System.out.println(text);
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

    // /gettoken/:chatid
    public static String makeGetStringForPullingNewToken(String serverUrl, String user) {
        return serverUrl + "gettoken/" + user;
    }

    // /changechatid/:oldchatid/:newchatid/:token
    public static String makeGetStringForChangingChatID(String serverUrl, String oldUser, String newUser, String token) {
        return serverUrl + "changechatid/" + oldUser + "/" + newUser + "/" + token;
    }

    // /verifykey/:chatid/:token
    public static String makeVerifyKeyURL(String serverUrl, String user, String token) {
        return serverUrl + "verifykey/" + user + "/" + token;
    }


    ///sendmessage/:tochatid/:fromchatid/:messagetosend/:token
    public static String makeGetStringForPublishingMessages(String serverUrl, String toid, String fromid, String message, String token) {
        return serverUrl + "sendmessage/" + toid + "/" + fromid + "/" + message + "/" + token;
    }

    // /messages/:chatid/:token
    public static String makeGetStringForPullingMessagesWithToken(String serverUrl, String user, String token) {
        return serverUrl + "messages/" + user + "/" + token;
    }

    // /messagesaftertime/:chatid/:time/:token
    public static String makeGetStringForPullingMessagesAfterTime(String serverUrl, String user, long time, String token) {
        return serverUrl + "messagesaftertime/" + user + "/" + Long.toString(time) + "/" + token;
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
