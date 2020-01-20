<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<script type="text/javascript" src="../js/jquery-1.7.2.min.js"></script>
<html>
<head>
    <title>主页</title>
    <script type="text/javascript">  

    $(document).ready(function() {
        $('input[type=radio][name=symbol]').change(function() {
        	$("#config").html('');
        });
    });
    
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
    
	    function findConfig(){  
	    	$("#message").html('');
	    	$("#findConfigSubmit").attr("disabled","true");
	    	$("#config").html("");
	        $.ajax({  
	            type : "get",
	            url : "/Config/findConfig",
	            data : "symbol="+$('input[name="symbol"]:checked').val(),

	            //成功
	            success : function(data) {
	            	if(data.indexOf("登录") > -1) {
	            		window.location.href='User/logout';
	            	} else {
	            		var jsonObject= jQuery.parseJSON(data);  
	            		if(jsonObject.status == 'error') {
	            			$("#message").html(jsonObject.msg);
	            		} else {
	            			jsonObject = jQuery.parseJSON(jsonObject.msg);  
	            			var str = '';
	            			str += "<input type=\"hidden\" id=\"rate\" id=\"rate\" value=" + jsonObject.rate + " />" ;
	            			str += "<input type=\"hidden\" id=\"id\" id=\"id\" value=" + jsonObject.id + " />" ;
	            			str += "<tr><td><b>即时下单数量（个）</b></td><td><input type=\"text\" id=\"marketAmount\" name=\"marketAmount\" value=" + jsonObject.marketAmount + " /></td></tr>" ;
	            			str += "<tr><td><b>合约本金（刀）</b></td><td><input type=\"text\" id=\"limitAmount\" name=\"limitAmount\" value=" + jsonObject.limitAmount + " /></td></tr>" ;
	            			str += "<tr><td><b>最大亏损率（百分比）</b></td><td><input type=\"text\" id=\"maxLoss\" name=\"maxLoss\" value=" + jsonObject.maxLoss + " /></td></tr>" ;
	            			str += "<tr><td><b>下单偏移量（万分比）</b></td><td><input type=\"text\" id=\"tradeOffset\" name=\"tradeOffset\" value=" + jsonObject.tradeOffset + " /></td></tr>" ;
	            			str += "<tr><td><b>止损单触发偏移量（万分比）</b></td><td><input type=\"text\" id=\"lossTriggerOffset\" name=\"lossTriggerOffset\" value=" + jsonObject.lossTriggerOffset + " /></td></tr>" ;
	            			str += "<tr><td><b>止损单委托偏移量（万分比）</b></td><td><input type=\"text\" id=\"lossEntrustOffset\" name=\"lossEntrustOffset\" value=" + jsonObject.lossEntrustOffset + " /></td></tr>" ;
	            			str += "<tr><td><b>止损对标</b></td><td><input type=\"radio\" name=\"lossWorkingType\" value=\"MARK_PRICE\" " + isChecked(jsonObject.lossWorkingType, 'MARK_PRICE') + ">标记价格"
	            				+"<input type=\"radio\" name=\"lossWorkingType\" value=\"CONTRACT_PRICE\" " + isChecked(jsonObject.lossWorkingType, 'CONTRACT_PRICE') + ">合约价格</td></tr>" ;
	            			str += "<tr><td><b>止损方式</b></td><td><input type=\"radio\" name=\"lossType\" value=\"0\" " + isChecked(jsonObject.lossType, 0) + ">限价"
            					+"<input type=\"radio\" name=\"lossType\" value=\"1\" " + isChecked(jsonObject.lossType, 1) + ">市价</td></tr>" ;
    	            		str += "<tr><td><b>自动下单</b></td><td><input type=\"radio\" name=\"autoTrade\" value=\"0\" " + isChecked(jsonObject.autoTrade, 0) + ">否"
            					+"<input type=\"radio\" name=\"autoTrade\" value=\"1\" " + isChecked(jsonObject.autoTrade, 1) + ">是</td></tr>" ;
        	            	str += "<tr><td><b>自动撤单</b></td><td><input type=\"radio\" name=\"autoCancel\" value=\"0\" " + isChecked(jsonObject.autoCancel, 0) + ">否"
            					+"<input type=\"radio\" name=\"autoCancel\" value=\"1\" " + isChecked(jsonObject.autoCancel, 1) + ">是</td></tr>" ;            					
            				str += "<input type=\"button\" id=\"save\ name=\"save\" value=\"保存\" onclick =\"save()\"/>";
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


	    function save(){  
	    	$("#message").html('');
			if(!validateFloat3($("#marketAmount").val())) {
				$("#message").html("“即时下单数量”必须是小数点后三位的小数");
				$("#marketAmount").focus();
				return;
			} else if(!validateInteger($("#limitAmount").val())) {
				$("#message").html("“限价单合约金额”必须是整数");
				$("#limitAmount").focus();
				return;
			} else if(!validateFloat($("#maxLoss").val())) {
				$("#message").html("“最大亏损值”必须是小数点后两位的小数");
				$("#maxLoss").focus();
				return;
			} else if(!validateFloat($("#tradeOffset").val())) {
				$("#message").html("“下单偏移量”必须是小数点后两位的小数");
				$("#tradeOffset").focus();
				return;
			} else if(!validateFloat($("#lossTriggerOffset").val())) {
				$("#message").html("“止损单触发偏移量”必须是小数点后两位的小数");
				$("#lossTriggerOffset").focus();
				return;
			} else if(!validateFloat($("#lossEntrustOffset").val())) {
				$("#message").html("“止损单委托偏移量”必须是小数点后两位的小数");
				$("#lossEntrustOffset").focus();
				return;
			} else if(!validateInteger($("#rate").val())) {
				$("#message").html("“杠杆倍数”必须是整数");
				$("#rate").focus();
				return;
			} 
	    	$("#save").attr("disabled","true");
	        $.ajax({  
	            type : "get",
	            url : "/Config/save",
	            data : "id=" + $("#id").val() + "&marketAmount=" + $("#marketAmount").val() + "&limitAmount=" + $("#limitAmount").val() 
	            	+ "&maxLoss=" + $("#maxLoss").val() + "&tradeOffset=" + $("#tradeOffset").val() + "&lossTriggerOffset=" + $("#lossTriggerOffset").val() 
	            	+ "&lossEntrustOffset=" + $("#lossEntrustOffset").val() + "&lossWorkingType="+$('input[name="lossWorkingType"]:checked').val()
	            	+ "&lossType="+$('input[name="lossType"]:checked').val() + "&rate=" + $("#rate").val() + "&autoTrade="+$('input[name="autoTrade"]:checked').val()
	            	+ "&autoCancel="+$('input[name="autoCancel"]:checked').val(),

	            //成功
	            success : function(data) {
	            	if(data.indexOf("登录") > -1) {
	            		window.location.href='User/logout';
	            	} else {
	            		var jsonObject= jQuery.parseJSON(data);  
	            		if(jsonObject.status == 'error') {
	            			$("#message").html(data);
	            		} else {
	            			$("#message").html($('input[name="symbol"]:checked').val() + "的配置已保存");	 
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
类型：
<input type="radio" name="symbol" value="BTCUSDT" checked>BTCUSDT
<input type="radio" name="symbol" value="ETHUSDT">ETHUSDT
<input type="radio" name="symbol" value="BCHUSDT">BCHUSDT
<input type="radio" name="symbol" value="XRPUSDT">XRPUSDT
<input type="radio" name="symbol" value="EOSUSDT">EOSUSDT
<input type="radio" name="symbol" value="LTCUSDT">LTCUSDT
<input type="radio" name="symbol" value="TRXUSDT">TRXUSDT
<input type="radio" name="symbol" value="ETCUSDT">ETCUSDT
<input type="radio" name="symbol" value="LINKUSDT">LINKUSDT
<input type="button" id="findConfigSubmit" id="findConfigSubmit" value="查询配置" onclick ="findConfig()"/>
<table id="config" name="config" width="100%" cellpadding="1" cellspacing="0" border="1"></table>
<p>
<a href="${pageContext.request.contextPath}/Account/index">交易页面</a>
<a href="${pageContext.request.contextPath}/User/index">用户页面</a>
<a href="${pageContext.request.contextPath}/User/logout">退出登录</a>
</body>
</html>
