package eu.cactosfp7.cactoscale.runtimemodelupdater.generation;

import org.eclipse.emf.cdo.eresource.CDOResource;
import org.eclipse.emf.cdo.net4j.CDONet4jSession;
import org.eclipse.emf.cdo.net4j.CDONet4jSessionConfiguration;
import org.eclipse.emf.cdo.net4j.CDONet4jUtil;
import org.eclipse.emf.cdo.transaction.CDOTransaction;
import org.eclipse.net4j.Net4jUtil;
import org.eclipse.net4j.connector.IConnector;
import org.eclipse.net4j.tcp.TCPUtil;
import org.eclipse.net4j.util.container.IPluginContainer;
import org.eclipse.net4j.util.lifecycle.ILifecycle;
import org.eclipse.net4j.util.lifecycle.LifecycleEventAdapter;
import org.eclipse.net4j.util.lifecycle.LifecycleUtil;
import org.eclipse.net4j.util.security.PasswordCredentialsProvider;
@Deprecated
public class CdoServerConnection {

	private CDONet4jSession session = null;
	private CDOTransaction transaction = null;
	private String cdoServerResourcePrefix = null;
	private CDOResource physicalDCResource = null;
	private CDOResource physicalLoadResource = null;
	private CDOResource logicalDCResource = null;
	private CDOResource logicalLoadResource = null;
	private CDOResource architectureTypeResource = null;
	private CDOResource hypervisorTypeResource = null;

	public CdoServerConnection(String repositoryName, String productGroup,
			String protocolType, String serverUri,
			String cdoServerResourcePrefix, String user, String password) {
		this.session = this.openSession(repositoryName, productGroup,
				protocolType, serverUri, user, password);
		this.cdoServerResourcePrefix = cdoServerResourcePrefix;
	}

	private CDONet4jSession openSession(String repositoryName,
			String productGroup, String protocolType, String serverUri,
			String user, String password) {

		Net4jUtil.prepareContainer(IPluginContainer.INSTANCE);
		TCPUtil.prepareContainer(IPluginContainer.INSTANCE);
		CDONet4jUtil.prepareContainer(IPluginContainer.INSTANCE); // Register
																	// CDO
																	// factories
		LifecycleUtil.activate(IPluginContainer.INSTANCE);

		final IConnector connector = (IConnector) IPluginContainer.INSTANCE
				.getElement( //
						productGroup, // Product group
						protocolType, // Type
						serverUri // Description
				);

		CDONet4jSessionConfiguration config = CDONet4jUtil
				.createNet4jSessionConfiguration();
		config.setConnector(connector);
		config.setRepositoryName(repositoryName);

		// Create credentials
		PasswordCredentialsProvider credentialsProvider = new PasswordCredentialsProvider(
				user, password);
		config.setCredentialsProvider(credentialsProvider);

		this.session = config.openNet4jSession();

		this.session.addListener(new LifecycleEventAdapter() {
			@Override
			protected void onDeactivated(ILifecycle lifecycle) {
//				connector.close();
			}
		});

		System.out.println("SESSION: " + session);

		// this.getTransaction();

		return this.session;
	}

	public CDONet4jSession getSession(/* add arguments again from cstr optionally */) {
		return this.session;
	}

	public CDOTransaction getTransaction() {
		if (this.transaction == null) {
			this.transaction = this.getSession().openTransaction();
		}
		// this.transaction.options().setStaleReferencePolicy(CDOStaleReferencePolicy.PROXY);
		return this.transaction;
	}

	public CDOResource getPhysicalDCResource() {
		if (this.physicalDCResource == null) {
			this.physicalDCResource = this.getTransaction()
					.getOrCreateResource(
							this.cdoServerResourcePrefix + "/physical");
		}
		return this.physicalDCResource;
	}

	public CDOResource getPhysicalLoadResource() {
		if (this.physicalLoadResource == null) {
			this.physicalLoadResource = this.getTransaction()
					.getOrCreateResource(
							this.cdoServerResourcePrefix + "/physical_load");
		}
		return this.physicalLoadResource;
	}

	public CDOResource getHypervisorTypeResource() {
		if (this.hypervisorTypeResource == null) {
			this.hypervisorTypeResource = this.getTransaction()
					.getOrCreateResource(
							this.cdoServerResourcePrefix + "/hypervisor");
		}
		return this.hypervisorTypeResource;
	}

	public CDOResource getArchitectureTypeResource() {
		if (this.architectureTypeResource == null) {
			this.architectureTypeResource = this.getTransaction()
					.getOrCreateResource(
							this.cdoServerResourcePrefix + "/architecturetype");
		}
		return this.architectureTypeResource;
	}

	public CDOResource getLogicalDCResource() {
		if (this.logicalDCResource == null) {
			this.logicalDCResource = this.getTransaction().getOrCreateResource(
					this.cdoServerResourcePrefix + "/logical");
		}
		return this.logicalDCResource;
	}

	public CDOResource getLogicalLoadesource() {
		if (this.logicalLoadResource == null) {
			this.logicalLoadResource = this.getTransaction()
					.getOrCreateResource(
							this.cdoServerResourcePrefix + "/logical_load");
		}
		return this.logicalLoadResource;
	}
}
