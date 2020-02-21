<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<script type="text/javascript" src="../js/jquery-1.7.2.min.js"></script>
<html>
<head>
    <title>主页</title>
    <script type="text/javascript">  

    window.onload = function(){
    	findConfigs();
    }
    
    function isChecked(left, right) {
		if(left == right) {
    		return "checked";			
		} else {
			return "";
		}
    }
    
    function validateFloat(val){
    	 var patten = /^-?\d+\.?\d{0,2}$/;
    	 return patten.test(val);
	}    
    
    function validateFloat3(val){
   	 var patten = /^-?\d+\.?\d{0,3}$/;
   	 return patten.test(val);
	} 

    function validateInteger(val){
    	var patten = /^\d+$/;
    	return patten.test(val);
  	 }
    
	    function findConfigs(){  
	    	$("#message").html('');
	    	$("#findConfigSubmit").attr("disabled","true");
	    	$("#config").html("");
	        $.ajax({  
	            type : "get",
	            url : "/Config/findConfigs",
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
	            			var list = JSON.parse(jsonObject.msg);
		            		if(list.length > 0) {
	            				var str = "";
		            			str += "<tr>" + 
		            				"<td rowspan=\"2\" align=\"center\"><b>类型</b></td>" +
			            			"<td align=\"center\"><b>合约本金</b></td>" + 
			            			"<td align=\"center\"><b>最大亏损率</b></td>" + 
			            			"<td align=\"center\"><b>下单偏移量</b></td>" + 
			            			"<td align=\"center\"><b>止损单触发偏移量</b></td>" + 
			            			"<td align=\"center\"><b>止损单委托偏移量</b></td>" + 
			            			"<td rowspan=\"2\" align=\"center\"><b>止损对标</b></td>" + 
			            			"<td rowspan=\"2\" align=\"center\"><b>止损方式</b></td>" + 
			            			"<td rowspan=\"2\" align=\"center\"><b>自动下单</b></td>" + 
			            			"<td rowspan=\"2\" align=\"center\"><b>自动撤单</b></td>" + 
			            			"<td rowspan=\"2\" align=\"center\"><b>操作</b></td>" + 
			            			"</tr>";
		            			str += "<tr>" + 
			            			"<td align=\"center\"><b>（刀）</b></td>" + 
			            			"<td align=\"center\"><b>（百分比）</b></td>" + 
			            			"<td align=\"center\"><b>（万分比）</b></td>" + 
			            			"<td align=\"center\"><b>（万分比）</b></td>" + 
			            			"<td align=\"center\"><b>（万分比）</b></td>" + 
			            			"</tr>";			            			
		            		}
		            		for (i in list) {
		            			str += "<tr>" + 
			            			"<td>" + list[i].type + "<input type=\"hidden\" id=\"marketAmount" + list[i].type + "\" name=\"marketAmount" + list[i].type + "\" value=" + list[i].marketAmount + " />" 
			            			+ "<input type=\"hidden\" id=\"rate" + list[i].type + "\" name=\"rate" + list[i].type + "\" value=" + list[i].rate + " /></td>" + 
			            			"<td><input type=\"text\" id=\"limitAmount" + list[i].type + "\" name=\"limitAmount" + list[i].type + "\" value=" + list[i].limitAmount + " size=5 /></td>" +
			            			"<td><input type=\"text\" id=\"maxLoss" + list[i].type + "\" name=\"maxLoss" + list[i].type + "\" value=" + list[i].maxLoss + " size=5 /></td>" +
			            			"<td><input type=\"text\" id=\"tradeOffset" + list[i].type + "\" name=\"tradeOffset" + list[i].type + "\" value=" + list[i].tradeOffset + " size=5 /></td>" +
			            			"<td><input type=\"text\" id=\"lossTriggerOffset" + list[i].type + "\" name=\"lossTriggerOffset" + list[i].type + "\" value=" + list[i].lossTriggerOffset + " size=5 /></td>" +
			            			"<td><input type=\"text\" id=\"lossEntrustOffset" + list[i].type + "\" name=\"lossEntrustOffset" + list[i].type + "\" value=" + list[i].lossEntrustOffset + " size=5 /></td>" +
			            			"<td><input type=\"radio\" name=\"lossWorkingType" + list[i].type + "\" value=\"MARK_PRICE\" " + isChecked(list[i].lossWorkingType, 'MARK_PRICE') + ">标记价格"
		            					+"<input type=\"radio\" name=\"lossWorkingType" + list[i].type + "\" value=\"CONTRACT_PRICE\" " + isChecked(list[i].lossWorkingType, 'CONTRACT_PRICE') + ">合约价格</td>" +
				            		"<td><input type=\"radio\" name=\"lossType" + list[i].type + "\" value=\"0\" " + isChecked(list[i].lossType, 0) + ">限价"
	            						+"<input type=\"radio\" name=\"lossType" + list[i].type + "\" value=\"1\" " + isChecked(list[i].lossType, 1) + ">市价</td>" +	
					            	"<td><input type=\"radio\" name=\"autoTrade" + list[i].type + "\" value=\"0\" " + isChecked(list[i].autoTrade, 0) + ">否"
	            						+"<input type=\"radio\" name=\"autoTrade" + list[i].type + "\" value=\"1\" " + isChecked(list[i].autoTrade, 1) + ">是</td>" +	
						            "<td><input type=\"radio\" name=\"autoCancel" + list[i].type + "\" value=\"0\" " + isChecked(list[i].autoCancel, 0) + ">否"
	            						+"<input type=\"radio\" name=\"autoCancel" + list[i].type + "\" value=\"1\" " + isChecked(list[i].autoCancel, 1) + ">是</td>" +
	            					"<td><input type=\"button\" value=\"保存\" id=\"save" + list[i].type + "\" name=\"save" + list[i].type + "\" onclick =\"save(" + list[i].id + ", '" + list[i].type + "')\"/></td>" +
			            			"</tr>";
		            		}
	            			$("#config").html(str);
	            		}

	            	}
	            },

	            //错误情况
	            error : function(error) {
	                console.log("error : " + error);
	            },

	            //请求完成后回调函数 (请求成功或失败之后均调用)。
	            complete: function(message) {
	            	$("#findConfigSubmit").removeAttr("disabled");
	            } 
	        });  
	    }    


	    function save(id, type){  
	    	$("#message").html('');
			if(!validateInteger($("#limitAmount"+type).val())) {
				$("#message").html(type + "“限价单合约金额”必须是整数");
				$("#limitAmount"+type).focus();
				return;
			} else if(!validateFloat($("#maxLoss"+type).val())) {
				$("#message").html(type + "“最大亏损值”必须是小数点后两位的小数");
				$("#maxLoss"+type).focus();
				return;
			} else if(!validateFloat($("#tradeOffset"+type).val())) {
				$("#message").html(type + "“下单偏移量”必须是小数点后两位的小数");
				$("#tradeOffset"+type).focus();
				return;
			} else if(!validateFloat($("#lossTriggerOffset"+type).val())) {
				$("#message").html(type + "“止损单触发偏移量”必须是小数点后两位的小数");
				$("#lossTriggerOffset"+type).focus();
				return;
			} else if(!validateFloat($("#lossEntrustOffset"+type).val())) {
				$("#message").html(type + "“止损单委托偏移量”必须是小数点后两位的小数");
				$("#lossEntrustOffset"+type).focus();
				return;
			} 
	    	if(!confirm(type + "是否保存")) {
	    		return;
	    	}
	    	$("#save"+type).attr("disabled","true");
	        $.ajax({  
	            type : "get",
	            url : "/Config/save",
	            timeout : 10000,
	            data : "id=" + id + "&marketAmount=" + $("#marketAmount"+type).val() + "&limitAmount=" + $("#limitAmount"+type).val() 
	            	+ "&maxLoss=" + $("#maxLoss"+type).val() + "&tradeOffset=" + $("#tradeOffset"+type).val() + "&lossTriggerOffset=" + $("#lossTriggerOffset"+type).val() 
	            	+ "&lossEntrustOffset=" + $("#lossEntrustOffset"+type).val() + "&lossWorkingType="+$('input[name="lossWorkingType' + type + '"]:checked').val()
	            	+ "&lossType="+$('input[name="lossType' + type + '"]:checked').val() + "&rate=" + $("#rate"+type).val() + "&autoTrade="+$('input[name="autoTrade' + type + '"]:checked').val()
	            	+ "&autoCancel="+$('input[name="autoCancel' + type + '"]:checked').val(),

	            //成功
	            success : function(data) {
	            	if(data.indexOf("登录") > -1) {
	            		window.location.href='User/logout';
	            	} else {
	            		var jsonObject= jQuery.parseJSON(data);  
	            		if(jsonObject.status == 'error') {
	            			$("#message").html(data);
	            		} else {
	            			$("#message").html(type + "的配置已保存");	 
	            		}
	            	}
	            },

	            //错误情况
	            error : function(error) {
	                console.log("error : " + error);
	            },

	            //请求完成后回调函数 (请求成功或失败之后均调用)。
	            complete: function(message) {
	            	$("#save"+type).removeAttr("disabled");
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
<input type="button" id="findConfigSubmit" id="findConfigSubmit" value="刷新" onclick ="findConfigs()"/>
<table id="config" name="config" width="100%" cellpadding="1" cellspacing="0" border="1"></table>
<p>
<hr>
<a href="${pageContext.request.contextPath}/Account/index">交易页面</a>
<a href="${pageContext.request.contextPath}/User/index">用户页面</a>
<a href="${pageContext.request.contextPath}/User/logout">退出登录</a>
</body>
</html>
