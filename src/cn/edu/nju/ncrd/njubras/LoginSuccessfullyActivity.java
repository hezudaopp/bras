package cn.edu.nju.ncrd.njubras;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.edu.nju.ncrd.njubras.model.UserInfo;
import cn.edu.nju.ncrd.njubras.task.HttpPostTask;
import cn.edu.nju.ncrd.njubras.task.UpdateUserInfoListTask;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.SimpleAdapter;

public class LoginSuccessfullyActivity extends ListActivity {
	private UserInfo mUserinfo = null;
	private List<Map<String, String>> mList = new ArrayList<Map<String, String>>();	

	public static final String KEY = "key";
	public static final String VALUE = "value"; 

	// Now create a new list adapter bound to the cursor.
    private BaseAdapter adapter;
	private Button logoutButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.userinfo_list_view);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
		    mUserinfo = new UserInfo(extras.getString("username"),
		    						 Long.parseLong(extras.getString("userip")),
		    						 extras.getString("useripv6"),
		    						 extras.getString("mac"),
		    						 Long.parseLong(extras.getString("acctstarttime")),
		    						 extras.getString("fullname"),
		    						 extras.getString("service_name"),
		    						 extras.getString("area_name"),
		    						 extras.getString("payamount"));
		    adapter = new SimpleAdapter(this, mList, 
					R.layout.userinfo_list_item, 
					new String[] {KEY, VALUE}, 
					new int[] {R.id.userinfo_key_text_view, R.id.userinfo_value_text_view});
		    // Bind to our new adapter.
	        setListAdapter(adapter);
	         
	        logoutButton = (Button) findViewById(R.id.logout_button);
	        logoutButton.setOnClickListener(new OnClickListener() {
	        	@Override
				public void onClick(View v) {
					sendLogoutRequest();
	        	}
	        });
	        
	        UpdateUserInfoListTask mUpdateUserInfoListTask = new UpdateUserInfoListTask(this);
		    mUpdateUserInfoListTask.execute();
		}
		
	}
	
	private void sendLogoutRequest() {
		HttpPostTask mHttpPostTask = new HttpPostTask(this, MainActivity.ACTION_REQUEST_URL);
		mHttpPostTask.execute(new String[]{"logout"});
	}
	
	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle(getResources().getString(R.string.logout_dialog_title))
		.setMessage(getResources().getString(R.string.logout_dialog_message))
		.setPositiveButton(getResources().getString(R.string.dialog_positive), new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				sendLogoutRequest();
			}
		})
		.setNegativeButton(getResources().getString(R.string.dialog_negative), null)
		.show();
	}
	
	public String loginUsername() {
		return mUserinfo.getUsername();
	}
	
	/**
	 * 1.check isOnline, if so, update user information, else, go to 2
	 * 2.check current login user isOnline in this machine, else, go to 3
	 * 3.send login request.
	 */
	@Override
	protected void onRestart() {
		super.onRestart();
		HttpPostTask mHttpPostTask = new HttpPostTask(this, MainActivity.INFO_REQUEST_URL);
		mHttpPostTask.execute(new String[]{"info"});
	}
	
	public void updateUI() {
		adapter.notifyDataSetChanged();
	}
	
	public UserInfo getmUserinfo() {
		if (mUserinfo == null) {
			mUserinfo = new UserInfo();
		}
		return mUserinfo;
	}

	public void setmUserinfo(UserInfo mUserinfo) {
		this.mUserinfo = mUserinfo;
	}
	
	public List<Map<String, String>> getmList() {
		return mList;
	}
}
