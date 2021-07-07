package com.ecosense.app.helper;

/**
 * Created by Bhavesh Patel on 08-01-2017.
 */


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;

import java.util.HashMap;
import java.util.Locale;

import com.ecosense.app.activity.LoginWithMobile;

public class UserSessionManger {

    // Shared Preferences reference
    SharedPreferences pref;

    // Editor reference for Shared preferences
    Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREFER_NAME = "DWMS";


    // All Shared Preferences Keys
    private static final String IS_USER_LOGIN = "IsUserLoggedIn";
    public  static String KEY_APP_INSTALL_DATE = "App_InstallDate";
    // User name (make variable public to access from outside)
    public static final String KEY_psNo = "psNo";
    public static final String KEY_userWardNo = "userWardNo";
    public static final String KEY_psName = "psName";
    public static final String KEY_UserProfilePic = "UserProfilePic";
    public static final String KEY_userType = "userType";
    public static final String KEY_userSubType = "userSubType";
    private static final String KEY_MOBILE_NUMBER = "mobile_number";
    private static final String KEY_COST_CENTER = "cost_center";
    private static final String KEY_DL_NUMBER = "dl_number";
    public static final String KEY_EMAIL = "email";
    private static final String KEY_IS_WAITING_FOR_SMS = "IsWaitingForSms";
    public static final String KEY_MY_SERVER_IP = "MY_SERVER_IP";
    public static final String KEY_OCTET1 = "OCTET1";
    public static final String KEY_OCTET2 = "OCTET2";
    public static final String KEY_OCTET3 = "OCTET3";
    public static final String KEY_OCTET4 = "OCTET4";

    public static final String KEY_AppInstall_1stTime = "AppInstall_1stTime";
    public static final String KEY_language = "language";

    public static final String KEY_PORT = "PORT";

    public static final String KEY_MY_SERVER_URL = "MY_SERVER_URL";
    public static final String KEY_URL = "URL";


//    // Email address (make variable public to access from outside)
//    public static final String KEY_EMAIL = "email";

    // Constructor
    public UserSessionManger(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREFER_NAME, 0);

