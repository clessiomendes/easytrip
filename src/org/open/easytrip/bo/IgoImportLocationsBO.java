package org.open.easytrip.bo;

import java.io.BufferedReader;
import java.util.Date;

import org.open.easytrip.AppConstants;
import org.open.easytrip.AppUtils;
import org.open.easytrip.dao.DAOFactory;
import org.open.easytrip.entity.DirectionTypeEnum;
import org.open.easytrip.entity.LocationBean;
import org.open.easytrip.entity.LocationTypeEnum;
import org.open.easytrip.entity.YesOrNoEnum;
import org.open.easytrip.exception.AppRuntimeException;

import android.util.Log;

public class IgoImportLocationsBO extends AppBO implements IImportLocationsBO {

	/**
	 * Android has a bug. No file descriptor can be opened over compressed files inside assets folder.
	 * The ugly workaround is to use a file extension that is not compressed. ".jet" is just one of such.
	 */
	private static final String IMPORT_FILE_NAME = "import.jet";

	@Override
	public int reloadLocations(OnReloadProgress progressListener) {
		long lastImportFileSize = daos.getSharedPreferencesDAO().getLastImportFileSize(); 
		long currentImportFileSize = daos.getImportAlertsDAO().getFileSize(IMPORT_FILE_NAME);
		if (lastImportFileSize == currentImportFileSize) {
			return 0;
		} else {
			daos.getLocationDAO().clearNonUserLocations();
			int totalLocationsLoaded = loadData(daos.getImportAlertsDAO().getFile(IMPORT_FILE_NAME), progressListener, currentImportFileSize);
			//Signals the operation is done. If something goes wrong before that, the operation
			//will start over the next time the application is opened.
			daos.getSharedPreferencesDAO().setLastImportFileSize(currentImportFileSize);
			return totalLocationsLoaded;
		}
	}

	/**
	 * Update locations database form a CSV
	 * @param locationsCSV
	 * @param progressListener 
	 * @param totalFileSize 
	 */
	@org.open.easytrip.annotation.CheckLocationBean
	(longitude="",latitude="",id="", direction = "", directionType = "", speedLimit = "", type = "", userDefined = "", creation = "")
	protected int loadData(BufferedReader locationsCSV, OnReloadProgress progressListener, float totalFileSize) {
		String line="";
		int field = 0;
		float bytesReadSoFar = 0;
		int currentPercentage = 0;
		int lastPercentage = 0;
		int locationsCount = 0;
		Date now = new Date();
		
		Log.i(AppConstants.LOG_TAG, "Inserting...");
		try {//Preparing for disposing in the finally section
			//Skip first line (field names)
			locationsCSV.readLine();
			
			progressListener.reportProgress(0);
			
			while ((line = locationsCSV.readLine()) != null)
				if (! line.trim().equals("")) //skip blank lines
				{
					final String locationDetails[] = line.split(",");

					LocationBean locationBean = new LocationBean();

					locationBean.setLongitude(AppUtils.parseDouble(locationDetails[field=0], "longitude"));
					locationBean.setLatitude(AppUtils.parseDouble(locationDetails[++field], "latitude"));

					//Check "location type" codes and, if unexpected, switch for type 0 (others)
					LocationTypeEnum locationType = translateLocatioType(locationDetails[++field]);
					locationBean.setType(locationType != null ? locationType : LocationTypeEnum.OTHERS);

					locationBean.setSpeedLimit(AppUtils.parseInteger(locationDetails[++field], "speedLimit"));

					DirectionTypeEnum directionType = DirectionTypeEnum.valueOf(AppUtils.parseInteger(locationDetails[++field], "directionType"));
					//Check "direction type" codes and, if unexpected, switch for type 0 (all directions)
					locationBean.setDirectionType(directionType != null ? directionType : DirectionTypeEnum.ALL_DIRECTIONS);

					locationBean.setDirection(AppUtils.parseInteger(locationDetails[++field], "direction"));
					locationBean.setUserDefined(YesOrNoEnum.NO);
					locationBean.setCreation(now);

					daos.getLocationDAO().insert(locationBean);

					locationsCount++; 

					{//reporting progress
						bytesReadSoFar += line.length() + 2/*CR+LF*/;
						currentPercentage = Math.round((bytesReadSoFar / totalFileSize)*100);
						if (currentPercentage != lastPercentage) {
							progressListener.reportProgress(currentPercentage);
							lastPercentage = currentPercentage;
						}
					}
				}
			Log.i(AppConstants.LOG_TAG, "Finished loading locations ("+locationsCount+")");
			return locationsCount;
						
		} catch (Exception e) {
			throw new AppRuntimeException("Error loading location table. Line content: "+line, e);
		}
	}

	/**
	 * Necessary to deal with iGO Primo codes, witch are different from iGO 8 and iGO Amigo
	 * @param locationDetail
	 * @return
	 */
	private LocationTypeEnum translateLocatioType(String locationDetail) {
		int code = AppUtils.parseInteger(locationDetail, "type");
		
/*
 *  iGO 8   x  Primo
 *  
 *      2   |  11
 *      7   |  17
 *      8   |  18
 */
		switch (code) {
		case 11:
			code = 2;
			break;
		case 17:
			code = 7;
			break;
		case 18:
			code = 8;
			break;
		}
		
		return LocationTypeEnum.valueOf(code);
	}
	
}
