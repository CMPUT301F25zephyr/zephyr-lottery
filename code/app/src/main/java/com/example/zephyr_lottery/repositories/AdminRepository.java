//package com.example.zephyr_lottery.repositories;
//
//import android.util.Log;
//
//import androidx.lifecycle.LiveData;
//import androidx.lifecycle.MutableLiveData;
//
//import com.example.zephyr_lottery.models.Admin;
//import com.example.zephyr_lottery.models.AdminLevel;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.firestore.DocumentSnapshot;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.firestore.Query;
//
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * Repository class for Admin data operations
// * Handles all Firebase Firestore interactions for Admin objects
// *
// * Implements:
// * - US 03.01.01 - 03.08.01: All admin functionalities
// */
//public class AdminRepository {
//
//    private static final String TAG = "AdminRepository";
//    private static final String COLLECTION_ADMINS = "admins";
//
//    private final FirebaseFirestore db;
//    private static AdminRepository instance;
//
//    // Private constructor for singleton pattern
//    private AdminRepository() {
//        db = FirebaseFirestore.getInstance();
//    }
//
//    // Singleton instance
//    public static synchronized AdminRepository getInstance() {
//        if (instance == null) {
//            instance = new AdminRepository();
//        }
//        return instance;
//    }
//
//    // ==================== CREATE ====================
//
//    /**
//     * Create a new admin account
//     * Only super admins should call this
//     */
//    public Task<Void> createAdmin(Admin admin) {
//        if (admin.getAdminId() == null || admin.getAdminId().isEmpty()) {
//            throw new IllegalArgumentException("Admin ID cannot be null or empty");
//        }
//
//        admin.setCreatedAt(new Date());
//        admin.setLastLoginAt(new Date());
//
//        return db.collection(COLLECTION_ADMINS)
//                .document(admin.getAdminId())
//                .set(admin.toMap())
//                .addOnSuccessListener(aVoid ->
//                        Log.d(TAG, "Admin created successfully: " + admin.getAdminId()))
//                .addOnFailureListener(e ->
//                        Log.e(TAG, "Error creating admin", e));
//    }
//
//    // ==================== READ ====================
//
//    /**
//     * Get admin by ID
//     */
//    public void getAdminById(String adminId, AdminCallback callback) {
//        db.collection(COLLECTION_ADMINS)
//                .document(adminId)
//                .get()
//                .addOnSuccessListener(documentSnapshot -> {
//                    if (documentSnapshot.exists()) {
//                        Admin admin = documentSnapshot.toObject(Admin.class);
//                        callback.onSuccess(admin);
//                        Log.d(TAG, "Admin retrieved: " + adminId);
//                    } else {
//                        callback.onError("Admin not found");
//                        Log.d(TAG, "Admin not found: " + adminId);
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    callback.onError(e.getMessage());
//                    Log.e(TAG, "Error retrieving admin", e);
//                });
//    }
//
//    /**
//     * Get admin by ID as LiveData
//     */
//    public LiveData<Admin> getAdminLiveData(String adminId) {
//        MutableLiveData<Admin> adminLiveData = new MutableLiveData<>();
//
//        db.collection(COLLECTION_ADMINS)
//                .document(adminId)
//                .addSnapshotListener((documentSnapshot, error) -> {
//                    if (error != null) {
//                        Log.e(TAG, "Listen failed for admin: " + adminId, error);
//                        return;
//                    }
//
//                    if (documentSnapshot != null && documentSnapshot.exists()) {
//                        Admin admin = documentSnapshot.toObject(Admin.class);
//                        adminLiveData.setValue(admin);
//                    }
//                });
//
//        return adminLiveData;
//    }
//
//    /**
//     * Get admin by device ID
//     */
//    public void getAdminByDeviceId(String deviceId, AdminCallback callback) {
//        db.collection(COLLECTION_ADMINS)
//                .whereEqualTo("deviceId", deviceId)
//                .whereEqualTo("isActive", true)
//                .limit(1)
//                .get()
//                .addOnSuccessListener(querySnapshot -> {
//                    if (!querySnapshot.isEmpty()) {
//                        Admin admin = querySnapshot.getDocuments().get(0).toObject(Admin.class);
//                        callback.onSuccess(admin);
//                    } else {
//                        callback.onError("No active admin found for device");
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    callback.onError(e.getMessage());
//                    Log.e(TAG, "Error retrieving admin by device", e);
//                });
//    }
//
//    /**
//     * Get all admins (super admin only)
//     */
//    public void getAllAdmins(AdminsCallback callback) {
//        db.collection(COLLECTION_ADMINS)
//                .orderBy("createdAt", Query.Direction.DESCENDING)
//                .get()
//                .addOnSuccessListener(querySnapshot -> {
//                    List<Admin> admins = new ArrayList<>();
//                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
//                        Admin admin = doc.toObject(Admin.class);
//                        if (admin != null) {
//                            admins.add(admin);
//                        }
//                    }
//                    callback.onSuccess(admins);
//                    Log.d(TAG, "Retrieved " + admins.size() + " admins");
//                })
//                .addOnFailureListener(e -> {
//                    callback.onError(e.getMessage());
//                    Log.e(TAG, "Error retrieving all admins", e);
//                });
//    }
//
//    /**
//     * Get admins by level
//     */
//    public void getAdminsByLevel(AdminLevel level, AdminsCallback callback) {
//        db.collection(COLLECTION_ADMINS)
//                .whereEqualTo("level", level.toString())
//                .whereEqualTo("isActive", true)
//                .get()
//                .addOnSuccessListener(querySnapshot -> {
//                    List<Admin> admins = new ArrayList<>();
//                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
//                        Admin admin = doc.toObject(Admin.class);
//                        if (admin != null) {
//                            admins.add(admin);
//                        }
//                    }
//                    callback.onSuccess(admins);
//                })
//                .addOnFailureListener(e -> {
//                    callback.onError(e.getMessage());
//                    Log.e(TAG, "Error retrieving admins by level", e);
//                });
//    }
//
//    /**
//     * Check if admin exists
//     */
//    public void checkAdminExists(String adminId, ExistsCallback callback) {
//        db.collection(COLLECTION_ADMINS)
//                .document(adminId)
//                .get()
//                .addOnSuccessListener(documentSnapshot -> {
//                    boolean exists = documentSnapshot.exists() &&
//                            documentSnapshot.getBoolean("isActive");
//                    callback.onResult(exists);
//                })
//                .addOnFailureListener(e -> {
//                    callback.onResult(false);
//                    Log.e(TAG, "Error checking admin existence", e);
//                });
//    }
//
//    /**
//     * Check if device has admin access
//     */
//    public void checkDeviceHasAdminAccess(String deviceId, ExistsCallback callback) {
//        db.collection(COLLECTION_ADMINS)
//                .whereEqualTo("deviceId", deviceId)
//                .whereEqualTo("isActive", true)
//                .limit(1)
//                .get()
//                .addOnSuccessListener(querySnapshot ->
//                        callback.onResult(!querySnapshot.isEmpty()))
//                .addOnFailureListener(e -> {
//                    callback.onResult(false);
//                    Log.e(TAG, "Error checking device admin access", e);
//                });
//    }
//
//    // ==================== UPDATE ====================
//
//    /**
//     * Update admin information
//     */
//    public Task<Void> updateAdmin(String adminId, Map<String, Object> updates) {
//        return db.collection(COLLECTION_ADMINS)
//                .document(adminId)
//                .update(updates)
//                .addOnSuccessListener(aVoid ->
//                        Log.d(TAG, "Admin updated successfully: " + adminId))
//                .addOnFailureListener(e ->
//                        Log.e(TAG, "Error updating admin", e));
//    }
//
//    /**
//     * Update admin level (super admin only)
//     */
//    public Task<Void> updateAdminLevel(String adminId, AdminLevel level) {
//        Map<String, Object> updates = new HashMap<>();
//        updates.put("level", level.toString());
//        return updateAdmin(adminId, updates);
//    }
//
//    /**
//     * Record admin login
//     */
//    public Task<Void> recordLogin(String adminId) {
//        Map<String, Object> updates = new HashMap<>();
//        updates.put("lastLoginAt", new Date());
//        return updateAdmin(adminId, updates);
//    }
//
//    /**
//     * Increment actions performed counter
//     */
//    public Task<Void> incrementActionsPerformed(String adminId) {
//        return db.collection(COLLECTION_ADMINS)
//                .document(adminId)
//                .get()
//                .continueWithTask(task -> {
//                    if (task.isSuccessful() && task.getResult() != null) {
//                        Admin admin = task.getResult().toObject(Admin.class);
//                        if (admin != null) {
//                            admin.incrementActionsPerformed();
//                            Map<String, Object> updates = new HashMap<>();
//                            updates.put("totalActionsPerformed", admin.getTotalActionsPerformed());
//                            return db.collection(COLLECTION_ADMINS)
//                                    .document(adminId)
//                                    .update(updates);
//                        }
//                    }
//                    throw new Exception("Failed to increment actions");
//                });
//    }
//
//    /**
//     * Activate/deactivate admin account
//     */
//    public Task<Void> setAdminActive(String adminId, boolean active) {
//        Map<String, Object> updates = new HashMap<>();
//        updates.put("isActive", active);
//        return updateAdmin(adminId, updates);
//    }
//
//    // ==================== DELETE ====================
//
//    /**
//     * Delete admin account (super admin only)
//     */
//    public Task<Void> deleteAdmin(String adminId) {
//        return db.collection(COLLECTION_ADMINS)
//                .document(adminId)
//                .delete()
//                .addOnSuccessListener(aVoid ->
//                        Log.d(TAG, "Admin deleted: " + adminId))
//                .addOnFailureListener(e ->
//                        Log.e(TAG, "Error deleting admin", e));
//    }
//
//    // ==================== CALLBACK INTERFACES ====================
//
//    public interface AdminCallback {
//        void onSuccess(Admin admin);
//        void onError(String error);
//    }
//
//    public interface AdminsCallback {
//        void onSuccess(List<Admin> admins);
//        void onError(String error);
//    }
//
//    public interface ExistsCallback {
//        void onResult(boolean exists);
//    }
//}