package cn.edu.nju.ncrd.njubras.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import cn.edu.nju.ncrd.njubras.LoginSuccessfullyActivity;
import cn.edu.nju.ncrd.njubras.R;
import cn.edu.nju.ncrd.njubras.model.UserInfo;

import android.os.AsyncTask;

public class UpdateUserInfoListTask extends AsyncTask<JSONObject, Void, Void> {
	private LoginSuccessfullyActivity mUserinfoActivity;
	private UserInfo mUserinfo;
	private List<Map<String, String>> mList;
	
	public UpdateUserInfoListTask(LoginSuccessfullyActivity userinfoActivity) {
		this.mUserinfoActivity = userinfoActivity;
	}
	
	@Override
	protected void onPreExecute() {
		mUserinfo = this.mUserinfoActivity.getmUserinfo();
		mList = this.mUserinfoActivity.getmList();
	}
	
	@Override
	protected Void doInBackground(JSONObject... params) {
		if (params.length == 1) {
			mUserinfo.updateUserInfo(params[0]);
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(Void none) {
		updateListInfo();
		this.mUserinfoActivity.updateUI();
	}
	
	private void updateListInfo() {
		mList.clear();
		HashMap<String, String> usernameMap = new HashMap<String, String>();
	    usernameMap.put(LoginSuccessfullyActivity.KEY, mUserinfoActivity.getResources().getString(R.string.user_name));
	    usernameMap.put(LoginSuccessfullyActivity.VALUE, mUserinfo.getUsername());
	    mList.add(usernameMap);
	    HashMap<String, String> useripMap = new HashMap<String, String>();
	    useripMap.put(LoginSuccessfullyActivity.KEY, mUserinfoActivity.getResources().getString(R.string.userip));
	    useripMap.put(LoginSuccessfullyActivity.VALUE, mUserinfo.getReadableIp());
	    mList.add(useripMap);
//	    HashMap<String, String> useripv6Map = new HashMap<String, String>();
//	    useripv6Map.put(LoginSuccessfullyActivity.KEY, mUserinfoActivity.getResources().getString(R.string.useripv6));
//	    useripv6Map.put(LoginSuccessfullyActivity.VALUE, mUserinfo.getUseripv6());
//	    mList.add(useripv6Map);
	    HashMap<String, String> macMap = new HashMap<String, String>();
	    macMap.put(LoginSuccessfullyActivity.KEY, mUserinfoActivity.getResources().getString(R.string.mac));
	    macMap.put(LoginSuccessfullyActivity.VALUE, mUserinfo.getMac());
	    mList.add(macMap);
	    HashMap<String, String> acctstarttimeMap = new HashMap<String, String>();
	    acctstarttimeMap.put(LoginSuccessfullyActivity.KEY, mUserinfoActivity.getResources().getString(R.string.acctstarttime));
//	    acctstarttimeMap.put(LoginSuccessfullyActivity.VALUE, String.LoginSuccessfullyActivity.VALUEOf(mUserinfo.getOnlineHours()) + mUserinfoActivity.getResources().getString(R.string.hour)
//	    		+ String.LoginSuccessfullyActivity.VALUEOf(mUserinfo.getOnlineMinutes()) + mUserinfoActivity.getResources().getString(R.string.minute)
//	    		+ String.LoginSuccessfullyActivity.VALUEOf(mUserinfo.getOnlineSeconds()) + mUserinfoActivity.getResources().getString(R.string.second));
	    acctstarttimeMap.put(LoginSuccessfullyActivity.VALUE, mUserinfo.getReadableAcctstarttime());
	    mList.add(acctstarttimeMap);
	    HashMap<String, String> fullnameMap = new HashMap<String, String>();
	    fullnameMap.put(LoginSuccessfullyActivity.KEY, mUserinfoActivity.getResources().getString(R.string.fullname));
	    fullnameMap.put(LoginSuccessfullyActivity.VALUE, mUserinfo.getFullname());
	    mList.add(fullnameMap);
	    HashMap<String, String> serviceNameMap = new HashMap<String, String>();
	    serviceNameMap.put(LoginSuccessfullyActivity.KEY, mUserinfoActivity.getResources().getString(R.string.service_name));
	    serviceNameMap.put(LoginSuccessfullyActivity.VALUE, mUserinfo.getServiceName());
	    mList.add(serviceNameMap);
	    HashMap<String, String> areaNameMap = new HashMap<String, String>();
	    areaNameMap.put(LoginSuccessfullyActivity.KEY, mUserinfoActivity.getResources().getString(R.string.area_name));
	    areaNameMap.put(LoginSuccessfullyActivity.VALUE, mUserinfo.getAreaName());
	    mList.add(areaNameMap);
	    HashMap<String, String> payamountMap = new HashMap<String, String>();
	    payamountMap.put(LoginSuccessfullyActivity.KEY, mUserinfoActivity.getResources().getString(R.string.payamount));
	    payamountMap.put(LoginSuccessfullyActivity.VALUE, mUserinfo.getPayamount());
	    mList.add(payamountMap);
	}
}
