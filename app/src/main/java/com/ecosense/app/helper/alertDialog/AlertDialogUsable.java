package com.ecosense.app.helper.alertDialog;

/**
 * <h1>Interface declaring the contract for alert dialogs to make them usable(visible with listeners)</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public interface AlertDialogUsable {
    void show();

    void initListeners();
}
