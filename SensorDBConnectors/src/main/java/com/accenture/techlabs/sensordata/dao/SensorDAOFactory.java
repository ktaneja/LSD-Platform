package com.accenture.techlabs.sensordata.dao;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class SensorDAOFactory {
	public static final int SENSOR_TIMESERIES = 1;
	public static final int APP_METADATA = 2;
	public static final int SENSOR_BLOBDATA = 3;
	public static final int SENSOR_METADATA = 3;
	
	
	public static SensorDataDAO getSensorDAO(int type) { 
        if (type == SENSOR_TIMESERIES) {
            return new CassandraSensorDataDAO();
        } 
        else if (type == SENSOR_METADATA){
        	//return new JenaTdbDAO();
        }else {
            new NotImplementedException();
        }
		return null;
    }
}
