package eu.cactosfp7.cactoscale.runtimemodelupdater.generation;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.eclipse.emf.cdo.eresource.CDOResource;
import org.eclipse.emf.cdo.transaction.CDOTransaction;
import org.eclipse.emf.cdo.util.CommitException;
import org.eclipse.emf.cdo.util.ConcurrentAccessException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.net4j.Net4jUtil;
import org.eclipse.net4j.tcp.TCPUtil;
import org.eclipse.net4j.util.container.IPluginContainer;

import eu.cactosfp7.cdosession.CactosCdoSession;
import eu.cactosfp7.infrastructuremodels.load.logical.LogicalFactory;
import eu.cactosfp7.infrastructuremodels.load.physical.PhysicalFactory;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.CoreFactory;

public class ModelLoaderCdo {

	private CactosCdoSession cactosCdoSession;
	private CDOTransaction cdoTransaction;
	private final static Logger logger = Logger.getLogger(ModelLoaderCdo.class);

	public void setCactosCdoSession(CactosCdoSession cactosCdoSession, CDOTransaction cdoTransaction) {
		this.cactosCdoSession = cactosCdoSession;
		this.cdoTransaction = cdoTransaction;
	}

	public ArrayList<Object> loadModelInstances() {

		ArrayList<Object> result = new ArrayList<Object>();

		try {

			Net4jUtil.prepareContainer(IPluginContainer.INSTANCE);
			TCPUtil.prepareContainer(IPluginContainer.INSTANCE);

			CDOResource physicalDCResource = cactosCdoSession.getResource(cdoTransaction,
					cactosCdoSession.getPhysicalModelPath());
			CDOResource logicalDCResource = cactosCdoSession.getResource(cdoTransaction,
					cactosCdoSession.getLogicalModelPath());
			CDOResource physicalLoadResource = cactosCdoSession.getResource(cdoTransaction,
					cactosCdoSession.getPhysicalLoadPath());
			CDOResource logicalLoadResource = cactosCdoSession.getResource(cdoTransaction,
					cactosCdoSession.getLogicalLoadPath());
			CDOResource hypervisorTypeResource = cactosCdoSession.getResource(cdoTransaction,
					cactosCdoSession.getHypervisorPath());
			CDOResource architectureTypeResource = cactosCdoSession.getResource(cdoTransaction,
					cactosCdoSession.getArchitectureTypePath());

			EList<EObject> contentsPhysicalDCResource = physicalDCResource.getContents();
			EList<EObject> contentsPhysicalLoadResource = physicalLoadResource.getContents();
			EList<EObject> contentsHypervisorTypeResource = hypervisorTypeResource.getContents();
			EList<EObject> contentsLogicalDCResource = logicalDCResource.getContents();
			EList<EObject> contentslLogicalLoadResource = logicalLoadResource.getContents();
			EList<EObject> contentsArchitectureTypeResource = architectureTypeResource.getContents();

			// We assume there is only 1 (root) model per resource
			if (contentsPhysicalDCResource.size() == 0) {
				contentsPhysicalDCResource.add(CoreFactory.INSTANCE.createPhysicalDCModel());
			}
			result.add(contentsPhysicalDCResource.get(0));

			if (contentsPhysicalLoadResource.size() == 0) {
				contentsPhysicalLoadResource.add(PhysicalFactory.INSTANCE.createPhysicalLoadModel());
			}
			result.add(contentsPhysicalLoadResource.get(0));

			if (contentsHypervisorTypeResource.size() == 0) {
				contentsHypervisorTypeResource
						.add(eu.cactosfp7.infrastructuremodels.logicaldc.hypervisor.HypervisorFactory.INSTANCE
								.createHypervisorRepository());
			}
			result.add(contentsHypervisorTypeResource.get(0));

			if (contentsLogicalDCResource.size() == 0) {
				contentsLogicalDCResource.add(
						eu.cactosfp7.infrastructuremodels.logicaldc.core.CoreFactory.INSTANCE.createLogicalDCModel());
			}
			result.add(contentsLogicalDCResource.get(0));

			if (contentslLogicalLoadResource.size() == 0) {
				contentslLogicalLoadResource.add(LogicalFactory.INSTANCE.createLogicalLoadModel());
			}
			result.add(contentslLogicalLoadResource.get(0));

			if (contentsArchitectureTypeResource.size() == 0) {
				contentsArchitectureTypeResource
						.add(eu.cactosfp7.infrastructuremodels.physicaldc.architecturetype.ArchitecturetypeFactory.INSTANCE
								.createArchitectureTypeRepository());
			}
			result.add(contentsArchitectureTypeResource.get(0));

			return result;

		} catch (Exception e) {
			cdoTransaction.rollback();
			logger.error("Loading data from the CDO repo failed", e);
			return null;
		}
	}

	public void commitAndCloseTransaction() {
		try {
			cactosCdoSession.commitAndCloseConnection(cdoTransaction);
		} catch (ConcurrentAccessException e) {
			cdoTransaction.rollback();
			logger.error("cannot commit", e);
		} catch (CommitException e) {
			cdoTransaction.rollback();
			logger.error("cannot commit", e);
		} finally {
			cactosCdoSession.closeConnection(cdoTransaction);
		}
	}
}
