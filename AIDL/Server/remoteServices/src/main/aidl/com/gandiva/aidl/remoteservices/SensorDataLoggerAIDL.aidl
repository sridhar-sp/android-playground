// SensorDataLoggerAIDL.aidl
package com.gandiva.aidl.remoteservices;

import com.gandiva.aidl.remoteservices.SensorDataCallback;

interface SensorDataLoggerAIDL {

    String getSpeedInKm();

    int getRPM();

    void startLogging(in SensorDataCallback callback);

    void stopLogging();

}