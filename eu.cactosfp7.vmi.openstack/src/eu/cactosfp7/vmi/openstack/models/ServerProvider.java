package eu.cactosfp7.vmi.openstack.models;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The ServerProvider is used by Jersey to parse the Server model from/to JSON.
 * 
 * @author christopher
 *
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ServerProvider implements MessageBodyWriter<Server>,
		MessageBodyReader<Server> {

	private static final Logger logger = Logger.getLogger(ServerProvider.class.getName());
	
	@Override
	public long getSize(Server server, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return -1;
	}

	@Override
	public boolean isWriteable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return Server.class.isAssignableFrom(type);
	}

	/**
	 * This would be needed if the HTTP Response would consist of a server object.
	 * This is not the case, this method is never called! 
	 * Class Servers returns a string with json directly.
	 */
	@Override
	public void writeTo(Server server, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException,
			WebApplicationException {
		
		throw new RuntimeException("Parser from Server object to json not implemented since not required.");
//		PrintWriter writer = new PrintWriter(entityStream);
//		Gson gson = new Gson();
//		ServerWrapper wrapper = new ServerWrapper(server);
//		writer.println(gson.toJson(wrapper));
//		writer.flush();
	}

	@Override
	public boolean isReadable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return Server.class.isAssignableFrom(type);
	}

	/**
	 * Reading from HTTP Request, incoming REST Call from OpenStack Clients
	 */
	@Override
	public Server readFrom(Class<Server> type, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
			throws IOException, WebApplicationException {

		/**
		Example Headers:
		
		{Host=[134.60.30.73:9090], 
		Accept=[application/json], 
		Content-Length=[133],
		Content-Type=[application/json], 
		X-Auth-Token=[MIINGgYJKoZIhvcNAQcCoIINCzCCDQcCAQExCTAHBgUrDgMCGjCCC3AGCSqGSIb3DQEHAaCCC2EEggtdeyJhY2Nlc3MiOiB7InRva2VuIjogeyJpc3N1ZWRfYXQiOiAiMjAxNS0wNi0zMFQxNDoyMzo0MS4wODY1NzQiLCAiZXhwaXJlcyI6ICIyMDE1LTA2LTMwVDE1OjIzOjQxWiIsICJpZCI6ICJwbGFjZWhvbGRlciIsICJ0ZW5hbnQiOiB7ImRlc2NyaXB0aW9uIjogIiIsICJlbmFibGVkIjogdHJ1ZSwgImlkIjogIjI4ODQ0ZjJmZTBjNTRhYjdhZmEwNjBjNjIyMjBjMGMyIiwgIm5hbWUiOiAiY2FjdG9zIn19LCAic2VydmljZUNhdGFsb2ciOiBbeyJlbmRwb2ludHMiOiBbeyJhZG1pblVSTCI6ICJodHRwOi8vY29udHJvbG5vZGU6ODc3NC92Mi8yODg0NGYyZmUwYzU0YWI3YWZhMDYwYzYyMjIwYzBjMiIsICJyZWdpb24iOiAicmVnaW9uT25lIiwgImludGVybmFsVVJMIjogImh0dHA6Ly9jb250cm9sbm9kZTo4Nzc0L3YyLzI4ODQ0ZjJmZTBjNTRhYjdhZmEwNjBjNjIyMjBjMGMyIiwgImlkIjogIjJjNThhZTY0ZjVkMTRjN2FhMTIxMzEwNzAwNGFiNGE3IiwgInB1YmxpY1VSTCI6ICJodHRwOi8vb21pc3RhY2suZS10ZWNobmlrLnVuaS11bG0uZGU6ODc3NC92Mi8yODg0NGYyZmUwYzU0YWI3YWZhMDYwYzYyMjIwYzBjMiJ9XSwgImVuZHBvaW50c19saW5rcyI6IFtdLCAidHlwZSI6ICJjb21wdXRlIiwgIm5hbWUiOiAibm92YSJ9LCB7ImVuZHBvaW50cyI6IFt7ImFkbWluVVJMIjogImh0dHA6Ly9jb250cm9sbm9kZTo5Njk2IiwgInJlZ2lvbiI6ICJyZWdpb25PbmUiLCAiaW50ZXJuYWxVUkwiOiAiaHR0cDovL2NvbnRyb2xub2RlOjk2OTYiLCAiaWQiOiAiMjM4ZTQ5ZWVjZmM0NGZmMTg0MGNjYjkxYzFiYjE0ZTAiLCAicHVibGljVVJMIjogImh0dHA6Ly9vbWlzdGFjay5lLXRlY2huaWsudW5pLXVsbS5kZTo5Njk2In1dLCAiZW5kcG9pbnRzX2xpbmtzIjogW10sICJ0eXBlIjogIm5ldHdvcmsiLCAibmFtZSI6ICJuZXV0cm9uIn0sIHsiZW5kcG9pbnRzIjogW3siYWRtaW5VUkwiOiAiaHR0cDovL2NvbnRyb2xub2RlOjg3NzYvdjIvMjg4NDRmMmZlMGM1NGFiN2FmYTA2MGM2MjIyMGMwYzIiLCAicmVnaW9uIjogInJlZ2lvbk9uZSIsICJpbnRlcm5hbFVSTCI6ICJodHRwOi8vY29udHJvbG5vZGU6ODc3Ni92Mi8yODg0NGYyZmUwYzU0YWI3YWZhMDYwYzYyMjIwYzBjMiIsICJpZCI6ICIwMGE3ZjljMTVhNzU0YzdkYjVmYjUxOTcwOTRjNDlhNyIsICJwdWJsaWNVUkwiOiAiaHR0cDovL29taXN0YWNrLmUtdGVjaG5pay51bmktdWxtLmRlOjg3NzYvdjIvMjg4NDRmMmZlMGM1NGFiN2FmYTA2MGM2MjIyMGMwYzIifV0sICJlbmRwb2ludHNfbGlua3MiOiBbXSwgInR5cGUiOiAidm9sdW1ldjIiLCAibmFtZSI6ICJjaW5kZXJ2MiJ9LCB7ImVuZHBvaW50cyI6IFt7ImFkbWluVVJMIjogImh0dHA6Ly9jb250cm9sbm9kZTo5MjkyIiwgInJlZ2lvbiI6ICJyZWdpb25PbmUiLCAiaW50ZXJuYWxVUkwiOiAiaHR0cDovL2NvbnRyb2xub2RlOjkyOTIiLCAiaWQiOiAiMzJlNTg0MTQwZGZlNDhlMWE0ZWRhMzQ5NmUwN2ZiNDgiLCAicHVibGljVVJMIjogImh0dHA6Ly9vbWlzdGFjay5lLXRlY2huaWsudW5pLXVsbS5kZTo5MjkyIn1dLCAiZW5kcG9pbnRzX2xpbmtzIjogW10sICJ0eXBlIjogImltYWdlIiwgIm5hbWUiOiAiZ2xhbmNlIn0sIHsiZW5kcG9pbnRzIjogW3siYWRtaW5VUkwiOiAiaHR0cDovL2NvbnRyb2xub2RlOjg3NzciLCAicmVnaW9uIjogInJlZ2lvbk9uZSIsICJpbnRlcm5hbFVSTCI6ICJodHRwOi8vY29udHJvbG5vZGU6ODc3NyIsICJpZCI6ICI3ODBhNWQ2NzU3MTM0Mjk1OGFiYTlmOWVmMmQyNGVhYiIsICJwdWJsaWNVUkwiOiAiaHR0cDovL29taXN0YWNrLmUtdGVjaG5pay51bmktdWxtLmRlOjg3NzcifV0sICJlbmRwb2ludHNfbGlua3MiOiBbXSwgInR5cGUiOiAibWV0ZXJpbmciLCAibmFtZSI6ICJjZWlsb21ldGVyIn0sIHsiZW5kcG9pbnRzIjogW3siYWRtaW5VUkwiOiAiaHR0cDovL2NvbnRyb2xub2RlOjg3NzYvdjEvMjg4NDRmMmZlMGM1NGFiN2FmYTA2MGM2MjIyMGMwYzIiLCAicmVnaW9uIjogInJlZ2lvbk9uZSIsICJpbnRlcm5hbFVSTCI6ICJodHRwOi8vY29udHJvbG5vZGU6ODc3Ni92MS8yODg0NGYyZmUwYzU0YWI3YWZhMDYwYzYyMjIwYzBjMiIsICJpZCI6ICI1MTdjNjE3MDcyMGY0NDk0YTRhY2QxMjIyZTkzMzE5ZCIsICJwdWJsaWNVUkwiOiAiaHR0cDovL29taXN0YWNrLmUtdGVjaG5pay51bmktdWxtLmRlOjg3NzYvdjEvMjg4NDRmMmZlMGM1NGFiN2FmYTA2MGM2MjIyMGMwYzIifV0sICJlbmRwb2ludHNfbGlua3MiOiBbXSwgInR5cGUiOiAidm9sdW1lIiwgIm5hbWUiOiAiY2luZGVyIn0sIHsiZW5kcG9pbnRzIjogW3siYWRtaW5VUkwiOiAiaHR0cDovL2NvbnRyb2xub2RlOjM1MzU3L3YyLjAiLCAicmVnaW9uIjogInJlZ2lvbk9uZSIsICJpbnRlcm5hbFVSTCI6ICJodHRwOi8vY29udHJvbG5vZGU6NTAwMC92Mi4wIiwgImlkIjogIjIyMjk3MDFlMGNiZTQ2ZGU5NmNlZTk1YjU4NzdjODMyIiwgInB1YmxpY1VSTCI6ICJodHRwOi8vb21pc3RhY2suZS10ZWNobmlrLnVuaS11bG0uZGU6NTAwMC92Mi4wIn1dLCAiZW5kcG9pbnRzX2xpbmtzIjogW10sICJ0eXBlIjogImlkZW50aXR5IiwgIm5hbWUiOiAia2V5c3RvbmUifV0sICJ1c2VyIjogeyJ1c2VybmFtZSI6ICJoYXVzZXIiLCAicm9sZXNfbGlua3MiOiBbXSwgImlkIjogIjVlMDQzNzcwY2VmNjQ2MWFhYTllZmVmMWM1NGM0NWIzIiwgInJvbGVzIjogW3sibmFtZSI6ICJfbWVtYmVyXyJ9LCB7Im5hbWUiOiAiYWRtaW4ifSwgeyJuYW1lIjogIk1lbWJlciJ9XSwgIm5hbWUiOiAiaGF1c2VyIn0sICJtZXRhZGF0YSI6IHsiaXNfYWRtaW4iOiAwLCAicm9sZXMiOiBbIjlmZTJmZjllZTQzODRiMTg5NGE5MDg3OGQzZTkyYmFiIiwgImUzODdlMjJhZTNhNDQ3OGM4NTFlMDU0NDgwMzIzM2ZhIiwgImE2ZWIwMjVjY2UxZjRjYTJiYTdmYmEwZjZmMzcyNDkyIl19fX0xggGBMIIBfQIBATBcMFcxCzAJBgNVBAYTAlVTMQ4wDAYDVQQIDAVVbnNldDEOMAwGA1UEBwwFVW5zZXQxDjAMBgNVBAoMBVVuc2V0MRgwFgYDVQQDDA93d3cuZXhhbXBsZS5jb20CAQEwBwYFKw4DAhowDQYJKoZIhvcNAQEBBQAEggEAf37dY9lQUOOrI4nxfx+B5bk-BIOSgl2iJXxiT0SPq5pM2CUpcGrVZeWOL6P41324u2pOF5EuNlI+4c9ubsDXKgDQkMkiqcD2KZq4+bzYmb9zQCyrCFQ-S0F0U8H3zXJcSg7PxjPYXoyEMRvX0h7vKlFhmH+rdyZvfRl5IQ+GpRdr6rncg+cM56fKTdanWrdhlUsGEiBEBH6bhcKB2-y3cJUXOs1ThXzQGcc5mzt7ay14ID02ta9mX135+7w6LkQP8yQYntLUKcwYv02OAL2upUAi4iNGddE77ETW7Fo-JGWBh3zJ3gq41ldSVccScDk3e0ATeLEkFAr7aHPnH6KybQ==], 
		X-Auth-Project-Id=[cactos], 
		Connection=[keep-alive], 
		User-Agent=[python-novaclient], 
		Accept-Encoding=[gzip, deflate]}
		
		Example JSON:
		{"server": 
			{"min_count": 1, 
			"flavorRef": "1", 
			"name": "ch-test", 
			"imageRef": "bff7c603-52ad-43e2-bb7d-573c757fefe9", 
			"max_count": 1}
		}
		 */
		
		String json = "";
		try {
			// Parse JSON
			json = scanInput(entityStream, httpHeaders);
			JsonElement element = scanJson(json);
			Server server = new Server();
			
			JsonObject jobject = element.getAsJsonObject();

			// Read required values and fill server object
			server.setRequest_json(json);
			server.setRequest_headers(httpHeaders);
			pickFlavour(jobject, server);
		    pickImage(jobject, server);
		    pickMetadata(jobject, server);
		    
			return server;
//		} catch(IOException ex) {
//			logger.log(Level.SEVERE, "'unexpected ioexception", ex);
//			throw ex;
		} catch(WebApplicationException webex) {
			logger.log(Level.SEVERE, "'unexpected exception: " + json, webex);
			throw webex;
		} catch(Error er) {
			logger.log(Level.SEVERE, "'unexpected error"+ json, er);
			throw er;
		} catch(RuntimeException ex) {
			logger.log(Level.SEVERE, "'unexpected exception"+ json, ex);
			throw ex;
		} catch(Throwable t) {
			logger.log(Level.SEVERE, "'unexpected throwable"+ json, t);
			throw new WebApplicationException(t);
		}
	}
	
	private void assertServer(JsonObject jobject) {
		JsonObject obj = jobject.getAsJsonObject("server");
		if(obj == null) {
			logger.log(Level.SEVERE, "'server' object is null");
			throw new NullPointerException("'server' object is null");
		}
	}
	
	private void pickMetadata(JsonObject jobject, Server server) {
		HashMap<String, String> metadata = new HashMap<String, String>();
		if(jobject.getAsJsonObject("server").get("metadata") != null){
			JsonElement el = jobject.getAsJsonObject("server").get("metadata"); 
			if(el.isJsonObject()){
				JsonObject jObj = el.getAsJsonObject();
				for (Map.Entry<String,JsonElement> entry : jObj.entrySet()) {
					metadata.put(entry.getKey(), entry.getValue().getAsString());
				}
			} else {
				logger.log(Level.SEVERE, "'metadata' is not JSON, " + el);
			}
	    }
	    server.setMetadata(metadata);
	}
	
	private void pickImage(JsonObject jobject, Server server) {
		String imageRef = jobject.getAsJsonObject("server").get("imageRef").getAsString();
		if(imageRef == null) {
			logger.log(Level.SEVERE, "'imageRef' object is null");
			throw new NullPointerException("'imageRef' object is null");
		}
		server.setImageRef(imageRef);
	}
	
	private void pickFlavour(JsonObject jobject, Server server) {
		String flavorRef = jobject.getAsJsonObject("server").get("flavorRef").getAsString();
		if(flavorRef == null) {
			logger.log(Level.SEVERE, "'flavorRef' object is null");
			throw new NullPointerException("'flavorRef' object is null");
		}
		server.setFlavorRef(flavorRef);
	}
	
	private String scanInput(InputStream entityStream, MultivaluedMap<String, String> httpHeaders) {
		Scanner scanner = new Scanner(entityStream);
		StringBuffer buf = new StringBuffer();
		while(scanner.hasNext())
				buf.append(scanner.nextLine());
		scanner.close();
		String json = buf.toString();
		logger.log(Level.INFO, "hijacking http request. Headers: " + httpHeaders + ", Body: " + json);
		return json;
	}
	
	
	private JsonElement scanJson(String json) {
		JsonElement element;
		try{
			element = new JsonParser().parse(json);
		}catch(JsonSyntaxException e){
			logger.log(Level.SEVERE, "Cannot parse incoming request's json payload.", e);
			throw e;
		}
		return element;
	}


}
