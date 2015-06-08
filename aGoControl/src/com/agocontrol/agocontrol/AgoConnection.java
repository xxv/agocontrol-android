package com.agocontrol.agocontrol;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import org.alexd.jsonrpc.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Base64;
import android.util.Log;

public class AgoConnection {
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
    public static String LASTSEEN = "lastseen";
    public static String HANDLEDBY = "handled-by";
	public static String DEVICE_ROOM = "room";
	public static String DEVICE_STATE = "state";
	public static String DEVICE_NAME = "name";
    public static String DEVICE_INTERNALID = "internalid";

	public AgoConnection(String host, String port) {
		this.host = host;
		this.port = port;
		System.out.println(host + ":" + port);
		client = JSONRPCClient.create("http://" + host + ":" + port + "/jsonrpc", JSONRPCParams.Versions.VERSION_2);
		client.setConnectionTimeout(2000);
		client.setSoTimeout(2000);
		deviceList = new ArrayList<AgoDevice>();
		getDevices();
	}
	
	public ArrayList<AgoDevice> getDeviceList() {
//        System.out.println("here are devices: " + deviceList);
		return deviceList;
	}
	
	public void getDevices() {
		inventory = getInventory();
		try {
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

            JSONObject schema = inv.getJSONObject(SCHEMA);

            schemaVersion = schema.getDouble(SCHEMA_VERSION);
            System.out.println("schema version: " + schemaVersion);
		      
            JSONObject devices = inv.getJSONObject(DEVICES);
            Log.i(AgoConnection.class.getName(),
			          "Number of devices: " + devices.length());
            Iterator<?> iter = devices.keys();
            while (iter.hasNext()) {
                ArrayList deviceValues = new ArrayList();
                String deviceUuid = (String)iter.next();
                // System.out.println("UUid: " + deviceUuid);
                JSONObject device = devices.getJSONObject(deviceUuid);
                String deviceType = device.getString(DEVICE_TYPE);
                String deviceName = device.getString(DEVICE_NAME);
                String deviceRoom = device.getString(DEVICE_ROOM);
                String handledBy = device.getString(HANDLEDBY);
                String lastseen = device.getString(LASTSEEN);
                String deviceInternalID = device.getString(DEVICE_INTERNALID);
                String deviceUnits = "0";

                if (device.getJSONObject("values").has("temperature")) {
                    double tempLevel = device.getJSONObject("values").getJSONObject("temperature").getDouble("level");
                    deviceValues.add(String.valueOf(tempLevel));
                    deviceUnits = device.getJSONObject("values").getJSONObject("temperature").getString("unit");
                    if ((deviceUnits.equalsIgnoreCase("degF")) || (deviceUnits.equalsIgnoreCase("F"))) {
                        deviceUnits = "\u00b0" + "F";
                    } else if ((deviceUnits.equalsIgnoreCase("degC")) || (deviceUnits.equalsIgnoreCase("C"))) {
                        deviceUnits = "\u00b0" + "C";
                    }
                    deviceValues.add(deviceUnits);
                }

                if (device.getJSONObject("values").has("humidity")) {
                    deviceValues.add(device.getJSONObject("values").getJSONObject("humidity").getDouble("level"));
                    deviceValues.add(device.getJSONObject("values").getJSONObject("humidity").getString("unit"));
                }

                if (device.getJSONObject("values").has("windspeed")) {
                    deviceValues.add(device.getJSONObject("values").getJSONObject("windspeed").getDouble("level"));
                    deviceValues.add(device.getJSONObject("values").getJSONObject("windspeed").getString("unit"));
                    deviceValues.add(device.getJSONObject("values").getJSONObject("direction").getString("level"));
                }

                if (device.getJSONObject("values").has("windchill")) {
                    deviceValues.add(device.getJSONObject("values").getJSONObject("windchill").getString("level"));
                    deviceUnits = device.getJSONObject("values").getJSONObject("windchill").getString("unit");
                    if ((deviceUnits.equalsIgnoreCase("degF")) || (deviceUnits.equalsIgnoreCase("F"))) {
                        deviceUnits = "\u00b0" + "F";
                    } else if ((deviceUnits.equalsIgnoreCase("degC")) || (deviceUnits.equalsIgnoreCase("C"))) {
                        deviceUnits = "\u00b0" + "C";
                    }
                    deviceValues.add(deviceUnits);
                }

                if (device.getJSONObject("values").has("pressure")) {
                    deviceValues.add(device.getJSONObject("values").getJSONObject("pressure").getDouble("level"));
                    deviceValues.add(device.getJSONObject("values").getJSONObject("pressure").getString("unit"));
                }

                if (device.getJSONObject("values").has("visibility")) {
                    deviceValues.add(device.getJSONObject("values").getJSONObject("visibility").getDouble("level"));
                    deviceValues.add(device.getJSONObject("values").getJSONObject("visibility").getString("unit"));
                }

                if (device.getJSONObject("values").has("dewpoint")) {
                    deviceValues.add(device.getJSONObject("values").getJSONObject("dewpoint").getString("level"));
                    deviceUnits = device.getJSONObject("values").getJSONObject("dewpoint").getString("unit");
                    if ((deviceUnits.equalsIgnoreCase("degF")) || (deviceUnits.equalsIgnoreCase("F"))) {
                        deviceUnits = "\u00b0" + "F";
                    } else if ((deviceUnits.equalsIgnoreCase("degC")) || (deviceUnits.equalsIgnoreCase("C"))) {
                        deviceUnits = "\u00b0" + "C";
                    }
                    deviceValues.add(deviceUnits);
                }

                if (device.getJSONObject("values").has("error")) {
                    deviceValues.add("Error: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("error").getString("level"));
                }

                if (device.getJSONObject("values").has("fanspeed")) {
                    deviceValues.add("Fanspeed: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("fanspeed").getString("level"));
                }

                if (device.getJSONObject("values").has("filterflag")) {
                    deviceValues.add("Filter Flag: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("filterflag").getString("level"));
                }

                if (device.getJSONObject("values").has("mode")) {
                    deviceValues.add("Mode: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("mode").getString("level"));
                }

                if (device.getJSONObject("values").has("roomtemperature")) {
                    deviceValues.add("Room Temp: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("roomtemperature").getDouble("level"));
                }

                if (device.getJSONObject("values").has("setpointtemperature")) {
                    deviceValues.add("Setpoint: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("setpointtemperature").getDouble("level"));
                }

                if (device.getJSONObject("values").has("status")) {
                    deviceValues.add("Status: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("status").getString("level"));
                }

                if (device.getJSONObject("values").has("expansionvalve")) {
                    deviceValues.add("Expansion Valve: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("expansionvalve").getString("level"));
                }

                if (device.getJSONObject("values").has("gastemp")) {
                    deviceValues.add("Gas Temp: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("gastemp").getDouble("level"));
                }

                if (device.getJSONObject("values").has("liquidtemp")) {
                    deviceValues.add("Liquid Temp: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("liquidtemp").getDouble("level"));
                }

                if (device.getJSONObject("values").has("suctiontemp")) {
                    deviceValues.add("Suction Temp: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("suctiontemp").getDouble("level"));
                }

                if (device.getJSONObject("values").has("thermalon")) {
                    deviceValues.add("Thermal On: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("thermalon").getString("level"));
                }

                if (device.getJSONObject("values").has("setpointtemp")) {
                    deviceValues.add("Setpoint Temperature: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("setpointtemp").getDouble("level"));
                }

                if (device.getJSONObject("values").has("aet")) {
                    deviceValues.add("AET: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("aet").getDouble("level"));
                }

                if (device.getJSONObject("values").has("ambienttemp")) {
                    deviceValues.add("Ambient Temperature: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("ambienttemp").getDouble("level"));
                }

                if (device.getJSONObject("values").has("coilt")) {
                    deviceValues.add("Coil Temperature: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("coilt").getDouble("level"));
                }

                if (device.getJSONObject("values").has("condtemp")) {
                    deviceValues.add("Cond Temp: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("condtemp").getDouble("level"));
                }

                if (device.getJSONObject("values").has("coolmode")) {
                    deviceValues.add("Cool Mode: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("coolmode").getDouble("level"));
                }

                if (device.getJSONObject("values").has("ct1")) {
                    deviceValues.add("CT1: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("ct1").getDouble("level"));
                }

                if (device.getJSONObject("values").has("ct2")) {
                    deviceValues.add("CT2: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("ct2").getDouble("level"));
                }

                if (device.getJSONObject("values").has("current")) {
                    deviceValues.add("Current: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("current").getDouble("level"));
                }

                if (device.getJSONObject("values").has("distinv")) {
                    deviceValues.add("Dist Inv: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("distinv").getDouble("level"));
                }

                if (device.getJSONObject("values").has("diststd1")) {
                    deviceValues.add("Dist Std 1: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("diststd1").getDouble("level"));
                }

                if (device.getJSONObject("values").has("diststd2")) {
                    deviceValues.add("Dist Std 2: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("diststd2").getDouble("level"));
                }

                if (device.getJSONObject("values").has("ev1")) {
                    deviceValues.add("EV1: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("ev1").getDouble("level"));
                }

                if (device.getJSONObject("values").has("ev2")) {
                    deviceValues.add("EV2: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("ev2").getDouble("level"));
                }

                if (device.getJSONObject("values").has("evaptemp")) {
                    deviceValues.add("Evap Temperature: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("evaptemp").getDouble("level"));
                }

                if (device.getJSONObject("values").has("fan")) {
                    deviceValues.add("Fan: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("fan").getString("level"));
                }

                if (device.getJSONObject("values").has("heatmode")) {
                    deviceValues.add("Heat Mode: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("heatmode").getString("level"));
                }

                if (device.getJSONObject("values").has("horsepower")) {
                    deviceValues.add("Horsepower: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("horsepower").getDouble("level"));
                }

                if (device.getJSONObject("values").has("inv")) {
                    deviceValues.add("Inv: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("inv").getDouble("level"));
                }

                if (device.getJSONObject("values").has("invc")) {
                    deviceValues.add("Inv Comp: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("invc").getDouble("level"));
                }

                if (device.getJSONObject("values").has("invfan")) {
                    deviceValues.add("Inv Fan: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("invfan").getDouble("level"));
                }

                if (device.getJSONObject("values").has("invt")) {
                    deviceValues.add("Inv Temp: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("invt").getDouble("level"));
                }

                if (device.getJSONObject("values").has("ir")) {
                    deviceValues.add("Inv Revolutions: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("ir").getDouble("level"));
                }

                if (device.getJSONObject("values").has("oilreturn")) {
                    deviceValues.add("Oil Return: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("oilreturn").getDouble("level"));
                }

                if (device.getJSONObject("values").has("rliqt")) {
                    deviceValues.add("R Liq Temp: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("rliqt").getDouble("level"));
                }

                if (device.getJSONObject("values").has("standby")) {
                    deviceValues.add("Standby: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("standby").getString("level"));
                }

                if (device.getJSONObject("values").has("std1")) {
                    deviceValues.add("Std 1: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("std1").getDouble("level"));
                }

                if (device.getJSONObject("values").has("std2")) {
                    deviceValues.add("Std 2: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("std2").getDouble("level"));
                }

                if (device.getJSONObject("values").has("targetcond")) {
                    deviceValues.add("Target Cond Temp: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("targetcond").getDouble("level"));
                }

                if (device.getJSONObject("values").has("targetevap")) {
                    deviceValues.add("Target Evap Temp: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("targetevap").getDouble("level"));
                }

                if (device.getJSONObject("values").has("totalhp")) {
                    deviceValues.add("Total HP: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("totalhp").getDouble("level"));
                }

                if (device.getJSONObject("values").has("ts")) {
                    deviceValues.add("TS: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("ts").getDouble("level"));
                }

                if (device.getJSONObject("values").has("ventmode")) {
                    deviceValues.add("Vent Mode: ");
                    deviceValues.add(device.getJSONObject("values").getJSONObject("ventmode").getString("level"));
                }

                if (deviceName != null && deviceName.length() > 0 && !deviceType.equals("event")) {
                    String roomName = null;
                    JSONObject rooms = inv.getJSONObject(ROOMS);
                    Iterator<?> roomit = rooms.keys();

                    while (roomit.hasNext()) {
                        String roomUuid = (String)roomit.next();
                        if (roomUuid.equals(deviceRoom)) {
                            // System.out.println("matched room");
                            JSONObject room = rooms.getJSONObject(roomUuid);
                            roomName = room.getString(DEVICE_NAME);
                        }
                    }

                    UUID tmpUuid = UUID.fromString(deviceUuid);
                    AgoDevice newDevice = new AgoDevice(tmpUuid, deviceType, deviceName, 0, handledBy, deviceInternalID, lastseen, deviceValues);
                    if (roomName != null && roomName.length() > 0) {
                        newDevice.setName(roomName + " - " + deviceName);
                    } else {
                        newDevice.setName(deviceName);
                    }
                    newDevice.setConnection(this);
                    deviceList.add(newDevice);
                }

            }
		     
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public boolean sendCommand(UUID uuid, String command) {
	    try {
	    	JSONObject agocommand = new JSONObject();
	    	agocommand.put("command", command);
	    	agocommand.put("uuid", uuid.toString());
	    	JSONObject params = new JSONObject();
	    	params.put("content", agocommand); 
	    	JSONObject result = client.callJSONObject("message", params);
	    	return true;
	    } catch (JSONRPCException e) {
	    	  e.printStackTrace();
	    } catch (JSONException e) {
	    		e.printStackTrace();
		}
		return false;
	}
	
	public boolean sendEvent(String subject, String data) {
		try {
	    	JSONObject agoevent = new JSONObject();
	    	agoevent.put("data", data);
	    	JSONObject params = new JSONObject();
	    	params.put("content", agoevent);
	    	params.put("subject", subject); 
	    	JSONObject result = client.callJSONObject("message", params);
	    	return true;
	    } catch (JSONRPCException e) {
	    	  e.printStackTrace();
	    } catch (JSONException e) {
	    		e.printStackTrace();
		}
		return false;
	
	}
	
	public boolean setDeviceLevel(UUID uuid, String level) {
	    try {
	    	JSONObject agocommand = new JSONObject();
	    	agocommand.put("command", "setlevel");
	    	agocommand.put("level", level);
	    	agocommand.put("uuid", uuid.toString());
	    	JSONObject params = new JSONObject();
	    	params.put("content", agocommand); 
	    	JSONObject result = client.callJSONObject("message", params);
	    	return true;
	    } catch (JSONRPCException e) {
	    	  e.printStackTrace();
	    } catch (JSONException e) {
	    		e.printStackTrace();
		}
	    return false;
	}
	
	public byte[] getVideoFrame(UUID uuid) {
	    try {
	    	JSONObject agocommand = new JSONObject();
	    	agocommand.put("command", "getvideoframe");
	    	agocommand.put("uuid", uuid.toString());
	    	JSONObject params = new JSONObject();
	    	params.put("content", agocommand); 

            String frame;

            JSONObject result = client.callJSONObject("message", params);
            if (result.has(RESULT_DATA)) {
                JSONObject data = result.getJSONObject(RESULT_DATA);
                frame = data.getString("image");
            } else {
                frame = result.getString("image");
            }
            return Base64.decode(frame, Base64.DEFAULT);
	    } catch (JSONRPCException e) {
    	  e.printStackTrace();
	    } catch (JSONException e) {
    		e.printStackTrace();
	    }
		return null;
	}
	
	
	private String getInventory() {
	    StringBuilder builder = new StringBuilder();
	    try {
	    	JSONObject command = new JSONObject();
	    	command.put("command", "inventory");
	    	JSONObject params = new JSONObject();
	    	params.put("content", command); 
	    	JSONObject result = client.callJSONObject("message", params);
	    	builder.append(result.toString());
	    } catch (JSONRPCException e) {
	    	  e.printStackTrace();
	    } catch (JSONException e) {
	    		e.printStackTrace();
		}
	    return builder.toString();
	}

}
