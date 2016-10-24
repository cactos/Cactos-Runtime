package eu.cactosfp7.cactoscale.runtimemodelupdater.modelbuilder;

public class RowKeyName implements NameKeyTuple {
	final String name;

	public RowKeyName(String _name) {
		name = _name;
	}

	public String getName() {
		return name;
	};
}
