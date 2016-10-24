package eu.cactosfp7.runtimemanagement.hbase.cactos.models;

import eu.cactosfp7.runtimemanagement.hbase.models.Row;

public final class VirtualMachine extends Model {


	/*
	 * Extracted information below
	 */
	private String computenodeId;
	private String uuid;
	private String name;
	private String vm_state;
	private String ram_used;
	private String ram_total;
	private String disk_total;
	private String disk_used;
	private String disk_read;
	private String disk_write;	
	private String cpu_vm;
	private String cpu_cs;
	private String network;
	
	public VirtualMachine(Row row){
		super(row);
		fillFields();
	}

	private void fillFields() {
		computenodeId = getContent(Fields.meta_csource);
		uuid = getContent(Fields.hardware_UUID);
		name = getContent(Fields.hardware_vmname);
		vm_state = getContent(Fields.meta_vm_state);
		ram_used = getContent(Fields.hardware_ram_used);
		ram_total = getContent(Fields.hardware_ram_total);
		disk_total = getContent(Fields.storage_disk_total);
		disk_used = getContent(Fields.storage_disk_used);
		disk_read = getContent(Fields.storage_disk_read);
		disk_write = getContent(Fields.storage_disk_write);	
		cpu_vm = getContent(Fields.hardware_CpuVM);
		cpu_cs = getContent(Fields.hardware_CpuCS);
		network = getContent(Fields.network_network);
	}	
	
	@Override
	public String toString() {
		return "VirtualMachine [computenodeId=" + computenodeId + ", uuid=" + uuid + ", name=" + name + ", vm_state="
				+ vm_state + ", ram_used=" + ram_used + ", ram_total=" + ram_total + ", disk_total=" + disk_total
				+ ", disk_used=" + disk_used + ", disk_read=" + disk_read + ", disk_write=" + disk_write + ", cpu_vm="
				+ cpu_vm + ", cpu_cs=" + cpu_cs + ", network=" + network + "]";
	}

	public String getComputenodeId() {
		return computenodeId;
	}

	public String getUuid() {
		return uuid;
	}

	public String getName() {
		return name;
	}

	public String getVm_state() {
		return vm_state;
	}

	public String getRam_used() {
		return ram_used;
	}

	public String getRam_total() {
		return ram_total;
	}

	public String getDisk_total() {
		return disk_total;
	}

	public String getDisk_used() {
		return disk_used;
	}

	public String getDisk_read() {
		return disk_read;
	}

	public String getDisk_write() {
		return disk_write;
	}

	public String getCpu_vm() {
		return cpu_vm;
	}

	public String getCpu_cs() {
		return cpu_cs;
	}

	public String getNetwork() {
		return network;
	}

	public enum Fields implements Model.Fields{
		
		hardware_UUID("hardware:UUID"),
		hardware_vmname("hardware:vmname"),
		meta_capp("meta:capp"),
		network_csource("network:csource"),
		hardware_capp("hardware:capp"),
		hardware_ram_used("hardware:ram-used"),
		storage_disk_write("storage:disk-write"),
		network_ctags("network:ctags"),
		hardware_csource("hardware:csource"),
		hardware_ctags("hardware:ctags"),
		storage_ctags("storage:ctags"),
		meta_ctags("meta:ctags"),
		network_capp("network:capp"),
		meta_vm_state("meta:vm_state"),
		storage_capp("storage:capp"),
		storage_disk_total("storage:disk-total"),
		storage_csource("storage:csource"),
		storage_disk_read("storage:disk-read"),
		hardware_ram_total("hardware:ram-total"),
		hardware_CpuVM("hardware:CpuVM"),
		storage_disk_used("storage:disk-used"),
		meta_csource("meta:csource"),
		network_network("network:network"),
		hardware_CpuCS("hardware:CpuCS");
		
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
hardware:UUID
hardware:vmname
meta:capp
network:csource
hardware:capp
hardware:ram-used
storage:disk-write
network:ctags
hardware:csource
hardware:ctags
storage:ctags
meta:ctags
network:capp
meta:vm_state
storage:capp
storage:disk-total
storage:csource
storage:disk-read
hardware:ram-total
hardware:CpuVM
storage:disk-used
meta:csource
network:network
hardware:CpuCS
 */
