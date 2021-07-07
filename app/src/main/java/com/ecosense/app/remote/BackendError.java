package com.ecosense.app.remote;

/**
 * <h1>Enum for backend errors</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public enum BackendError {
    /**
     * Token expired backend error.(jwt token expired)
     */
    TOKEN_EXPIRED, //jwt token expired
    /**
     * Invalid backend error.(request format is incorrect)
     */
    INVALID, //invalid request
    /**
     * Unsuccessful backend error.(api route not found)
     */
    UNSUCCESSFUL, //route not found
    /**
     * Parsing backend error.(json to object/model parsing error)
     */
    PARSING, // json to object parsing error
    /**
     * Network backend error. (no network connection)
     */
    NETWORK; // no network connection
}
