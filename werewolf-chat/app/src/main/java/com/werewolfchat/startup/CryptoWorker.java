package com.werewolfchat.startup;


import com.werewolfchat.startup.ntru.encrypt.EncryptionKeyPair;
import com.werewolfchat.startup.ntru.encrypt.EncryptionParameters;
import com.werewolfchat.startup.ntru.encrypt.EncryptionPublicKey;
import com.werewolfchat.startup.ntru.encrypt.NtruEncrypt;

import static com.werewolfchat.startup.ntru.util.ArrayEncoder.bytesToHex;
import static com.werewolfchat.startup.ntru.util.ArrayEncoder.hexStringToByteArray;

public class CryptoWorker
{




    public static String convertPlainTextStringToEncryptedHexString(EncryptionPublicKey pubKey, NtruEncrypt encrypter, String plainText) {
        return bytesToHex(encrypter.encrypt(plainText.getBytes(), pubKey));
    }

    public static String convertEncryptedHexStringToPlainTextString(EncryptionKeyPair key_pair, NtruEncrypt encrypter, String cypherText) {
        return new String(encrypter.decrypt(hexStringToByteArray(cypherText), key_pair));
    }



    public static void printHelp()
    {

        System.out.println(
                "Cryptoworker.jar Version " +returnVersionStr() + "\n"+
                        "to encrypt -e key_in_hex string_to_encrypt\n"+
                        "to encrypt --encrypt key_in_hex string_to_encrypt\n"+
                        "to print the verion -v\n"+
                        "to print this help message -h or --help"
        );


    }

    public static String returnVersionStr()
    {
        return "0.1";
    }

    public static void printVersion()
    {
        System.out.println(returnVersionStr());
    }



    public static String encryptStr(String key, String thingToEncrypt)
    {

        NtruEncrypt ntru = new NtruEncrypt(EncryptionParameters.APR2011_439_FAST);
        EncryptionPublicKey epk = new EncryptionPublicKey(hexStringToByteArray(key));
        return convertPlainTextStringToEncryptedHexString(epk, ntru, thingToEncrypt);
    }




    public static void main(String[] args){

        System.out.println(args.length);

        if(args.length ==0)
        {
            System.out.println("no arguments were passed");
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
            switch (args[0]) {

                //encryption stuff
                case "-e":
                    System.out.println(encryptStr(args[1], args[2]));
                    break;

                case "--encrypt":
                    System.out.println(encryptStr(args[1], args[2]));
                    break;

            /*
            //decryption stuff
            case "-d":
                decryptStr(args[2], args[3]);
                break;

            case "--decrypt":
                decryptStr(args[2], args[3]);
                break;
            */
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