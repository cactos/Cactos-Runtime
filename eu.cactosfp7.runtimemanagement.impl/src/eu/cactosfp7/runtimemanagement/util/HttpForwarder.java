package eu.cactosfp7.runtimemanagement.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpForwarder {
	
	public static final String RESPONSE_CODE_KEY = "code";
	private static final Logger logger = Logger.getLogger(HttpForwarder.class.getName());
	public static final String RESPONSE_OUTPUT_KEY = "output";
	
	private static void send(HttpURLConnection connection, String requestMethod, String targetUrl, Map<String,String> headers, byte[] body) throws IOException {
	    
	    connection.setRequestMethod(requestMethod);
	    
	    // Set Header Fields
	    for (Map.Entry<String, String> entry : headers.entrySet()){
        	connection.setRequestProperty(entry.getKey(), entry.getValue());
	    }
	    connection.setUseCaches(false);
	    connection.setDoOutput(true);

	    //Send request
	    if(body != null){
		    DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
		    wr.write(body);
		    wr.close();
	    }
	}
	
	private static String readResult(HttpURLConnection connection) throws IOException {
	    InputStream is = connection.getInputStream();
	    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
	    StringBuilder response = new StringBuilder(); 
	    String line;
	    while((line = rd.readLine()) != null) {
	    	response.append(line);
	    	response.append('\r');
	    }
	    rd.close();
	    logger.log(Level.INFO, "RESPONSE " + connection.getHeaderFields() + " Body: " + response.toString());
	    return response.toString();
	}
	
	public static String simpleForward(String requestMethod, String targetUrl, Map<String,String> headers, byte[] body) {
		Map<String,Object> map = forward(requestMethod, targetUrl, headers, body);
		if(map != null) {
			return (String) map.get(RESPONSE_OUTPUT_KEY);
		} 
		logger.log(Level.SEVERE, "simpleForward returning null.", new Exception());
		return null;
	}
	
	private static int CONNECT_TIMEOUT = 3000; // 3 seconds
	private static int READ_TIMEOUT = 6000; // 6 seconds
	public static Map<String, Object> forward(String requestMethod, String targetUrl, Map<String,String> headers, byte[] body) {
		logger.log(Level.INFO, "Forward " + requestMethod + " http request to " + targetUrl + " with headers: " + headers + " body: " + body);
		HttpURLConnection connection = null;  
		try {
		    //	Create connection
	            URL url = new URL(targetUrl);
		    connection = (HttpURLConnection)url.openConnection();
		    connection.setConnectTimeout(CONNECT_TIMEOUT);
		    connection.setReadTimeout(READ_TIMEOUT);
		    send(connection, requestMethod, targetUrl, headers, body);

		    Map<String,Object> map = new HashMap<String,Object>();
		    addResponseCode(map, connection);
		    addResult(map, connection);
		    logger.log(Level.INFO, "returning results: " + map);
		    return map;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "problem when forwarding message", e);
			return null;
		} catch(Error er){
		    	logger.log(Level.SEVERE, "problem when forwarding message", er);
			return null;
		} finally {
			if(connection != null) {
				connection.disconnect(); 
			}
		   logger.log(Level.INFO, "leaving forwarding logic");
		}
	}
	
	private static void addResponseCode(Map<String, Object> map, HttpURLConnection connection) {
		Integer value = -1;
		try {
			value = connection.getResponseCode();
		} catch (IOException e) {
			logger.log(Level.INFO, "could not read response code", e);
		} finally {
			map.put(RESPONSE_CODE_KEY, value);
		}
	}
	
	private static void addResult(Map<String, Object> map, HttpURLConnection connection) {
		String response = null;
		try {
			response = readResult(connection);
		} catch (IOException e) {
			logger.log(Level.INFO, "could not read result", e);
			try {
				// let's assume the problem is that we got a 
				// response not in the 200 range
				response = connection.getResponseMessage();
			} catch(IOException ioe) {
				// something really went bad //
				// that's already printed above //
			}
		} finally {
			map.put(RESPONSE_OUTPUT_KEY, response);
		}
	}
}
