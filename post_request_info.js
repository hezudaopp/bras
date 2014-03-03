var url = "/portal/portal_io.do";
var customer = '南京大学';
var username;
var password;

$(function(){	return info_request();});

var logout_request = function(){
	$.ajax({
		type : 'POST',
		url : url,
		dateType : 'json',
		data : {action: 'logout'},
		timeout : 32000,
		cache : false,
		beforeSend: function(){	logout_wait();}, // 请求之前动作
		error : function(){  $.messager.alert('错误','系统错误!');},
		success : function(resp){
			if(resp.reply_code == 201){
				$.messager.alert('信息','下线成功!');
				login_form();	
			}else{
				$('.request_mask').remove();
				$.messager.alert('错误',resp.reply_message);
			}
		}
	});
	return ;
};

var login_request = function(){
	username = $('#username').val();
	password = $('#password').val();
	var save_password = false;

	if($('#save_password').prop( "checked" ))	save_password = true;

	if(username == ''){
		$.messager.alert('错误','请输入帐号!');
		return false;	
	}

	if(password == ''){
		$.messager.alert('错误','请输入密码!');
		return false;
	}

	// 发送登陆请求
	$.ajax({
		type : 'POST',
		url : url,
		dateType : 'json',
		data:{ action:'login',username:username,password:password} ,
		timeout : 20000,
		cache : false,
		beforeSend: function(){ request_wait();}, // 请求之前动作
		error : function(){  $.messager.alert('错误','系统错误!');},
		success : function(resp){
			if(resp.reply_code == 101){	// 登陆成功，
				if(save_password){	// 保存密码
					$.cookie('username', base64encode(username), { expires: 365 });
					$.cookie('password', base64encode(password), { expires: 365 });
					$.cookie('save_password', 'true', { expires: 365 });
				}
				
				main(resp);	// 创建主页面
			}else if((resp.reply_code == 103 ) && (resp.reply_message == "E002 您的登录数已达最大并发登录数!")){
				onlinelist_display();
			}else{
				$('.request_mask').remove();
				$.messager.alert('错误',resp.reply_message);
			}
		}
	});
	return true;
};

var info_request = function(){
	$.ajax({
		type : 'POST',
		url : '/proxy/online.php',
		dateType : 'json',
		data : {action: 'info'},
		cache : false,
		timeout : 32000,
		beforeSend: function(){ request_wait();}, // 请求之前动作
		error : function(){  $.messager.alert('错误','系统错误!');},
		success : function(resp){
			if(resp.reply_code == 301){
				main(resp);
			}else{
				login_form();
			}
		}
	});
	return ;
};

// 清除页面内容，现实登陆窗口
var login_form = function(){

	login_div = document.createElement('div');
	foot_div = document.createElement('div');
	$(login_div).addClass('login_div');
	$(foot_div).attr('style','margin:0 auto;position:relative;top:120px;');
	$('body').empty().attr("style","background-color:#E5E5E5;").append(login_div).append(foot_div);

	row_username = document.createElement('div');	// 创建用户名输入行
	row_password = document.createElement('div');	// 创建密码输入行
	row_save_password = document.createElement('div');	// 创建保存密码行
	row_button = document.createElement('div');		// 创建按钮行

	username_label = document.createElement('span');
	username_input = document.createElement('input');
	$(username_label).append('<strong>帐号:</strong>').addClass('login_label');
	$(username_input).attr('type','text').attr('id','username').addClass('login_input');

	$(row_username).append(username_label).append(username_input).addClass('login_row');

	password_label = document.createElement('span');
	password_input = document.createElement('input');
	$(password_label).append('<strong>密码:</strong>').addClass('login_label');
	$(password_input).attr('type','password').attr('id','password').addClass('login_input');

	$(row_password).append(password_label).append(password_input).addClass('login_row');

	save_password_check = document.createElement('input');
	$(save_password_check).attr('id','save_password').attr('type','checkbox').bind('click',function(){
		if(!($(this).prop( "checked" ))){
			$.removeCookie('username');
			$.removeCookie('password');
			$.removeCookie('save_password');
		}
	});	
	$(row_save_password).append(save_password_check).append('保存密码').addClass('login_row').
		append('<br><font color="red">(公共机器请勿点击)</font>');

        $(username_input).keypress(function(event){      if(event.which == 13)   login_request();});
        $(password_input).keypress(function(event){      if(event.which == 13)   login_request();});

	submit_button = document.createElement('input');
	$(submit_button).attr('type','button').attr('value','登录').bind('click',function(){ return login_request();});
	$(row_button).append(submit_button).addClass('login_row');	

	$(login_div).append(row_username).append(row_password).append(row_save_password).append(row_button);

	$(foot_div).append('<table align="center"><tr><td valign="top">提示：</td><td>1.请使用BRAS系统的用户名和密码登录，计费策略同BRAS。<br>2.下线请访问<a href="http://p.nju.edu.cn/" target="top">http://p.nju.edu.cn</a><br>3.咨询电话：89683791<br></td></tr><tr><td colspan=2 align=center><hr>南京大学网络信息中心</td></tr></table>');
	
	if($.cookie('save_password') != null){
		username_decode = $.cookie('username');
		if(username_decode != null)		var username = base64decode(username_decode);
		password_decode = $.cookie('password');
		if(password_decode != null)		var password = base64decode(password_decode);

		$('#username').val(username);
		$('#password').val(password);
		$('#save_password').attr('checked',true);
	}
};


