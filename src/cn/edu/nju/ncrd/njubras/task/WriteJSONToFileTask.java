package cn.edu.nju.ncrd.njubras.task;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.json.JSONException;
import org.json.JSONObject;

import cn.edu.nju.ncrd.njubras.MainActivity;
import android.content.Context;
import android.os.AsyncTask;

public class WriteJSONToFileTask extends AsyncTask<String, Void, Void> {
	private MainActivity mainActivity;
	
	public WriteJSONToFileTask(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
	}
	
	@Override
	protected void onPreExecute() {
//		if (this.mainActivity.getUserList().length() == 0) {
//			ReadJSONFromFileTask mReadJSONFromFileTask = new ReadJSONFromFileTask(this.mainActivity);
//			mReadJSONFromFileTask.execute(new String[]{MainActivity.USERS_JSON_FILENAME});
//		}
	}
	
	@Override
	protected Void doInBackground(String... params) {
		if (params.length == 3) {
			String username = params[0];
			String password = params[1];
			JSONObject userList = this.mainActivity.getUserList();
			try {
				userList.put(username, password);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			saveToFile();
		}
		return null;
	}

	private void saveToFile() {
		PrintWriter writer = null;
		try {
			FileOutputStream fos = this.mainActivity.openFileOutput(
					MainActivity.USERS_JSON_FILENAME, Context.MODE_PRIVATE);
			writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
					fos)));
			writer.println(this.mainActivity.getUserList().toString());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != writer) {
				writer.close();
			}
		}
	}
}
