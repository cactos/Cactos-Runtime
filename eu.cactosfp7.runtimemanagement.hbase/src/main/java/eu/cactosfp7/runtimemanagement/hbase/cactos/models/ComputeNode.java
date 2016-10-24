package eu.cactosfp7.runtimemanagement.hbase.cactos.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import eu.cactosfp7.runtimemanagement.hbase.models.Cell;
import eu.cactosfp7.runtimemanagement.hbase.models.Row;

public final class ComputeNode extends Model {

	/*
	 * Extracted information below
	 */
	private String name;
	private String state;
	private String cpu_arch;
	private String cpu_cores;
	private String cpu_freq;
	private String mem_freq;
	private String mem_size;
	private String cpu_sys;
	private String cpu_usr;
	private String cpu_wio;
	private String mem_buff;
	private String mem_cache;
	private String mem_free;
	private String mem_swpd;
	private String netw_speed;
	private String netw_through;
	
	private List<String> virtualMachineIDs = new ArrayList<String>();
	
	public ComputeNode(Row row){
		super(row);
		fillFields();
	}

	private void fillFields(){
		name = getContent(Fields.hardware_csource);
		state = getContent(Fields.meta_state);
		cpu_arch = getContent(Fields.hardware_cpu_arch);
		cpu_cores = getContent(Fields.hardware_cpu_cores);
		cpu_freq = getContent(Fields.hardware_cpu_freq);
		mem_freq = getContent(Fields.hardware_mem_freq);
		mem_size = getContent(Fields.hardware_mem_size);
		cpu_sys = getContent(Fields.hardware_util_cpu_sys);
		cpu_usr = getContent(Fields.hardware_util_cpu_usr);
		cpu_wio = getContent(Fields.hardware_util_cpu_wio);
		mem_buff = getContent(Fields.hardware_util_mem_buff);
		mem_cache = getContent(Fields.hardware_util_mem_cache);
		mem_free = getContent(Fields.hardware_util_mem_free);
		mem_swpd = getContent(Fields.hardware_util_mem_swpd);
		netw_speed = getContent(Fields.network_netw_speed);
		netw_through = getContent(Fields.network_util_net_through);
		
		for (Entry<String, Cell> entryCell : row.findCells( Fields.vms_vm_uuid.toString() + "\\.\\d?" ).entrySet()) {
			Cell cell = entryCell.getValue();
			virtualMachineIDs.add(cell.getContent());
		}
	}
	
	@Override
	public String toString() {
		return "ComputeNode [name=" + name + ", state=" + state + ", cpu_arch=" + cpu_arch + ", cpu_cores=" + cpu_cores
				+ ", cpu_freq=" + cpu_freq + ", mem_freq=" + mem_freq + ", mem_size=" + mem_size + ", cpu_sys="
				+ cpu_sys + ", cpu_usr=" + cpu_usr + ", cpu_wio=" + cpu_wio + ", mem_buff=" + mem_buff + ", mem_cache="
				+ mem_cache + ", mem_free=" + mem_free + ", mem_swpd=" + mem_swpd + ", netw_speed=" + netw_speed
				+ ", netw_through=" + netw_through + ", virtualMachineIDs=" + virtualMachineIDs + "]";
	}

	public String getName() {
		return name;
	}

	public String getState() {
		return state;
	}	
	
	public String getCpu_arch() {
		return cpu_arch;
	}

	public String getCpu_cores() {
		return cpu_cores;
	}

	public String getCpu_freq() {
		return cpu_freq;
	}

	public String getMem_freq() {
		return mem_freq;
	}

	public String getMem_size() {
		return mem_size;
	}

	public String getCpu_sys() {
		return cpu_sys;
	}

	public String getCpu_usr() {
		return cpu_usr;
	}

	public String getCpu_wio() {
		return cpu_wio;
	}

	public String getMem_buff() {
		return mem_buff;
	}

	public String getMem_cache() {
		return mem_cache;
	}

	public String getMem_free() {
		return mem_free;
	}

	public String getMem_swpd() {
		return mem_swpd;
	}

	public String getNetw_speed() {
		return netw_speed;
	}

	public String getNetw_through() {
		return netw_through;
	}

	public List<String> getVirtualMachineIDs() {
		return virtualMachineIDs;
	}

	public enum Fields implements Model.Fields {
		
