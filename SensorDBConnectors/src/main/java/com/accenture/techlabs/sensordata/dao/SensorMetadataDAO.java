package com.accenture.techlabs.sensordata.dao;

import com.accenture.techlabs.sensordata.model.SensorDevice;
import com.datastax.driver.core.DataType;

public interface SensorMetadataDAO {
	public void saveDeviceMetadata(SensorDevice device);
	public DataType getdataType(SensorDevice device);
	public SensorDevice getDevice(String device);
}
