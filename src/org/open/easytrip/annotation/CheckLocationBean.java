package org.open.easytrip.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Used to generate compile errors to warn the developer about new attributes to be supported
 * in some methods that must deal with <b>all<b> attributes (like filling an entity from database).
 * Just add the new attribute to the annotation parameter to stop compilation error (and, of course,
 * taker care of it inside the method code).
 */
@Retention(RetentionPolicy.SOURCE)
public @interface CheckLocationBean {
	String id();
	String latitude();
	String longitude();
	String type(); 
	String speedLimit();
	String directionType(); 
	String direction();
	String userDefined();
	String creation();
}
