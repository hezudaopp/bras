package cn.edu.nju.ncrd.njubras.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

public class UserInfo {
	private static final long HOUR = 3600000;
	private static final long MINUTE = 60000;
	private static final long SECOND = 1000;
	
	private String username;
	private long userip;
	private String useripv6;
	private String mac;
	private long acctstarttime;
	private String fullname;
	private String serviceName;
	private String areaName;
	private String payamount;
	
	public UserInfo(){}
	
	public UserInfo(String username, long userip, String useripv6, String mac,
			long acctstarttime, String fullname, String serviceName,
			String areaName, String payamount) {
		super();
		this.username = username;
		this.userip = userip;
		this.useripv6 = useripv6;
		this.mac = mac;
		this.acctstarttime = acctstarttime;
		this.fullname = fullname;
		this.serviceName = serviceName;
		this.areaName = areaName;
		this.payamount = payamount;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public long getUserip() {
		return userip;
	}

	public void setUserip(long userip) {
		this.userip = userip;
	}

	public String getUseripv6() {
		return useripv6;
	}

	public void setUseripv6(String useripv6) {
		this.useripv6 = useripv6;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public long getAcctstarttime() {
		return acctstarttime;
	}

	public void setAcctstarttime(long acctstarttime) {
		this.acctstarttime = acctstarttime;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getAreaName() {
		return areaName;
	}

	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}

	public String getPayamount() {
		return payamount;
	}

	public void setPayamount(String payamount) {
		this.payamount = payamount;
	}
	
	public String getReadableIp() {
		return getReadableIp(this.userip);
	}
	
	public static String getReadableIp(long userip) {
		StringBuilder sb = new StringBuilder(15);
	    for (int i = 0; i < 4; i++) {
	        sb.insert(0, Long.toString(userip & 0xff));
	        if (i < 3) {
	            sb.insert(0, '.');
	        }
	        userip >>= 8;
	    }
	    return sb.toString();
	}
	
	public long getOnlineHours() {
		return this.acctstarttime / HOUR;
	}
	
	public long getOnlineMinutes() {
		return this.acctstarttime % HOUR / (MINUTE);
	}
	
	public long getOnlineSeconds() {
		return this.acctstarttime % HOUR % MINUTE / SECOND;
	}
	
	public String getReadableAcctstarttime() {
		return getReadableAcctstarttime(this.acctstarttime);
	}
	
	public static String getReadableAcctstarttime(long acctstarttime) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.SIMPLIFIED_CHINESE);
		return dateFormat.format(new Date((acctstarttime)*1000));
	}
	
	public void updateUserInfo(JSONObject userInfo) {
		try {
			Object mUsername = userInfo.get("username") == null ? "" : userInfo.get("username");
			this.username = mUsername.toString();
			Object mUserip = userInfo.get("userip") == null ? "" : userInfo.get("userip");
			this.userip = Long.parseLong(mUserip.toString());
			Object mUseripv6 = userInfo.get("useripv6") == null ? "" : userInfo.get("useripv6");
			this.useripv6 = mUseripv6.toString();
			Object mMac = userInfo.get("mac") == null ? "" : userInfo.get("mac");
			this.mac = mMac.toString();
			Object mAcctstarttime = userInfo.get("acctstarttime") == null ? "" : userInfo.get("acctstarttime");
			this.acctstarttime = Long.parseLong(mAcctstarttime.toString());
			Object mFullname = userInfo.get("fullname") == null ? "" : userInfo.get("fullname");
			this.fullname = mFullname.toString();
			Object mServicename = userInfo.get("service_name") == null ? "" : userInfo.get("service_name");
			this.serviceName = mServicename.toString();
			Object mAreaname = userInfo.get("area_name") == null ? "" : userInfo.get("area_name");
			this.areaName = mAreaname.toString();
			Object mPayamount = userInfo.get("payamount") == null ? "" : userInfo.get("payamount");
			this.payamount = mPayamount.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
