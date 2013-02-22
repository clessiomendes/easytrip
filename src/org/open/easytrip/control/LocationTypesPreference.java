package org.open.easytrip.control;

import java.util.ArrayList;
import java.util.List;

import org.open.easytrip.entity.LocationTypeEnum;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

public class LocationTypesPreference extends DialogPreference {

	private List<CheckBox> options = new ArrayList<CheckBox>();
	
    public LocationTypesPreference(Context oContext, AttributeSet attrs)
    {
        this(oContext, attrs, android.R.attr.dialogPreferenceStyle);
    }
    
    public LocationTypesPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
//        setDialogLayoutResource(R.layout.location_types_preference);
        setPersistent(false);
      }
     
    @Override
    protected View onCreateDialogView() {
    	super.onCreateDialogView();
//    	ViewGroup container = (ViewGroup)getContext().getResources().getLayout(R.layout.location_types_preference);
    	LinearLayout container = new LinearLayout(getContext());
    	container.setOrientation(LinearLayout.VERTICAL);
    	container.setPadding(10, 5, 10, 5);

    	//Holds the current selected options
        SharedPreferences sharedPreferences = getSharedPreferences();

    	for (LocationTypeEnum l : LocationTypeEnum.values()) {
    		CheckBox check = new CheckBox(container.getContext());
    		check.setText(getContext().getString(l.resourceId));
    		check.setTag(l.toString());
    		/* 
    		 * Only API 11 allows for string arrays stored in a SharedPreferences
    		 * So we must simulate a multiple-value key, ie location_type.FIXED_SPEED_CAMERA
    		 */
			check.setChecked(sharedPreferences.getBoolean(compoundKey(l.toString()) , true/*New keys marked by default*/));
    		container.addView(check); //shows on screen
    		options.add(check); //keeps in cache for easier access when closing the dialog
		} 
    	return container;
    }
    
    private String compoundKey(String sufix) {
		return this.getKey()+"."+sufix;
	}

	@Override
    protected void onBindDialogView(View view) {
    	super.onBindDialogView(view);
    	//Load persisted preferences
    }
    
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        
        if (positiveResult) {
        	//store selected values
            Editor editor = getEditor(); //To store multiple values

            //Stores each option (either as true or false) presented to the user
            for (CheckBox check : options) 
            	editor.putBoolean(compoundKey(check.getTag().toString()), check.isChecked());

            editor.commit();
        }
    }
    
    
}
