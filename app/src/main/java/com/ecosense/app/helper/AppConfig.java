package com.ecosense.app.helper;

public class AppConfig {
    public static final String TAG = "gplaces";
    //        e.g. [Like, Dislike, Share, Favourites, Volunteer, Directions, AddToCalendar]
    public static final int PROGRASS_postDelayed = 2000;
    public final static String APP_CAPTUREIMAGE_DIR = "DWMS";
    public static final String KEY_DEVICE = "id";
    public static final String KEY_URL = "url";
    public static final String KEY_INTERVAL = "interval";
    public static final String KEY_DISTANCE = "distance";
    public static final String KEY_ANGLE = "angle";
    public static final String KEY_ACCURACY = "accuracy";
    public static final String KEY_STATUS = "status";

    public static final String API_SEND_OTP = "otp";
    public static final String API_VERIFY_OTP = "otp/verify";

    public static final String API_vehicle_deployed_all = "vehicle_deployed_all";
    public static final String API_vehicle_not_deployed = "vehicle_not_deployed";
    public static final String API_vehicle_deployed = "vehicle_deployed";
    public static final String API_bin_status = "bin_status";
    public static final String API_ward_no = "ward_no";
    public static final String API_complaint_dashboard_indv = "complaint_dashboard_indv";
    public static final String API_route_info = "route_info";
    public static final String API_bin_cleared_filter = "bin_cleared_filter";
    public static final String API_route_coverage_summary = "route_coverage_summary";
    public static final String API_vehicle_type = "vehicle_type";
    public static final String API_reason = "reason";
    public static final String API_driver_not_assigned = "driver_not_assigned";
    public static final String API_supervisordetails = "supervisordetails";
    public static final String API_notification = "notification";


    public static final String PLATFORM = "Android";
    public static final String ITEM_GROUP_Fixed_Asset = "Fixed Asset";
    public static final String ITEM_stock_uom = "Nos";

    public static final String Vehicle_deployed_status = "Deployed";
    public static final String Vehicle_not_deployed_status = "Not Deployed";
    public static final String Voucher_status_Used = "Used";
    public static final String Voucher_status_Unused = "Unused";
    public static final String Voucher_status_Pending = "Pending";

    public static final String item_code_Vehicle = "Vehicle";
    public static final String item_code_BIN = "BIN";
    public static final String FA_Toilet = "Toilet";

    public static final String fatype_Asset = "Asset";
    public static final String Active_Status = "Active";
    public static final String Left_Status = "Left";
    public static final String Deactive_Status = "Deactive";
    public static final String Complaints_Status_Active = "Active";
    public static final String Complaints_Status_Deactive = "Deactive";
    public static final String ROUTE_Status_In_Progress = "In-Progress";
    public static final String ROUTE_Status_Complete = "Complete";
    public static final String ROUTE_Status_Pending = "Pending";

    public static final String Expense_Status_Approved = "Approved";
    public static final String Expense_Status_Reject = "Reject";
    public static final String Expense_Status_Draft = "Draft";


    public static final String BIN_Status_Pending = "Pending";
    public static final String BIN_Status_Clean = "Clean";
    public static final String BIN_Status_Scheduled = "Scheduled";
    public static final String BIN_Status_Submitted = "Submitted";

    public static final String Team_Login_Yes = "Yes";
    public static final String Team_Login_No = "No";
    public static final String UType_Citizen = "Citizen";
    public static final String UType_Team = "Team";
    public static final String USubType_Individual = "Individual";
    public static final String USubType_Corporate = "Corporate";
    public static final String USubType_Driver = "Driver";
    public static final String USubType_Data_Collector = "Data Collector";
    public static final String USubType_Supervisor = "Supervisor";
    public static final String Uaacttype_Like = "Like";
    public static final String Uaacttype_Dislike = "Dislike";
    public static final String Uaacttype_Share = "Share";
    public static final String Uaacttype_Favourites = "Favourites";
    public static final String Uaacttype_Volunteer = "Volunteer";
    public static final String Uaacttype_Directions = "Directions";
    public static final String Uaacttype_AddToCalendar = "AddToCalendar";

    public static final String Uaartcltype_News = "News";
    public static final String Uaartcltype_Events = "Events";
    public static final String Uaartcltype_How_Tos = "HOW_TOs";
    public static final String Uaartcltype_Survey = "RoutePoint";
    public static final String Uaplatform_Android = "Android";

    public static final String CPTSTATUS_New = "New";
    public static final String CPTSTATUS_Noted = "Noted";
    public static final String CPTSTATUS_Pending = "Pending";
    public static final String CPTSTATUS_Complete = "Complete";
    public static final String CPTSTATUS_In_Process = "In-Process";

    public static final String RESULTS = "results";
    public static final String STATUS = "status";

    public static final String OK = "OK";
    public static final String ZERO_RESULTS = "ZERO_RESULTS";
    public static final String REQUEST_DENIED = "REQUEST_DENIED";
    public static final String OVER_QUERY_LIMIT = "OVER_QUERY_LIMIT";
    public static final String UNKNOWN_ERROR = "UNKNOWN_ERROR";
    public static final String INVALID_REQUEST = "INVALID_REQUEST";

    //    Key for nearby places json from google
    public static final String GEOMETRY = "geometry";
    public static final String LOCATION = "location";
    public static final String LATITUDE = "lat";
    public static final String LONGITUDE = "lng";
    public static final String ICON = "icon";
    public static final String SUPERMARKET_ID = "id";
    public static final String NAME = "name";
    public static final String PLACE_ID = "place_id";
    public static final String REFERENCE = "reference";
    public static final String VICINITY = "vicinity";
    public static final String RATING = "rating";


    // remember to change the browser api key
    public static final String GOOGLE_BROWSER_API_KEY =
            "AIzaSyCZ12OHkGQQVXzhQIE99pu73OietPlkjQM";
    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final int PROXIMITY_RADIUS = 9000;
    // The minimum distance to change Updates in meters
    public static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    // The minimum time between updates in milliseconds
    public static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    //added by Vivekanand Mishra
    public static final String KEY_CURRENT_ROUTE_NAME = "current_route_name";
    public static final String KEY_CURRENT_CLIENT_ROUTE_ID = "current_client_route_id";
    public static final String KEY_CURRENT_VEHICLE_ID = "current_vehicle_id";
    public static final String KEY_CURRENT_VEHICLE_NUMBER = "current_vehicle_number";
    public static final String KEY_CURRENT_ZONE_ID = "current_zone_id";
    public static final String KEY_CURRENT_ZONE_NUMBER = "current_zone_number";
    public static final String KEY_CURRENT_WARD_ID = "current_ward_id";
    public static final String KEY_CURRENT_WARD_NUMBER = "current_ward_number";

    public static final String KEY_PLOTTING_TIMER_RUNNING = "plotting_timer_running";
    public static final String KEY_PLOTTING_START_TIME = "plotting_start_time";
    public static final String KEY_PLOTTING_TOTAL_TIME = "plotting_total_time";
}