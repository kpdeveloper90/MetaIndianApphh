//package com.ecosense.app.pojo.response;
//
//import androidx.annotation.NonNull;
//
//import com.google.gson.annotations.SerializedName;
//
//import com.ecosense.app.pojo.model.User;
//
///**
// * <h1>POJO class representing a user authentication response</h1>
// * <p>
// * Copyright 2021 Vivekanand Mishra.
// * All rights reserved.
// *
// * @author Vivekanand Mishra
// * @version 1.0
// */
//public class UserResponse extends BackendResponse {
//    //    {
////        "code": 200,
////            "message": "Login success",
////            "data": {
////        "_id": "603e4c9a43633e4148f1b049",
////                "name": "vivek12",
////                "email": "vivek@gmail.com"
////    }
////    }
//    @SerializedName("data")
//    @NonNull
//    private User user;
//
//    public UserResponse(int code, @NonNull String message, @NonNull User user) {
//        super(code, message);
//        this.user = user;
//    }
//
//    @NonNull
//    public User getUser() {
//        return user;
//    }
//
//    public void setUser(@NonNull User user) {
//        this.user = user;
//    }
//}
