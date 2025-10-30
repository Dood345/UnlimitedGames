package com.appsters.unlimitedgames.data.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * A singleton service for accessing Firebase instances.
 * This class provides a single point of access to the FirebaseAuth and FirebaseFirestore
 * instances throughout the application.
 */
public class FirebaseService {

    private static FirebaseService instance;
    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;

    /**
     * Constructs a new FirebaseService and initializes the FirebaseAuth and FirebaseFirestore
     * instances.
     */
    private FirebaseService() {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    /**
     * Gets the singleton instance of the FirebaseService.
     *
     * @return The singleton instance of the FirebaseService.
     */
    public static synchronized FirebaseService getInstance() {
        if (instance == null) {
            instance = new FirebaseService();
        }
        return instance;
    }

    /**
     * Gets the FirebaseAuth instance.
     *
     * @return The FirebaseAuth instance.
     */
    public FirebaseAuth getAuth() {
        return auth;
    }

    /**
     * Gets the FirebaseFirestore instance.
     *
     * @return The FirebaseFirestore instance.
     */
    public FirebaseFirestore getFirestore() {
        return firestore;
    }
}
