// SensorDataCallback.aidl
package com.gandiva.aidl.remoteservices;

import com.gandiva.aidl.remoteservices.model.SensorData;

interface SensorDataCallback {

    /**
     * in, out, and inout are direction specifiers used in AIDL (Android Interface Definition Language)
     * to indicate the direction of data transfer for method parameters.
     *
     * in: The parameter is an input parameter and data is transferred from the client to the server.
     * This means that the value of the parameter is passed from the client to the server, but any modifications to the
     * parameter's value on the server are not passed back to the client.
     *
     * out: The parameter is an output parameter and data is transferred from the server to the client.
     * This means that the value of the parameter is initially undefined on the client, but the server sets a value,
     * which is then passed back to the client.
     *
     * inout: The parameter is both an input and output parameter, and data is transferred in both directions.
     * This means that the value of the parameter is initially passed from the client to the server,
     * and the server may modify the value and pass it back to the client.
     *
     */
    void onEvent(in SensorData data);
}