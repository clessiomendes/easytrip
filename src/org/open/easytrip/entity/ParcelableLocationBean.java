package org.open.easytrip.entity;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableLocationBean implements Parcelable {
	LocationBean location;
	
	public LocationBean getLocationBean() {
		return location;
	}

	public ParcelableLocationBean(LocationBean _location) {
		super();
		this.location = _location;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	@org.open.easytrip.annotation.CheckLocationBean
	(longitude="",latitude="",id="", direction = "", directionType = "", speedLimit = "", type = "", userDefined = "", creation = "")
	public void writeToParcel(Parcel dest, int flags) {
//		writeObject(dest, location.getId());
//		writeObject(dest, location.getLatitude());
//		writeObject(dest, location.getLongitude());

		dest.writeValue(location.getId());
		dest.writeValue(location.getLatitude());
		dest.writeValue(location.getLongitude());
		dest.writeValue(location.getType());
		dest.writeValue(location.getSpeedLimit());
		dest.writeValue(location.getDirectionType());
		dest.writeValue(location.getDirection());
		dest.writeValue(location.getUserDefined());
		dest.writeValue(location.getCreation());
	}
	
	@org.open.easytrip.annotation.CheckLocationBean
	(longitude="",latitude="",id="", direction = "", directionType = "", speedLimit = "", type = "", userDefined = "", creation = "")
    private ParcelableLocationBean(Parcel in) {

    	location = new LocationBean();
    	location.setId((Integer)in.readValue(null));
    	location.setLatitude((Double)in.readValue(null));
    	location.setLongitude((Double)in.readValue(null));
        location.setType((LocationTypeEnum)in.readValue(null));
        location.setSpeedLimit((Integer)in.readValue(null));
        location.setDirectionType((DirectionTypeEnum)in.readValue(null));
        location.setDirection((Integer)in.readValue(null));
        location.setUserDefined((YesOrNoEnum)in.readValue(null));
        location.setCreation((Date)in.readValue(null));
    }

	/*
     * Parcelable interface must have a static field called CREATOR,
     * which is an object implementing the Parcelable.Creator interface.
     * Used to un-marshal or de-serialize object from Parcel.
     */
    public static final Parcelable.Creator<ParcelableLocationBean> CREATOR =
            new Parcelable.Creator<ParcelableLocationBean>() {
        public ParcelableLocationBean createFromParcel(Parcel in) {
            return new ParcelableLocationBean(in);
        }
 
        public ParcelableLocationBean[] newArray(int size) {
            return new ParcelableLocationBean[size];
        }
    };
    

}
