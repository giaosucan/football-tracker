package com.android.gps.server;

import java.util.ArrayList;

import com.android.gps.util.Constant;
import com.android.gps.util.PreferentUtils;

import android.content.Context;
import android.os.AsyncTask;

public class ServerTask extends AsyncTask<String, Void, String[]> {

    MyFTPClient mFtpClient = new MyFTPClient();

    private String ftpHost = "ftp.toiansach.com";
    private String ftpUser = "nhtoi39d";
    private String ftpPass = "^#Dx6$h8W]_}";
    private int ftpPort = 21;
    // private Context mContext;
    String requestCommand;
    String content;

    FTPRequestResult mListener;

    public ServerTask(Context context, FTPRequestResult listener) {
	mListener = listener;
	// mContext = context;
	PreferentUtils prefUtils = new PreferentUtils(context);

	ftpHost = prefUtils.getFTPServer();
	ftpUser = prefUtils.getFTPUserName();
	ftpPass = prefUtils.getFTPPass();
	ftpPort = (int) prefUtils.getFTPPort();

    }

    @Override
    protected String[] doInBackground(String... params) {

	requestCommand = params[0];

	if (params[0] == Constant.COMMAND_UPLOAD) {
	    String downloadResult[] = { "", "" };

	    boolean bresult = uploadFtpFile(params[1], params[2], null);
	    downloadResult[0] = String.valueOf(bresult);

	    return downloadResult;

	}
	if (params[0] == Constant.COMMAND_DOWNLOAD) {
	    String result[] = { "", "" };
	    result[0] = downloadFtpFile(params[1]);
	    return result;
	}
	if (params[0] == Constant.COMMAND_LISTFILE) {
	    return getListFile();
	}
	if (params[0] == Constant.COMMAND_CONNECT) {
	    String loginResult[] = { "", "" };
	    if (mFtpClient.ftpConnect(ftpHost, ftpUser, ftpPass, ftpPort)) {
		loginResult[0] = "1";
	    } else
		loginResult[0] = "0";

	    return loginResult;
	}
	return null;
    }

    public void setFtpAccount(String server, String user, String pass) {
	ftpHost = server;
	ftpUser = user;
	ftpPass = pass;
    }

    @Override
    protected void onPostExecute(String[] result) {
	if (mListener != null)
	    mListener.onRequestResult(requestCommand, result);
	super.onPostExecute(result);
    }

    private boolean uploadFtpFile(String name, String content, String directory) {
	if (mFtpClient.ftpConnect(ftpHost, ftpUser, ftpPass, ftpPort))
	    return mFtpClient.ftpUpload(name, content, directory);
	else
	    return false;
    }

    private String downloadFtpFile(String srcFilePath) {
	if (mFtpClient.ftpConnect(ftpHost, ftpUser, ftpPass, ftpPort)) {
	    return mFtpClient.fileTextDownload(srcFilePath);
	} else
	    return null;
    }

    private String[] getListFile() {
	if (mFtpClient.ftpConnect(ftpHost, ftpUser, ftpPass, ftpPort)) {
	    ArrayList<String> listFile = mFtpClient.fileList("/");
	    return listFile.toArray(new String[listFile.size()]);
	} else
	    return null;
    }

    public interface FTPRequestResult {

	public void onRequestResult(String command, String[] array);

    }

}
