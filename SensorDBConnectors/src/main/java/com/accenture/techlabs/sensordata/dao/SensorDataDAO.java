package com.accenture.techlabs.sensordata.dao;

import java.util.Calendar;

import com.accenture.techlabs.sensordata.model.DeviceObservationData;
import com.accenture.techlabs.sensordata.model.DeviceDataType;

public interface SensorDataDAO {
	public boolean createTable(String deviceID, DeviceDataType datatype);
	public boolean addData(String deviceID, DeviceObservationData data);
	public DeviceObservationData getData(String deviceID, Calendar from, Calendar to);
}
