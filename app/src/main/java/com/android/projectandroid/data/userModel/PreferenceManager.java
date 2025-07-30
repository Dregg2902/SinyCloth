package com.android.projectandroid.data.userModel;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;

public class PreferenceManager {
    private static final String PREF_NAME = "user_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_FULLNAME = "fullname";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE_NUMBER = "phone_number";
    private static final String KEY_DATE_OF_BIRTH = "date_of_birth";
    private static final String KEY_USER_TYPE = "user_type";

    private static final String KEY_USER_DATA = "user_data";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_AVATAR = "AVATAR";

    // ✅ THÊM KEY CHO POINTS
    private static final String KEY_POINTS = "points";
    private static final String KEY_LAST_POINTS_UPDATE = "last_points_update";

    // Remember Login Keys
    private static final String KEY_REMEMBER_LOGIN = "remember_login";
    private static final String KEY_SAVED_USERNAME = "saved_username";
    private static final String KEY_SAVED_PASSWORD = "saved_password";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Gson gson;

    public PreferenceManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
        gson = new Gson();
    }

    // Lưu thông tin user đầy đủ (updated method)
    public void saveCompleteUserData(String userId, String authToken, String username, String fullname,
                                     String email, String phoneNumber, String dateOfBirth,
                                     String userType,String avatar, Object userData) {
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_AUTH_TOKEN, authToken);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_FULLNAME, fullname);
        editor.putString(KEY_AVATAR, avatar);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PHONE_NUMBER, phoneNumber);
        editor.putString(KEY_DATE_OF_BIRTH, dateOfBirth);
        editor.putString(KEY_USER_TYPE, userType);

        if (userData != null) {
            String userDataJson = gson.toJson(userData);
            editor.putString(KEY_USER_DATA, userDataJson);
        }

        editor.apply();
    }

    // Lưu thông tin user từ NormalUser object
    public void saveNormalUserData(String userId, String authToken, String userType,
                                   NormalUser normalUser) {
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_AUTH_TOKEN, authToken);
        editor.putString(KEY_USER_TYPE, userType);

        if (normalUser != null) {
            editor.putString(KEY_AVATAR, normalUser.getAvatarUrl());
            editor.putString(KEY_FULLNAME, normalUser.getFullName());
            editor.putString(KEY_USERNAME, normalUser.getUsername());
            editor.putString(KEY_EMAIL, normalUser.getEmail());
            editor.putString(KEY_PHONE_NUMBER, normalUser.getPhoneNumber());
            editor.putString(KEY_DATE_OF_BIRTH, normalUser.getDateOfBirth());
            editor.putString(KEY_ADDRESS, normalUser.getAddress());
            editor.putString(KEY_GENDER, normalUser.getGender());

            // ✅ LUU POINTS
            editor.putInt(KEY_POINTS, normalUser.getPoints());
            editor.putLong(KEY_LAST_POINTS_UPDATE, System.currentTimeMillis());

            // Lưu toàn bộ object
            String userDataJson = gson.toJson(normalUser);
            editor.putString(KEY_USER_DATA, userDataJson);
        }

        editor.apply();
    }

    // Getters
    public String getUserId() {
        return prefs.getString(KEY_USER_ID, "");
    }

    public String getAuthToken() {
        return prefs.getString(KEY_AUTH_TOKEN, "");
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }
    public String getFullname() {
        return prefs.getString(KEY_FULLNAME, "");
    }

    public String getAvatar() {
        return prefs.getString(KEY_AVATAR, "");
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    public String getPhoneNumber() {
        return prefs.getString(KEY_PHONE_NUMBER, "");
    }

    public String getDateOfBirth() {
        return prefs.getString(KEY_DATE_OF_BIRTH, "");
    }

    public String getUserType() {
        return prefs.getString(KEY_USER_TYPE, "");
    }

    public String getAddress() {
        return prefs.getString(KEY_ADDRESS, "");
    }

    public String getGender() {
        return prefs.getString(KEY_GENDER, "");
    }


    // ✅ GETTERS CHO POINTS
    public int getPoints() {
        return prefs.getInt(KEY_POINTS, 0);
    }

    public long getLastPointsUpdate() {
        return prefs.getLong(KEY_LAST_POINTS_UPDATE, 0);
    }

    public String getUserDataJson() {
        return prefs.getString(KEY_USER_DATA, "");
    }

    // Lấy user data dưới dạng object
    public <T> T getUserData(Class<T> classType) {
        String userDataJson = prefs.getString(KEY_USER_DATA, "");
        if (!userDataJson.isEmpty()) {
            return gson.fromJson(userDataJson, classType);
        }
        return null;
    }

    // ✅ LẤY NORMALUSER VỚI POINTS CẬP NHẬT
    public NormalUser getNormalUser() {
        NormalUser user = getUserData(NormalUser.class);
        if (user != null) {
            // Sync điểm từ SharedPreferences (có thể đã được cập nhật riêng)
            user.setPoints(getPoints());
        }
        return user;
    }

    // ✅ TẠO NORMALUSER TỪ PREFERENCES (nếu không có JSON)
    public NormalUser createNormalUserFromPrefs() {
        if (getUserId().isEmpty()) {
            return null;
        }

        NormalUser user = new NormalUser();
        user.setAvatarUrl(getAvatar());
        user.set_id(getUserId());
        user.setUsername(getUsername());
        user.setFullName(getFullname());
        user.setEmail(getEmail());
        user.setPhoneNumber(getPhoneNumber());
        user.setDateOfBirth(getDateOfBirth());
        user.setAddress(getAddress());
        user.setGender(getGender());
        user.setPoints(getPoints());

        return user;
    }

    // ✅ LẤY CURRENT USER (ưu tiên JSON, fallback về individual fields)
    public NormalUser getCurrentUser() {
        NormalUser user = getNormalUser();
        if (user == null) {
            user = createNormalUserFromPrefs();
        }
        return user;
    }

    // Kiểm tra đăng nhập
    public boolean isLoggedIn() {
        return !getAuthToken().isEmpty() && !getUserId().isEmpty();
    }

    // ✅ METHODS CHO POINTS MANAGEMENT

    /**
     * Cập nhật điểm và sync với NormalUser object
     */
    public void updatePoints(int newPoints) {
        editor.putInt(KEY_POINTS, newPoints);
        editor.putLong(KEY_LAST_POINTS_UPDATE, System.currentTimeMillis());

        // Cập nhật trong NormalUser object nếu có
        NormalUser user = getUserData(NormalUser.class);
        if (user != null) {
            user.setPoints(newPoints);
            String userDataJson = gson.toJson(user);
            editor.putString(KEY_USER_DATA, userDataJson);
        }

        editor.apply();
    }

    /**
     * Cộng điểm
     */
    public void addPoints(int pointsToAdd) {
        int currentPoints = getPoints();
        updatePoints(currentPoints + pointsToAdd);
    }

    /**
     * Trừ điểm (cho redemption)
     */
    public boolean deductPoints(int pointsToDeduct) {
        int currentPoints = getPoints();
        if (currentPoints >= pointsToDeduct) {
            updatePoints(currentPoints - pointsToDeduct);
            return true;
        }
        return false;
    }

    /**
     * Kiểm tra đủ điểm không
     */
    public boolean hasEnoughPoints(int requiredPoints) {
        return getPoints() >= requiredPoints;
    }

    /**
     * Lấy điểm được format
     */
    public String getFormattedPoints() {
        return PointsUtils.formatPoints(getPoints());
    }

    /**
     * Lấy level hiện tại
     */
    public String getCurrentLevel() {
        return PointsUtils.getLevelFromPoints(getPoints());
    }

    /**
     * Kiểm tra cần sync điểm từ server không (nếu quá lâu chưa update)
     */
    public boolean needsPointsSync() {
        long lastUpdate = getLastPointsUpdate();
        long now = System.currentTimeMillis();
        long hoursSinceUpdate = (now - lastUpdate) / (1000 * 60 * 60);
        return hoursSinceUpdate > 1; // Sync nếu > 1 tiếng
    }

    // Remember Login Methods
    public void setRememberLogin(boolean remember) {
        editor.putBoolean(KEY_REMEMBER_LOGIN, remember);
        editor.apply();
    }

    public boolean isRememberLoginEnabled() {
        return prefs.getBoolean(KEY_REMEMBER_LOGIN, false);
    }

    public void saveCredentials(String username, String password) {
        editor.putString(KEY_SAVED_USERNAME, username);
        editor.putString(KEY_SAVED_PASSWORD, password);
        editor.apply();
    }

    public String getSavedUsername() {
        return prefs.getString(KEY_SAVED_USERNAME, "");
    }

    public String getSavedPassword() {
        return prefs.getString(KEY_SAVED_PASSWORD, "");
    }

    public void clearSavedCredentials() {
        editor.remove(KEY_SAVED_USERNAME);
        editor.remove(KEY_SAVED_PASSWORD);
        editor.apply();
    }

    // Xóa dữ liệu user (updated for logout)
    public void clearUserData() {
        // ALWAYS clear auth token when logging out
        editor.remove(KEY_AUTH_TOKEN);

        // Clear all session data
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_USERNAME);
        editor.remove(KEY_EMAIL);
        editor.remove(KEY_FULLNAME);
        editor.remove(KEY_PHONE_NUMBER);
        editor.remove(KEY_DATE_OF_BIRTH);
        editor.remove(KEY_USER_TYPE);
        editor.remove(KEY_USER_DATA);
        editor.remove(KEY_ADDRESS);
        editor.remove(KEY_GENDER);
        editor.remove(KEY_AVATAR);

        // ✅ CLEAR POINTS DATA
        editor.remove(KEY_POINTS);
        editor.remove(KEY_LAST_POINTS_UPDATE);

        // Only clear saved credentials if remember login is disabled
        if (!isRememberLoginEnabled()) {
            clearSavedCredentials();
            editor.remove(KEY_REMEMBER_LOGIN);
        }

        editor.apply();
    }

    // Xóa chỉ token (logout nhưng giữ một số thông tin)
    public void clearAuthToken() {
        editor.remove(KEY_AUTH_TOKEN);
        editor.apply();
    }

    // Cập nhật token mới
    public void updateAuthToken(String newToken) {
        editor.putString(KEY_AUTH_TOKEN, newToken);
        editor.apply();
    }

    // Cập nhật thông tin user
    public void updateUserInfo(String username, String email, String phoneNumber, String fullname) {
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_FULLNAME, fullname);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PHONE_NUMBER, phoneNumber);
        editor.apply();
    }

    // ✅ CẬP NHẬT NORMALUSER VỚI SYNC POINTS
    public void updateNormalUser(NormalUser normalUser) {
        if (normalUser != null) {
            editor.putString(KEY_AVATAR, normalUser.getAvatarUrl());
            editor.putString(KEY_USERNAME, normalUser.getUsername());
            editor.putString(KEY_EMAIL, normalUser.getEmail());
            editor.putString(KEY_FULLNAME, normalUser.getFullName());
            editor.putString(KEY_PHONE_NUMBER, normalUser.getPhoneNumber());
            editor.putString(KEY_DATE_OF_BIRTH, normalUser.getDateOfBirth());
            editor.putString(KEY_ADDRESS, normalUser.getAddress());
            editor.putString(KEY_GENDER, normalUser.getGender());

            // ✅ CẬP NHẬT POINTS
            editor.putInt(KEY_POINTS, normalUser.getPoints());
            editor.putLong(KEY_LAST_POINTS_UPDATE, System.currentTimeMillis());

            String userDataJson = gson.toJson(normalUser);
            editor.putString(KEY_USER_DATA, userDataJson);
            editor.apply();
        }
    }

    // ✅ SYNC USER DATA TỪ API RESPONSE
    public void syncUserDataFromApi(UserInfoResponse.UserData userData) {
        if (userData != null) {
            editor.putString(KEY_USERNAME, userData.getUsername());
            editor.putString(KEY_EMAIL, userData.getEmail());
            editor.putString(KEY_FULLNAME, userData.getFullname());
            editor.putString(KEY_PHONE_NUMBER, userData.getPhoneNumber());
            editor.putString(KEY_DATE_OF_BIRTH, userData.getDateOfBirth());
            editor.putString(KEY_ADDRESS, userData.getAddress());
            editor.putString(KEY_GENDER, userData.getGender());

            // ✅ CẬP NHẬT POINTS TỪ SERVER
            editor.putInt(KEY_POINTS, userData.getPoints());
            editor.putLong(KEY_LAST_POINTS_UPDATE, System.currentTimeMillis());

            // Tạo NormalUser object và lưu
            NormalUser normalUser = new NormalUser();
            normalUser.set_id(getUserId());
            normalUser.setUsername(userData.getUsername());
            normalUser.setFullName(userData.getFullname());
            normalUser.setEmail(userData.getEmail());
            normalUser.setPhoneNumber(userData.getPhoneNumber());
            normalUser.setDateOfBirth(userData.getDateOfBirth());
            normalUser.setAddress(userData.getAddress());
            normalUser.setGender(userData.getGender());
            normalUser.setPoints(userData.getPoints());

            String userDataJson = gson.toJson(normalUser);
            editor.putString(KEY_USER_DATA, userDataJson);

            editor.apply();
        }
    }

    // Clear everything (complete reset)
    public void clearAll() {
        editor.clear();
        editor.apply();
    }

    // ✅ DEBUG METHODS
    public void logCurrentState() {
        android.util.Log.d("PreferenceManager", "=== USER STATE ===");
        android.util.Log.d("PreferenceManager", "User ID: " + getUserId());
        android.util.Log.d("PreferenceManager", "Username: " + getUsername());
        android.util.Log.d("PreferenceManager", "Points: " + getPoints());
        android.util.Log.d("PreferenceManager", "Level: " + getCurrentLevel());
        android.util.Log.d("PreferenceManager", "Is Logged In: " + isLoggedIn());
        android.util.Log.d("PreferenceManager", "Last Points Update: " + getLastPointsUpdate());
        android.util.Log.d("PreferenceManager", "Needs Points Sync: " + needsPointsSync());
    }
}