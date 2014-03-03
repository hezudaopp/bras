var onlinelist_display = function(){
	if($("#onlinelist_win").length == 0)	$("body").append('<div id="onlinelist_win"></div>');
	$.ajax({
		url:"/proxy/onlinelist.php",
		type:'POST',
		data : {username:username,password:password},
		dataType:'json',
		cache : false,
		timeout : 10000,
		success: function(resp){
//			$('.request_mask').remove();
			if(resp.reply_code == 501){	// 获取到在线信息
				var html = '';
				$.each(resp.online,function(k,v){
					html += "IP地址：" + long2ip(v.user_ip) + "&nbsp;&nbsp;";
					html += "登录时间："+ (new Date(v.acctstarttime*1000).format('MM-dd hh:mm')) + "&nbsp;&nbsp;";
					html += '<a href="javascript:void(0);" onclick="bras_disconnect(\''+v.acctsessionid+'\');">点击下线由本机登录</a><br>';
				});
				$("#onlinelist_win").window({
					title : username+" 您好，您的帐号已在其他地点使用中：",
					collapsible : false,
					minimizable : false,
					maximizable : false,
					resizable : false,
					width : 400,
					height : 100,
					onBeforeClose : function(){$('#onlinelist_win').empty();$('.request_mask').remove();},
					content : html
				});
			}else if(resp.reply_code == 502){	// 不在线
				login_request();
			}else if(resp.reply_code == 503){
				$('.request_mask').remove();
				$.messager.alert('错误',resp.reply_msg);
			}else{
				$.messager.alert('错误',"请求错误!");
			}
		}
	});

	return ;
};

var bras_disconnect = function(acctsessionid){
	$.ajax({
		url:"/proxy/disconnect.php",
		type:'POST',
		data : {username:username,password:password,acctsessionid:acctsessionid},
		dataType:'json',
		timeout : 5000,
		cache : false,
		beforeSend: function(){
			$("#onlinelist_win").window('destroy');
			$('.request_mask').remove();
			request_wait();
		}, // 请求之前动作
		success: function(resp){
			if(resp.reply_code == 601){ // 下线成功
				login_request();
			}
		}
	});
	return ;
};

var router_warning = function(){
	var html = "";
	html += '<div style="PADDING-BOTTOM: 5px; MARGIN: 5px; PADDING-LEFT: 5px; PADDING-RIGHT: 5px; PADDING-TOP: 5px;">';
	html += '小路由器上网模式将可能导致出校访问速度慢、IPv6网站无法访问、个人信息泄露等问题。';
	html += '校园网用户请遵守国家有关互联网使用的相关规定，帐号限个人使用，不得转借、转让。';
	html += '有关小路由器转为交换机的设置方法，';
	html += '请参考<a href="http://BBS.NJU.EDU.CN/bbscon?board=NetResources&file=G.1314347439.A&num=1077" target="_blank">bbs.nju.edu.cn</a>。</a>';
	$('#warning').panel({
		width:600,
		title: "友情提醒>>",
		collapsible: false,
		loadingMessage : '数据加载中...',
		cls : 'div_center',
		content : html
	}).after("<br>");
};

