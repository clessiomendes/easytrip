package org.open.easytrip.bo;


public class BOFactory {

	public static BOFactory mInstance;
	
	public static BOFactory getInstance() {
		if (mInstance == null)
			init();
//			throw new AppRuntimeException("BOFactory must be intialized before asking for an instance.");
		return mInstance;
	}

	public static void init() {
		if (mInstance != null)
			return;
		mInstance = new BOFactory();
	}

	/*================== Singletons to be returned by the factory ==================*/
	
	UpdateLocationsBO updateLocationsBO;
	public UpdateLocationsBO getUpdateLocationsBO() {
		if (updateLocationsBO == null) {
			updateLocationsBO = new UpdateLocationsBO();
			//Place eventual injections here
		}
		return updateLocationsBO;
	}
	
	IImportLocationsBO importLocationsBO;
	public IImportLocationsBO getImportLocationsBO() {
		if (importLocationsBO == null) {
			//Using Igo as the unique import implementation. TODO Make this dynamic to support different import types (NDrive, Garmin, TomTom, etc)
			importLocationsBO = new IgoImportLocationsBO();
			//Place eventual injections here
		}
		return importLocationsBO;
	}
	
	RetrieveLocationsBO searchLocationsBO;
	public RetrieveLocationsBO getRetrieveLocationsBO() {
		if (searchLocationsBO == null) {
			searchLocationsBO = new RetrieveLocationsBO();
			//Place eventual injections here
		}
		return searchLocationsBO;
	}
	
	DatabaseStructureBO databaseStructureBO;
	public DatabaseStructureBO getDatabaseStructureBO() {
		if (databaseStructureBO == null) {
			databaseStructureBO = new DatabaseStructureBO();
			//Place eventual injections here
		}
		return databaseStructureBO;
	}
	
	AlertBO alertBO;
	public AlertBO getAlertBO() {
		if (alertBO == null) {
			alertBO = new AlertBO();
			//Place eventual injections here
		}
		return alertBO;
	}

	RetrievePreferencesBO preferencesBO;
	public RetrievePreferencesBO getPreferencesBO() {
		if (preferencesBO == null) {
			preferencesBO = new RetrievePreferencesBO();
			//Place eventual injections here
		}
		return preferencesBO;
	}

}
