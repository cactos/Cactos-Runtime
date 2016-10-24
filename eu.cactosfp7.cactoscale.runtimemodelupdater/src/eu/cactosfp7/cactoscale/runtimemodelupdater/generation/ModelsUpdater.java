package eu.cactosfp7.cactoscale.runtimemodelupdater.generation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.emf.cdo.transaction.CDOTransaction;
import org.eclipse.emf.cdo.util.CommitException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.util.EcoreUtil;

import eu.cactosfp7.cactoscale.runtimemodelupdater.SettingsInitializer;
import eu.cactosfp7.cdosession.CactosCdoSession;
import eu.cactosfp7.cdosession.settings.CactosUser;
import eu.cactosfp7.cdosession.util.CdoHelper;
import eu.cactosfp7.cdosessionclient.CdoSessionClient;
import eu.cactosfp7.infrastructuremodels.load.logical.LogicalLoadModel;
import eu.cactosfp7.infrastructuremodels.load.physical.PhysicalLoadModel;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.LogicalDCModel;
import eu.cactosfp7.infrastructuremodels.logicaldc.hypervisor.HypervisorRepository;
import eu.cactosfp7.infrastructuremodels.logicaldc.hypervisor.HypervisorType;
import eu.cactosfp7.infrastructuremodels.physicaldc.architecturetype.ArchitectureType;
import eu.cactosfp7.infrastructuremodels.physicaldc.architecturetype.ArchitectureTypeRepository;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.PhysicalDCModel;

public class ModelsUpdater implements Runnable {

	private final static Logger logger = Logger.getLogger(ModelsUpdater.class);

	private ModelLoaderCdo cdoModelLoader = null;
	private ModelLoaderHbase hbaseModelLoader = null;
	private Collection<String> availableComputenodeNodes = new ArrayList<String>();

	public ModelsUpdater() {
	}

	public void run() {
		final CactosCdoSession cactosCdoSession = CdoSessionClient.INSTANCE.getService().getCactosCdoSession(CactosUser.CACTOSCALE);
		availableComputenodeNodes.clear();
		String filteredComputenodesString = SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.FILTERED_COMPUTE_NODES);
		if (filteredComputenodesString != null) {
			String[] filteredComputenodes = filteredComputenodesString.split(";");
			for (int i = 0; i < filteredComputenodes.length; i++) {
				availableComputenodeNodes.add(filteredComputenodes[i]);
			}
		}

