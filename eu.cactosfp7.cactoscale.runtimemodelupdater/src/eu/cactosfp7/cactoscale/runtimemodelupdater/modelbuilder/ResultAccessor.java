package eu.cactosfp7.cactoscale.runtimemodelupdater.modelbuilder;

public interface ResultAccessor {

	public <T> T getResult(String rowKey, String familyName, String qualifierName, Class<T> clazz, T defVal) ;
	
	public <T> T getResultMap(String rowKey, String familyName) ;
}
