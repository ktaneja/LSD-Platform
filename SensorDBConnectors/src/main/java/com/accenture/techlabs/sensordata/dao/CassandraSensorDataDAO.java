package com.accenture.techlabs.sensordata.dao;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import au.com.bytecode.opencsv.CSVWriter;

import com.accenture.techlabs.sensordata.dbconnectors.CassandraConnector;
import com.accenture.techlabs.sensordata.model.DeviceObservationData;
import com.accenture.techlabs.sensordata.model.DeviceDataType;
import com.accenture.techlabs.sensordata.model.DeviceDataType.Type;
import com.accenture.techlabs.sensordata.model.DeviceObservationData.Pair;
import com.accenture.techlabs.sensordata.model.DeviceObservationData.SensorObservationData;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ColumnDefinitions.Definition;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;



public class CassandraSensorDataDAO implements SensorDataDAO {
	
	private CassandraConnector connection;
	
	public CassandraSensorDataDAO(){
		connection = CassandraConnector.getConnection();
	}
	
	public boolean createTable(String deviceID, DeviceDataType datatype) {
		String queryString = synthesizeCreateTableQuery(deviceID, datatype);
		connection.getSession().execute(queryString);
		return false;
	}

	private String synthesizeCreateTableQuery(String deviceID, DeviceDataType datatype) {
		// CREATE TABLE sample_data ( sensor_id text, event_time timestamp, cpu_usage double, PRIMARY KEY(sensor_id, event_time) );
		Map<String, List<String>> memberMap = datatype.getMembers();
		Map<String, List<Type>> typeMap = datatype.getTypes();
		deviceID = deviceID.replaceAll("-", "");
		String queryString = "CREATE TABLE " + deviceID + " ( sensor_id text, event_time timestamp, ";
		for (String sensor : typeMap.keySet()) {
			
			List<String> members = memberMap.get(sensor);
			List<Type> types = typeMap.get(sensor);
			
			for (int i=0; i<types.size(); i++) {
				String column_name = sensor;
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

	public static List<String> synthesizeCreateTableQueries(DeviceObservationData observationData) {
		List<String> queries = new ArrayList<String>();
		String devicename = observationData.getDeviceName();
		String uuid = observationData.getSensorUUID();
		
		devicename = devicename.replaceAll("-", "");
		StringBuilder queryPrefix = new StringBuilder("INSERT INTO " + devicename + " (sensor_id, event_time");
		DeviceDataType dataType = observationData.getDataType();
		
		Map<String, List<String>> members = dataType.getMembers();
		Map<String, List<Type>> dTypes = dataType.getTypes();
		for (String sensor : dTypes.keySet()) {
			List<String> readings = members.get(sensor);
			for (String reading : readings) {
				String columnName = sensor;
				if(reading != null && reading.trim().length() != 0)
					columnName = sensor + "_" +  reading;
				queryPrefix.append(", " + columnName);
			}
			
		}
		queryPrefix.replace(queryPrefix.length()-1, queryPrefix.length()-1, "");
		queryPrefix.append(") VALUES (");
		
		Map<String, SensorObservationData> data = observationData.getObservationValues();
		
		for (String sensor : dTypes.keySet()) {
			
			SensorObservationData sensorObservationData = data.get(sensor);
			List<Pair<Long, List<Object>>> sensordata = sensorObservationData.getSensorData();
			for (Pair<Long, List<Object>> pair : sensordata) {
				StringBuilder query = new StringBuilder(queryPrefix);
				
				List<Object> observations = pair.getValue();
				query.append("'" + devicename + "', " + pair.getKey()); 
				
				for (Object object : observations) {
					if(dTypes.get(sensor).get(observations.indexOf(object)) == Type.TEXT)
						query.append( ", '" + object + "'");
					else if (dTypes.get(sensor).get(observations.indexOf(object)) == Type.DOUBLE)
						query.append("," + (Double)object);
					else{
						double n = (Double) object;
						query.append("," + (int)n);
					}
					
				}
				query.replace(query.length()-1, query.length()-1, "");
				query.append(")");
				queries.add(query.toString());
				
			}
		}
		
		return queries;
	}
	
	public boolean addData(DeviceObservationData data) {
		List<String> queries = synthesizeCreateTableQueries(data);
		for (String query : queries) {
			if(connection == null)
				connection = CassandraConnector.getConnection();
			connection.getSession().execute(query);
		}
		return true;
	}

	public String getData(String deviceID, String from,
			String to) {
		deviceID = deviceID.replaceAll("-", "");
		String selectQuery = synthesizeSelectQuery(deviceID, from, to);
		if(connection == null)
			connection = CassandraConnector.getConnection();
		ResultSet rs = connection.getSession().execute(selectQuery);
		
		
		String rawHeader = rs.getColumnDefinitions().toString();
		String header = rawHeader.substring(rawHeader.indexOf('[')+1, rawHeader.length()-1);
		StringBuilder csvBuilder = new StringBuilder();
		csvBuilder.append(header + "\n");
		
		Iterator<Row> iter = rs.iterator();
		while(iter.hasNext()){
			Row r = iter.next();
			String rawData = r.toString();
			String data = rawData.substring(rawData.indexOf('[')+1, rawData.length()-1);
			csvBuilder.append(data + "\n");
		}
		//writer.writeAll(rs, true);
		return csvBuilder.toString();
	}

	private static String synthesizeSelectQuery(String deviceID, String from, String to) {
		Date fromDate = new Date(Long.parseLong(from));
		Date toDate = new Date(Long.parseLong(to));
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String formattedFrom = dateFormat.format(fromDate);
		String formattedTo = dateFormat.format(toDate);
		String query = "Select * from " + deviceID + " where event_time >= '" + formattedFrom + 
				"' AND event_time <= '" + formattedTo + "' ALLOW FILTERING";
		return query;
	}
	public static void main(String[] args) {
		Date from = new Date();
		Date to = new Date();
		from.setTime(from.getTime()-86400000 );
		String query = synthesizeSelectQuery("vm_qiandemo_gui", Long.toString(from.getTime()), Long.toString(to.getTime()));
		StringBuilder csvBuilder = new StringBuilder();
		
		CassandraConnector conn = CassandraConnector.getConnection();
		ResultSet rs = conn.getSession().execute(query);
		String rawHeader = rs.getColumnDefinitions().toString();
		String header = rawHeader.substring(rawHeader.indexOf('[')+1, rawHeader.length()-1);
		
		csvBuilder.append(header + "\n");
		
		Iterator<Row> iter = rs.iterator();
		while(iter.hasNext()){
			Row r = iter.next();
			String rawData = r.toString();
			String data = rawData.substring(rawData.indexOf('[')+1, rawData.length()-1);
			csvBuilder.append(data + "\n");
		}
		System.out.println(csvBuilder.toString());
		
	}

	

	
	


}