		CDOTransaction cdoTransaction = null;
		try {
			logger.info("MODELS GENERATION START");
			cdoModelLoader = new ModelLoaderCdo();
			cdoTransaction = cactosCdoSession.createTransaction();
			// cdoTransaction.getSession().refresh();
			cdoModelLoader.setCactosCdoSession(cactosCdoSession, cdoTransaction);
			ArrayList<Object> cdoModels = cdoModelLoader.loadModelInstances();
			
			long start_model_generation = System.currentTimeMillis();
			hbaseModelLoader = new ModelLoaderHbase();
			hbaseModelLoader.setCactosCdoSession(cactosCdoSession, cdoTransaction);
			logger.info("Load hbase models time START");
			long start_time_load_hbase_Models = System.currentTimeMillis();
			List<Object> hbaseModels = hbaseModelLoader.loadModelInstances();
			long end_time_load_hbase_Models = System.currentTimeMillis();
			logger.info("Load hbase models time " + (end_time_load_hbase_Models - start_time_load_hbase_Models) + "ms");
			logger.info("Load hbase models time END");
			logger.info("Comparison time START");
			long start_time_frank = System.currentTimeMillis();

			PhysicalDCModel cdoPhysicalDCModel = CdoHelper.findPhysicalDCModel(cdoModels);
			PhysicalDCModel hbasePhysicalDCModel = CdoHelper.findPhysicalDCModel(hbaseModels);

			LogicalDCModel cdoLogicalDCModel = CdoHelper.findLogicalDCModel(cdoModels);
			LogicalDCModel hbaseLogicalDCModel = CdoHelper.findLogicalDCModel(hbaseModels);

			LogicalLoadModel cdoLogicalLoadModel = CdoHelper.findLogicalLoadModel(cdoModels);
			LogicalLoadModel hbaseLogicalLoadModel = CdoHelper.findLogicalLoadModel(hbaseModels);

			PhysicalLoadModel cdoPhysicalLoadModel = CdoHelper.findPhysicalLoadModel(cdoModels);
			PhysicalLoadModel hbasePhysicalLoadModel = CdoHelper.findPhysicalLoadModel(hbaseModels);

			ArchitectureTypeRepository cdoArchitectureTypeRepository = CdoHelper.findArchitectureTypeRepository(cdoModels);
			ArchitectureTypeRepository hbaseArchitectureTypeRepository = CdoHelper.findArchitectureTypeRepository(hbaseModels);

			HypervisorRepository cdoHypervisorRepository = CdoHelper.findHypervisorRepository(cdoModels);
			HypervisorRepository hbaseHypervisorRepository = CdoHelper.findHypervisorRepository(hbaseModels);
			// START - Move to Cdo helper
			if (cdoArchitectureTypeRepository != null && hbaseArchitectureTypeRepository != null) {
				logger.debug("iterating cdo architecture types");
				EList<ArchitectureType> cdoArchitectureTypes = cdoArchitectureTypeRepository.getArchitectureTypes();
				EList<ArchitectureType> hbaseArchitectureTypes = hbaseArchitectureTypeRepository.getArchitectureTypes();

				List<ArchitectureType> toDelete = new ArrayList<ArchitectureType>();

				for (ArchitectureType cdoArchitectureType : cdoArchitectureTypes) {
					ArchitectureType hbaseArchitectureType = CdoHelper.getModelByIdentifier(hbaseArchitectureTypes, cdoArchitectureType.getName());

					if (hbaseArchitectureType != null) {
						logger.debug("ArchitectureType: It exists already in hbase: " + cdoArchitectureType.getName());
					} else {
						logger.debug("ArchitectureType: Delete the cdo one: " + cdoArchitectureType.getName());
						toDelete.add(cdoArchitectureType);
					}

				}

				for (ArchitectureType typeToDelete : toDelete) {
					EcoreUtil.delete(typeToDelete);
				}

				List<ArchitectureType> toAdd = new ArrayList<ArchitectureType>();

				for (ArchitectureType hbaseArchitectureType : hbaseArchitectureTypes) {
					logger.debug("iterating hbase architectureTypes.");
					ArchitectureType cdoArchitectureType = CdoHelper.getModelByIdentifier(cdoArchitectureTypes, hbaseArchitectureType.getName());

					if (cdoArchitectureType == null) {
						logger.debug("ArchitectureType: Add the hbase model: " + hbaseArchitectureType.getName());
						toAdd.add(hbaseArchitectureType);
					}
				}

				for (ArchitectureType typeToAdd : toAdd) {
					typeToAdd.setArchitectureTypeRepository(cdoArchitectureTypeRepository);
				}
			}

			if (cdoHypervisorRepository != null && hbaseHypervisorRepository != null) {
				logger.debug("iterating cdo hypervisor types");
				EList<HypervisorType> cdoHypervisorTypes = cdoHypervisorRepository.getHypervisorTypes();
				EList<HypervisorType> hbaseHypervisorTypes = hbaseHypervisorRepository.getHypervisorTypes();

				List<HypervisorType> toDelete = new ArrayList<HypervisorType>();

				for (HypervisorType cdoHypervisorType : cdoHypervisorTypes) {
					HypervisorType hbaseHypervisorType = CdoHelper.getModelByIdentifier(hbaseHypervisorTypes, cdoHypervisorType.getName());

					if (hbaseHypervisorType != null) {
						logger.debug("HypervisorType: It exists already: " + cdoHypervisorType.getName());
					} else {
						logger.debug("HypervisorType: Delete the cdo one." + cdoHypervisorType.getName());
						toDelete.add(cdoHypervisorType);
					}
				}

				for (HypervisorType typeToDelete : toDelete) {
					EcoreUtil.delete(typeToDelete);
				}

				List<HypervisorType> toAdd = new ArrayList<HypervisorType>();

				for (HypervisorType hbaseHypervisorType : hbaseHypervisorTypes) {
					logger.debug("iterating hbase hypervisor types");
					HypervisorType cdoHypervisorType = CdoHelper.getModelByIdentifier(cdoHypervisorTypes, hbaseHypervisorType.getName());

					if (cdoHypervisorType == null) {
						logger.debug("HypervisorType: Add the hbase model: " + hbaseHypervisorType.getName());
						toAdd.add(hbaseHypervisorType);
					}
				}

				for (HypervisorType typeToAdd : toAdd) {
					typeToAdd.setHypervisorRepository(cdoHypervisorRepository);
				}
			}
			// END - Move to Cdo helper
			// Only update utilisation measurements when fast delivery is off!
			boolean fastdeliveryEnabled = SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.ENABLE_FASTDELIVERY).equals("true");
			boolean updateUtilisation = !fastdeliveryEnabled;

