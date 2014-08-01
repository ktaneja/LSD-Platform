package com.accenture.techlabs.sensordata.dao;

import java.util.Calendar;

import com.accenture.techlabs.sensordata.model.SensorDataType;
import com.accenture.techlabs.sensordata.model.SensorObservationData;

public interface SensorDataDAO {
	public boolean createTable(String deviceID, SensorDataType datatype);
	public boolean addData(String deviceID, SensorObservationData data);
	public SensorObservationData getData(String deviceID, Calendar from, Calendar to);
}
