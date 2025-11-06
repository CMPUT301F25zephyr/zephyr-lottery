//package com.example.zephyr_lottery.repositories;
//
//import android.util.Log;
//
//import androidx.annotation.NonNull;
//import androidx.lifecycle.LiveData;
//import androidx.lifecycle.MutableLiveData;
//
//import com.example.zephyr_lottery.models.User;
//import com.example.zephyr_lottery.models.UserRole;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.firestore.DocumentSnapshot;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.firestore.Query;
//import com.google.firebase.firestore.QuerySnapshot;
//import com.google.firebase.firestore.SetOptions;
//
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * Repository class for User data operations
// * Handles all Firebase Firestore interactions for User objects
// *
// * Implements:
// * - US 01.02.01: Create user profile
// * - US 01.02.02: Update user information
// * - US 01.02.04: Delete user profile
// * - US 01.07.01: Device-based identification
// * - US 03.02.01: Admin remove profiles
// * - US 03.05.01: Admin browse profiles
// */
//public class UserRepository {
//
//    private static final String TAG = "UserRepository";
//    private static final String COLLECTION_USERS = "users";
//
//    private final FirebaseFirestore db;
//    private static UserRepository instance;
//
//    // Private constructor for singleton pattern
//    private UserRepository() {
//        db = FirebaseFirestore.getInstance();
//    }
//
//    // Singleton instance
//    public static synchronized UserRepository getInstance() {
//        if (instance == null) {
//            instance = new UserRepository();
//        }
//        return instance;
//    }
//
//    // ==================== CREATE ====================
//
//    /**
//     * Create a new user in Firestore
//     * US 01.02.01: Provide personal information
//     */
//    public Task<Void> createUser(User user) {
//        if (user.getUserId() == null || user.getUserId().isEmpty()) {
//            throw new IllegalArgumentException("User ID cannot be null or empty");
//        }
//
//        user.setCreatedAt(new Date());
//        user.setUpdatedAt(new Date());
//
//        return db.collection(COLLECTION_USERS)
//                .document(user.getUserId())
//                .set(user.toMap())
//                .addOnSuccessListener(aVoid ->
//                        Log.d(TAG, "User created successfully: " + user.getUserId()))
//                .addOnFailureListener(e ->
//                        Log.e(TAG, "Error creating user", e));
//    }
//
//    /**
//     * Create or update user (upsert)
//     */
//    public Task<Void> saveUser(User user) {
//        if (user.getUserId() == null || user.getUserId().isEmpty()) {
//            throw new IllegalArgumentException("User ID cannot be null or empty");
//        }
//
//        user.setUpdatedAt(new Date());
//
//        return db.collection(COLLECTION_USERS)
//                .document(user.getUserId())
//                .set(user.toMap(), SetOptions.merge())
//                .addOnSuccessListener(aVoid ->
//                        Log.d(TAG, "User saved successfully: " + user.getUserId()))
//                .addOnFailureListener(e ->
//                        Log.e(TAG, "Error saving user", e));
//    }
//
//    // ==================== READ ====================
//
//    /**
//     * Get user by ID (device ID)
//     * US 01.07.01: Device-based identification
//     */
//    public void getUserById(String userId, UserCallback callback) {
//        db.collection(COLLECTION_USERS)
//                .document(userId)
//                .get()
//                .addOnSuccessListener(documentSnapshot -> {
//                    if (documentSnapshot.exists()) {
//                        User user = documentSnapshot.toObject(User.class);
//                        callback.onSuccess(user);
//                        Log.d(TAG, "User retrieved: " + userId);
//                    } else {
//                        callback.onError("User not found");
//                        Log.d(TAG, "User not found: " + userId);
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    callback.onError(e.getMessage());
//                    Log.e(TAG, "Error retrieving user", e);
//                });
//    }
//
//    /**
//     * Get user by ID as LiveData
//     */
//    public LiveData<User> getUserLiveData(String userId) {
//        MutableLiveData<User> userLiveData = new MutableLiveData<>();
//
//        db.collection(COLLECTION_USERS)
//                .document(userId)
//                .addSnapshotListener((documentSnapshot, error) -> {
//                    if (error != null) {
//                        Log.e(TAG, "Listen failed for user: " + userId, error);
//                        return;
//                    }
//
//                    if (documentSnapshot != null && documentSnapshot.exists()) {
//                        User user = documentSnapshot.toObject(User.class);
//                        userLiveData.setValue(user);
//                    }
//                });
//
//        return userLiveData;
//    }
//
//    /**
//     * Get user by email
//     */
//    public void getUserByEmail(String email, UserCallback callback) {
//        db.collection(COLLECTION_USERS)
//                .whereEqualTo("email", email)
//                .whereEqualTo("isDeleted", false)
//                .limit(1)
//                .get()
//                .addOnSuccessListener(querySnapshot -> {
//                    if (!querySnapshot.isEmpty()) {
//                        User user = querySnapshot.getDocuments().get(0).toObject(User.class);
//                        callback.onSuccess(user);
//                    } else {
//                        callback.onError("User not found with email: " + email);
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    callback.onError(e.getMessage());
//                    Log.e(TAG, "Error retrieving user by email", e);
//                });
//    }
//
//    /**
//     * Get all users (for admin)
//     * US 03.05.01: Browse profiles
//     */
//    public void getAllUsers(UsersCallback callback) {
//        db.collection(COLLECTION_USERS)
//                .whereEqualTo("isDeleted", false)
//                .orderBy("createdAt", Query.Direction.DESCENDING)
//                .get()
//                .addOnSuccessListener(querySnapshot -> {
//                    List<User> users = new ArrayList<>();
//                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
//                        User user = doc.toObject(User.class);
//                        if (user != null) {
//                            users.add(user);
//                        }
//                    }
//                    callback.onSuccess(users);
//                    Log.d(TAG, "Retrieved " + users.size() + " users");
//                })
//                .addOnFailureListener(e -> {
//                    callback.onError(e.getMessage());
//                    Log.e(TAG, "Error retrieving all users", e);
//                });
//    }
//
//    /**
//     * Get users by role
//     */
//    public void getUsersByRole(UserRole role, UsersCallback callback) {
//        db.collection(COLLECTION_USERS)
//                .whereEqualTo("role", role.toString())
//                .whereEqualTo("isDeleted", false)
//                .get()
//                .addOnSuccessListener(querySnapshot -> {
//                    List<User> users = new ArrayList<>();
//                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
//                        User user = doc.toObject(User.class);
//                        if (user != null) {
//                            users.add(user);
//                        }
//                    }
//                    callback.onSuccess(users);
//                })
//                .addOnFailureListener(e -> {
//                    callback.onError(e.getMessage());
//                    Log.e(TAG, "Error retrieving users by role", e);
//                });
//    }
//
//    /**
//     * Check if user exists by device ID
//     */
//    public void checkUserExists(String deviceId, ExistsCallback callback) {
//        db.collection(COLLECTION_USERS)
//                .document(deviceId)
//                .get()
//                .addOnSuccessListener(documentSnapshot -> {
//                    boolean exists = documentSnapshot.exists() &&
//                            !documentSnapshot.getBoolean("isDeleted");
//                    callback.onResult(exists);
//                })
//                .addOnFailureListener(e -> {
//                    callback.onResult(false);
//                    Log.e(TAG, "Error checking user existence", e);
//                });
//    }
//
//    // ==================== UPDATE ====================
//
//    /**
//     * Update user information
//     * US 01.02.02: Update personal information
//     */
//    public Task<Void> updateUser(String userId, Map<String, Object> updates) {
//        updates.put("updatedAt", new Date());
//
//        return db.collection(COLLECTION_USERS)
//                .document(userId)
//                .update(updates)
//                .addOnSuccessListener(aVoid ->
//                        Log.d(TAG, "User updated successfully: " + userId))
//                .addOnFailureListener(e ->
//                        Log.e(TAG, "Error updating user", e));
//    }
//
//    /**
//     * Update user name
//     */
//    public Task<Void> updateUserName(String userId, String name) {
//        Map<String, Object> updates = new HashMap<>();
//        updates.put("name", name);
//        return updateUser(userId, updates);
//    }
//
//    /**
//     * Update user email
//     */
//    public Task<Void> updateUserEmail(String userId, String email) {
//        Map<String, Object> updates = new HashMap<>();
//        updates.put("email", email);
//        return updateUser(userId, updates);
//    }
//
//    /**
//     * Update user phone number
//     */
//    public Task<Void> updateUserPhone(String userId, String phoneNumber) {
//        Map<String, Object> updates = new HashMap<>();
//        updates.put("phoneNumber", phoneNumber);
//        return updateUser(userId, updates);
//    }
//
//    /**
//     * Update user profile image
//     */
//    public Task<Void> updateUserProfileImage(String userId, String imageUrl) {
//        Map<String, Object> updates = new HashMap<>();
//        updates.put("profileImageUrl", imageUrl);
//        updates.put("hasCustomProfileImage", imageUrl != null && !imageUrl.isEmpty());
//        return updateUser(userId, updates);
//    }
//
//    /**
//     * Update user role
//     */
//    public Task<Void> updateUserRole(String userId, UserRole role) {
//        Map<String, Object> updates = new HashMap<>();
//        updates.put("role", role.toString());
//        return updateUser(userId, updates);
//    }
//
//    /**
//     * Update FCM token for notifications
//     */
//    public Task<Void> updateFcmToken(String userId, String fcmToken) {
//        Map<String, Object> updates = new HashMap<>();
//        updates.put("fcmToken", fcmToken);
//        return updateUser(userId, updates);
//    }
//
//    /**
//     * Update notification preferences
//     * US 01.04.03: Opt out of notifications
//     */
//    public Task<Void> updateNotificationPreferences(String userId, boolean enabled) {
//        Map<String, Object> updates = new HashMap<>();
//        updates.put("notificationsEnabled", enabled);
//        return updateUser(userId, updates);
//    }
//
//    /**
//     * Add event to user's registered events
//     */
//    public Task<Void> addRegisteredEvent(String userId, String eventId) {
//        return db.collection(COLLECTION_USERS)
//                .document(userId)
//                .get()
//                .continueWithTask(task -> {
//                    if (task.isSuccessful() && task.getResult() != null) {
//                        User user = task.getResult().toObject(User.class);
//                        if (user != null) {
//                            user.addRegisteredEvent(eventId);
//                            return db.collection(COLLECTION_USERS)
//                                    .document(userId)
//                                    .update("registeredEventIds", user.getRegisteredEventIds(),
//                                            "updatedAt", new Date());
//                        }
//                    }
//                    throw new Exception("Failed to add registered event");
//                });
//    }
//
//    /**
//     * Add event to user's selected events
//     */
//    public Task<Void> addSelectedEvent(String userId, String eventId) {
//        return db.collection(COLLECTION_USERS)
//                .document(userId)
//                .get()
//                .continueWithTask(task -> {
//                    if (task.isSuccessful() && task.getResult() != null) {
//                        User user = task.getResult().toObject(User.class);
//                        if (user != null) {
//                            user.addSelectedEvent(eventId);
//                            return db.collection(COLLECTION_USERS)
//                                    .document(userId)
//                                    .update("selectedEventIds", user.getSelectedEventIds(),
//                                            "updatedAt", new Date());
//                        }
//                    }
//                    throw new Exception("Failed to add selected event");
//                });
//    }
//
//    // ==================== DELETE ====================
//
//    /**
//     * Soft delete user (marks as deleted but keeps data)
//     * US 01.02.04: Delete profile
//     */
//    public Task<Void> softDeleteUser(String userId) {
//        Map<String, Object> updates = new HashMap<>();
//        updates.put("isDeleted", true);
//        updates.put("isActive", false);
//        updates.put("updatedAt", new Date());
//
//        return db.collection(COLLECTION_USERS)
//                .document(userId)
//                .update(updates)
//                .addOnSuccessListener(aVoid ->
//                        Log.d(TAG, "User soft deleted: " + userId))
//                .addOnFailureListener(e ->
//                        Log.e(TAG, "Error soft deleting user", e));
//    }
//
//    /**
//     * Hard delete user (permanently removes from database)
//     * US 03.02.01: Admin remove profiles
//     */
//    public Task<Void> hardDeleteUser(String userId) {
//        return db.collection(COLLECTION_USERS)
//                .document(userId)
//                .delete()
//                .addOnSuccessListener(aVoid ->
//                        Log.d(TAG, "User permanently deleted: " + userId))
//                .addOnFailureListener(e ->
//                        Log.e(TAG, "Error deleting user", e));
//    }
//
//    /**
//     * Restore a soft-deleted user
//     */
//    public Task<Void> restoreUser(String userId) {
//        Map<String, Object> updates = new HashMap<>();
//        updates.put("isDeleted", false);
//        updates.put("isActive", true);
//        updates.put("updatedAt", new Date());
//
//        return db.collection(COLLECTION_USERS)
//                .document(userId)
//                .update(updates)
//                .addOnSuccessListener(aVoid ->
//                        Log.d(TAG, "User restored: " + userId))
//                .addOnFailureListener(e ->
//                        Log.e(TAG, "Error restoring user", e));
//    }
//
//    // ==================== CALLBACK INTERFACES ====================
//
//    public interface UserCallback {
//        void onSuccess(User user);
//        void onError(String error);
//    }
//
//    public interface UsersCallback {
//        void onSuccess(List<User> users);
//        void onError(String error);
//    }
//
//    public interface ExistsCallback {
//        void onResult(boolean exists);
//    }
//
//    public interface OperationCallback {
//        void onSuccess();
//        void onError(String error);
//    }
//}