package eu.cactosfp7.runtimemanagement.hbase.cactos;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import eu.cactosfp7.runtimemanagement.hbase.cactos.models.ComputeNode;
import eu.cactosfp7.runtimemanagement.hbase.cactos.models.VirtualMachine;

public class ExampleQueries {

	private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	
	public static void main(String... args){

//		scheduler.scheduleAtFixedRate(new Runnable() {
//
//			@Override
//			public void run() {
//				try {
//					System.out.println(new Date());
//					exampleCpuUsages();
//				} catch (Throwable t) {
//					t.printStackTrace();
//				}
//			}
//
//		}, 0, 10, TimeUnit.SECONDS);
		
//		exampleCpuUsagesAtTimestamp();
		
		exampleListCNState("computenode11");

	}
	
	private static void exampleListCNState(String computenode){

	}	
	
	private static void exampleListVMs(){
		StorageSnapshot snapshot = StorageRequest.loadSnapshot();
		Map<String, ComputeNode> cns = StorageRequest.getComputenodes(snapshot);
		Map<String, VirtualMachine> vms = StorageRequest.getVirtualMachines(snapshot);
		ComputeNode node = cns.get("computenode15");
		for(int i = 0; i < node.getVirtualMachineIDs().size(); i++){
			String vmID = node.getVirtualMachineIDs().get(i);
			VirtualMachine vm = vms.get(vmID);
			if(vm == null){
				System.out.println("VM not found!" + vmID);
			}else{
				System.out.println("\t" + vm);
			}
		}
	}
	
	private static void exampleCpuUsages(){
		StorageSnapshot snapshot = StorageRequest.loadSnapshot();
		Map<String, ComputeNode> cns = StorageRequest.getComputenodes(snapshot);
		Map<String, VirtualMachine> vms = StorageRequest.getVirtualMachines(snapshot);
		
		for (Entry<String, ComputeNode> entry : cns.entrySet()) {
			ComputeNode node = entry.getValue();

			System.out.println("ComputeNode " + node.getName() + " CPU (usr/sys/wio) " + node.getCpu_usr() + "/" + node.getCpu_sys() + "/" + node.getCpu_wio());
			
			for(int i = 0; i < node.getVirtualMachineIDs().size(); i++){
				String vmID = node.getVirtualMachineIDs().get(i);
				VirtualMachine vm = vms.get(vmID);
				if(vm == null){
					//System.out.println("VM not found!" + vmID);
				}else{
					System.out.println("\tVirtualMachine " + vm.getUuid() + " CPU " + vm.getCpu_vm());
				}
			}
		}
	}
	
	private static void exampleCpuUsagesAtTimestamp() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Date date = formatter.parse("2016-02-03 17:00:00");
			//System.out.println(date);
			StorageSnapshot snapshot = StorageRequest.loadSnapshot(date);
			// System.out.println(snapshot.getCnTable());
			Map<String, ComputeNode> cns = StorageRequest.getComputenodes(snapshot);
			Map<String, VirtualMachine> vms = StorageRequest.getVirtualMachines(snapshot);

			for (Entry<String, ComputeNode> entry : cns.entrySet()) {
				ComputeNode node = entry.getValue();

				System.out.println("ComputeNode " + node.getName() + " CPU (usr/sys/wio) " + node.getCpu_usr() + "/"
						+ node.getCpu_sys() + "/" + node.getCpu_wio());

				for (int i = 0; i < node.getVirtualMachineIDs().size(); i++) {
					String vmID = node.getVirtualMachineIDs().get(i);
					VirtualMachine vm = vms.get(vmID);
					if (vm == null) {
						System.out.println("VM not found!" + vmID);
					} else {
						//System.out.println("\tVirtualMachine " + vm.getUuid() + " CPU " + vm.getCpu_vm());
						System.out.println("\tVirtualMachine " + vm);
					}
				}
			}

		} catch (ParseException e) {
			e.printStackTrace();
			return;
		}
	}
	
	
}
