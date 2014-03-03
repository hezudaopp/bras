package cn.edu.nju.ncrd.njubras.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import cn.edu.nju.ncrd.njubras.MainActivity;

import android.os.AsyncTask;
import android.widget.ArrayAdapter;

public class ReadJSONFromFileTask extends AsyncTask<String, Void, JSONObject> {
	MainActivity mainActivity;
	
	public ReadJSONFromFileTask(MainActivity currentActivity) {
		this.mainActivity = currentActivity;
	}

	@Override
	protected JSONObject doInBackground(String... params) {
		String mJSONString = readJSONStringFromFile(params[0]);
		JSONObject result = null;
		try {
			result = new JSONObject(mJSONString);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		return result;
	}
	
	@Override
	protected void onPostExecute(JSONObject result) {
		mainActivity.setUserList(result);
		if (mainActivity.mUsername != null && !"".equals(mainActivity.mUsername)) {
			try {
				mainActivity.mPassword = mainActivity.getUserList().getString(mainActivity.mUsername.toString());
				mainActivity.updateUI();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		HttpPostTask mHttpPostTask = new HttpPostTask(mainActivity, MainActivity.INFO_REQUEST_URL);
		mHttpPostTask.execute(new String[]{"info"});
		JSONObject userList = mainActivity.getUserList();
		int userCount = userList.length();
		String[] usernameList = new String[userCount];
		Iterator<?> keys = userList.keys();
		int i = 0;
		while(keys.hasNext()) {
			String key = (String) keys.next();
			usernameList[i++] = key;
		}
		ArrayAdapter<String> adapter =  new ArrayAdapter<String>(mainActivity, android.R.layout.simple_list_item_1, usernameList);
		mainActivity.getUsernameView().setAdapter(adapter);
	}

	private String readJSONStringFromFile(String filename) {
		File file = mainActivity.getApplicationContext().getFileStreamPath(filename);
		if (!file.exists()) {
			try {
				file.createNewFile();
	        } catch (IOException e) {
	            e.printStackTrace();
	            return "";
	        }
		}
		
		try {
			InputStream is = mainActivity.getApplicationContext().openFileInput(
					filename);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			StringBuffer sb = new StringBuffer();
			String s;
			while (null != (s = reader.readLine())) {
				sb.append(s);
			}
			return sb.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return "";
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}

	}
}
