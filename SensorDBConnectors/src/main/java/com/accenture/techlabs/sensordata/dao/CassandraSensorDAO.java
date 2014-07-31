package com.accenture.techlabs.sensordata.dao;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import com.accenture.techlabs.sensordata.dao.SensorDataType.Type;


public class CassandraSensorDAO implements SensorDAO {
	
	private CassandraConnection connection;
	
	public CassandraSensorDAO(){
		connection = CassandraConnection.getConnection();
	}
	
	public boolean createTable(String deviceID, SensorDataType datatype) {
		String queryString = synthesizeCreateTableQuery(deviceID, datatype);
		connection.getSession().execute(queryString);
		return false;
	}

	private String synthesizeCreateTableQuery(String deviceID, SensorDataType datatype) {
		// CREATE TABLE sample_data ( sensor_id text, event_time timestamp, cpu_usage double, PRIMARY KEY(sensor_id, event_time) );
		Map<String, List<String>> memberMap = datatype.getMembers();
		Map<String, List<Type>> typeMap = datatype.getTypes();
		deviceID = deviceID.replaceAll("-", "");
		String queryString = "CREATE TABLE " + deviceID + " ( sensor_id text, event_time timestamp, ";
		for (String sensor : typeMap.keySet()) {
			String column_name = sensor;
			List<String> members = memberMap.get(sensor);
			List<Type> types = typeMap.get(sensor);
			
			for (int i=0; i<types.size(); i++) {
				String m = members.get(i);
				Type t = types.get(i);
				if(m != null)
					column_name = column_name + "_" +m;
				queryString = queryString + column_name + " " + t.toString().toLowerCase() + ", ";
			}
		}
		queryString = queryString + "PRIMARY KEY(sensor_id, event_time) );";
		
		return queryString;
	}

	public boolean addData(String deviceID, SensorObservationData data) {
		// TODO Auto-generated method stub
		return false;
	}

	public SensorObservationData getData(String deviceID, Calendar from,
			Calendar to) {
		// TODO Auto-generated method stub
		return null;
	}

	

	
	


}
