package org.open.easytrip.bo;

import java.util.List;

import org.open.easytrip.AppUtils;
import org.open.easytrip.dao.DAOFactory;
import org.open.easytrip.dao.LocationDAO;
import org.open.easytrip.entity.DirectionTypeEnum;
import org.open.easytrip.entity.LocationBean;
import org.open.easytrip.entity.LocationTypeEnum;
import org.open.easytrip.helper.IgnoreListHelper;



public class RetrieveLocationsBO extends AppBO {
	
	private static final int MAX_DIRECTION_DIFF_TO_ALARM = 135;
	public static final int OPPOSITE_DIRECTION_RANGE = 90/*degrees*/;
	public static final double POSITION_EQUALITY = AppUtils.meters2degrees(5);
	private static final int TIME_BEFORE_ALERT = 15 /*seconds*/;
//	private static final int CONVERSION_FACTOR = (int)(TIME_BEFORE_ALERT * 3.6); /*from m/s to km/h*/

	/**
	 * Search the storage for the closest location from a given point, inside a scope radius, ignoring opposite directions.

	 * @param currentLatitude Center of the search radius 
	 * @param currentLongitude Center of the search radius
	 * @param ignoreList A helper object containing a list of location IDs to be ignored during the search
	 * @param currentSpeed The current speed of movement, used to predict the search radius. In meters per second.
	 * @param searchRadius The radius from the location to be analyzed. 
	 * @param typesToSearch The location types to be considered (other types will be ignored)
	 * @param currentDirection The current direction (bearing) of movement. Null if not moving. Clockwise degrees from north direction.
	 * @return A populated instance of the closest location or null if none is found
	 */
	public LocationBean findClosestLocation(double currentLatitude, double currentLongitude, 
			IgnoreListHelper ignoreList, Integer currentSpeed, Integer currentDirection) {
		LocationDAO locationDAO = daos.getLocationDAO();

//		int searchRadius = bos.getPreferencesBO().getSearchRadius();
		//searchRadius is now defined by the current speed
		int searchRadius = currentSpeed * TIME_BEFORE_ALERT;
		//Never shorter than 100m or longer then 1000m
		searchRadius = searchRadius < 100 ? 100 : searchRadius > 1000 ? 1000 : searchRadius;
		
		List<LocationTypeEnum> typesToSearch = bos.getPreferencesBO().getWarningTypes(); 
		
		//Will store the distance to the closest location, in degrees
		double shortestDistance = searchRadius;
		LocationBean closest = null; 

		//Convert the circle to a square (with size = 2*radius), suitable to database searches
		LocationBean[] locations = locationDAO.getLocationsByArea(
				currentLatitude - AppUtils.meters2degrees(searchRadius), 
				currentLatitude + AppUtils.meters2degrees(searchRadius), 
				currentLongitude - AppUtils.meters2degrees(searchRadius), 
				currentLongitude + AppUtils.meters2degrees(searchRadius));

		for (LocationBean location : locations) {
			//Skip locations that are marked to be ignored
			if (ignoreList.ignore(location.getId()))
				continue; 
			
			//Skip locations types marked not to be warned
			if (! typesToSearch.contains(location.getType()))
				continue;
			
			//Skip locations out of direction (when direction infos are present and location is unidirectional)
			if (currentDirection != null && location.getDirection() != null && 
					location.getDirectionType() != null && location.getDirectionType().equals(DirectionTypeEnum.ONE_DIRECTION) 
					&& AppUtils.absDirectionDiff(currentDirection, location.getDirection()) > MAX_DIRECTION_DIFF_TO_ALARM )
				continue;
				
			double thisDistance = AppUtils.distance(currentLatitude, currentLongitude, location.getLatitude(), location.getLongitude());
			//TODO consider directions
			if (AppUtils.degrees2meters(thisDistance) < shortestDistance) {
				closest = locationDAO.getLocation(location.getId());
				//Transient, used to show a percent to target info
				closest.setSearchRadius(searchRadius);
				shortestDistance = thisDistance;
			}
		}
		return closest;
	}
	
	/**
	 * Get full persisted instance from storage
	 * @param id
	 * @return
	 */
	public LocationBean getLocation(int id) {
		return daos.getLocationDAO().getLocation(id);
	}

	/**
	 * Simply counts all locations stored  
	 */
	public int locationsCount() {
		LocationDAO locationDAO = daos.getLocationDAO();
		return locationDAO.locationsCount();
	}

	/**
	 * Entities with the same id are equal. Otherwise, the equality is based in rules over the 
	 * attributes type (exactly the same) latitude, longitude and direction (approximate)
	 * @return true if both beans are not null and have some similar attributes
	 */
	public boolean compare(LocationBean first, LocationBean second) {
		//TODO Test me!
		if (second == null || first == null)
			return false;
		
		//Test IDs, if both are not null (which indicates persisted entities)
		if (second.getId() != null && first.getId() != null)
			return first.getId().equals(second.getId());
		
		//For not persisted entities verify other fields
		boolean result = true; //All tests are ANDed and must evaluate to true
		
		if (first.getType() == null)
			result &= second.getType() == null;
		else
			result &= first.getType().equals(second.getType());
		
		//Directions within a DIRECTION_EQUALITY (90 degrees) angle are assumed the same due to the low precision of this attribute
		if (first.getDirection() == null)
			result &= second.getDirection() == null;
		else {
			if (second.getDirection() == null)
				result &= false;
			else
				result &= Math.abs(first.getDirection() - second.getDirection()) < OPPOSITE_DIRECTION_RANGE / 2;
		}

		//Coordinates within a 5 meters range are considered the same
		result &= AppUtils.distance(null2zero(first.getLatitude()), null2zero(first.getLongitude()), null2zero(second.getLatitude()), null2zero(second.getLongitude())) < POSITION_EQUALITY;

		return result;
	}
	
	private static double null2zero(Double value) {
		return value == null ? 0 : value;
	}

	public LocationBean[] getLastUserCreated(int max) {
		LocationDAO locationDAO = daos.getLocationDAO();
		return locationDAO.getUserDefinedLocations(max);
	}
	
}
