package com.ecosense.app.helper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Connection {

    public Connection() {
    }

    //    public static String ip = "206.189.134.136:8080";
//    http://203.109.125.77:7070/api/method/login?usr=Administrator&pwd=admin
    public static final String Default_language = "en";


    public static final String AppDefaultUser = "Guest";
    public static final String AppDefaultEMail = "guest@gmial.com";
    public static final String AppDefaultMobile = "0000000000";
    public static final String AppInstall_1stTime = "Yes";

//    kainext.com:8005

    public static int OCTET1 = 159;
    public static int OCTET2 = 89;
    public static int OCTET3 = 164;
    public static int OCTET4 = 145;
    public static int PORT = 80;
    public static String ip = OCTET1 + "." + OCTET2 + "." + OCTET3 + "." + OCTET4 + ":" + PORT;

    //    public static final String projectName = "/api/resource/Visitor_Management/";
//    public static String MY_SERVER_IP = "http://" + ip;
    public static String MY_SERVER_IP = "http://3.12.23.25";
    public static String MY_SERVER_METHOD = "/api/method/erpnext.accounts.doctype.account.account.";


    /**
     * Traccar Value Setup
     **/
//    public static String Traccar_url_value="http://192.168.29.218:5055";
//    public static String Traccar_url_value="http://206.189.134.136:5444";
    public static String Traccar_url_value = "http://3.12.23.25:8082/";
    public static String KEY_DEVICE_value = "123456";
    public static String KEY_INTERVAL_value = "1";
    public static String KEY_ANGLE_value = "0";
    public static String KEY_DISTANCE_value = "0";
    public static String KEY_ACCURACY_value = "high";
    public static Boolean KEY_STATUS_value = true;

    public static String API_OTP_SEND = "https://api.msg91.com/api/v5/";

    // server URL configuration
    public static final String URL_REQUEST_SMS = "http://control.msg91.com/api/sendotp.php";
    public static final String URL_VERIFY_OTP = "http://192.168.0.101/android_sms/msg91/verify_otp.php";

    // SMS provider identification
    // It should match with your SMS gateway origin
    // You can use  MSGIND, TESTER and ALERTS as sender ID
    // If you want custom sender Id, approve MSG91 to get one
    public static final String SMS_ORIGIN = "ANHIVE";

    // special character to prefix the otp. Make sure this character appears only once in the sms
    public static final String OTP_DELIMITER = ":";

    public static String getCurrentDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public static String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    public static String getCurrentDate_dd_mm_yyyy() {
        return new SimpleDateFormat("dd-MM-yyyy").format(new Date());
    }

    public static String getCurrentDateTime12Hours() {
        return new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a").format(new Date());
    }

    public static String encodeBitmapToFromBase64(Bitmap bitmap) {
        String encodedImage = "";

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
//        int flags = Base64.DEFAULT | Base64.NO_PADDING | Base64.NO_WRAP;
//        encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
        try {
//            encodedImage = "data:image/jpeg;base64,"+ URLEncoder.encode(Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT), "UTF-8");
            encodedImage = "data:image/jpeg;base64," + Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
//        } catch (UnsupportedEncodingException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }

        return encodedImage;
    }

    public static Bitmap decodeFromBase64ToBitmap(String base64String) {
        String base64Image = base64String.split(",")[1];
        byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        return decodedByte;
    }
    public static final int MY_SOCKET_TIMEOUT_MS = 5000;
    private static final String DIRECTION_API = "https://maps.googleapis.com/maps/api/directions/json?origin=";
//    public static final String API_KEY = "AIzaSyB2iEMpmZN50OEgD4txYrmvDbDzRAvieSs";
    public static final String API_KEY = "AIzaSyBupLYWAq0aKBZp7IhB6iCoRs2msS3xbjw";
    public static String getUrl(String originLat, String originLon, String destinationLat, String destinationLon){
        return Connection.DIRECTION_API + originLat+","+originLon+"&destination="+destinationLat+","+destinationLon+"&key="+API_KEY;
    }
}
