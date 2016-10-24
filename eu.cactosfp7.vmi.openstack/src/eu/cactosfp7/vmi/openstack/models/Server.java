package eu.cactosfp7.vmi.openstack.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Server {

	private String imageRef;
	private String flavorRef;
	private HashMap<String,String> metadata;
	
	// REST request vars
	private Map<String, List<String>> request_headers;
	private String request_json;

	public Server(){
		
	}

	public String getImageRef() {
		return imageRef;
	}

	public void setImageRef(String imageRef) {
		this.imageRef = imageRef;
	}

	public String getFlavorRef() {
		return flavorRef;
	}

	public void setFlavorRef(String flavorRef) {
		this.flavorRef = flavorRef;
	}

	public HashMap<String, String> getMetadata() {
		return metadata;
	}

	public void setMetadata(HashMap<String, String> metadata) {
		this.metadata = metadata;
	}

	public Map<String, List<String>> getRequest_headers() {
		return request_headers;
	}

	public void setRequest_headers(Map<String, List<String>> request_headers) {
		this.request_headers = request_headers;
	}

	public String getRequest_json() {
		return request_json;
	}

	public void setRequest_json(String request_json) {
		this.request_json = request_json;
	}
	
	
	
}
