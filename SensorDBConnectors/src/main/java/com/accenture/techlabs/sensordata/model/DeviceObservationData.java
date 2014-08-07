package com.accenture.techlabs.sensordata.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.accenture.techlabs.sensordata.dao.CassandraSensorDataDAO;
import com.accenture.techlabs.sensordata.model.DeviceDataType.Type;
import com.google.gson.Gson;


public class DeviceObservationData {
	String deviceName;
	String deviceUUID;
	Map<String, SensorObservationData> observationValues; 
	
	DeviceDataType dataType;
	
	public DeviceDataType getDataType() {
		return dataType;
	}

	public void setDataType(DeviceDataType dataType) {
		this.dataType = dataType;
	}
	
	public DeviceObservationData(String device, String uuid){
		observationValues = new HashMap<String, SensorObservationData>();
		this.deviceName = device;
		this.deviceUUID = uuid;
	}
	
	public DeviceObservationData(String device, String uuid, DeviceDataType dt){
		observationValues = new HashMap<String, SensorObservationData>();
		this.deviceName = device;
		this.deviceUUID = uuid;
		this.dataType = dt;
	}
	
	public String getDeviceName() {
		return deviceName;
	}
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	public String getSensorUUID() {
		return deviceUUID;
	}
	public void setSensorUUID(String sensorUUID) {
		this.deviceUUID = sensorUUID;
	}
	public Map<String, SensorObservationData> getObservationValues() {
		return observationValues;
	}
	public void setObservationValues(Map<String, SensorObservationData> observationValues) {
		this.observationValues = observationValues;
	}
	
	public void addObservationValue(String sensor, SensorObservationData obs){
		observationValues.put(sensor, obs);
	}
	
	
	public static void main(String[] args) {
		Gson gson = new Gson();
		DateTime time = DateTime.now();
		
		DeviceObservationData data = new DeviceObservationData("vm_qiandemo_gui", "123");
		SensorObservationData sData = data.new SensorObservationData("vcenter_1");
		DeviceDataType type = new DeviceDataType();
		type.addMember("vcenter_1", "cpureading", Type.DOUBLE);
		type.addMember("vcenter_1", "diskreading", Type.INT);
		type.addMember("vcenter_1", "memoryreading", Type.DOUBLE);
		data.setDataType(type);
		List<Object> obs = new ArrayList<Object>();
		obs.add(55);
		obs.add(1);
		obs.add(99);
		sData.addSensorEntry(time, obs);
		
		time = DateTime.now();
		sData.addSensorEntry(time, obs);
		
		data.addObservationValue("vcenter_1", sData);
		String json = gson.toJson(data);
		System.out.println(json);
		DeviceObservationData xData  = gson.fromJson(json, DeviceObservationData.class);
		List<String> queries = CassandraSensorDataDAO.synthesizeCreateTableQueries(xData);
		for (String query : queries) {
			System.out.println(query);
		}
	}
	
	public class Pair<K, V>{
		K time;
		V observation;
		
		public Pair(K key, V value){
			this.time = key;
			this.observation = value;
		}
		
		public Object getKey() {
			return time;
		}
		public void setKey(K key) {
			this.time = key;
		}
		public V getValue() {
			return observation;
		}
		public void setValue(V value) {
			this.observation = value;
		}
		
		
	}
	
	public class SensorObservationData{
		String property;
		List<Pair<Long, List<Object>>> sensorData;
		
		public SensorObservationData(String property){
			this.property = property;
			sensorData = new ArrayList<Pair<Long,List<Object>>>();
		
		}
		
		public void addSensorEntry(DateTime time, List<Object> observations){
			sensorData.add(new Pair<Long, List<Object>>(time.getMillis(), observations));
		}
		
		public String getSensorName() {
			return property;
		}
		public void setSensorName(String sensorName) {
			this.property = sensorName;
		}
		public List<Pair<Long, List<Object>>> getSensorData() {
			return sensorData;
		}
		public void setSensorData(List<Pair<Long, List<Object>>> sensorData) {
			this.sensorData = sensorData;
		}
		
	}
}
