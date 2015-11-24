package com.agocontrol.agocontrol;

import android.util.Base64;
import android.util.Log;

import org.alexd.jsonrpc.JSONRPCClient;
import org.alexd.jsonrpc.JSONRPCException;
import org.alexd.jsonrpc.JSONRPCParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

public class AgoConnection {
    private static final String TAG = AgoConnection.class.getSimpleName();

    String host;
    String port;
    String inventory;
    Double schemaVersion;
    ArrayList<AgoDevice> deviceList;
    JSONRPCClient client;
    public static String RESULT = "result";
    public static String RESULT_DATA = "data";
    public static String SCHEMA = "schema";
    public static String SCHEMA_VERSION = "version";
    public static String DEVICES = "devices";
    public static String ROOMS = "rooms";
    public static String DEVICE_TYPE = "devicetype";
    public static String DEVICE_ROOM = "room";
    public static String DEVICE_STATE = "state";
    public static String DEVICE_NAME = "name";
    public static String DEVICE_TYPE_EVENT = "event";

    public AgoConnection(final String host, final String port) {
        this.host = host;
        this.port = port;
        Log.i(TAG, host + ':' + port);
        client = JSONRPCClient.create("http://" + host + ':' + port + "/jsonrpc", JSONRPCParams.Versions.VERSION_2);
        client.setConnectionTimeout(2000);
        client.setSoTimeout(2000);
    }

    public ArrayList<AgoDevice> getDeviceList() throws JSONRPCException, JSONException {
        if (deviceList == null) {
            getDevices();
        }

        return deviceList;
    }

    public void getDevices() throws JSONException, JSONRPCException {
        inventory = getInventory();
        JSONObject inv = new JSONObject(inventory);
        Log.i(AgoConnection.class.getName(),
                "Number of entries " + inv.length());
        // Log.i(AgoConnection.class.getName(), "Contents: " + inv);

        if (inv.has(RESULT_DATA)) {
            // new style reply
            inv = inv.getJSONObject(RESULT_DATA);
            Log.i(AgoConnection.class.getName(), "Newresult number of entries: " + inv.length());
        }
        // schema = data.getJSONObject(SCHEMA);

        final JSONObject schema = inv.getJSONObject(SCHEMA);

        schemaVersion = schema.getDouble(SCHEMA_VERSION);
        Log.d(TAG, "schema version: " + schemaVersion);

        final JSONObject devices = inv.getJSONObject(DEVICES);
        Log.i(AgoConnection.class.getName(),
                "Number of devices: " + devices.length());
        final Iterator<?> iter = devices.keys();
        final ArrayList<AgoDevice> newDeviceList = new ArrayList<>();
        while (iter.hasNext()) {
            final String deviceUuid = (String) iter.next();
            // Log.d(TAG, "UUid: " + deviceUuid);
            final JSONObject device = devices.getJSONObject(deviceUuid);
            final String deviceType = device.getString(DEVICE_TYPE);
            final String deviceName = device.getString(DEVICE_NAME);
            final String deviceRoom = device.getString(DEVICE_ROOM);

            if (deviceName != null && deviceName.length() > 0
                    && !deviceType.equals(DEVICE_TYPE_EVENT)) {
                String roomName = null;
                final JSONObject rooms = inv.getJSONObject(ROOMS);
                final Iterator<?> roomit = rooms.keys();

                while (roomit.hasNext()) {
                    final String roomUuid = (String) roomit.next();
                    if (roomUuid.equals(deviceRoom)) {
                        // Log.d(TAG, "matched room");
                        final JSONObject room = rooms.getJSONObject(roomUuid);
                        roomName = room.getString(DEVICE_NAME);
                    }
                }
                final UUID tmpUuid = UUID.fromString(deviceUuid);
                final AgoDevice newDevice = new AgoDevice(tmpUuid, deviceType);
                if (roomName != null && roomName.length() > 0) {
                    newDevice.setName(roomName + " - " + deviceName);
                } else {
                    newDevice.setName(deviceName);
                }
                newDevice.setConnection(this);
                newDeviceList.add(newDevice);
            }
        }
        deviceList = newDeviceList;
    }

    public void sendCommand(final UUID uuid, final String command) throws JSONRPCException, JSONException {
        final JSONObject agocommand = new JSONObject();
        agocommand.put("command", command);
        agocommand.put("uuid", uuid.toString());
        final JSONObject params = new JSONObject();
        params.put("content", agocommand);
        final JSONObject result = client.callJSONObject("message", params);
    }

    public void sendEvent(final String subject, final String data) throws JSONRPCException,
            JSONException {
        final JSONObject agoevent = new JSONObject();
        agoevent.put("data", data);
        final JSONObject params = new JSONObject();
        params.put("content", agoevent);
        params.put("subject", subject);
        final JSONObject result = client.callJSONObject("message", params);

    }

    public void setDeviceLevel(final UUID uuid, final String level) throws JSONRPCException,
            JSONException {
        final JSONObject agocommand = new JSONObject();
        agocommand.put("command", "setlevel");
        agocommand.put("level", level);
        agocommand.put("uuid", uuid.toString());
        final JSONObject params = new JSONObject();
        params.put("content", agocommand);
        final JSONObject result = client.callJSONObject("message", params);
    }

    public byte[] getVideoFrame(final UUID uuid) throws JSONRPCException, JSONException {
        final JSONObject agocommand = new JSONObject();
        agocommand.put("command", "getvideoframe");
        agocommand.put("uuid", uuid.toString());
        final JSONObject params = new JSONObject();
        params.put("content", agocommand);

        final String frame;

        final JSONObject result = client.callJSONObject("message", params);
        if (result.has(RESULT_DATA)) {
            final JSONObject data = result.getJSONObject(RESULT_DATA);
            frame = data.getString("image");
        } else {
            frame = result.getString("image");
        }
        return Base64.decode(frame, Base64.DEFAULT);
    }


    private String getInventory() throws JSONRPCException, JSONException {
        final StringBuilder builder = new StringBuilder();
        final JSONObject command = new JSONObject();
        command.put("command", "inventory");
        final JSONObject params = new JSONObject();
        params.put("content", command);
        final JSONObject result = client.callJSONObject("message", params);
        builder.append(result);
        return builder.toString();
    }

}
