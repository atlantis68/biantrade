<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<script type="text/javascript" src="../js/jquery-1.7.2.min.js"></script>
<html>
<head>
    <title>主页</title>
    <script type="text/javascript">  

    window.onload = function(){
    	findUser();
    }
    
	    function findUser(){  
	    	$("#message").html('');
	        $.ajax({  
	            type : "get",
	            url : "/User/findUser",
	            timeout : 10000,
	            data : '',

	            //成功
	            success : function(data) {
	            	if(data.indexOf("登录") > -1) {
	            		window.location.href='User/logout';
	            	} else {
	            		var jsonObject= jQuery.parseJSON(data);  
	            		if(jsonObject.status == 'error') {
	            			$("#message").html(jsonObject.msg);
	            		} else {
	            			var msgObject= jQuery.parseJSON(jsonObject.msg);  
	            			$("#id").val(msgObject.id);
	            			$("#password").val(msgObject.password);
	            			$("#apiKey").val(msgObject.apiKey);
	            			$("#secretKey").val(msgObject.secretKey);
	            			$("#mail").val(msgObject.mail);
	            		}

	            	}
	            },

	            //错误情况
	            error : function(error) {
	                console.log("error : " + error);
	            },

	            //请求完成后回调函数 (请求成功或失败之后均调用)。
	            complete: function(message) {
	            } 
	        });  
	    }    


	    function save(){  
	    	$("#message").html('');
	    	$("#save").attr("disabled","true");
	        $.ajax({  
	            type : "get",
	            url : "/User/update",
	            timeout : 10000,
	            data : "id=" + $("#id").val() + "&password=" + $("#password").val() + "&apiKey=" + $("#apiKey").val() 
	            	+ "&secretKey=" + $("#secretKey").val() + "&mail=" + $("#mail").val(),

	            //成功
	            success : function(data) {
	            	if(data.indexOf("登录") > -1) {
	            		window.location.href='User/logout';
	            	} else {
	            		var jsonObject= jQuery.parseJSON(data);  
	            		if(jsonObject.status == 'error') {
	            			$("#message").html(data);
	            		} else {
	            			$("#message").html(jsonObject.msg);	 
	            		}
	            	}
	            },

	            //错误情况
	            error : function(error) {
	                console.log("error : " + error);
	            },

	            //请求完成后回调函数 (请求成功或失败之后均调用)。
	            complete: function(message) {
	            	$("#save").removeAttr("disabled");
	            } 
	        });  
	    }
    </script>
</head>
<font color="red">
    <%-- 提示信息--%>
    <span id="message" name="message"></span>
</font>
<p>
<input type="button" id="findUserSubmit" id="findUserSubmit" value="刷新" onclick ="findUser()"/>
<table id="config" name="config" width="100%" cellpadding="1" cellspacing="0" border="1">
<input type="hidden" id="id" name="id">
<tr>
<td align="left"><b>password（密码）</b></td>
<td><input type="text" id="password" name="password" value="" size="50"></td>
</tr>
<tr>
<td align="left"><b>apiKey（公钥）</b></td>
<td><input type="text" id="apiKey" name="apiKey" value="" size="80"></td>
</tr>
<tr>
<td align="left"><b>secretKey（私钥）</b></td>
<td><input type="text" id="secretKey" name="secretKey" value="" size="80"></td>
</tr>
<tr>
<td align="left"><b>mail（邮箱）</b></td>
<td><input type="text" id="mail" name="mail" value="" size="50"></td>
</tr>
<tr>
<td colspan="2" align="left"><input type="button" name="save" id="save" value="保存" onclick ="save()"/></td>
</tr>
</table>
<p>
<hr>
<a href="${pageContext.request.contextPath}/Account/index">交易页面</a>
<a href="${pageContext.request.contextPath}/Config/index">配置页面</a>
<a href="${pageContext.request.contextPath}/User/logout">退出登录</a>
</body>
</html>
