package org.open.easytrip.entity;

import java.util.Date;

/**
 * Entity for locations to be alarmed
 */
public class LocationBean {
	private Integer id;
	private Double latitude;
	private Double longitude;
	private LocationTypeEnum type; 
	private Integer speedLimit;
	private DirectionTypeEnum directionType; 
	private Integer direction;
	private YesOrNoEnum userDefined;
	private Date creation;
	private Integer searchRadius;
	/* ==============================>  IMPORTANT!  <================================= 
	 * Any new non-transient attribute should be added to CheckLocationBean annotation to point out
	 * which part of the code must be updated in consequence of new attributes. */
	
	public Date getCreation() {
		return creation;
	}

	public void setCreation(Date creation) {
		this.creation = creation;
	}

	public YesOrNoEnum getUserDefined() {
		return userDefined;
	}

	public void setUserDefined(YesOrNoEnum userDefined) {
		this.userDefined = userDefined;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public LocationTypeEnum getType() {
		return type;
	}

	public void setType(LocationTypeEnum type) {
		this.type = type;
	}

	public Integer getSpeedLimit() {
		return speedLimit;
	}

	public void setSpeedLimit(Integer speedLimit) {
		this.speedLimit = speedLimit;
	}

	public DirectionTypeEnum getDirectionType() {
		return directionType;
	}

	public void setDirectionType(DirectionTypeEnum directionType) {
		this.directionType = directionType;
	}

	public Integer getDirection() {
		return direction;
	}

	public void setDirection(Integer direction) {
		this.direction = direction;
	}

	/* ==================== Inherited methods ========================= */
	@Override
	/**
	 * Please, use SearchLocationBO.compare()
	 */
	public boolean equals(Object o) {
		return super.equals(o);
	}
	
	@Override
	public String toString() {
		return "Id:" + id + " Latitude: "+latitude+" Longitude: "+longitude+" Direction: "+direction+" Type: "+type;
	}

	/* ================= Transient (not persisted to database) =====================*/
	public Integer getSearchRadius() {
		return searchRadius;
	}
	
	public void setSearchRadius(Integer searchRadius) {
		this.searchRadius = searchRadius;
	}
	
}
