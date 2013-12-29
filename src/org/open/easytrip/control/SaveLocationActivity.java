package org.open.easytrip.control;

import java.util.Date;
import java.util.List;

import org.open.easytrip.R;
import org.open.easytrip.bo.BOFactory;
import org.open.easytrip.entity.DirectionTypeEnum;
import org.open.easytrip.entity.LocationBean;
import org.open.easytrip.entity.LocationTypeEnum;
import org.open.easytrip.entity.ParcelableLocationBean;
import org.open.easytrip.entity.YesOrNoEnum;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class SaveLocationActivity extends AppActivity {
	private LocationBean userDefinedLocation;

	/**
	 * Holds the onClick event for all the location type radios
	 */
	private final OnClickListener locationTypeClickListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			//Change locationBean to reflect the selected option
			LocationTypeEnum locationTypeEnum = LocationTypeEnum.valueOf((Integer)view.getTag());
			userDefinedLocation.setType(locationTypeEnum);
			
			//Show/hide the speed options
			showSpeedOptions(locationTypeEnum);
		}
	};
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_save_location);
		
		ParcelableLocationBean parcelableLocation = (ParcelableLocationBean)getIntent().getParcelableExtra("location");
		userDefinedLocation = parcelableLocation.getLocationBean();
		fillInDefaults(userDefinedLocation);

		showBasicInfo();
		showLocationTypeOptions();
		//Show/hide the speed options
		showSpeedOptions(userDefinedLocation.getType());

		//Doesn't show delete button when creating a new location
		findViewById(R.id.btnDelete).setVisibility(userDefinedLocation.getId() != null ? View.VISIBLE : View.GONE);
	}
	
	/**
	 * Load defaults to the location bean.
	 * @param location
	 */
	private void fillInDefaults(LocationBean location) {
		//Forced. Always presume one direction only.
		if (location.getDirectionType() == null)
			location.setDirectionType(DirectionTypeEnum.ONE_DIRECTION);
		if (location.getUserDefined() == null)
			location.setUserDefined(YesOrNoEnum.YES);
		if (location.getCreation() == null)
			location.setCreation(new Date()/*now*/);
	}

	/**
	 * Show on screen: coordinates, directions, etc.
	 */
	private void showBasicInfo() {
		setBasicInfoWraper(userDefinedLocation.getLatitude(), R.string.latitude_info, R.id.txtLatitude); 
		setBasicInfoWraper(userDefinedLocation.getLongitude(), R.string.longitude_info, R.id.txtLongitude); 
		setBasicInfoWraper(userDefinedLocation.getDirection(), R.string.bearing_info, R.id.txtBearing);
		//Convert enum to human readable text
		String directionTypeStr = userDefinedLocation.getDirectionType() == null ? null : getString(userDefinedLocation.getDirectionType().resourceId);
		setBasicInfoWraper(directionTypeStr, R.string.directions_info, R.id.txtDirections);
	}

	/**
	 * Decides to show or hide view based on content to show
	 * @param strValue what will be shown
	 * @param resourceContent id for the string resource
	 * @param resourceComponent id for the view
	 */
	private void setBasicInfoWraper(Object strValue, int resourceContent, int resourceComponent) {
		if (strValue == null) {
			textView(resourceComponent).setVisibility(View.GONE);
		} else {
			textView(resourceComponent).setVisibility(View.VISIBLE);
			textView(resourceComponent).setText(getString(resourceContent, strValue));
		}
	}

	/**
	 * Shows all the possible location types options for the user to select.
	 */
	private void showLocationTypeOptions() {
		LinearLayout layout = (LinearLayout)findViewById(R.id.mainLayout);
		
		RadioGroup radioGroup = new RadioGroup(this);
		
		//Shows only location types chosen in preference screen
		List<LocationTypeEnum> warningTypes = bos.getPreferencesBO().getWarningTypes();
		for (LocationTypeEnum value : warningTypes)
			addOption(radioGroup, getString(value.resourceId), value.intValue);
		
		View separator = new View(this);
		separator.setVisibility(View.VISIBLE);
		separator.setBackgroundColor(Color.BLACK);
		radioGroup.addView(separator, radioGroup.getChildCount() /*last view on the layout*/, 
				new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2));
		
		layout.addView(radioGroup, 1/*as the second layout, after layoutBasicInfo */);
		
	}

	private void addOption(ViewGroup radioGroup, String textOption, int intValue) {
		RadioButton radio = new RadioButton(this);
		radio.setText(textOption);
		radio.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
		radio.setOnClickListener(locationTypeClickListener);

		//The tag attribute can be used to store associated data. We will store the intValue from the enumeration.
		radio.setTag(Integer.valueOf(intValue)); 
		radioGroup.addView(radio, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

		//Set checked <b>after</b> adding the radio to the radio group. Used when opening an already saved location for update.
		if (userDefinedLocation.getType() != null)
			radio.setChecked(intValue == userDefinedLocation.getType().intValue);
		
	}
	
	@org.open.easytrip.annotation.CheckLocationBean
	(longitude="",latitude="",id="", direction = "", directionType = "", speedLimit = "", type = "", userDefined = "", creation = "")
	public void btnSaveClick(View view)  
	{
		//Calculate and set the speed limit based on the user input
		if (userDefinedLocation.getType().speedControl) {
			int speedLimit = isChecked(R.id.radio60) ? 60 : 
				isChecked(R.id.radio70) ? 70 :
					isChecked(R.id.radio80) ? 80 :
						isChecked(R.id.radio90) ? 90 :
							isChecked(R.id.radio100) ? 100 :
								isChecked(R.id.radio110) ? 110 :
									isChecked(R.id.radioOther) ? 
											Integer.valueOf(((EditText)findViewById(R.id.editOther)).getText().toString()) : 0;
			userDefinedLocation.setSpeedLimit(speedLimit);
		} else {
			userDefinedLocation.setSpeedLimit(null);
		}
			
		//Saves the location (either inserting or updating)
		String returnMsg = bos.getUpdateLocationsBO().saveLocation(userDefinedLocation);
		toast(returnMsg);

		//Returns the created location with the generated id
		setResult(RESULT_OK, new Intent().putExtra("location", new ParcelableLocationBean(userDefinedLocation)));

		finish();
	}  
	
	public void btnCancelClick(View view)  
	{
		finish();
	}  
	
	public void editOtherOnClick(View view)  
	{
		radioSpeedOnClick(findViewById(R.id.radioOther));
	}  
	
	public void radioOtherOnClick(View view) {
		radioSpeedOnClick(view);
		
		//Change the cursor focus to the editOther field and opens the keyboard for input
		findViewById(R.id.editOther).requestFocus();
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);		

	}
	
	/**
	 * Overrides default radio group behavior. As we want to use linear layouts inside the radio
	 * group, the "only one selected" property must be implemented by hand.  
	 */
	public void radioSpeedOnClick(View view) {
//		ViewGroup radioGroupSpeed = (ViewGroup)findViewById(R.id.radioGroupSpeed);
//		clearRadioGroup(radioGroupSpeed);
//		RadioButton radio = (RadioButton)view;
//		radio.setChecked(true);
	}

	/**
	 * Recursively search for radio buttons inside a view group and uncheck them.
	 * Used to fix android's missing feature of inner layouts inside a RadioGroup.
	 */
	private void clearRadioGroup(ViewGroup radioGroupSpeed) {
		for (int i = 0; i < radioGroupSpeed.getChildCount(); i++) {
			if (radioGroupSpeed.getChildAt(i) instanceof ViewGroup) 
				clearRadioGroup((ViewGroup)radioGroupSpeed.getChildAt(i));
			else if (radioGroupSpeed.getChildAt(i) instanceof RadioButton)
				((RadioButton)radioGroupSpeed.getChildAt(i)).setChecked(false);
		}
	}

	/**
	 * Shows or hides speed options based on the type of the location
	 * @param locationTypeEnum the type of the location or the corresponding type of the selected radio button type
	 */
	private void showSpeedOptions(LocationTypeEnum locationTypeEnum) {
		int visibility = locationTypeEnum != null && locationTypeEnum.speedControl ? View.VISIBLE : View.GONE;
		findViewById(R.id.radioGroupSpeed).setVisibility(visibility);
		
		//Fill the speed limit info into the screen
		if (userDefinedLocation.getSpeedLimit() != null) {
			//Try to match a common speed
			RadioButton radio = getRadioByText((ViewGroup)findViewById(R.id.radioGroupSpeed), userDefinedLocation.getSpeedLimit());
			if (radio == null) { //Not a common speed. Set to "other"
				radio = (RadioButton)findViewById(R.id.radioOther);
				//Fill with the uncommon speed value
				((EditText)findViewById(R.id.editOther)).setText(""+userDefinedLocation.getSpeedLimit());			
			}
			radio.setChecked(true);
			//Check this radio and clear the others
			radioSpeedOnClick(radio);
		}
		
		//Shut up, editOther!
//		findViewById(R.id.editOther).setFocusable(false);		
//		findViewById(R.id.editOther).setFocusableInTouchMode(false);		
	}

	/**
	 * Recursively tries to match a radio button text to a speed limit. Traverse inner groups and layouts.
	 * @param viewGroup root for the search
	 * @param speedLimit value to match with the text
	 * @return the matched radio button. Null if none.
	 */
	private RadioButton getRadioByText(ViewGroup viewGroup, Integer speedLimit) {
		//Test all children
		for (int i = 0; i < viewGroup.getChildCount(); i++) {
			if (viewGroup.getChildAt(i) instanceof RadioButton) { //try this radio button
				final RadioButton radio = (RadioButton)viewGroup.getChildAt(i);
				if (radio.getText().toString().equals(speedLimit+""))
					return radio; // Found it!
			} else if (viewGroup.getChildAt(i) instanceof ViewGroup) { //Recurse this view group
				RadioButton result = getRadioByText((ViewGroup)viewGroup.getChildAt(i), speedLimit);
				if (result != null)
					return result; //Found it!
			}
		}
		return null; //Not found yet
	}

	/**
	 * Deletes the current location being displayed
	 * @param view
	 */
	public void btnDeleteClick(View view) {
		android.content.DialogInterface.OnClickListener clickListener =
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				//Delete the location in the storage
				String msg = bos.getUpdateLocationsBO().delete(userDefinedLocation.getId());
				toast(msg);
				//After deleting the location, there's no reason to keep showing it on screen.
				SaveLocationActivity.this.finish();
				dialog.dismiss();
			}
		};

		confirmationDialog(getString(R.string.confirm_remove_alert), null, clickListener);
	}

}
