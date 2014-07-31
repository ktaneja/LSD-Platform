package com.accenture.techlabs.sensordata.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SensorDataType {
	public enum Type {DOUBLE, INT, TEXT};
	
	Map<String, List<String>> members; //name of data types of each sensors, key is sensor_name
	Map<String, List<Type>> types;
	
	public SensorDataType(){
		members = new HashMap<String, List<String>>();
		types = new HashMap<String, List<Type>>();
	}
	
	public void addMember(String sensor, String name, Type type){
		if(members.containsKey(sensor)){
			members.get(sensor).add(name);
			types.get(sensor).add(type);
		}
		else{
			List<String> m = new ArrayList<String>();
			List<Type> t = new ArrayList<Type>();
			m.add(name);
			t.add(type);
			members.put(sensor, m);
			types.put(sensor, t);
		}
		
	}

	public Map<String, List<String>> getMembers() {
		return members;
	}

	

	public Map<String, List<Type>> getTypes() {
		return types;
	}

	

}
