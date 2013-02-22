package org.open.easytrip.bo;

public class DatabaseStructureBO extends AppBO {

	public void createDatabase() {
		daos.getDatabaseStructureDAO().createDatabase();
	}

	@org.open.easytrip.annotation.CheckLocationBean //Call upgrade method to add the new field
	(longitude="",latitude="",id="", direction = "", directionType = "", speedLimit = "", type = "", userDefined = "", creation = "")
	public void upgradeDatabase(int oldVersion, int newVersion) {
		switch (oldVersion+1) {
		case 4:
			daos.getDatabaseStructureDAO().upgradeTo4();
		case 5:
			daos.getDatabaseStructureDAO().upgradeTo5();
		case 6:
			//Do not break. Keep upgrading...
		}
	}

	
}
