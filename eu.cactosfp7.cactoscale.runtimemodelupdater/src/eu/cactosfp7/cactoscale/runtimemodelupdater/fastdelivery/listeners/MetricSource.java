package eu.cactosfp7.cactoscale.runtimemodelupdater.fastdelivery.listeners;

import java.util.ArrayList;
import java.util.List;

public final class MetricSource {

	private final List<MetricListener> listeners = new ArrayList<MetricListener>();

	public synchronized void addEventListener(MetricListener listener) {
		listeners.add(listener);
	}

	public synchronized void removeEventListener(MetricListener listener) {
		listeners.remove(listener);
	}

	public void fireEvent(Metric e) {
		List<MetricListener> copy = null;
		synchronized (this) {
			copy = new ArrayList<MetricListener>(listeners);
		}
		processMetricEvent(e, copy);
	}

	private void processMetricEvent(Metric e, List<MetricListener> copy) {
		for (MetricListener listener : listeners) {
			listener.handleMetric(e);
		}
	}
}
