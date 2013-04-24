package org.open.easytrip.dao;

import org.open.easytrip.R;
import org.open.easytrip.exception.AppRuntimeException;
import org.open.easytrip.helper.AppDatabaseHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.SoundPool;
import android.preference.PreferenceManager;

/**
 * Factory that instantiates and injects specific initialization properties in each DAO.
 * Completely threadsafe. 
 */
public class DAOFactory {

	public static DAOFactory mInstance;
	Context context;
	
	public static DAOFactory getInstance() {
		if (mInstance == null)
			throw new AppRuntimeException("DAOFactory must be intialized before asking for an instance.");
		return mInstance;
	}

	/**
	 * Hides the constructor
	 */
	private DAOFactory() {
	}
	
	/**
	 * Initializes the factory injecting the database connection.
	 * @param _db
	 */
	public synchronized static void init(Context _context) {
		if (mInstance != null)
			return;
		mInstance = new DAOFactory();
		mInstance.context = _context;
	}

	/*================== Singletons to be returned by the factory ==================*/
	
	LocationDAO locationDAO;
	public synchronized LocationDAO getLocationDAO() {
		if (locationDAO == null) {
				locationDAO = new LocationDAO();
				locationDAO.setDatabase(AppDatabaseHelper.getInstance(context).getWritableDatabase());//Injects database connection
		}
		return locationDAO;
	}
	
	DatabaseStructureDAO databaseStructureDAO;
	/**
	 * Passing an SQLiteDatabase parameter is mandatory while dealing with the database structure. As the database is still in process of opening,
	 * recurrent calls to getWritableDatabase will throw java.lang.IllegalStateException: getWritableDatabase called recursively 
	 * @param newDb
	 * @return
	 */
	public synchronized DatabaseStructureDAO getDatabaseStructureDAO(SQLiteDatabase newDb) {
		if (databaseStructureDAO == null) {
			databaseStructureDAO = new DatabaseStructureDAO();
			databaseStructureDAO.setDatabase(newDb);//Injects database connection
		}
		return databaseStructureDAO;
	}
	
	SharedPreferencesDAO sharedPreferencesDAO;
	public synchronized SharedPreferencesDAO getSharedPreferencesDAO() {
		if (sharedPreferencesDAO == null) {
			sharedPreferencesDAO = new SharedPreferencesDAO(
					PreferenceManager.getDefaultSharedPreferences(context),
					context.getResources());
		}
		return sharedPreferencesDAO;
	}
	
	AssetsDAO importAlertsDAO;
	public synchronized AssetsDAO getImportAlertsDAO() {
		if (importAlertsDAO == null) {
			importAlertsDAO = new AssetsDAO(context.getAssets());
		}
		return importAlertsDAO;
	}
	
	MemoryStorageDAO memoryStorageDAO;
	public synchronized MemoryStorageDAO getMemoryStorageDAO() {
		if (memoryStorageDAO == null) {
			memoryStorageDAO = new MemoryStorageDAO();
		}
		return memoryStorageDAO;
	}
	
}
