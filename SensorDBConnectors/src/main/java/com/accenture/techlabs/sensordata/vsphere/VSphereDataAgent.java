package com.accenture.techlabs.sensordata.vsphere;

import java.net.URL;
import java.rmi.RemoteException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.accenture.techlabs.sensordata.model.DeviceObservationData;
import com.accenture.techlabs.sensordata.model.DeviceObservationData.SensorObservationData;
import com.vmware.vim25.*;
import com.vmware.vim25.mo.*;



public class VSphereDataAgent {
	
	static final String ACN_vCenter="SVNGSAVCS001.techlabs.accenture.com";
	static final String ACN_vCenter_user="svc_NGSA_SH_demo";
	static final String ACN_vCenter_pwd="X.W>nm26Mf";
	private static final String ACN_vCenter_host="svatlesx026.techlabs.accenture.com";
	private static final String ACN_vCenter_VM="Qian-Demo_gui";
	
	private static final int CPU_USAGE_COUNTER_ID = 2; // 1 : Usage (cpu) in Percent (rate)
	private static final int DISK_USAGE_COUNTER_ID = 125; 
	private static final int MEMORY_USAGE_COUNTER_ID = 24;

	
	public static final int SAMPLE_PERIOD_HOUR = 20;
	public static final int SAMPLE_PERIOD_DAY = 300;
	public static final int SAMPLE_PERIOD_Week = 1800;
	public static final int SAMPLE_PERIOD_MONTH = 7200;
	public static final int SAMPLE_PERIOD_YEAR = 86400;
	
	
	public static int getSamplingInterval(GregorianCalendar start, GregorianCalendar end){
		long diff = end.getTimeInMillis() - start.getTimeInMillis();
		
		if(diff < 1000*60*60)
			return SAMPLE_PERIOD_HOUR;
		if(diff < 1000*60*60*24)
			return SAMPLE_PERIOD_DAY;
		if(diff < 1000*60*60*24*7)
			return SAMPLE_PERIOD_Week;
		if(diff < 1000*60*60*24*31)
			return SAMPLE_PERIOD_MONTH;
		
		return SAMPLE_PERIOD_YEAR;
		
	}
	
	public static void getDataFromLastHour(String VM){
		
	}
	