		filesystem_available ("filesystem:available"),
		filesystem_capp("filesystem:capp"),
		filesystem_csource("filesystem:csource"),
		filesystem_ctags("filesystem:ctags"),
		filesystem_mount("filesystem:mount"),
		filesystem_readbandmax("filesystem:readbandmax"),
		filesystem_type("filesystem:type"),
		filesystem_used("filesystem:used"),
		filesystem_writebandmax("filesystem:writebandmax"),
		hardware_capp("hardware:capp"),
		hardware_cpu_arch("hardware:cpu_arch"),
		hardware_cpu_cores("hardware:cpu_cores"),
		hardware_cpu_freq("hardware:cpu_freq"),
		hardware_csource("hardware:csource"),
		hardware_ctags("hardware:ctags"),
		hardware_mem_freq("hardware:mem_freq"),
		hardware_mem_size("hardware:mem_size"),
		hardware_util_capp("hardware_util:capp"),
		hardware_util_cpu_sys("hardware_util:cpu_sys"),
		hardware_util_cpu_usr("hardware_util:cpu_usr"),
		hardware_util_cpu_wio("hardware_util:cpu_wio"),
		hardware_util_csource("hardware_util:csource"),
		hardware_util_ctags("hardware_util:ctags"),
		hardware_util_mem_buff("hardware_util:mem_buff"),
		hardware_util_mem_cache("hardware_util:mem_cache"),
		hardware_util_mem_free("hardware_util:mem_free"),
		hardware_util_mem_swpd("hardware_util:mem_swpd"),
		meta_state("meta:state"),
		network_capp("network:capp"),
		network_csource("network:csource"),
		network_ctags("network:ctags"),
		network_netw_speed("network:netw_speed"),
		network_util_capp("network_util:capp"),
		network_util_csource("network_util:csource"),
		network_util_ctags("network_util:ctags"),
		network_util_net_through("network_util:net_through"),
		power_capacity("power:capacity"),
		power_capp("power:capp"),
		power_csource("power:csource"),
		power_ctags("power:ctags"),
		power_serial("power:serial"),
		power_util_capp("power_util:capp"),
		power_util_consumption("power_util:consumption"),
		power_util_csource("power_util:csource"),
		power_util_ctags("power_util:ctags"),
		storage_capp("storage:capp"),
		storage_csource("storage:csource"),
		storage_ctags("storage:ctags"),
		storage_disk_device("storage:disk_device"),
		storage_disk_mount("storage:disk_mount"),
		storage_disk_name("storage:disk_name"),
		storage_disk_parent("storage:disk_parent"),
		storage_disk_size("storage:disk_size"),
		storage_disk_type("storage:disk_type"),
		storage_util_Device_("storage_util:Device:"),
		storage_util_Mountpoint("storage_util:Mountpoint"),
		storage_util_capp("storage_util:capp"),
		storage_util_csource("storage_util:csource"),
		storage_util_ctags("storage_util:ctags"),
		storage_util_kB_read("storage_util:kB_read"),
		storage_util_kB_readps("storage_util:kB_read/s"),
		storage_util_kB_wrtn("storage_util:kB_wrtn"),
		storage_util_kB_wrtnps("storage_util:kB_wrtn/s"),
		storage_util_tps("storage_util:tps"),
		vms_capp("vms:capp"),
		vms_csource("vms:csource"),
		vms_ctags("vms:ctags"),
		vms_vm_image_uuid("vms:vm_image_uuid"),
		vms_vm_name("vms:vm_name"),
		vms_vm_tenant_uuid("vms:vm_tenant_uuid"),
		vms_vm_uuid("vms:vm_uuid");
		
		public final String name;
		
		private Fields(final String name){
			this.name = name; 
		}

		@Override
		public String toString() {
			return name;
		}
		
	}
	
}

