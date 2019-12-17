package com.werewolfchat.startup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class PublicKeyListerner implements ValueEventListener {

    public Map<String, WerewolfChatPublicKey> outputMap;
    //   public CountDownLatch latch;
    public boolean isEmpty;

    public PublicKeyListerner() {
        outputMap = new HashMap<String, WerewolfChatPublicKey>();
        // latch = new CountDownLatch(1);
        isEmpty = true;
    }


    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {


        if (dataSnapshot.getChildrenCount() == 0) {
            Utility.dumb_debugging("database returned 0 rows for public keys");
            return;
        }
        isEmpty = false;
        Utility.dumb_debugging(Long.toString(dataSnapshot.getChildrenCount()));
        DataSnapshot keys = dataSnapshot.getChildren().iterator().next();
        for (DataSnapshot row : dataSnapshot.getChildren()) {
            Utility.dumb_debugging(row.getKey());
            outputMap.putAll((Map<String, WerewolfChatPublicKey>) row.getValue());

            //put(row.getKey(),new WerewolfChatPublicKey(row.get);
        }
        //latch.countDown();
        return;
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        // Getting Post failed, log a message
        Utility.dumb_debugging("database done broke");                // ...
    }

}