	public static void main(String[] args){
		VSphereDataAgent vSphereMetrics = new VSphereDataAgent();
		// list all metrics 
		// ?? there are repeated metric IDs listed: e.g. 1-4
		
		
		String url = "https://" + ACN_vCenter + "/sdk/vimService";

		try{
			ServiceInstance si = vSphereMetrics.Initialisation(url, ACN_vCenter_user, ACN_vCenter_pwd);
			Folder rootFolder = si.getRootFolder();
			//ManagedEntity[] mes = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");
			PerformanceManager pManager = si.getPerformanceManager();
			PerfCounterInfo[] perfCounters = pManager.getPerfCounter();
			for (int i = 0; i < perfCounters.length; i++) {
				PerfCounterInfo perfCounterInfo = perfCounters[i];
				String perfCounterString = perfCounterInfo.getNameInfo().getLabel() + " (" + perfCounterInfo.getGroupInfo().getKey() + ") in "
						+ perfCounterInfo.getUnitInfo().getLabel() + " (" + perfCounterInfo.getStatsType().toString() + ")";
				System.out.println(perfCounterInfo.getLevel() + " ::: " + perfCounterInfo.getKey() + " : " + perfCounterString);
			}
			si.getServerConnection().logout();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		try{
			ServiceInstance si = vSphereMetrics.Initialisation(url, ACN_vCenter_user, ACN_vCenter_pwd);
			Folder rootFolder = si.getRootFolder();
			//HostSystem host = (HostSystem) new InventoryNavigator(si.getRootFolder()).searchManagedEntity("HostSystem", ACN_vCenter_host);
			VirtualMachine VM = (VirtualMachine) new InventoryNavigator(si.getRootFolder()).searchManagedEntity("VirtualMachine", ACN_vCenter_VM);
			
			PerformanceManager pManager = si.getPerformanceManager();
			PerfCounterInfo[] counterinfo = pManager.queryPerfCounterByLevel(3);
			
			System.out.println(counterinfo[0].dynamicType);
			
			//PerfProviderSummary summary = pManager.queryPerfProviderSummary(host);
			
			PerfInterval[] perfIntervals = pManager.getHistoricalInterval();
			pManager.updatePerfInterval(perfIntervals[0]);
			
			
			Calendar beginTime = new GregorianCalendar();
			beginTime.add(GregorianCalendar.MINUTE, -60);
			//beginTime.set(2014, 7, 7, 16, 50, 00);
			Calendar endTime = new GregorianCalendar();
			//endTime.set(2014, 7, 7, 17, 30, 30);
			//System.out.println(DateUtil.format((GregorianCalendar) beginTime));
			//System.out.println(DateUtil.format((GregorianCalendar) endTime));
			
			int perfInterval = 20;//getSamplingInterval((GregorianCalendar)beginTime, (GregorianCalendar)endTime);
			System.out.println("Refresh rate = " + perfInterval);
			//PerfMetricId[] queryAvailablePerfMetric = pManager.queryAvailablePerfMetric(host, beginTime, endTime, perfInterval);
			PerfMetricId[] queryAvailablePerfMetric = pManager.queryAvailablePerfMetric(VM, beginTime, endTime, perfInterval);
			
			ArrayList<PerfMetricId> list = new ArrayList<PerfMetricId>();
			for(int i=0; i<queryAvailablePerfMetric.length; i++){
				PerfMetricId perfMetricId = queryAvailablePerfMetric[i];
				int counterID = perfMetricId.getCounterId();
				if (MEMORY_USAGE_COUNTER_ID == counterID || DISK_USAGE_COUNTER_ID == counterID ||
						CPU_USAGE_COUNTER_ID == counterID) {
					list.add(perfMetricId);
				}
				
			}
			
			PerfMetricId[] pmis = list.toArray(new PerfMetricId[list.size()]);
			
			PerfQuerySpec qSpec = new PerfQuerySpec();
			//qSpec.setEntity(host.getMOR());
			qSpec.setEntity(VM.getMOR());
			qSpec.setMetricId(pmis);
			qSpec.intervalId = perfInterval;
			qSpec.setStartTime(beginTime);
			qSpec.setEndTime(endTime);
			//qSpec.setFormat("csv");
			
			
			PerfEntityMetricBase[] pembs = pManager.queryPerf(new PerfQuerySpec[] { qSpec });
			Map<Long, List<Object>> observations = new HashMap<Long, List<Object>>();
			
			
			for (int i = 0; pembs != null && i < pembs.length; i++) {
				PerfEntityMetricBase val = pembs[i];
				PerfEntityMetric pem = (PerfEntityMetric) val;
				PerfMetricSeries[] vals = pem.getValue();
				PerfSampleInfo[] infos = pem.getSampleInfo();
				
				for (int j = 0; vals != null && j < vals.length; ++j) {
					PerfMetricIntSeries val1 = (PerfMetricIntSeries) vals[j];
					long[] longs = val1.getValue();
					for (int k = 0; k < longs.length; k++) {
						GregorianCalendar cal = (GregorianCalendar) infos[k].getTimestamp();
						//System.out.println(DateUtil.format(cal) + " ");
						System.out.println(infos[k].getTimestamp().getTime() + " : " + longs[k]);
						
						long time = cal.getTimeInMillis();
						if(observations.containsKey(time)){
							observations.get(time).add(longs[k]);
						}
						else{
							List<Object> readings = new ArrayList<Object>();
							readings.add(longs[k]);
							observations.put(time, readings);
						}
					}
					System.out.println();
				}
			}
			si.getServerConnection().logout();
			
			
			DeviceObservationData data = new DeviceObservationData("vm_qiandemo_gui", "da4cc105-309a-447d-9fc7-b5b6067cbfe9");
			SensorObservationData sData = data.new SensorObservationData("vcenter_1");
			
			for (Long time : observations.keySet()) {
				sData.addSensorEntry(time, observations.get(time));
			}
			
			
			
		}
		catch(InvalidProperty e){
			e.printStackTrace();
			return;
		}
		catch(RuntimeFault e){
			e.printStackTrace();
			return;
		}
		catch(RemoteException e){
			e.printStackTrace();
			return;
		}
		catch(MalformedURLException e){
			e.printStackTrace();
			return;
		}
		
	}
	
    public ServiceInstance Initialisation(String url, String username, String password) throws RemoteException, MalformedURLException{  
        ServiceInstance si = new ServiceInstance(new URL(url), username, password, true);  
        return si;  
    }  

}
