package com.ecosense.app.pojo.eventBus;

import com.ecosense.app.Traccar.Position;

/**
 * <h1>POJO class to notify route activity on timeUpdate</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class CoordinateUpdateMessageEvent {
    private Position position;
    private boolean isPersisted;

    public CoordinateUpdateMessageEvent(Position position, boolean isPersisted) {
        this.position = position;
        this.isPersisted = isPersisted;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public boolean isPersisted() {
        return isPersisted;
    }

    public void setPersisted(boolean persisted) {
        isPersisted = persisted;
    }
}
