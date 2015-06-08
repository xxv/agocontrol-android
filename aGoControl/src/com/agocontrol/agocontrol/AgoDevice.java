package com.agocontrol.agocontrol;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.UUID;

public class AgoDevice {
	UUID uuid;
	String name;
	String deviceType;
    String deviceLevel;
    String handledBy;
    String units;
    String lastseen;
    String deviceInternalID;
	int status;
    ArrayList deviceValues;
	AgoConnection connection;


	
	public AgoDevice(UUID _uuid) {
		name = "";
		uuid = _uuid;
		deviceType = "switch";
		status = 0;
	}
	
	public AgoDevice(UUID _uuid, String _deviceType) {
		name = "";
		uuid = _uuid;
		deviceType = _deviceType;
		status = 0;
	}
		
	public AgoDevice(UUID _uuid, String _deviceType, String _name) {
		name = _name;
		uuid = _uuid;
		deviceType = _deviceType;
		status = 0;
	}
	public AgoDevice(UUID _uuid, String _deviceType, String _name, int _status) {
		name = _name;
		uuid = _uuid;
		deviceType = _deviceType;
		status = _status;
	}
    public AgoDevice(UUID _uuid, String _deviceType, String _name, int _status, String _deviceLevel) {
        name = _name;
        uuid = _uuid;
        deviceType = _deviceType;
        deviceLevel = _deviceLevel;
        status = _status;
    }

    public AgoDevice(UUID _uuid, String _deviceType, String _name, int _status, String _deviceLevel, String _handledBy) {
        name = _name;
        uuid = _uuid;
        deviceType = _deviceType;
        deviceLevel = _deviceLevel;
        status = _status;
        handledBy = _handledBy;
    }

    public AgoDevice(UUID _uuid, String _deviceType, String _name, int _status, String _deviceLevel, String _handledBy, String _units) {
        name = _name;
        uuid = _uuid;
        deviceType = _deviceType;
        deviceLevel = _deviceLevel;
        status = _status;
        handledBy = _handledBy;
        units = _units;
    }

    public AgoDevice(UUID _uuid, String _deviceType, String _name, int _status, String _handledBy, String _deviceInternalID, String _lastseen, ArrayList _deviceValues) {
        name = _name;
        uuid = _uuid;
        deviceType = _deviceType;
        status = _status;
        handledBy = _handledBy;
        lastseen = _lastseen;
        deviceInternalID = _deviceInternalID;
        deviceValues = _deviceValues;
    }

    public int getStatus() { return status; }
    public String getDeviceType() { return deviceType; }
    public String getName() { return name; }
    public String getDeviceLevel() {return deviceLevel; }
    public String getLastseen() {return lastseen; }
    public String getHandledBy() {return handledBy; }
    public String units() {return units; }
    public String getDeviceInternalID() {return deviceInternalID; }
    public ArrayList deviceValues() {return deviceValues; }
    public UUID getUuid() { return uuid; }
    public AgoConnection getConnection() { return connection; }
    public void setUuid(UUID _uuid) { uuid = _uuid; }
    public void setStatus(int _status) { status = _status; }
    public void setName(String _name) { name = _name; }
    public void setDeviceType(String _deviceType) { deviceType = _deviceType; }
    public void setDeviceLevel(String _deviceLevel) { deviceLevel = _deviceLevel; }
    public void setLastseen(String _lastseen) { lastseen = _lastseen; }
    public void setHandledBy(String _handledBy) { handledBy = _handledBy; }
    public void setConnection(AgoConnection _connection) { connection = _connection; }
    public void setDeviceValues(ArrayList _deviceValues) {deviceValues = _deviceValues; }
	
	@Override
	public String toString() {
		return name;
		
	}

}