// 显示主页面
var main = function(resp){
	var url_decode = null;
	var url = null;

	var logo = document.createElement('div');
	var userinfo = document.createElement('div');
	var notice = document.createElement('div');
	var warning = document.createElement('div');

	$(logo).attr('id','logo');
	$(userinfo).attr('id','userinfo').append('<div id="div_userinfo" class="div_userinfo">');
	$(notice).attr('id','notice').append('<div id="div_notice" class="div_userinfo">');
	$(warning).attr('id','warning').append('<div id="div_warning" class="div_userinfo">');

	$('body').empty().append(logo).append('<br><br>').append(userinfo).append(notice).append(warning)
		.append('<div class="div_userinfo"><a href="http://nic.nju.edu.cn">南京大学网络信息中心 &nbsp;&nbsp;咨询电话：89683791</a></div>');

	title = document.createElement('div');
	$(title).append(customer+'网络接入系统').addClass('title');
	$('#logo').addClass('logo').append(title);

	url_decode = get_url("redirect");
	if(url_decode)	url = base64decode(url_decode);

	userinfo_display(resp);
	notice_display();
	router_warning();
};

var request_wait = function(){
	$('body').append('<div class="request_mask"></div>');
	$(".request_mask").empty().append('<div style="background:#FFFFFF;position:relative;width:420px;top:300;height:50;text-align:center;margin:0 auto;">系统正在处理，请稍侯...<div id="pbr" style="background:#FFFFFF;position:relative;width:400px;top:0;text-align:left;margin:0 auto;"></div></div>');

	$('#pbr').progressbar();

	if($("#pbr").length)	pbr_i("pbr");

	return ;
};

var pbr_i = function(element){
	if($("#"+element).length == 0)	return;
	var value = $("#"+element).progressbar('getValue');
	if(value < 100){
		value += 2;
		$("#"+element).progressbar('setValue', value);
		setTimeout(function(){ pbr_i(element);}, 100);
	}
	return;
};

var long2ip = function(num){
	var str;
	var tt = new Array();
	tt[0] = (num >>> 24) >>> 0;
	tt[1] = ((num << 8) >>> 24) >>> 0;
	tt[2] = (num << 16) >>> 24;
	tt[3] = (num << 24) >>> 24;
	str = String(tt[0]) + "." + String(tt[1]) + "." + String(tt[2]) + "." + String(tt[3]);
	return str;
};

var get_url = function(param){
	var url = location.href;
	var paraString = url.substring(url.indexOf("?")+1,url.length).split("&");
	var paraObj = {}
	for (i=0; j=paraString[i]; i++){
		paraObj[j.substring(0,j.indexOf("=")).toLowerCase()] = j.substring(j.indexOf("=")+1,j.length);
	}
	var returnValue = paraObj[param.toLowerCase()];
	if(typeof(returnValue)=="undefined"){
		return "";
	}else{
		return returnValue;
	}
}