			/**
			 * Update the Physical DC Repository and its nodes.
			 */
			CdoHelper.checkAndUpdatePhysicalDCModel(cdoPhysicalDCModel, hbasePhysicalDCModel, cdoLogicalDCModel, cdoHypervisorRepository, cdoArchitectureTypeRepository, updateUtilisation,
					availableComputenodeNodes);

			/**
			 * Update the Logical DC and its nodes.
			 */
			CdoHelper.checkAndUpdateLogicalDCModel(cdoLogicalDCModel, hbaseLogicalDCModel, cdoPhysicalDCModel, cdoHypervisorRepository, cdoArchitectureTypeRepository, updateUtilisation,
					availableComputenodeNodes);
			/**
			 * Fix references between the physical and logical dc
			 */
			CdoHelper.fixReferencesBetweenPhysicalDCAndLogicalDC(cdoPhysicalDCModel, cdoLogicalDCModel);
			/**
			 * Compare the LogicalLoadModel
			 */
			CdoHelper.checkAndUpdateLogicalLoadModel(cdoLogicalLoadModel, hbaseLogicalLoadModel, cdoLogicalDCModel, updateUtilisation, availableComputenodeNodes);
			/**
			 * Compare the PhysicalLoadModel
			 */
			CdoHelper.checkAndUpdatePhysicalLoadModel(cdoPhysicalLoadModel, hbasePhysicalLoadModel, cdoPhysicalDCModel, updateUtilisation, availableComputenodeNodes);
			long end_time_frank = System.currentTimeMillis();
			logger.info("Comparison time " + (end_time_frank - start_time_frank) + "ms");
			logger.info("Comparison time END");
			/**
			 * Commit and close the transaction
			 */
			long commit_time_start = System.currentTimeMillis();
			cdoModelLoader.commitAndCloseTransaction();
			logger.info("Runtime Model Updater finished!");

			long end_model_generation = System.currentTimeMillis();
			logger.info("COMMIT TIME: " + (end_model_generation - commit_time_start) + "ms");
			logger.info("TOTAL RUNTIME " + (end_model_generation - start_model_generation) + "ms");
			logger.info("MODELS GENERATION END");

		} catch (CommitException ex) {
			logger.error("Runtime Model Updater failed to commit!", ex);
			if(null != cdoTransaction)
				cdoTransaction.rollback();
		} catch (Exception ex) {
			logger.error("Runtime Model Updater failed!", ex);			
		} catch(Error er) {
			logger.error("Runtime Model Updater failed!", er);
		} finally {
			if(null != cdoTransaction)
				cactosCdoSession.closeConnection(cdoTransaction);
			logger.info("leaving update loop.");
		}
	}
};
