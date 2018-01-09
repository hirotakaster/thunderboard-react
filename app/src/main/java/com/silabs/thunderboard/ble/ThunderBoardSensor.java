package com.silabs.thunderboard.ble;

/**
 * Created by niisato on 2018/01/05.
 */

public abstract class ThunderBoardSensor {
    public Boolean isNotificationEnabled;
    public Boolean isSensorDataChanged = false;

    public abstract ThunderboardSensorData getSensorData();
}