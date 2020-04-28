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
package com.werewolfchat.werewolf_chat_cli;

import com.werewolfchat.werewolf_chat_cli.ntru.encrypt.EncryptionKeyPair;
import com.werewolfchat.werewolf_chat_cli.ntru.encrypt.EncryptionPublicKey;
import com.werewolfchat.werewolf_chat_cli.ntru.encrypt.NtruEncrypt;
import com.werewolfchat.werewolf_chat_cli.ntru.util.ArrayEncoder;


public class WerewolfMessage {

    private String id;
    private String text;
    private String name;
    private String photoUrl;
    private String imageUrl;
    private String senderId;
    private String destinationId;
    //private EncryptionPublicKey destKey;


    public WerewolfMessage() {
    }

    public WerewolfMessage(String text, String name, String photoUrl, String imageUrl,
                           String sender, String dest) {
        this.text = text;
        this.name = name;
        this.photoUrl = photoUrl;
        this.imageUrl = imageUrl;
        this.senderId = sender;
        this.destinationId = dest;
        //this.destKey = otherKey;
    }


    public WerewolfMessage(String dest, String sender, String text) {
        this.text = text;
        this.name = "annon";
        this.photoUrl = null;
        this.imageUrl = null;
        this.senderId = sender;
        this.destinationId = dest;
        //this.destKey = otherKey;
    }


    public String getSenderId() {
        return senderId;
    }

    public String getDestinationId() {
        return destinationId;
    }

    public void setSenderId(String sender) {
        this.senderId = sender;
    }

    public void setDestinationId(String dest) {
        this.destinationId = dest;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getText() {
        return text;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void convertPlainTextStringToEncryptedHexString(EncryptionPublicKey pubKey, NtruEncrypt encrypter) {
        this.text = ArrayEncoder.bytesToHex(encrypter.encrypt(this.text.getBytes(), pubKey));
    }

    public void convertEncryptedHexStringToPlainTextString(EncryptionKeyPair key_pair, NtruEncrypt encrypter) {
        this.text = new String(encrypter.decrypt(ArrayEncoder.hexStringToByteArray(this.text), key_pair));
    }


}