        editor = pref.edit();


    }

    public void updateAppLanguage(Context context, String language) {

        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
    }
    public void setApp_InstallDate(String InstallDate) {
        editor.putString(KEY_APP_INSTALL_DATE, InstallDate).commit();
    }

    public String getuserCost_Center() {
        return pref.getString(KEY_COST_CENTER, null);
    }

    public void setuserCost_Center(String cost_center) {
        editor.putString(KEY_COST_CENTER, cost_center).commit();
    }


    public void setuserWardNo(String userWardNo) {
        editor.putString(KEY_userWardNo, userWardNo).commit();
    }
    public String getuserWardNo() {
        return pref.getString(KEY_userWardNo,null);
    }

  public void setUserProfilePic(String UserProfilePic) {
        editor.putString(KEY_UserProfilePic, UserProfilePic).commit();
    }

    public String getUserProfilePic() {
        return pref.getString(KEY_UserProfilePic,null);
    }

    public void setDlNumber(String dlNO) {
        editor.putString(KEY_DL_NUMBER, dlNO).commit();
    }

    public String getDLNumber() {
        return pref.getString(KEY_DL_NUMBER,null);
    }

 public void setAppInstall_1stTime(String status) {
        editor.putString(KEY_AppInstall_1stTime, status).commit();
    }

    public String getAppInstall_1stTime() {
        return pref.getString(KEY_AppInstall_1stTime, Connection.AppInstall_1stTime);
    }

    public void setAppLanguage(String language) {
        editor.putString(KEY_language, language).commit();
    }

    public String getAppLanguage() {
        return pref.getString(KEY_language, Connection.Default_language);
    }

    public void setIsWaitingForSms(boolean isWaiting) {
        editor.putBoolean(KEY_IS_WAITING_FOR_SMS, isWaiting);
        editor.commit();
    }

    public boolean isWaitingForSms() {
        return pref.getBoolean(KEY_IS_WAITING_FOR_SMS, false);
    }


    public void setMobileNumber(String mobileNumber) {
        editor.putString(KEY_MOBILE_NUMBER, mobileNumber);
        editor.commit();
    }

    public String getMobileNumber() {
        return pref.getString(KEY_MOBILE_NUMBER, Connection.AppDefaultMobile);
    }

    public void setEmailLogin(String Email) {
        editor.putString(KEY_EMAIL, Email).commit();
    }

    public String getEmailLogin() {
        return pref.getString(KEY_EMAIL, Connection.AppDefaultEMail);
    }


    public void setMyServerIP(String SERVER_IP) {
        editor.putString(KEY_MY_SERVER_IP, SERVER_IP).commit();
    }

    public String getMyServerIP() {
        return pref.getString(KEY_MY_SERVER_IP, Connection.MY_SERVER_IP);
    }

    public void setMyServerURL(String ServerURL) {
        editor.putString(KEY_MY_SERVER_URL, ServerURL).commit();
    }

    public String getMyServerURL() {
        return pref.getString(KEY_MY_SERVER_URL, Connection.MY_SERVER_IP);
    }

    public void setOCTET1(int OCTET1) {
        editor.putInt(KEY_OCTET1, OCTET1).commit();
    }

    public int getOCTET1() {
        return pref.getInt(KEY_OCTET1, Connection.OCTET1);
    }

    public void setOCTET2(int OCTET2) {
        editor.putInt(KEY_OCTET2, OCTET2).commit();
    }

    public int getOCTET2() {
        return pref.getInt(KEY_OCTET2, Connection.OCTET2);
    }

    public void setOCTET3(int OCTET3) {
        editor.putInt(KEY_OCTET3, OCTET3).commit();
    }

    public int getOCTET3() {
        return pref.getInt(KEY_OCTET3, Connection.OCTET3);
    }

    public void setOCTET4(int OCTET4) {
        editor.putInt(KEY_OCTET4, OCTET4).commit();
    }

    public int getOCTET4() {
        return pref.getInt(KEY_OCTET4, Connection.OCTET4);
    }

    public void setPORT(int port) {
        editor.putInt(KEY_PORT, port).commit();
    }

    public int getPORT() {
        return pref.getInt(KEY_PORT, Connection.PORT);
    }

    public void setURL(String ServerURL) {
        editor.putString(KEY_URL, ServerURL).commit();
    }

    public String getURL() {
        return pref.getString(KEY_URL, Connection.MY_SERVER_IP);
    }

    public void setpsNo(String psno) {
        editor.putString(KEY_psNo, psno).commit();
    }

    public String getpsNo() {
        return pref.getString(KEY_psNo, null);
    }

    public void setpsName(String psName) {
        editor.putString(KEY_psName, psName).commit();
    }

    public String getpsName() {
        return pref.getString(KEY_psName, Connection.AppDefaultUser);
    }

    public void setuserType(String userType) {
        editor.putString(KEY_userType, userType).commit();
    }

    public String getuserType() {
        return pref.getString(KEY_userType, null);
    }

    public void setuserSubType(String userSubType) {
        editor.putString(KEY_userSubType, userSubType).commit();
    }

    public String getuserSubType() {
        return pref.getString(KEY_userSubType, null);
    }

    //Create login session
    public void createUserLoginSession(String psno, String psname, String mno, String userType) {
        // Storing login value as TRUE

        editor.putBoolean(IS_USER_LOGIN, true);

        // Storing name in pref
        editor.putString(KEY_psName, psname);
        editor.putString(KEY_MOBILE_NUMBER, mno);

//         Storing psno in pref
        editor.putString(KEY_psNo, psno);
        editor.putString(KEY_userType, userType);

        // commit changes
        editor.commit();
    }

    //Create login session
    public void createUserLoginSessionUsingMobile(String mno, String email) {
        // Storing login value as TRUE

        editor.putBoolean(IS_USER_LOGIN, true);

        editor.putString(KEY_MOBILE_NUMBER, mno);

        editor.putString(KEY_EMAIL, email);


        // commit changes
        editor.commit();
    }


    public boolean checkLogin() {
        // Check login status
        if (!this.isUserLoggedIn()) {

            // user is not logged in redirect him to LoginWithMobile Activity
            Intent i = new Intent(_context, LoginWithMobile.class);
            // Closing all the Activities from stack
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // Staring LoginWithMobile Activity
            _context.startActivity(i);

            return true;
        }
        return false;
    }


    /**
     * Get stored session data
     */
    public HashMap<String, String> getUserDetails() {

        //Use hashmap to store user credentials
        HashMap<String, String> user = new HashMap<String, String>();

        // user name
        user.put(KEY_psNo, pref.getString(KEY_psNo, null));
        user.put(KEY_psName, pref.getString(KEY_psName, null));
        user.put(KEY_userType, pref.getString(KEY_userType, null));

        // user email id
//        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));

        // return user
        return user;
    }


    /**
     * Clear session details
     */
    public void logoutUser() {

        // Clearing all user data from Shared Preferences

        editor.clear();
        editor.commit();

        // After logout redirect user to Login Activity
        Intent i = new Intent(_context, LoginWithMobile.class);

        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // Staring Login Activity
        _context.startActivity(i);
    }


    // Check for login
    public boolean isUserLoggedIn() {
        return pref.getBoolean(IS_USER_LOGIN, false);
    }
}