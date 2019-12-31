package com.werewolfchat.startup;

import com.android.volley.RequestQueue;
import com.werewolfchat.startup.ntru.encrypt.EncryptionKeyPair;
import com.werewolfchat.startup.ntru.encrypt.NtruEncrypt;

import static com.werewolfchat.startup.Utility.dumb_debugging;
import static com.werewolfchat.startup.Utility.queryURL;
import static com.werewolfchat.startup.ntru.util.ArrayEncoder.hexStringToByteArray;

public class TokenManager {

    private String tokenString;

    public String getTokenString() {
        return tokenString;
    }


    public boolean isTokenSet() {
        return isSet;
    }


    private boolean isSet;
    private boolean requestInProgress;
    private String userId;
    private String serverURL;
    private EncryptionKeyPair encKP;
    private NtruEncrypt ntruEnc;

    public String convertEncryptedHexStringToPlainTextString(String cypherText) {
        return new String(ntruEnc.decrypt(hexStringToByteArray(cypherText), encKP));
    }

    //String should be all caps 24 chars long and with no numbers
    public boolean isStringAGoodTokenString(String inputString) {


        if (inputString.matches("\\p{javaUpperCase}*") && inputString.length() == 24) {
            dumb_debugging("Decrypted Token String is good:\n" + inputString);
            return true;
        }


        if (inputString.matches(".*\\d.*")) {
            dumb_debugging("Decrypted Token String has number in it:\n" + inputString);
            return false;
        }

        if (inputString.length() != 24) {
            dumb_debugging("Decrypted Token String is the wrong length:\n" + inputString);
            return false;
        }

        if (inputString.matches(".*\\p{javaLowerCase}.*")) {
            dumb_debugging("Decrypted Token String has lowercase letter in it:\n" + inputString);
            return false;
        }

        dumb_debugging("Decrypted Token String has something other wrong char in it:\n" + inputString);

        return false;

    }

    public class FeedBackWrapperForTokenPulls implements Utility.Command {
        public Utility.Command feedBackWorker;
        public Utility.Command tokenPullDebugger;

        public FeedBackWrapperForTokenPulls(Utility.Command feedBack, Utility.Command tokenPuller) {
            this.feedBackWorker = feedBack;
            this.tokenPullDebugger = tokenPuller;
        }

        public void execute(String data) {
            feedBackWorker.execute(data);
            tokenPullDebugger.execute(data);
        }
    }


    public class GoodTokenPull implements Utility.Command {

        public Utility.Command actionToTakeAfter;
        public Utility.Command actionToTakeIfStringMathces;
        public boolean runFollowOnAction;
        public boolean runPreProssingCommand;
        public String filterString;

        public GoodTokenPull() {

            runFollowOnAction = false;
            runPreProssingCommand = false;
        }

        public GoodTokenPull(Utility.Command inputCommand) {
            runFollowOnAction = true;
            actionToTakeAfter = inputCommand;
            runPreProssingCommand = false;
        }

        public GoodTokenPull(Utility.Command inputCommand, String inputStr, Utility.Command actionToTakeIfStringMathcesInput) {
            runFollowOnAction = true;
            actionToTakeAfter = inputCommand;
            runPreProssingCommand = true;
            filterString = inputStr;
            actionToTakeIfStringMathces = actionToTakeIfStringMathcesInput;
        }

        public void execute(String data) {
            requestInProgress = false;
            dumb_debugging("in good token pull right now with this data:\n" + data);

            if (runPreProssingCommand) {
                if (data.matches(filterString)) {
                    isSet = false;
                    actionToTakeIfStringMathces.execute(data);
                    return;
                }
            }


            if (data.equals("fail: user not found")) {
                isSet = false;
                dumb_debugging("filed to get token because of bad id");
                return;
            } else if (data.equals("fail: server error")) {
                isSet = false;

                dumb_debugging("filed to get token because of server error");
            } else if (data.startsWith("fail:")) {
                isSet = false;
                dumb_debugging(data);
            } else if (data.startsWith("good:")) {
                String dataStringparts[] = data.split(":");
                if (dataStringparts.length != 2) {
                    isSet = false;

                    dumb_debugging("the server returned success, but the result is malformed or empty");
                    return;
                }
                String encryptedToken = dataStringparts[1].trim();
                dumb_debugging("about to try to decrypt this:\n" + encryptedToken);
                dumb_debugging("the string is " + encryptedToken.length() + " chars long");
                String newTokenString = convertEncryptedHexStringToPlainTextString(encryptedToken);
                if (isStringAGoodTokenString(newTokenString)) {

                    dumb_debugging("the decrypted key is good");
                    isSet = true;
                    tokenString = newTokenString;
                    if (runFollowOnAction) {
                        dumb_debugging("about to try to run follow on action");
                        actionToTakeAfter.execute(data + "%" + newTokenString);
                    }


                } else {
                    dumb_debugging("the decrypted key was the wrong format");
                    isSet = false;

                }


            }

        }
    }


