package com.ecosense.app.pojo;

public class Notification {

    private long notificationId;
    private String notificationTitle,notification_desc,notification_DateTime,notification_read_status;
    private byte[] notification_img;


    public long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(long notificationId) {
        this.notificationId = notificationId;
    }

    public String getNotificationTitle() {
        return notificationTitle;
    }

    public void setNotificationTitle(String notificationTitle) {
        this.notificationTitle = notificationTitle;
    }

    public String getNotification_desc() {
        return notification_desc;
    }

    public void setNotification_desc(String notification_desc) {
        this.notification_desc = notification_desc;
    }

    public String getNotification_DateTime() {
        return notification_DateTime;
    }

    public void setNotification_DateTime(String notification_DateTime) {
        this.notification_DateTime = notification_DateTime;
    }

    public String getNotification_read_status() {
        return notification_read_status;
    }

    public void setNotification_read_status(String notification_read_status) {
        this.notification_read_status = notification_read_status;
    }

    public byte[] getNotification_img() {
        return notification_img;
    }

    public void setNotification_img(byte[] notification_img) {
        this.notification_img = notification_img;
    }
}
