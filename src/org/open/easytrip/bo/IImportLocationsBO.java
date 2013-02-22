package org.open.easytrip.bo;

/**
 * Used by implementation classes that fulfill locations import actions. 
 * @author clessio
 */
public interface IImportLocationsBO {
	
	public interface OnReloadProgress {
		void reportProgress(int percentage);
	}

	/**
	 * Must test if a new import is necessary and, in this case, start it straight away.
	 * If a new (treated merely as different) import file is present, clear and reload non-user defined locations.
	 * @param progressListener 
	 * @param locationsCSV
	 */
	public int reloadLocations(OnReloadProgress progressListener);

}