    public class BadTokenPull implements Utility.Command {
        public void execute(String data) {
            dumb_debugging("could not get a new token because of " + data);
            isSet = false;
            requestInProgress = false;
        }
    }


    public TokenManager(String id, String serverUrlInput, EncryptionKeyPair inputEncKP, NtruEncrypt inputEncypter) {
        requestInProgress = false;
        isSet = false;
        tokenString = "";
        userId = id;
        serverURL = serverUrlInput;
        encKP = inputEncKP;
        ntruEnc = inputEncypter;

    }

    public void getNewToken(RequestQueue passedInQueue) {
        if (this.requestInProgress) {
            dumb_debugging("request cancelled since one is already being sent");
            return;
        }
        isSet = false;
        requestInProgress = true;
        String getNewTokenUrl = Utility.makeGetStringForPullingNewToken(this.serverURL, this.userId);
        dumb_debugging("about to request new token with \n" + getNewTokenUrl);

        queryURL(getNewTokenUrl, passedInQueue, new GoodTokenPull(), new BadTokenPull());

    }

    public void getNewToken(RequestQueue passedInQueue, Utility.Command feedbackWorker) {
        if (this.requestInProgress) {
            dumb_debugging("request cancelled since one is already being sent");
            return;
        }
        isSet = false;
        requestInProgress = true;
        String getNewTokenUrl = Utility.makeGetStringForPullingNewToken(this.serverURL, this.userId);
        dumb_debugging("about to request new token with \n" + getNewTokenUrl);

        queryURL(getNewTokenUrl, passedInQueue, new FeedBackWrapperForTokenPulls(feedbackWorker, new GoodTokenPull()), new FeedBackWrapperForTokenPulls(feedbackWorker, new BadTokenPull()));

    }

    public void getNewTokenThenExecuteCommand(RequestQueue passedInQueue, Utility.Command followOnTask) {
        if (this.requestInProgress) {
            dumb_debugging("request cancelled since one is already being sent");
            return;
        }
        isSet = false;
        requestInProgress = true;
        String getNewTokenUrl = Utility.makeGetStringForPullingNewToken(this.serverURL, this.userId);
        dumb_debugging("about to request new token with \n" + getNewTokenUrl);

        queryURL(getNewTokenUrl, passedInQueue, new GoodTokenPull(followOnTask), new BadTokenPull());

    }


    public void getNewTokenThenExecuteCommandWithPrepossingCommandIfStringMatches(RequestQueue passedInQueue, Utility.Command followOnTask,
                                                                                  String stringToMatch, Utility.Command taskToRun) {


        isSet = false;
        requestInProgress = true;
        String getNewTokenUrl = Utility.makeGetStringForPullingNewToken(this.serverURL, this.userId);
        dumb_debugging("about to request new token with \n" + getNewTokenUrl);

        queryURL(getNewTokenUrl, passedInQueue, new GoodTokenPull(followOnTask, stringToMatch, taskToRun), new BadTokenPull());

    }



    public void loadTokenOffline(String passedInToken) {
        String newTokenString = convertEncryptedHexStringToPlainTextString(passedInToken);
        if (isStringAGoodTokenString(newTokenString)) {

            dumb_debugging("the decrypted key is good");
            isSet = true;
            tokenString = newTokenString;
        } else {
            dumb_debugging("the decrypted key was the wrong format");
            isSet = false;

        }
    }

    public void setTokenString(String newStr) {
        isSet = true;
        tokenString = newStr;
    }
}
