package cn.edu.nju.ncrd.njubras.task;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import cn.edu.nju.ncrd.njubras.LoginSuccessfullyActivity;
import cn.edu.nju.ncrd.njubras.MainActivity;
import cn.edu.nju.ncrd.njubras.R;
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

public class HttpPostTask extends AsyncTask<String, Void, JSONObject> {
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
			else if (this.currentActivity instanceof LoginSuccessfullyActivity) {
				this.mProgressDialog.setMessage(this.currentActivity.getResources().getString(R.string.progress_dialog_message_logoff));
			}
			this.mProgressDialog.show();
		}
	}

	@Override
	protected JSONObject doInBackground(String... params) {
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
		JSONObject errorMessage = new JSONObject();
		try {
			return mClient.execute(mPostRequest, responseHandler);
		} catch (ClientProtocolException e) {
			Log.e(TAG, "ClientProtocolException");
			e.printStackTrace();
			try {
				errorMessage.put(JSON_ERROR_KEY, e.getMessage());
			} catch (JSONException e1) {
				e1.printStackTrace();
				return null;
			}
		} catch (IOException e) {
			Log.e(TAG, "IOException");
			e.printStackTrace();
			try {
				errorMessage.put(JSON_ERROR_KEY, e.getMessage());
			} catch (JSONException e1) {
				return null;
			}
		} 
		return errorMessage;
	}
	
	@Override
	protected void onPostExecute(JSONObject result) {
		if (result == null) {
			return;
		}
		try {
			if (result.get(JSON_ERROR_KEY) != null) {
				onTaskFailed(result.get(JSON_ERROR_KEY).toString());
				return;
			}
		} catch (JSONException e) {
			// do nothing here
		}
		try {
			Object replyCodeObject = result.get("reply_code");
			if (replyCodeObject instanceof Integer) {
				int replyCode = (Integer) replyCodeObject;
				if (replyCode == 108) {	// 直通用户
					showMessage(result, "reply_message");
				} else if (replyCode == 101) {	// 登录成功
					showMessage(result, "reply_message");
					if (currentActivity instanceof MainActivity) {	// if not auto login, we should save user info
						if (!isAutoLogin){
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
								if (MainActivity.getUserList().has(mainActivity.mUsername.toString())) {
									MainActivity.getUserList().remove(mainActivity.mUsername.toString());
								}
							}
						}
						enterUserinfoActivity(result);
					}
				} else if (replyCode == 103) { // 用户名或密码无效 
					String replyMessage = result.get("reply_message").toString();
					if (replyMessage.contains("E002")) {	// E002 您的登录数已达最大并发登录数
						if (currentActivity instanceof MainActivity) {
							MainActivity mainActivity = (MainActivity) currentActivity;
							HttpPostTask mHttpPostTask = new HttpPostTask(mainActivity, MainActivity.ONLINE_INFO_REQUEST_URL);
							Log.d(TAG, mainActivity.mUsername.toString() + mainActivity.mPassword.toString());
							mHttpPostTask.execute(new String[]{mainActivity.mUsername.toString(), mainActivity.mPassword.toString()});
						} else if (currentActivity instanceof LoginSuccessfullyActivity) {
							LoginSuccessfullyActivity userinfoActivity = (LoginSuccessfullyActivity) currentActivity;
							String username = userinfoActivity.getmUserinfo().getUsername();
							String password = MainActivity.getUserList().getString(username);
							HttpPostTask mHttpPostTask = new HttpPostTask(userinfoActivity, MainActivity.ONLINE_INFO_REQUEST_URL);
							mHttpPostTask.execute(new String[]{username, password});
						}
					} else {
						resetPasswordToEmpty(currentActivity);
					}
				} else if (replyCode == 115) { // 缺少参数
					Toast.makeText(currentActivity, currentActivity.getApplicationContext().getString(R.string.empty_username_or_password), Toast.LENGTH_SHORT).show();
					resetPasswordToEmpty(currentActivity);
				} else if (replyCode == 201) {	//下线成功
					showMessage(result, "reply_message");
					if (currentActivity instanceof LoginSuccessfullyActivity) {
						currentActivity.setResult(Activity.RESULT_OK, new Intent().putExtra(MainActivity.USERNAME_KEY, ((LoginSuccessfullyActivity)currentActivity).loginUsername()));
						currentActivity.finish();
					}
				} else if (replyCode == 301) { //已经上线
					if (currentActivity instanceof MainActivity) {
						enterUserinfoActivity(result);
					} else if (currentActivity instanceof LoginSuccessfullyActivity){	// process LoginSuccessfullyActivity's restart request
						// update LoginSuccessfullyActivity's user information
						LoginSuccessfullyActivity userinfoActivity = (LoginSuccessfullyActivity) currentActivity;
						UpdateUserInfoListTask mUpdateUserInfoListTask = new UpdateUserInfoListTask(userinfoActivity);
					    mUpdateUserInfoListTask.execute(new JSONObject[]{result});
					}
				} else if (replyCode == 302) { //不在线
					// do nothing here.
					if (currentActivity instanceof MainActivity) {
						final MainActivity mainActivity = (MainActivity) currentActivity;
						if (mainActivity.isAutoLogin) {	// Autologin only when user is not online
							String defaultUsername = mainActivity.defaultPref.getString(MainActivity.DEFAULT_USERNAME, "");
							mainActivity.mUsername = defaultUsername;
							if (MainActivity.getUserList().has(defaultUsername)) { 
								String defaultPassword = "";
								try {
									defaultPassword = MainActivity.getUserList().getString(defaultUsername);
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
					} else if (currentActivity instanceof LoginSuccessfullyActivity) {	// process LoginSuccessfullyActivity's restart request
						// resend login request
						LoginSuccessfullyActivity userinfoActivity = (LoginSuccessfullyActivity) currentActivity;
						String username = userinfoActivity.getmUserinfo().getUsername();
						String password = MainActivity.getUserList().getString(username);
						if (!"".equals(username) && !"".equals(password)) {
							HttpPostTask mHttpPostTask = new HttpPostTask(userinfoActivity, MainActivity.ACTION_REQUEST_URL);
							mHttpPostTask.execute(new String[]{"login", username, password});
						}
					}
				} else if (replyCode == 501) {	// 获取到在线信息
					Log.d(TAG, result.get("online").toString());
					if (result.get("online") instanceof JSONArray) {
						JSONArray onlineInformations = (JSONArray) result.get("online");
						if (onlineInformations.length() >= 1) {
							if (onlineInformations.get(0) instanceof JSONObject) {
								final JSONObject firstOnlineInformation = (JSONObject) onlineInformations.get(0);
								String areaName = firstOnlineInformation.get("area_name") != null ? firstOnlineInformation.get("area_name").toString() : "";
								String userIp = firstOnlineInformation.get("user_ip") != null ? firstOnlineInformation.get("acctstarttime").toString() : "";
								String acctStarttime = firstOnlineInformation.get("acctstarttime") != null ? firstOnlineInformation.get("acctstarttime").toString() : "";
								if (firstOnlineInformation != null) {
									new AlertDialog.Builder(currentActivity)
									.setIcon(android.R.drawable.ic_dialog_alert)
									.setTitle(result.get("reply_msg").toString())
									.setMessage(currentActivity.getResources().getString(R.string.logon_at_another_area) + "\n" +
											currentActivity.getResources().getString(R.string.at) +
											areaName + "\n" +
											currentActivity.getResources().getString(R.string.userip) + 
											UserInfo.getReadableIp(Long.parseLong(userIp)) + "\n" +
											currentActivity.getResources().getString(R.string.acctstarttime) +
											UserInfo.getReadableAcctstarttime(Long.parseLong(acctStarttime)) + "\n" +
											currentActivity.getResources().getString(R.string.logoff_and_logon))
									.setPositiveButton(currentActivity.getResources().getString(R.string.dialog_positive), new DialogInterface.OnClickListener(){
				
										@Override
										public void onClick(DialogInterface dialog, int which) {	// logoff and then logon
											Object acctsessionid;
											try {
												acctsessionid = firstOnlineInformation.get("acctsessionid");
												if (currentActivity instanceof MainActivity) {
													MainActivity mainActivity = (MainActivity) currentActivity;
													HttpPostTask mHttpPostTask = new HttpPostTask(mainActivity, MainActivity.DISCONNECT_REQUEST_URL);
													mHttpPostTask.execute(new String[]{mainActivity.mUsername.toString(), mainActivity.mPassword.toString(), acctsessionid.toString()});
												} else if (currentActivity instanceof LoginSuccessfullyActivity) {
													LoginSuccessfullyActivity userinfoActivity = (LoginSuccessfullyActivity) currentActivity;
													HttpPostTask mHttpPostTask = new HttpPostTask(userinfoActivity, MainActivity.DISCONNECT_REQUEST_URL);
													String username = userinfoActivity.getmUserinfo().getUsername();
													String password = MainActivity.getUserList().getString(username);
													mHttpPostTask.execute(new String[]{username, password, acctsessionid.toString()});
												}
											} catch (JSONException e) {
												e.printStackTrace();
											}
										}
									})
									.setNegativeButton(currentActivity.getResources().getString(R.string.dialog_negative), new DialogInterface.OnClickListener(){
				
										@Override
										public void onClick(DialogInterface dialog, int which) {	// logoff and then logon
											if (currentActivity instanceof LoginSuccessfullyActivity) {
												currentActivity.setResult(Activity.RESULT_OK, new Intent().putExtra(MainActivity.USERNAME_KEY, ((LoginSuccessfullyActivity)currentActivity).loginUsername()));
												currentActivity.finish();
											}
										}
									})
									.show();
								}
							}
						}
					}
				} else if (replyCode == 502) {	//不在线
					// do nothing
				} else if (replyCode == 503) {
					// do nothing
				} else if (replyCode == 504) {	// 请求错误
					Toast.makeText(currentActivity, currentActivity.getResources().getString(R.string.wrong_username_or_password), Toast.LENGTH_SHORT).show();
				} else if (replyCode == 601) {	// 远程下线成功
					if (currentActivity instanceof MainActivity) {
						MainActivity mainActivity = (MainActivity) currentActivity;
						HttpPostTask mHttpPostTask = new HttpPostTask(mainActivity, MainActivity.ACTION_REQUEST_URL);
						if (!mainActivity.isAutoLogin){
							mHttpPostTask.execute(new String[]{"login", mainActivity.mUsername.toString(), mainActivity.mPassword.toString()});
						} else {
							mHttpPostTask.execute(new String[]{"login", mainActivity.mUsername.toString(), mainActivity.mPassword.toString(), AUTO_LOGIN_PARAM});
						}
					} else if (currentActivity instanceof LoginSuccessfullyActivity) {
						LoginSuccessfullyActivity userinfoActivity = (LoginSuccessfullyActivity) currentActivity;
						String username = userinfoActivity.getmUserinfo().getUsername();
						String password = MainActivity.getUserList().getString(username);
						Intent logoutIntent = new Intent();
						logoutIntent.putExtra(MainActivity.USERNAME_KEY, username);
						logoutIntent.putExtra(MainActivity.PASSWORD_KEY, password);
						userinfoActivity.setResult(Activity.RESULT_OK, logoutIntent);
						userinfoActivity.finish();
//						HttpPostTask mHttpPostTask = new HttpPostTask(userinfoActivity, MainActivity.ACTION_REQUEST_URL);
//						mHttpPostTask.execute(new String[]{"login", username, password});
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
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void resetPasswordToEmpty(Activity currentActivity) {
		if (currentActivity instanceof MainActivity) {
			MainActivity tmpActivity = (MainActivity) currentActivity;
			tmpActivity.mPassword = "";
			tmpActivity.updateUI();
		}
	}
	
	private void enterUserinfoActivity(JSONObject result) {
		try {
			if (result.get("userinfo") instanceof JSONObject) {
				JSONObject userinfo = (JSONObject) result.get("userinfo");
				Intent loginSuccessfullyIntent = new Intent(currentActivity, LoginSuccessfullyActivity.class);
				Iterator it = userinfo.keys();
				while (it.hasNext()) {
					Object key = it.next();
					if (key != null) {
						String k = key.toString();
						loginSuccessfullyIntent.putExtra(k, userinfo.get(k) == null ? "" : userinfo.get(k).toString());
					}
				}
				currentActivity.startActivityForResult(loginSuccessfullyIntent, MainActivity.GET_USERNAME);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void onTaskFailed (String message) {
		Toast.makeText(currentActivity, message, Toast.LENGTH_SHORT).show();
	}
	
	private class JSONResponseHandler implements ResponseHandler<JSONObject> {
		private static final String JSON_RESPONSE_TAG = "JsonResponse";

		@Override
		public JSONObject handleResponse(HttpResponse response)
				throws ClientProtocolException, IOException {
			String JSONResponseResult = new BasicResponseHandler().handleResponse(response);
			Log.i(JSON_RESPONSE_TAG, JSONResponseResult);
			JSONObject responseObject = null;
			try {
				JSONTokener mJSONTokener = new JSONTokener(JSONResponseResult);
				responseObject = (JSONObject) mJSONTokener.nextValue();
			} catch (JSONException e) {
				Log.e(JSON_RESPONSE_TAG, "JSONException");
				e.printStackTrace();
			}
			return responseObject;
		}
	}
	
	private void showMessage(JSONObject result, String msgKey) {
		Object message = null;
		try {
			message = result.get(msgKey);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (message != null) Toast.makeText(currentActivity, message.toString(), Toast.LENGTH_SHORT).show();
	}
}