var userinfo_display = function(resp){
	var userinfo_html = '';
	var disconnect_notice = '';
	var now = new Date();
	var tomorrow = new Date();
	tomorrow.setTime(now.getTime()+86400000);

	if(tomorrow.getDate() == 1){
		disconnect_notice = '<font color="red">请在今晚24点前登出，以免跨月计费。</font>';
	}

	if((resp.reply_code == 301) || (resp.reply_code == 101)){
		var userinfo = resp.userinfo;
		userinfo.html += '<span id="depart"></span>';

		if(userinfo.fullname != null) userinfo_html += userinfo.fullname;
		if(userinfo.username != null) userinfo_html += "（" + userinfo.username + "），";

		if((userinfo.area_name != null) && (userinfo.acctstarttime)){
			var unixTimestamp = new Date(userinfo.acctstarttime*1000);
			userinfo_html += "欢迎您自"+userinfo.area_name+"（"+unixTimestamp.format('yyyy-MM-dd hh:mm:ss')+"）登录。";

		}

		userinfo_html +='<span id="use"></span>';

		$.ajax({
			url:"/proxy/userinfo.php",
			type:'POST',
			dataType:'json',
			timeout : 5000,
			cache : false,
			success: function(resp){
				if(resp.reply_code == 401){
					if(resp.userinfo.depart)	$("#depart").html(resp.userinfo.depart+" ");
					if((resp.userinfo.total_time != null) && (resp.userinfo.payamount != null)){
						$("#use").html("截至本次登录前，您当月累计上网"+convert_time(resp.userinfo.total_time)+"，网络帐号余额&nbsp;" + resp.userinfo.payamount + "&nbsp;元。");
					}
				}
			}
		});


	}

	userinfo_html += disconnect_notice;
	userinfo_html += '&nbsp;&nbsp;<a href="javascript:void(0);" onclick="logout_request();"><font color="red">【退出登录】</font></a>';

	$('#userinfo').panel({
		width:600,
		title: "用户信息&gt;&gt;",
		collapsible: false,
		loadingMessage : '数据加载中...',
		cls : 'div_center',
		content : '<div id="userinfo_content" style="PADDING-BOTTOM: 5px; MARGIN: 5px; PADDING-LEFT: 5px; PADDING-RIGHT: 5px; PADDING-TOP: 5px;">'+userinfo_html+'</div>'
	}).after("<br>");
};

var notice_display = function(){
	$.ajax({
		url:"/proxy/notice.php",
		type:'POST',
		dataType:'json',
		timeout : 5000,
		cache : false,
		success: function(resp){
			var html = '';
			var i;
			if((resp.total != null) && (resp.total>0)){
				html += '<div id="notice_content" style="PADDING-BOTTOM: 5px; MARGIN: 5px; PADDING-LEFT: 5px; PADDING-RIGHT: 5px; PADDING-TOP: 5px;"><table>';
				for(i=0;i<resp.total;i++){
					no = i+1;
					if(resp.notice[i].url){
						html += "<tr><td width=10 align='center' valign='top'>" + no + ".</td><td align='left'><a href='"+resp.notice[i].url+"' target='_blank'>"+resp.notice[i].title+"</a></td></tr>";
					}else{
						html += "<tr><td width=10 align='center' valign='top'>" + no + ".</td><td align='left'>"+resp.notice[i].title+"</td></tr>";
					}
				}
				html += "</table><div>";

				$('#notice').panel({
					width:600,
					title: "通知公告&gt;&gt;",
					collapsible: false,
					loadingMessage : '数据加载中...',
					cls : 'div_center',
					content : html
				}).after("<br>");
			}
		}
	});
};

var logout_wait = function(){
	$('#userinfo').panel('setTitle',"系统正在处理中，请稍侯...");

	$("#userinfo_content").empty().append('<div id="pbr1" style="background:#FFFFFF;position:relative;width:570;px;top:0;"></div>');

	$('#pbr1').progressbar();

	if($("#pbr1").length)	pbr_i("pbr1");
	return ;
};


var convert_time = function(t){
	var time_str = "";
	var s;
	var m;
	var h1;
	var h;

	s=t%60;
	m=(t-s)/60;
	if (m>60){
		h1=m%60;
		h=(m-h1)/60;
		time_str=h+'小时'+h1+'分钟';
	}else if(m==0){
		time_str=s+'秒';
	}else{
		time_str=m+'分钟'+s+'秒';
	}
	return time_str;
};


Date.prototype.format = function(format) {
	var o = {
		"M+" : this.getMonth() + 1, // month
		"d+" : this.getDate(), // day
		"h+" : this.getHours(), // hour
		"m+" : this.getMinutes(), // minute
		"s+" : this.getSeconds(), // second
		"q+" : Math.floor((this.getMonth() + 3) / 3), // quarter 
		"S" : this.getMilliseconds()
	}
	if (/(y+)/.test(format)) {
		format = format.replace(RegExp.$1, (this.getFullYear() + "")
				.substr(4 - RegExp.$1.length));
	}
	for ( var k in o) {
		if (new RegExp("(" + k + ")").test(format)) {
			format = format.replace(RegExp.$1, RegExp.$1.length == 1 ? o[k] : ("00" + o[k]).substr(("" + o[k]).length));
		}
	}
	return format;
};

