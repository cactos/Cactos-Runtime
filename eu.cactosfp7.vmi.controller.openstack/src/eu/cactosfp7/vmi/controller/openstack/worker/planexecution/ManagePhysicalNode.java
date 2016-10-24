package eu.cactosfp7.vmi.controller.openstack.worker.planexecution;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.cdo.common.id.CDOID;
import org.eclipse.emf.cdo.transaction.CDOTransaction;
import org.eclipse.emf.cdo.util.CommitException;
import org.eclipse.emf.cdo.view.CDOView;

import eu.cactosfp7.cdosession.CactosCdoSession;
import eu.cactosfp7.cdosession.settings.CactosUser;
import eu.cactosfp7.cdosessionclient.CdoSessionClient;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.AbstractNode;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.NodeState;
import eu.cactosfp7.optimisationplan.ExecutionStatus;
import eu.cactosfp7.runtimemanagement.util.PropagateToChukwa;
import eu.cactosfp7.vmi.controller.openstack.VMIServiceImpl;

public class ManagePhysicalNode implements OptimisationActionStepExecution {

	AbstractNode managedNode;
	NodeState targetState;
	
	private final CactosCdoSession cactosCdoSession;
	
	private static final Logger logger = Logger.getLogger(ManagePhysicalNode.class.getName());
	
	public ManagePhysicalNode(AbstractNode managedNode, NodeState targetState) {
		this.managedNode = managedNode;
		this.targetState = targetState;
		cactosCdoSession = CdoSessionClient.INSTANCE.getService()
				.getCactosCdoSession(CactosUser.CACTOSCALE);		
	}

	@Override
	public ExecutionStatus execute() {
		// lookup config variables
		String ipmiproxy_token = VMIServiceImpl.ipmiProxyConf.getToken();
		String ipmiproxy_address = VMIServiceImpl.ipmiProxyConf.getAddress();
		int ipmiproxy_port = VMIServiceImpl.ipmiProxyConf.getPort();

		// Build request to IPMI-Proxy
		String action;
		if(targetState == NodeState.OFF){
			action = "off";
			PropagateToChukwa.writeState(managedNode.getName(), action);
		}else if(targetState == NodeState.RUNNING){
			action = "on";
		}else{
			logger.severe("targetState not useful");
			return ExecutionStatus.COMPLETED_FAILED;
		}
		String host = managedNode.getName();
		String request = ipmiproxy_token + " " + action + " " + host;
		String response = "";
		
		// Send request to IPMI-Proxy
		try{
		  Socket clientSocket = new Socket(ipmiproxy_address, ipmiproxy_port);   
		  DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());   
		  BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));   
		  outToServer.writeBytes(request + '\n');  
		  response = inFromServer.readLine();
		  logger.fine("For ManagePhysicalNode, ipmiproxy answers: " + response);
		  clientSocket.close();
		}catch(IOException ioexception){
			logger.severe("IOException while ManagePhysicalNode: " + ioexception.getMessage());
			return ExecutionStatus.COMPLETED_FAILED;
		}
		
		// interprete response
		if(action.equals("off") && !response.equals("Chassis Power Control: Soft")){
			return ExecutionStatus.COMPLETED_FAILED; 
		}
		if(action.equals("on") && !response.equals("Chassis Power Control: Up/On")){
			return ExecutionStatus.COMPLETED_FAILED; 
		} 

		// Change Power state of computenode to OFF 
		// to tell ModelUpdater this is by intention and not a failure
		
		if(action.equals("off")){
			if(!changeNodeState(managedNode.cdoID(), NodeState.OFF)){
				logger.log(Level.SEVERE, "Changing state of node failed. ManagePhysicalNode step failed.");
				return ExecutionStatus.COMPLETED_FAILED;
			}			
		}else if(action.equals("on")){
			if(!changeNodeState(managedNode.cdoID(), NodeState.UNKNOWN)){
				logger.log(Level.SEVERE, "Changing state of node failed. ManagePhysicalNode step failed.");
				return ExecutionStatus.COMPLETED_FAILED;
			}			
		}
		
		// Turning on, wait until it's up!
		if(action.equals("on")){
			// check periodically if hypervisor is running
			int i = 0;
			int MAX_TRIES = 520;
			while(i < MAX_TRIES){
				if(nodeIsRunning(managedNode.cdoID())){
					// migration executed successfully
					return ExecutionStatus.COMPLETED_SUCCESSFUL;	
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					logger.log(Level.SEVERE, "Cannot wait before next state lookup", e);
				}
				i++;
			}
			logger.log(Level.SEVERE, "ManagePhysicalNode turn on lookup gives up after " + i + " checks.");
			return ExecutionStatus.COMPLETED_FAILED;			
		}
		
		return ExecutionStatus.COMPLETED_SUCCESSFUL;
	}

	private boolean nodeIsRunning(CDOID cdoID) {
		CDOView view = cactosCdoSession.createView();
		try {
			AbstractNode node = (AbstractNode) view.getObject(cdoID);
			return NodeState.RUNNING.equals(node.getState());
		}finally{
			cactosCdoSession.closeConnection(view);
		}
	}

	private boolean changeNodeState(CDOID cdoID, NodeState state) {
		CDOTransaction transaction = cactosCdoSession.createTransaction();
		try {
			AbstractNode node = (AbstractNode) transaction.getObject(cdoID);
			node.setState(state);
			cactosCdoSession.commitAndCloseConnection(transaction);
			return true;
		}catch(CommitException e){
			if(transaction != null)
				transaction.rollback();
			logger.log(Level.SEVERE, "could not set state of AbstractNode " + cdoID + " to " + state, e);
			return false;
		}finally{
			cactosCdoSession.closeConnection(transaction);
		}
	}

}
