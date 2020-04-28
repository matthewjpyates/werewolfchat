package com.werewolfchat.werewolf_chat_cli;

public class WerewolfChatPublicKey {
    public String chat_id;
    public String public_key_hexstring;

    public WerewolfChatPublicKey(String id, String pub_key) {

        this.chat_id = id;
        this.public_key_hexstring = pub_key;
    }


}
