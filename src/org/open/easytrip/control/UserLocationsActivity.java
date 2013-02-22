package org.open.easytrip.control;

import org.open.easytrip.R;
import org.open.easytrip.bo.BOFactory;
import org.open.easytrip.entity.LocationBean;
import org.open.easytrip.entity.ParcelableLocationBean;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;

public class UserLocationsActivity extends AppActivity {

	private static final int MAX_LOCATIONS_TO_SHOW = 50;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_locations);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		listUserLocations();
	}

	/**
	 * Replicates rows for each user defined location currently stored
	 */
	private void listUserLocations() {
		TableLayout table = (TableLayout)findViewById(R.id.tableLocations);
		table.removeAllViews();
		
		LocationBean[] locations = bos.getRetrieveLocationsBO().getLastUserCreated(MAX_LOCATIONS_TO_SHOW);
		
		for (int i = 0; i < locations.length; i++) {
		    // Inflate your row "template" and fill out the fields.
			View row = LayoutInflater.from(this).inflate(R.layout.row_user_locations, null);

			//New instances after the row inflation
			TextView txtCreationTime = (TextView)row.findViewById(R.id.txtCreationTime);
			Button btnType = (Button)row.findViewById(R.id.btnType);
//			TextView txtId = (TextView)row.findViewById(R.id.txtId);

			//Fill in the contents
			CharSequence formatedTime = DateFormat.format("dd/MMM k:m", locations[i].getCreation());
			txtCreationTime.setText(formatedTime);
			String typeMessage = ""+getText(locations[i].getType().resourceId);
			if (locations[i].getSpeedLimit() != null)
				typeMessage += " ("+locations[i].getSpeedLimit()+")";
			btnType.setText(typeMessage);
//			txtId.setText("("+locations[i].getId()+")");
			
			//Associate the bean to the row so we can open it in the future.
			row.setTag(locations[i]);
			
		    table.addView(row);
		}
		table.requestLayout(); // Not sure if this is needed.		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		TableLayout table = (TableLayout)findViewById(R.id.tableLocations);
		clearRefences(table);
		
	}

	/**
	 * Clear references to data associated to visual components through the tag attribute  
	 */
	private void clearRefences(ViewGroup viewGroup) {
		for (int i = 0; i < viewGroup.getChildCount(); i++) {
			//Remove the reference of the associated data from the tag attribute
			viewGroup.getChildAt(i).setTag(null);
			if (viewGroup.getChildAt(i) instanceof ViewGroup)
				clearRefences((ViewGroup)viewGroup.getChildAt(i));
		}
	}

	/**
	 * Fired when either of the text views within a row is clicked
	 * @param view
	 */
	public void rowOnClick(View view) {
		View container = (View)view.getParent();
		LocationBean location = (LocationBean)container.getTag();
		
		//Call the update location activity passing the selected location
		startActivity (new Intent(this, SaveLocationActivity.class).
				putExtra("location", new ParcelableLocationBean(location)));
	}

}
