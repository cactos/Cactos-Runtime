package eu.cactosfp7.cactoscale.runtimemodelupdater.fastdelivery.mappers;

import java.util.Map;

import eu.cactosfp7.cdosession.CactosCdoSession;

public interface MetricsMapper {
	void map(Map<String, String> metric, CactosCdoSession cactosCdoSession);
}