/*
HBase Columns:

filesystem:available.0
filesystem:available.1
filesystem:capp
filesystem:csource
filesystem:ctags
filesystem:mount.0
filesystem:mount.1
filesystem:readbandmax.0
filesystem:readbandmax.1
filesystem:type.0
filesystem:type.1
filesystem:used.0
filesystem:used.1
filesystem:writebandmax.0
filesystem:writebandmax.1
hardware:capp
hardware:cpu_arch
hardware:cpu_cores
hardware:cpu_freq
hardware:csource
hardware:ctags
hardware:mem_freq
hardware:mem_size
hardware_util:capp
hardware_util:cpu_sys
hardware_util:cpu_usr
hardware_util:cpu_wio
hardware_util:csource
hardware_util:ctags
hardware_util:mem_buff
hardware_util:mem_cache
hardware_util:mem_free
hardware_util:mem_swpd
meta:state
network:capp
network:csource
network:ctags
network:netw_speed
network_util:capp
network_util:csource
network_util:ctags
network_util:net_through
power:capacity.0
power:capacity.1
power:capp
power:csource
power:ctags
power:serial.0
power:serial.1
power_util:capp
power_util:consumption
power_util:csource
power_util:ctags
storage:capp
storage:csource
storage:ctags
storage:disk_device.0
storage:disk_device.1
storage:disk_device.2
storage:disk_device.3
storage:disk_device.4
storage:disk_device.5
storage:disk_device.6
storage:disk_device.7
storage:disk_device.8
storage:disk_device.9
storage:disk_mount.0
storage:disk_mount.1
storage:disk_mount.2
storage:disk_mount.3
storage:disk_mount.4
storage:disk_mount.5
storage:disk_mount.6
storage:disk_mount.7
storage:disk_mount.8
storage:disk_mount.9
storage:disk_name.0
storage:disk_name.1
storage:disk_name.2
storage:disk_name.3
storage:disk_name.4
storage:disk_name.5
storage:disk_name.6
storage:disk_name.7
storage:disk_name.8
storage:disk_name.9
storage:disk_parent.0
storage:disk_parent.1
storage:disk_parent.2
storage:disk_parent.3
storage:disk_parent.4
storage:disk_parent.5
storage:disk_parent.6
storage:disk_parent.7
storage:disk_parent.8
storage:disk_parent.9
storage:disk_size.0
storage:disk_size.1
storage:disk_size.2
storage:disk_size.3
storage:disk_size.4
storage:disk_size.5
storage:disk_size.6
storage:disk_size.7
storage:disk_size.8
storage:disk_size.9
storage:disk_type.0
storage:disk_type.1
storage:disk_type.2
storage:disk_type.3
storage:disk_type.4
storage:disk_type.5
storage:disk_type.6
storage:disk_type.7
storage:disk_type.8
storage:disk_type.9
storage_util:Device:.0
storage_util:Device:.1
storage_util:Device:.2
storage_util:Device:.3
storage_util:Device:.4
storage_util:Device:.5
storage_util:Device:.6
storage_util:Device:.7
storage_util:Device:.8
storage_util:Device:.9
storage_util:Mountpoint.0
storage_util:Mountpoint.1
storage_util:Mountpoint.2
storage_util:Mountpoint.3
storage_util:Mountpoint.4
storage_util:Mountpoint.5
storage_util:Mountpoint.6
storage_util:Mountpoint.7
storage_util:Mountpoint.8
storage_util:Mountpoint.9
storage_util:capp
storage_util:csource
storage_util:ctags
storage_util:kB_read.0
storage_util:kB_read.1
storage_util:kB_read.2
storage_util:kB_read.3
storage_util:kB_read.4
storage_util:kB_read.5
storage_util:kB_read.6
storage_util:kB_read.7
storage_util:kB_read.8
storage_util:kB_read.9
storage_util:kB_read/s.0
storage_util:kB_read/s.1
storage_util:kB_read/s.2
storage_util:kB_read/s.3
storage_util:kB_read/s.4
storage_util:kB_read/s.5
storage_util:kB_read/s.6
storage_util:kB_read/s.7
storage_util:kB_read/s.8
storage_util:kB_read/s.9
storage_util:kB_wrtn.0
storage_util:kB_wrtn.1
storage_util:kB_wrtn.2
storage_util:kB_wrtn.3
storage_util:kB_wrtn.4
storage_util:kB_wrtn.5
storage_util:kB_wrtn.6
storage_util:kB_wrtn.7
storage_util:kB_wrtn.8
storage_util:kB_wrtn.9
storage_util:kB_wrtn/s.0
storage_util:kB_wrtn/s.1
storage_util:kB_wrtn/s.2
storage_util:kB_wrtn/s.3
storage_util:kB_wrtn/s.4
storage_util:kB_wrtn/s.5
storage_util:kB_wrtn/s.6
storage_util:kB_wrtn/s.7
storage_util:kB_wrtn/s.8
storage_util:kB_wrtn/s.9
storage_util:tps.0
storage_util:tps.1
storage_util:tps.2
storage_util:tps.3
storage_util:tps.4
storage_util:tps.5
storage_util:tps.6
storage_util:tps.7
storage_util:tps.8
storage_util:tps.9
vms:capp
vms:csource
vms:ctags
vms:vm_image_uuid.0
vms:vm_image_uuid.1
vms:vm_image_uuid.2
vms:vm_name.0
vms:vm_name.1
vms:vm_name.2
vms:vm_tenant_uuid.0
vms:vm_tenant_uuid.1
vms:vm_tenant_uuid.2
vms:vm_uuid.0
vms:vm_uuid.1
vms:vm_uuid.2 
*/
