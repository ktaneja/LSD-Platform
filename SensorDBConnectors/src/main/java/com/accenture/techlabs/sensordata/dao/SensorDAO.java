package com.accenture.techlabs.sensordata.dao;

import java.util.Calendar;

public interface SensorDAO {
	public boolean createTable(String deviceID, SensorDataType datatype);
	public boolean addData(String deviceID, SensorObservationData data);
	public SensorObservationData getData(String deviceID, Calendar from, Calendar to);
}
