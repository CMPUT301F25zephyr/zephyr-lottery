package com.example.zephyr_lottery;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import static org.junit.jupiter.api.Assertions.*;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

class AdminTest {
    @Test
    void testRetrieveProfile() {
        ArrayList<UserProfile> profileArrayList = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference profilesRef = db.collection("accounts");
        DocumentReference docRef = profilesRef.document("Test");

        docRef.set(new UserProfile("Test", "test@gmail.com", "Organizer"));

        profilesRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
            }
            if(value != null && !value.isEmpty()){
                profileArrayList.clear();
                for (QueryDocumentSnapshot snapshot : value){
                    String name = snapshot.getString("username");
                    String email = snapshot.getString("email");
                    String type = snapshot.getString("type");

                    profileArrayList.add(new UserProfile(name, email, type));
                }
            }
        });

        boolean accountFound = false;
        for (int i = 0; i < profileArrayList.size(); i++) {
            UserProfile profile = profileArrayList.get(i);
            if (profile.getUsername().equals("Test") && profile.getEmail().equals("test@gmail.com") && profile.getType().equals("Organizer")) {
                accountFound = true;
                break;
            }
        }
        assertTrue(accountFound);
    }
}
