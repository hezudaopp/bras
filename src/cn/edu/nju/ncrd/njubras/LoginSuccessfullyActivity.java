package cn.edu.nju.ncrd.njubras;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.nju.ncrd.njubras.model.UserInfo;
import cn.edu.nju.ncrd.njubras.task.HttpPostTask;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

public class LoginSuccessfullyActivity extends ListActivity {
	private UserInfo mUserinfo = null;
	private static final String KEY = "key";
	private static final String VALUE = "value"; 
	
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
		    List<Map<String, String>> mList = new ArrayList<Map<String, String>>();
		    HashMap<String, String> usernameMap = new HashMap<String, String>();
		    usernameMap.put(KEY, getResources().getString(R.string.user_name));
		    usernameMap.put(VALUE, mUserinfo.getUsername());
		    mList.add(usernameMap);
		    HashMap<String, String> useripMap = new HashMap<String, String>();
		    useripMap.put(KEY, getResources().getString(R.string.userip));
		    useripMap.put(VALUE, mUserinfo.getReadableIp());
		    mList.add(useripMap);
//		    HashMap<String, String> useripv6Map = new HashMap<String, String>();
//		    useripv6Map.put(KEY, getResources().getString(R.string.useripv6));
//		    useripv6Map.put(VALUE, mUserinfo.getUseripv6());
//		    mList.add(useripv6Map);
		    HashMap<String, String> macMap = new HashMap<String, String>();
		    macMap.put(KEY, getResources().getString(R.string.mac));
		    macMap.put(VALUE, mUserinfo.getMac());
		    mList.add(macMap);
		    HashMap<String, String> acctstarttimeMap = new HashMap<String, String>();
		    acctstarttimeMap.put(KEY, getResources().getString(R.string.acctstarttime));
//		    acctstarttimeMap.put(VALUE, String.valueOf(mUserinfo.getOnlineHours()) + getResources().getString(R.string.hour)
//		    		+ String.valueOf(mUserinfo.getOnlineMinutes()) + getResources().getString(R.string.minute)
//		    		+ String.valueOf(mUserinfo.getOnlineSeconds()) + getResources().getString(R.string.second));
		    acctstarttimeMap.put(VALUE, mUserinfo.getReadableAcctstarttime());
		    mList.add(acctstarttimeMap);
		    HashMap<String, String> fullnameMap = new HashMap<String, String>();
		    fullnameMap.put(KEY, getResources().getString(R.string.fullname));
		    fullnameMap.put(VALUE, mUserinfo.getFullname());
		    mList.add(fullnameMap);
		    HashMap<String, String> serviceNameMap = new HashMap<String, String>();
		    serviceNameMap.put(KEY, getResources().getString(R.string.service_name));
		    serviceNameMap.put(VALUE, mUserinfo.getServiceName());
		    mList.add(serviceNameMap);
		    HashMap<String, String> areaNameMap = new HashMap<String, String>();
		    areaNameMap.put(KEY, getResources().getString(R.string.area_name));
		    areaNameMap.put(VALUE, mUserinfo.getAreaName());
		    mList.add(areaNameMap);
		    HashMap<String, String> payamountMap = new HashMap<String, String>();
		    payamountMap.put(KEY, getResources().getString(R.string.payamount));
		    payamountMap.put(VALUE, mUserinfo.getPayamount());
		    mList.add(payamountMap);
		    
		     // Now create a new list adapter bound to the cursor.
	         ListAdapter adapter = new SimpleAdapter(this, mList, R.layout.userinfo_list_item, new String[] {KEY, VALUE}, new int[] {R.id.userinfo_key_text_view, R.id.userinfo_value_text_view});
	         // Bind to our new adapter.
	         setListAdapter(adapter);
	         
	         logoutButton = (Button) findViewById(R.id.logout_button);
	         logoutButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					sendLogoutRequest();
				}
	        	 
	         });
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
				LoginSuccessfullyActivity.super.onBackPressed();
			}
		})
		.setNegativeButton(getResources().getString(R.string.dialog_negative), null)
		.show();
	}
	
	public String loginUsername() {
		return mUserinfo.getUsername();
	}
}
