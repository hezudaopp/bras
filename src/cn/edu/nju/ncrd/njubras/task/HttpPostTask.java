package cn.edu.nju.ncrd.njubras.task;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import cn.edu.nju.ncrd.njubras.LoginSuccessfullyActivity;
import cn.edu.nju.ncrd.njubras.MainActivity;
import cn.edu.nju.ncrd.njubras.R;
import cn.edu.nju.ncrd.njubras.helper.JSONHelper;
import cn.edu.nju.ncrd.njubras.model.UserInfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class HttpPostTask extends AsyncTask<String, Void, Map<String, Object>> {
	private static final String TAG = "HttpPostTask";
	private static final String JSON_ERROR_KEY ="json_error";
	private static final String AUTO_LOGIN_PARAM = "auto_login";
	private String requestUrl;
	private boolean isAutoLogin = false;
	
	private AndroidHttpClient mClient = AndroidHttpClient.newInstance("");
	private Activity currentActivity;
	private ProgressDialog mProgressDialog;
	
	public HttpPostTask (Activity currentActivity, String url) {
		this.currentActivity = currentActivity;
		this.requestUrl = url;
	}
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		Log.d(TAG, "onPreExecute");
		if (this.requestUrl.compareTo(MainActivity.ACTION_REQUEST_URL) == 0) {
			this.mProgressDialog = new ProgressDialog(this.currentActivity);
			if (this.currentActivity instanceof MainActivity) {
				this.mProgressDialog.setMessage(this.currentActivity.getResources().getString(R.string.progress_dialog_message_logon));
				((MainActivity) this.currentActivity).updateUIInfo();
			}
			else if (this.currentActivity instanceof LoginSuccessfullyActivity)
				this.mProgressDialog.setMessage(this.currentActivity.getResources().getString(R.string.progress_dialog_message_logoff));
			this.mProgressDialog.show();
		}
	}

	@Override
	protected Map<String, Object> doInBackground(String... params) {
		HttpPost mPostRequest = new HttpPost(this.requestUrl);
		
		List<NameValuePair> postParams = new ArrayList<NameValuePair>();
		if (this.requestUrl == MainActivity.ACTION_REQUEST_URL) {
			if (params.length >= 3) {	// login request params
				postParams.add(new BasicNameValuePair("action", params[0]));
				postParams.add(new BasicNameValuePair("username", params[1]));
				postParams.add(new BasicNameValuePair("password", params[2]));
				if (params.length == 4 && AUTO_LOGIN_PARAM.equals(params[3])) {	// auto login request params
					isAutoLogin = true;
				}
			} else if (params.length == 1) {	// logout request params
				postParams.add(new BasicNameValuePair("action", params[0]));
			}
		} else if (this.requestUrl == MainActivity.INFO_REQUEST_URL) {
			if (params.length == 1) {	// info request params
				postParams.add(new BasicNameValuePair("action", params[0]));
			}
		} else if (this.requestUrl == MainActivity.ONLINE_INFO_REQUEST_URL) {
			if (params.length == 2) {	// online list info request
				postParams.add(new BasicNameValuePair("username", params[0]));
				postParams.add(new BasicNameValuePair("password", params[1]));
			}
		} else if (this.requestUrl == MainActivity.DISCONNECT_REQUEST_URL) {
			if (params.length == 3) {	// disconnect centain acctsessionid
				postParams.add(new BasicNameValuePair("username", params[0]));
				postParams.add(new BasicNameValuePair("password", params[1]));
				postParams.add(new BasicNameValuePair("acctsessionid", params[2]));
			}
		}
		try {
			mPostRequest.setEntity(new UrlEncodedFormEntity(postParams, HTTP.UTF_8));
		} catch (UnsupportedEncodingException e1) {
			Log.e(TAG, "ClientProtocolException");
			e1.printStackTrace();
		}  
		JSONResponseHandler responseHandler = new JSONResponseHandler();
		Map<String, Object> errorMap = new HashMap<String, Object>();
		try {
			return mClient.execute(mPostRequest, responseHandler);
		} catch (ClientProtocolException e) {
			Log.e(TAG, "ClientProtocolException");
			e.printStackTrace();
			errorMap.put(JSON_ERROR_KEY, e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, "IOException");
			e.printStackTrace();
			errorMap.put(JSON_ERROR_KEY, e.getMessage());
		} 
		return errorMap;
	}
	
	@Override
	protected void onPostExecute(Map<String, Object> result) {
		if (result == null) {
			return;
		}
		if (result.get(JSON_ERROR_KEY) != null) {
			onTaskFailed(result.get(JSON_ERROR_KEY).toString());
			return;
		}
		Object replyCodeObject = result.get("reply_code");
		if (replyCodeObject instanceof Integer) {
			int replyCode = (Integer) replyCodeObject;
			if (replyCode == 108) {	// 直通用户
				Toast.makeText(currentActivity, result.get("reply_message").toString(), Toast.LENGTH_SHORT).show();
			} else if (replyCode == 101) {	// 登录成功
				Toast.makeText(currentActivity, result.get("reply_message").toString(), Toast.LENGTH_SHORT).show();
				if (currentActivity instanceof MainActivity && !isAutoLogin) {	// if not auto login, we should save user info
					MainActivity mainActivity = (MainActivity) currentActivity;
					if (mainActivity.isSavePassword) {
						WriteJSONToFileTask mWriteJSONToFileTask = new WriteJSONToFileTask((MainActivity) currentActivity);
						mWriteJSONToFileTask.execute(new String[]{mainActivity.mUsername.toString(), mainActivity.mPassword.toString(), String.valueOf(mainActivity.isAutoLogin)});
						if (mainActivity.isAutoLogin 
							&& !(mainActivity.defaultPref.getString(MainActivity.DEFAULT_USERNAME, "").equals(mainActivity.mUsername))) {
							// We need an Editor object to make preference changes.
							// All objects are from android.context.Context
							SharedPreferences settings = mainActivity.defaultPref;
							SharedPreferences.Editor editor = settings.edit();
							editor.putString(MainActivity.DEFAULT_USERNAME, mainActivity.mUsername.toString());
							// Commit the edits!
							editor.commit();
						}
					} else {
						if (mainActivity.getUserList().has(mainActivity.mUsername.toString())) {
							mainActivity.getUserList().remove(mainActivity.mUsername.toString());
						}
					}
				}
				enterUserinfoActivity(result);
			} else if (replyCode == 103) { // 用户名或密码无效 
				String replyMessage = result.get("reply_message").toString();
//				Toast.makeText(currentActivity, replyMessage, Toast.LENGTH_SHORT).show();
				if (replyMessage.contains("E002")) {	// E002 您的登录数已达最大并发登录数
					if (currentActivity instanceof MainActivity) {
						MainActivity mainActivity = (MainActivity) currentActivity;
						HttpPostTask mHttpPostTask = new HttpPostTask(mainActivity, MainActivity.ONLINE_INFO_REQUEST_URL);
						Log.d(TAG, mainActivity.mUsername.toString() + mainActivity.mPassword.toString());
						mHttpPostTask.execute(new String[]{mainActivity.mUsername.toString(), mainActivity.mPassword.toString()});
					}
				} else {
					resetPasswordToEmpty(currentActivity);
				}
			} else if (replyCode == 115) { // 缺少参数
				Toast.makeText(currentActivity, currentActivity.getApplicationContext().getString(R.string.empty_username_or_password), Toast.LENGTH_SHORT).show();
				resetPasswordToEmpty(currentActivity);
			} else if (replyCode == 201) {	//下线成功
				Toast.makeText(currentActivity, result.get("reply_message").toString(), Toast.LENGTH_SHORT).show();
				if (currentActivity instanceof LoginSuccessfullyActivity) {
					currentActivity.setResult(Activity.RESULT_OK, new Intent().putExtra(MainActivity.USERNAME_KEY, ((LoginSuccessfullyActivity)currentActivity).loginUsername()));
					currentActivity.finish();
				}
			} else if (replyCode == 301) { //已经上线
				enterUserinfoActivity(result);
			} else if (replyCode == 302) { //不在线
				// do nothing here.
				if (currentActivity instanceof MainActivity) {
					final MainActivity mainActivity = (MainActivity) currentActivity;
					if (mainActivity.isAutoLogin) {	// Autologin only when user is not online
						String defaultUsername = mainActivity.defaultPref.getString(MainActivity.DEFAULT_USERNAME, "");
						mainActivity.mUsername = defaultUsername;
						if (mainActivity.getUserList().has(defaultUsername)) { 
							String defaultPassword = "";
							try {
								defaultPassword = mainActivity.getUserList().getString(defaultUsername);
							} catch (JSONException e) {
								e.printStackTrace();
							}
							if (!"".equals(defaultPassword)) {	// auto login only when getting user name and password correctly
								mainActivity.mPassword = defaultPassword;
								mainActivity.updateUI();
								HttpPostTask mHttpPostTask = new HttpPostTask(mainActivity, MainActivity.ACTION_REQUEST_URL);
								mHttpPostTask.execute(new String[]{"login", defaultUsername, defaultPassword, AUTO_LOGIN_PARAM});
							}
						}
					}
				}
			} else if (replyCode == 501) {	// 获取到在线信息
				Log.d(TAG, result.get("online").toString());
				
				if (result.get("online") instanceof List<?>) {
					List<Map<String, Object>> onlineInformations = (List<Map<String, Object>>) result.get("online");
					if (onlineInformations.size() >= 1) {
						final Map<String, Object> firstOnlineInformation = onlineInformations.get(0);;
						if (firstOnlineInformation != null) {
							new AlertDialog.Builder(currentActivity)
							.setIcon(android.R.drawable.ic_dialog_alert)
							.setTitle(result.get("reply_msg").toString())
							.setMessage(currentActivity.getResources().getString(R.string.logon_at_another_area) + "\n" +
									currentActivity.getResources().getString(R.string.at) +
									firstOnlineInformation.get("area_name").toString() + "\n" +
									currentActivity.getResources().getString(R.string.userip) + 
									UserInfo.getReadableIp(Long.parseLong(firstOnlineInformation.get("user_ip").toString())) + "\n" +
									currentActivity.getResources().getString(R.string.acctstarttime) +
									UserInfo.getReadableAcctstarttime(Long.parseLong(firstOnlineInformation.get("acctstarttime").toString())) + "\n" +
									currentActivity.getResources().getString(R.string.logoff_and_logon))
							.setPositiveButton(currentActivity.getResources().getString(R.string.dialog_positive), new DialogInterface.OnClickListener(){
		
								@Override
								public void onClick(DialogInterface dialog, int which) {	// logoff and then logon
									if (currentActivity instanceof MainActivity) {
										MainActivity mainActivity = (MainActivity) currentActivity;
										HttpPostTask mHttpPostTask = new HttpPostTask(mainActivity, MainActivity.DISCONNECT_REQUEST_URL);
										mHttpPostTask.execute(new String[]{mainActivity.mUsername.toString(), mainActivity.mPassword.toString(), firstOnlineInformation.get("acctsessionid").toString()});
									}
								}
							})
							.setNegativeButton(currentActivity.getResources().getString(R.string.dialog_negative), null)
							.show();
						}
					}
				}
			} else if (replyCode == 502) {	//不在线
				// do nothing
			} else if (replyCode == 503) {
				// do nothing
			} else if (replyCode == 504) {	// 请求错误
				Toast.makeText(currentActivity, currentActivity.getResources().getString(R.string.wrong_username_or_password), Toast.LENGTH_SHORT).show();
			} else if (replyCode == 601) {
				if (currentActivity instanceof MainActivity) {
					MainActivity mainActivity = (MainActivity) currentActivity;
					HttpPostTask mHttpPostTask = new HttpPostTask(mainActivity, MainActivity.ACTION_REQUEST_URL);
					if (!mainActivity.isAutoLogin){
						mHttpPostTask.execute(new String[]{"login", mainActivity.mUsername.toString(), mainActivity.mPassword.toString()});
					} else {
						mHttpPostTask.execute(new String[]{"login", mainActivity.mUsername.toString(), mainActivity.mPassword.toString(), AUTO_LOGIN_PARAM});
					}
				}
			}
		} else if (replyCodeObject == null){
			Log.e(TAG, "reply_code not found");
		} else {
			Log.e(TAG, "reply_code is not an integer");
		}
		if (mClient != null) mClient.close();
		if (this.mProgressDialog != null)
			this.mProgressDialog.dismiss();
	}
	
	private void resetPasswordToEmpty(Activity currentActivity) {
		if (currentActivity instanceof MainActivity) {
			MainActivity tmpActivity = (MainActivity) currentActivity;
			tmpActivity.mPassword = "";
			tmpActivity.updateUI();
		}
	}
	
	private void enterUserinfoActivity(Map<String, Object> result) {
		@SuppressWarnings("unchecked")
		Map<String, Object> userinfo = (Map<String, Object>) result.get("userinfo");
		Intent loginSuccessfullyIntent = new Intent(currentActivity, LoginSuccessfullyActivity.class);
		for (String key : userinfo.keySet()) {
			Object value = userinfo.get(key);
			if (value == null)
				value = "";
			loginSuccessfullyIntent.putExtra(key, value.toString());
		}
		currentActivity.startActivityForResult(loginSuccessfullyIntent, MainActivity.GET_USERNAME);
	}
	
	private void onTaskFailed (String message) {
		Toast.makeText(currentActivity, message, Toast.LENGTH_SHORT).show();
	}
	
	private class JSONResponseHandler implements ResponseHandler<Map<String, Object>> {
		private static final String JSON_RESPONSE_TAG = "JsonResponse";

		@Override
		public Map<String, Object> handleResponse(HttpResponse response)
				throws ClientProtocolException, IOException {
			Map<String, Object> result = new HashMap<String, Object>();
			String JSONResponseResult = new BasicResponseHandler().handleResponse(response);
			Log.i(JSON_RESPONSE_TAG, JSONResponseResult);
			try {
				JSONTokener mJSONTokener = new JSONTokener(JSONResponseResult);
				JSONObject responseObject = (JSONObject) mJSONTokener.nextValue();
				result.putAll(JSONHelper.toMap(responseObject));
			} catch (JSONException e) {
				Log.e(JSON_RESPONSE_TAG, "JSONException");
				e.printStackTrace();
			}
			return result;
		}
	}
	
}
