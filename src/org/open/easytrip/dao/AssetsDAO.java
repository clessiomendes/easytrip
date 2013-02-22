package org.open.easytrip.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.open.easytrip.control.EasyTripPrefs;
import org.open.easytrip.exception.AppRuntimeException;

import android.content.res.AssetManager;

public class AssetsDAO extends AppDAO {
	
	private AssetManager assetManager;
	
	public AssetsDAO(AssetManager assetManager) {
		super();
		this.assetManager = assetManager;
	}

	public long getFileSize(String importFileName) {
		try {
			return assetManager.openFd(importFileName).getLength();
		} catch (IOException e) {
			throw new AppRuntimeException(e);
		}
	}
	
	public BufferedReader getFile(String importFileName) {
		try {
			BufferedReader locationsCSV = new BufferedReader(new InputStreamReader(assetManager.open(importFileName)));
			return locationsCSV;
		} catch (IOException e) {
			throw new AppRuntimeException(e);
		}
	}

}
