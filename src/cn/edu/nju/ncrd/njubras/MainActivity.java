package cn.edu.nju.ncrd.njubras;

import org.json.JSONException;
import org.json.JSONObject;

import cn.edu.nju.ncrd.njubras.task.HttpPostTask;
import cn.edu.nju.ncrd.njubras.task.ReadJSONFromFileTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
	public static final String MAIN_TAG = "MainActivity";
	public static final String USERS_JSON_FILENAME = "users_json.txt";
	public static final String USER_LIST_KEY = "users";
	public static final String ACTION_REQUEST_URL = "http://p.nju.edu.cn/portal/portal_io.do";
	public static final String INFO_REQUEST_URL = "http://p.nju.edu.cn/proxy/online.php";
	public static final String ONLINE_INFO_REQUEST_URL = "http://p.nju.edu.cn/proxy/onlinelist.php";
	public static final String DISCONNECT_REQUEST_URL = "http://p.nju.edu.cn/proxy/disconnect.php";
	public static final String PREFERENCE_FILENAME = "Preference";
	public static final String DEFAULT_USERNAME = "DefaultUsername";
	public static final String DEFAULT_SAVE_PASSWORD = "DefaultSavePassword";
	public static final String DEFAULT_AUTO_LOGIN = "DefaultAutoLogin";
	public static final int GET_USERNAME = 0;
	public static final String USERNAME_KEY = "username";
	
	private AutoCompleteTextView mUsernameView;
	private EditText mPasswordView;
	private CheckBox savePasswordCheckBox;
	private CheckBox autoLoginCheckBox;
	private Button mLogonButton;
	
	public CharSequence mUsername;
	public CharSequence mPassword;
	public boolean isSavePassword;
	public boolean isAutoLogin;
	
	public SharedPreferences defaultPref;
	
	private JSONObject mUserList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		
		if (mUserList == null) {
			ReadJSONFromFileTask mReadJSONFromFileTask = new ReadJSONFromFileTask(this);
			mReadJSONFromFileTask.execute(new String[]{MainActivity.USERS_JSON_FILENAME});
		}
		
		mUsernameView = (AutoCompleteTextView) findViewById(R.id.username_text);
		mPasswordView = (EditText) findViewById(R.id.password_text);
		mUsername = mUsernameView.getText();
		mPassword = mPasswordView.getText();
		savePasswordCheckBox = (CheckBox) this.findViewById(R.id.save_password_checkbox);
		autoLoginCheckBox = (CheckBox) this.findViewById(R.id.auto_login_checkbox);
		mLogonButton = (Button) findViewById(R.id.logon_button);
		
		defaultPref = getSharedPreferences(PREFERENCE_FILENAME, 0);
		isSavePassword = defaultPref.getBoolean(DEFAULT_SAVE_PASSWORD, true);
		savePasswordCheckBox.setChecked(isSavePassword);
		isAutoLogin = defaultPref.getBoolean(DEFAULT_AUTO_LOGIN, true);
		autoLoginCheckBox.setChecked(isAutoLogin);
		autoCompletePassword(defaultPref.getString(DEFAULT_USERNAME, ""));
		
		mLogonButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				updateUIInfo();
				if (!"".equals(mPassword.toString().trim()) && !"".equals(mPassword.toString().trim())) {
					HttpPostTask mHttpPostTask = new HttpPostTask(MainActivity.this, ACTION_REQUEST_URL);
					mHttpPostTask.execute(new String[]{"login", mUsername.toString(), mPassword.toString()});
				} else {
					Toast.makeText(MainActivity.this, MainActivity.this.getApplicationContext().getString(R.string.empty_username_or_password), Toast.LENGTH_SHORT).show();
				}
			}
			
		});
		
		mUsernameView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				autoCompletePassword(((CharSequence)arg0.getItemAtPosition(arg2)).toString());
			}
			
		});
		mUsernameView.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				autoCompletePassword(mUsernameView.getText().toString());
			}
			
		});
		
	}
	
	private void autoCompletePassword(String username) {
		this.mUsername = username;
		if (this.getUserList().has(username.toString())) {
			try {
				this.mPassword = this.getUserList().getString(username.toString());
				this.updateUI();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			this.mPassword = "";
			this.updateUI();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public void onStop() {
		super.onStop();
		isSavePassword = savePasswordCheckBox.isChecked();
		isAutoLogin = autoLoginCheckBox.isChecked();
		SharedPreferences.Editor editor = defaultPref.edit();
		editor.putBoolean(DEFAULT_SAVE_PASSWORD, isSavePassword);
		editor.putBoolean(DEFAULT_AUTO_LOGIN, isAutoLogin);
		// Commit the edits!
	    editor.commit();
	}

	public JSONObject getUserList() {
		if (mUserList == null)
			mUserList = new JSONObject();
		return mUserList;
	}

	public void setUserList(JSONObject mUserList) {
		this.mUserList = mUserList;
	}

	public AutoCompleteTextView getUsernameView() {
		return mUsernameView;
	}
	
	public void updateUI() {
		this.mUsernameView.setText(mUsername);
		this.mPasswordView.setText(mPassword);
		this.savePasswordCheckBox.setChecked(isSavePassword);
		this.autoLoginCheckBox.setChecked(isAutoLogin);
	}
	
	public void updateUIInfo() {
		this.mUsername = mUsernameView.getText();
		this.mPassword = mPasswordView.getText();
		this.isAutoLogin = autoLoginCheckBox.isChecked();
		this.isSavePassword = savePasswordCheckBox.isChecked();
		if (isAutoLogin) {
			this.isSavePassword = true;
			updateUI();
		}
	}
	
	@Override
	public void onBackPressed() {
//		finish();
		super.onBackPressed();
	}
	
	protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        if (requestCode == GET_USERNAME) {
            if (resultCode == RESULT_OK) {
                String username = defaultPref.getString(DEFAULT_USERNAME, data.getExtras().getString(USERNAME_KEY));
                autoCompletePassword(username);
                updateUI();
            }
        }
    }
}
