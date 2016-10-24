package eu.cactosfp7.cactoscale.runtimemodelupdater.fastdelivery.listeners;

import java.util.EventObject;
import java.util.Map;

public class Metric extends EventObject {

	private static final long serialVersionUID = -3250686492263043080L;
	private Map<String, String> values;

	public Metric(Object source, Map<String, String> values) {
		super(source);
		this.values = values;
	}

	public Map<String, String> getValues() {
		return values;
	}

}
