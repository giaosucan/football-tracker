package com.android.gps.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

/**
 * 
 * File Manager
 * 
 */

public class FileManager {

    Context mContext;
    private final static String APP_DIRECTORY = "AtheleTracker";

    public FileManager(Context context) {
	mContext = context;
    }

    public void saveToSDCard(String name, String content) {
	File rootDir = checkAndCreateDirectory();
	File file = new File(rootDir, name);
	try {
	    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
		    new FileOutputStream(file));
	    outputStreamWriter.write(content);
	    outputStreamWriter.close();
	} catch (IOException e) {
	    Log.e("Exception", "File write failed: " + e.toString());
	}
    }

    public String readFromFile(String pathName) {

	String ret = "";
	File fileroot = checkAndCreateDirectory();
	File file = new File(fileroot, pathName);
	try {
	    InputStream inputStream = new FileInputStream(file);// mContext.openFileInput(pathName);

	    if (inputStream != null) {
		InputStreamReader inputStreamReader = new InputStreamReader(
			inputStream);
		BufferedReader bufferedReader = new BufferedReader(
			inputStreamReader);
		String receiveString = "";
		StringBuilder stringBuilder = new StringBuilder();

		while ((receiveString = bufferedReader.readLine()) != null) {
		    stringBuilder.append(receiveString);
		}

		inputStream.close();
		ret = stringBuilder.toString();
	    }
	} catch (FileNotFoundException e) {
	    Log.e("login activity", "File not found: " + e.toString());
	} catch (IOException e) {
	    Log.e("login activity", "Can not read file: " + e.toString());
	}

	return ret;
    }

    public String[] getListFile() {
	File yourDir = checkAndCreateDirectory();
	ArrayList<String> listFileArray = new ArrayList<String>();
	for (File f : yourDir.listFiles()) {
	    if (f.isFile())
		listFileArray.add(f.getName());
	}

	if (listFileArray != null) {
	    return listFileArray.toArray(new String[listFileArray.size()]);
	} else
	    return null;
    }

    private File checkAndCreateDirectory() {
	File f = new File(Environment.getExternalStorageDirectory() + "/"
		+ APP_DIRECTORY);
	if (!(f.exists() && f.isDirectory())) {
	    f.mkdirs();
	}

	return f;
    }
}