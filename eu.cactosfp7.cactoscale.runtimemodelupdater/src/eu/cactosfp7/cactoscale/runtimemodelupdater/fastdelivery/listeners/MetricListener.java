package eu.cactosfp7.cactoscale.runtimemodelupdater.fastdelivery.listeners;

import java.util.EventListener;

public interface MetricListener extends EventListener {
	public void handleMetric(Metric e);
}
