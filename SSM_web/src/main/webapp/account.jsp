<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<script type="text/javascript" src="../js/jquery-1.7.2.min.js"></script>
<html>
<head>
    <title>主页</title>
    <script type="text/javascript">  
    var role = 1;
    var number = 5;
    var coins = ["BTCUSDT","ETHUSDT","BCHUSDT","LTCUSDT","EOSUSDT","ETCUSDT","XRPUSDT","TRXUSDT","XLMUSDT","LINKUSDT","ATOMUSDT","DASHUSDT","ZECUSDT","ADAUSDT","BNBUSDT"];
    
    $(document).ready(function(){
    	if('${ids}') {
			var list = JSON.parse('${ids}');
			var str = "";
			for (x in list) {
				str += "<input type=\"radio\" name=\"relaids\" value=\"" + list[x].id + "\">" + list[x].nickname + "（" + list[x].username + "）";
			}
			str += "<input type=\"button\" id=\"changeUserSubmit\" name=\"changeUserSubmit\" value=\"切换用户\" onclick =\"changeUser()\"/>";
			$("#relaDiv").html(str);
    	} else {
    		$('#relaDiv').css('display','none');
    	}
    	var seq = 0;
    	var str = "<tr><td colspan=\"10\" align=\"center\"><span style=\"color:red;font-weight:bold;\">实时报价</span></td></tr>";
		for (x in coins) {
			if(seq % number == 0) {
				str += "<tr>";
			}
			if(seq == 0) {
				str += "<td><input type=\"radio\" name=\"symbol\" value=\"" + coins[x] + "\" checked>" + coins[x] + "</td>" 
				+ "<td><span id=\"" + coins[x] + "\" name=\"" + coins[x] + "\" style=\"color:red;font-weight:bold;\"></span></td>";
			} else {
				str += "<td><input type=\"radio\" name=\"symbol\" value=\"" + coins[x] + "\">" + coins[x] + "</td>" 
				+ "<td><span id=\"" + coins[x] + "\" name=\"" + coins[x] + "\"></span></td>";
			}
			seq += 1;
			if(seq % number == 0) {
				str += "</tr>";
			}
		}
		$("#showList").html(str); 	
		$("#userId").val('${id}');
		$('input[type=radio][name=symbol]').change(function() {
			for (x in coins) {
				$("#" + coins[x]).removeAttr("style");
			}
			$("#" + this.value).attr("style","color:red;font-weight:bold;");
			$("#title").text(this.value);
			$("#title1").text(this.value);
			$("#title2").text(this.value);
	    });
		init('${role}', '${nickname}', '${username}');
    });
    
    function init(vrole, nickname, username) {
    	if(vrole.indexOf("0") > -1) {
    		role = 0;
    	} else {
			role = 1;
		}    	
    	if(role == 0) {
    		$('#followDiv').css('display','none');
    		$('#levelDiv1').css('display','block');
    		$('#levelDiv2').css('display','block');
    	} else {
    		$('#followDiv').css('display','block');
    		$('#levelDiv1').css('display','none');
    		$('#levelDiv2').css('display','none');
    	}
		$("#userinfo").text(nickname + "（" + username + "）");
		showAll();
    }

    window.setInterval(getPrice, 5000); 
    
    function validateFloat(val){
   	 var patten = /^-?\d+\.?\d{0,5}$/;
   	 return patten.test(val);
	}   
    
    function validateFloat2(val){
      	 var patten = /^-?\d+\.?\d{0,2}$/;
      	 return patten.test(val);
   	}     

   function validateInteger(val){
   	var patten = /^\d+$/;
   	return patten.test(val);
 	 }    

    function getFormatDateByLong(longTime, pattern) {  
        return getFormatDate(new Date(longTime), pattern);  
    }

    function getFormatDate(date, pattern) {  
        if (date == undefined) {  
            date = new Date();  
        }  
        if (pattern == undefined) {  
            pattern = "yyyy-MM-dd hh:mm:ss";  
        }  

        var o = {  
                "M+": date.getMonth() + 1,  
                "d+": date.getDate(),  
                "h+": date.getHours(),  
                "m+": date.getMinutes(),  
                "s+": date.getSeconds(),  
                "q+": Math.floor((date.getMonth() + 3) / 3),  
                "S":  date.getMilliseconds()  
            };  
        if (/(y+)/.test(pattern)) {  
            pattern = pattern.replace(RegExp.$1, (date.getFullYear() + "").substr(4 - RegExp.$1.length));  
        }  

        for (var k in o) {  
            if (new RegExp("(" + k + ")").test(pattern)) {  
                pattern = pattern.replace(RegExp.$1, RegExp.$1.length == 1 ? o[k] : ("00" + o[k]).substr(("" + o[k]).length));  
            }  
        }  

        return pattern;  
    }
    
    function translateStatus(value) {
    	var result = "";
    	if(value != null) {
    		switch(value.toUpperCase()) {
    		case "NEW" :
    			result = "新建";
    			break;
    		case "PARTIALLY_FILLED" :
    			result = "部分成交";
    			break;
    		case "FILLED" :
    			result = "已成交";
    			break;
    		case "CANCELED" :
    			result = "已撤销";
    			break;
    		case "PENDING_CANCEL" :
    			result = "撤销中";
    			break;
    		case "REJECTED" :
    			result = "被拒绝";
    			break;  
    		case "EXPIRED" :
    			result = "已过期";
    			break;
    		}
    	}
    	return result;
    }

    function translateTimeInForce(value) {
    	var result = "";
    	if(value != null) {
    		switch(value.toUpperCase()) {
    		case "GTC" :
    			result = "成交为止";
    			break;
    		case "IOC" :
    			result = "无法立即成交的部分就撤销";
    			break;
    		case "FOK" :
    			result = "无法全部立即成交就撤销";
    			break;
    		}
    	}
    	return result;
    }

    function translateType(value) {
    	var result = "";
    	if(value != null) {
    		switch(value.toUpperCase()) {
    		case "LIMIT" :
    			result = "限价单";
    			break;
    		case "MARKET" :
    			result = "市价单";
    			break;
    		case "STOP" :
    			result = "限价止损单";
    			break;
    		case "STOP_MARKET" :
    			result = "市价止损单";
    			break;
    		case "TAKE_PROFIT":
    			result = "限价止盈单";
    			break;
    		case "TAKE_PROFIT_MARKET" :
    			result = "市价止盈单";
    			break;
    		}
    	}
    	return result;
    }

    function translateSide(value) {
    	var result = "";
    	if(value != null) {
    		switch(value.toUpperCase()) {
    		case "BUY" :
    			result = "开多";
    			break;
    		case "SELL" :
    			result = "开空";
    			break;
    		}
    	}
    	return result;
    }
    
    function translateSide1(value) {
    	var result = "";
    	if(value != null) {
    		switch(value.toUpperCase()) {
    		case "BUY" :
    			result = "空单";
    			break;
    		case "SELL" :
    			result = "多单";
    			break;
    		}
    	}
    	return result;
    }    

    function translateWorkingType(value) {
    	var result = "";
    	if(value != null) {
    		switch(value.toUpperCase()) {
    		case "MARK_PRICE" :
    			result = "标记价格";
    			break;
    		case "CONTRACT_PRICE" :
    			result = "合约最新价";
    			break;
    		}
    	}
    	return result;
    }

    function translateBoolean(value) {
    	var result = "";
    	if(value != null) {
    		if(value) {
    			result = "是";
    		} else {
    			result = "否";
    		}
    	}
    	return result;    	
    }
    
    function translateState(value) {
    	var result = "";
		switch(value) {
		case 0 :
			result = "未提交";
			break;
		case 1 :
			result = "已提交";
			break;
		case 2 :
			result = "预计已成交";
			break;
		case 3 :
			result = "发送失败";
			break;
		case 5 :
			result = "待处理";
			break;			
		case 6 :
			result = "失效";
			break;
		case 7 :
			result = "盈利";
			break;
		case 8 :
			result = "亏损";
			break;
		}
    	return result;
    }    
    
    function translateFrom(value) {
    	var result = "";
		switch(value) {
		case 0 :
			result = "自建";
			break;
		case 1 :
			result = "跟单";
			break;
		}
    	return result;   	
    }
    
    function translateLevel(value) {
    	var result = "";
		switch(value) {
		case 1 :
			result = "黄金";
			break;
		case 2 :
			result = "王者";
			break;
		case 5 :
			result = "独食";
			break;
		}
    	return result;   	
    }
    
    function translateCompare(value) {
    	var result = "";
		switch(value) {
		case 0 :
			result = "大于";
			break;
		case 1 :
			result = "小于";
			break;
		}
    	return result;   	
    }
    
    function translateNull(value) {
    	var result = "";
    	if(value != null) {
    		result = value;
    	}
    	return result;
    }
    
    function changeUser(){  
    	if(!$('input[name="relaids"]:checked').val()) {
    		$("#message").html("必须选择切换的用户");
    		return;
    	}
    	$("#message").html('');
    	$("#changeUserSubmit").attr("disabled","true");
        $.ajax({  
            type : "get",
            url : "/User/changeUser",
            timeout : 10000,
            data : 'id=' + $("#userId").val() + "&relaid="+ $('input[name="relaids"]:checked').val(),

            //成功
            success : function(data) {
            	if(data.indexOf("登录") > -1) {
            		window.location.href='User/logout';
            	} else {
            		var jsonObject= jQuery.parseJSON(data);  
            		if(jsonObject.status == 'ok') {
            			var user = JSON.parse(jsonObject.msg)
            			init(user.role, user.nickname, user.username);
            			if(user.id == $("#userId").val()) {
            				$('#myUser').css('display','block');
            			} else {
            				$('#myUser').css('display','none');
            			}
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
            	$("#changeUserSubmit").removeAttr("disabled");
            } 
        });  
    }    
    
    function showAll(){  
    	$("#showAllSubmit").attr("disabled","true");
    	$("#detailList").html("");
    	balance();
    	positionRisk();
    	findAllOrders();
    	if(role > 0) {
    		fllowPlans();    		
    	}
    	findCachePlans();
    	findAllPlans();
    	historyOrders();
    	setTimeout(function (){
    		$("#showAllSubmit").removeAttr("disabled");
        }, 10000);    	 
    }
    
    function clearAll(){  
    	$("#detailList").html("");
    	$("#balanceList").html("");
    	$("#positionRiskList").html("");
    	$("#ordersList").html("");
    	if(role > 0) {
    		$("#followList").html("");
    	}
    	$("#plansList").html("");
    	$("#historyList").html(""); 
    }
    
    function getPrice(){  
        $.ajax({  
            type : "get",
            url : "/Account/getPrice",
            timeout : 10000,
            data : '',

            //成功
            success : function(data) {
            	if(data.indexOf("登录") > -1) {
            		window.location.href='User/logout';
            	} else {
            		var jsonObject= jQuery.parseJSON(data);  
            		if(jsonObject.status == 'ok') {
 	            		var prices = JSON.parse(jsonObject.msg);
	            		for (x in prices) {
	            			$("#" + x).text(prices[x]);
	            		}
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
    
	    function findAllOrders(){  
	    	$("#message").html('');
	    	$("#findAllOrdersSubmit").attr("disabled","true");
	    	$("#ordersList").html("");
	        $.ajax({  
	            type : "get",
	            url : "/Account/findAllOrders",
	            timeout : 10000,
	            data : "symbol="+$('input[name="symbol"]:checked').val() + "&startTime="+$('input[name="startTime"]:checked').val(),

	            //成功
	            success : function(data) {
	            	if(data.indexOf("登录") > -1) {
	            		window.location.href='User/logout';
	            	} else {
	            		var jsonObject= jQuery.parseJSON(data);  
	            		if(jsonObject.status == 'ok') {
		            		var list = JSON.parse(jsonObject.msg);
		            		if(list.length > 0) {
		            			var str = '';
		            			str += "<tr>" + 
			            			"<td align=\"center\"><b>订单号</b></td>" + 
			            			"<td align=\"center\"><b>订单状态</b></td>" + 
			            			"<td align=\"center\"><b>挂单价格</b></td>" + 
			            			"<td align=\"center\"><b>挂单数量</b></td>" + 
			            			"<td align=\"center\"><b>成交数量</b></td>" + 
			            			"<td align=\"center\"><b>成交策略</b></td>" + 
			            			"<td align=\"center\"><b>订单类型</b></td>" + 
			            			"<td align=\"center\"><b>只能减少</b></td>" + 
			            			"<td align=\"center\"><b>订单方向</b></td>" + 
			            			"<td align=\"center\"><b>触发价格</b></td>" + 
			            			"<td align=\"center\"><b>触发类型</b></td>" + 
			            			"<td align=\"center\"><b>操作时间</b></td>" + 
			            			"<td align=\"center\"><b>更新时间</b></td>" + 
			            			"<td align=\"center\"><b>操作</b></td>" + 
			            			"</tr>";
		            		}
		            		for (x in list) {
		            			i = list.length - x - 1;
		            			if(list[i].status.toUpperCase() == "NEW" || list[i].status.toUpperCase() == "PARTIALLY_FILLED") {
			            			var edit = "<input type=\"button\" id=\"orderid" + list[i].orderId + "\" name=\"orderid" + list[i].orderId + "\" value=\"撤销订单\" onclick =\"cancel(" + list[i].orderId + ")\"/>";
			            			str += "<tr>" + 
				            			"<td>" + list[i].orderId + "</td>" + 
				            			"<td>" + translateStatus(list[i].status) + "</td>" + 
				            			"<td>" + list[i].price + "</td>" + 
				            			"<td>" + list[i].origQty + "</td>" + 
				            			"<td>" + list[i].executedQty + "</td>" + 
				            			"<td>" + translateTimeInForce(list[i].timeInForce) + "</td>" + 
				            			"<td>" + translateType(list[i].type) + "</td>" + 
				            			"<td>" + translateBoolean(list[i].reduceOnly) + "</td>" + 
				            			"<td>" + translateSide(list[i].side) + "</td>" + 
				            			"<td>" + list[i].stopPrice + "</td>" + 
				            			"<td>" + translateWorkingType(list[i].workingType) + "</td>" + 
				            			"<td>" + getFormatDateByLong(list[i].time) + "</td>" + 
				            			"<td>" + getFormatDateByLong(list[i].updateTime) + "</td>" + 
				            			"<td>" + edit + "</td>" + 
				            			"</tr>";	            				
		            			}
		            		}
	            			$("#ordersList").html(str);
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
	            	$("#findAllOrdersSubmit").removeAttr("disabled");
	            } 
	        });  
	    }    
	    
	    function balance(){  
	    	$("#message").html('');
	    	$("#balanceSubmit").attr("disabled","true");
	    	$("#balanceList").html("");
	        $.ajax({  
	            type : "get",
	            url : "/Account/balance",
	            timeout : 10000,
	            data : "",

	            //成功
	            success : function(data) {
	            	if(data.indexOf("登录") > -1) {
	            		window.location.href='User/logout';
	            	} else {
	            		var jsonObject= jQuery.parseJSON(data);  
	            		if(jsonObject.status == 'ok') {
	            			var list = JSON.parse(jsonObject.msg);
		            		if(list.length > 0) {
	            				var str = "<tr><td colspan=\"4\" align=\"center\"><span style=\"color:red;font-weight:bold;\">账户信息</span></td></tr>";
		            			str += "<tr>" + 
			            			"<td align=\"center\"><b>资金类型</b></td>" + 
			            			"<td align=\"center\"><b>资金数量</b></td>" + 
			            			"<td align=\"center\"><b>可用资金</b></td>" + 
			            			"<td align=\"center\"><b>更新时间</b></td>" + 
			            			"</tr>";
		            		}
		            		for (i in list) {
		            			if(list[i].asset == "USDT") {
			            			str += "<tr>" + 
				            			"<td>" + list[i].asset + "</td>" + 
				            			"<td>" + Number(list[i].balance.match(/^\-?\d+(?:\.\d{0,4})?/)) + "</td>" + 
				            			"<td>" + Number(list[i].withdrawAvailable.match(/^\-?\d+(?:\.\d{0,4})?/)) + "</td>" + 
				            			"<td>" + getFormatDateByLong(list[i].updateTime) + "</td>" + 
				            			"</tr>";
		            			}

		            		}	
	            			$("#balanceList").html(str);
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
            		$("#balanceSubmit").removeAttr("disabled");
	            } 
	        });  
	    } 

	    function positionRisk(){  
	    	$("#message").html('');
	    	$("#positionRiskSubmit").attr("disabled","true");
	    	$("#positionRiskList").html("");
	        $.ajax({  
	            type : "get",
	            url : "/Account/positionRisk",
	            timeout : 10000,
	            data : "",

	            //成功
	            success : function(data) {
	            	if(data.indexOf("登录") > -1) {
	            		window.location.href='User/logout';
	            	} else {
	            		var jsonObject= jQuery.parseJSON(data);  
	            		if(jsonObject.status == 'ok') {
		            		var list = JSON.parse(jsonObject.msg);
		            		if(list.length > 0) {
		            			str = "<tr><td colspan=\"9\" align=\"center\"><span style=\"color:red;font-weight:bold;\">用户持仓</span></td></tr>";
		            			str += "<tr>" + 
			            			"<td align=\"center\"><b>资金类型</b></td>" + 
			            			"<td align=\"center\"><b>持仓</b></td>" + 
			            			"<td align=\"center\"><b>买入价格</b></td>" + 
			            			"<td align=\"center\"><b>标记价格</b></td>" + 
			            			"<td align=\"center\"><b>盈利</b></td>" + 	
			            			"<td align=\"center\"><b>收益率</b></td>" + 
			            			"<td align=\"center\"><b>爆仓价格</b></td>" + 
			            			"<td align=\"center\"><b>杠杆</b></td>" + 
			            			"<td align=\"center\"><b>操作</b></td>" + 
			            			"</tr>";
		            		}
		            		for (i in list) {
		            			if(Math.abs(list[i].positionAmt) > 0) {
			            			var edit = "";
			            		   	if(list[i].positionAmt < 0) {
			            		   		edit = "<input type=\"type\" value=10 id=\"position" + list[i].symbol + "\" name=\"position" + list[i].symbol + "\")\" size=3/>%" 
			            		   			+ "<input type=\"button\" value=\"平仓\" id=\"market" + list[i].symbol + "\" name=\"market" + list[i].symbol + "\" onclick =\"tradeMarket2('BUY', '" + list[i].symbol + "')\"/>"
			            		   			+ "&nbsp&nbsp<input type=\"type\" value=50 id=\"prate" + list[i].symbol + "\" name=\"prate" + list[i].symbol + "\")\" size=5/>%" 
			            		   			+ "<input type=\"button\" value=\"止盈\" id=\"profit" + list[i].symbol + "\" name=\"profit" + list[i].symbol + "\" onclick =\"tradeMarket3('BUY', 'TAKE_PROFIT_MARKET', '" + list[i].symbol + "', '" + list[i].entryPrice + "', '" + list[i].leverage + "')\"/>"
			            		   			+ "&nbsp&nbsp<input type=\"type\" value=50 id=\"lrate" + list[i].symbol + "\" name=\"lrate" + list[i].symbol + "\")\" size=5/>%" 
			            		   			+ "<input type=\"button\" value=\"止损\" id=\"loss" + list[i].symbol + "\" name=\"loss" + list[i].symbol + "\" onclick =\"tradeMarket4('BUY', 'STOP_MARKET', '" + list[i].symbol + "', '" + list[i].entryPrice + "', '" + list[i].leverage + "')\"/>"            		   			
			            		    } else if(list[i].positionAmt > 0) {
			            		    	edit = "<input type=\"type\" value=10 id=\"position" + list[i].symbol + "\" name=\"position" + list[i].symbol + "\")\" size=3/>%" 
			            		    		+ "<input type=\"button\" value=\"平仓\" id=\"market" + list[i].symbol + "\" name=\"market" + list[i].symbol + "\" onclick =\"tradeMarket2('SELL', '" + list[i].symbol + "')\"/>"
			            		   			+ "&nbsp&nbsp<input type=\"type\" value=50 id=\"prate" + list[i].symbol + "\" name=\"prate" + list[i].symbol + "\")\" size=5/>%" 
			            		   			+ "<input type=\"button\" value=\"止盈\" id=\"profit" + list[i].symbol + "\" name=\"profit" + list[i].symbol + "\" onclick =\"tradeMarket3('SELL', 'TAKE_PROFIT_MARKET', '" + list[i].symbol + "', '" + list[i].entryPrice + "', '" + list[i].leverage + "')\"/>"
			            		   			+ "&nbsp&nbsp<input type=\"type\" value=50 id=\"lrate" + list[i].symbol + "\" name=\"lrate" + list[i].symbol + "\")\" size=5/>%" 
			            		   			+ "<input type=\"button\" value=\"止损\" id=\"loss" + list[i].symbol + "\" name=\"loss" + list[i].symbol + "\" onclick =\"tradeMarket4('SELL', 'STOP_MARKET', '" + list[i].symbol + "', '" + list[i].entryPrice + "', '" + list[i].leverage + "')\"/>"
			            		    }
			            		   	var profit = Number(list[i].unRealizedProfit.match(/^\-?\d+(?:\.\d{0,4})?/)) / Number(list[i].entryPrice.match(/^\-?\d+(?:\.\d{0,4})?/)) * 100 * Number(list[i].leverage) / Math.abs(Number(list[i].positionAmt));
			            			str += "<tr>" + 
				            			"<td>" + list[i].symbol + "</td>" + 
				            			"<td>" + Number(list[i].positionAmt.match(/^\-?\d+(?:\.\d{0,4})?/)) + "</td>" + 
				            			"<td>" + Number(list[i].entryPrice.match(/^\-?\d+(?:\.\d{0,4})?/)) + "</td>" + 
				            			"<td>" + Number(list[i].markPrice.match(/^\-?\d+(?:\.\d{0,4})?/)) + "</td>" + 
				            			"<td>" + Number(list[i].unRealizedProfit.match(/^\-?\d+(?:\.\d{0,4})?/)) + "</td>" + 
				            			"<td>" + Number((""+profit).match(/^\-?\d+(?:\.\d{0,2})?/)) + "%</td>" + 
				            			"<td><span style=\"color:red;font-weight:bold;\">" + Number(list[i].liquidationPrice.match(/^\-?\d+(?:\.\d{0,4})?/)) + "</span></td>" +
				            			"<td>" + list[i].leverage + "</td>" +
				            			"<td>" + edit + "</td>" + 
				            			"</tr>";
		            			}
		            		}
	            			$("#positionRiskList").html(str);
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
	            	$("#positionRiskSubmit").removeAttr("disabled");
	            } 
	        });  
	    } 

	    function cancel(orderId){  
	    	$("#message").html('');
	    	$("#orderid"+orderId).attr("disabled","true");
	        $.ajax({  
	            type : "get",
	            url : "/Account/cancel",
	            timeout : 10000,
	            data : "symbol="+$('input[name="symbol"]:checked').val() + "&orderId=" + orderId,

	            //成功
	            success : function(data) {
	            	if(data.indexOf("登录") > -1) {
	            		window.location.href='User/logout';
	            	} else {
	            		var jsonObject= jQuery.parseJSON(data);  
	            		if(jsonObject.status == 'error') {
	            			$("#message").html(jsonObject.msg);
	            		} else {
	            			findAllOrders();
	            			$("#message").html("订单" + orderId + "已撤销");	 
	            		}
	            	}
	            },

	            //错误情况
	            error : function(error) {
	                console.log("error : " + error);
	            },

	            //请求完成后回调函数 (请求成功或失败之后均调用)。
	            complete: function(message) {
	            	$("#orderid"+orderId).removeAttr("disabled");
	            } 
	        });  
	    }
	    
	    function cancelAll(){  
	    	$("#message").html('');
	    	$("#cancelAllSubmit").attr("disabled","true");
	        $.ajax({  
	            type : "get",
	            url : "/Account/cancelAll",
	            timeout : 10000,
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
	            			findAllOrders();
	            			$("#message").html($('input[name="symbol"]:checked').val() + "下所有挂单已撤销");	 
	            		}
	            	}
	            },

	            //错误情况
	            error : function(error) {
	                console.log("error : " + error);
	            },

	            //请求完成后回调函数 (请求成功或失败之后均调用)。
	            complete: function(message) {
	            	$("#cancelAllSubmit").removeAttr("disabled");
	            } 
	        });  
	    }
	    
	    function tradeMarket1(side, seq) { 
	    	if(!confirm($('input[name="symbol"]:checked').val() + "：是否" + translateSide(side))) {
	    		return;
	    	}
	     	$("#message").html('');
	    	$("#market"+seq).attr("disabled","true");
	    	
	        $.ajax({  
	            type : "get",
	            url : "/Account/tradeMarket",
	            timeout : 10000,
	            data : "symbol=" + $('input[name="symbol"]:checked').val() + "&side=" + side,

	            //成功
	            success : function(data) {
	            	if(data.indexOf("登录") > -1) {
	            		window.location.href='User/logout';
	            	} else {
	            		var jsonObject= jQuery.parseJSON(data);
	            		if(jsonObject.status == 'error') {
	            			$("#message").html(jsonObject.msg);
	            		} else {
	            			positionRisk();
	            			$("#message").html("市价单已提交");	
	            		}
	            	}
	            },

	            //错误情况
	            error : function(error) {
	                console.log("error : " + error);
	            },

	            //请求完成后回调函数 (请求成功或失败之后均调用)。
	            complete: function(message) {
	            	$("#market"+seq).removeAttr("disabled");
	            } 
	        }); 
	    }
	    
	    function tradeMarket2(side, symbol) { 
	    	if(!confirm(symbol + "：是否" + translateSide1(side) + "平仓")) {
	    		return;
	    	}
	     	$("#message").html('');
    		if(!validateFloat2($("#position" + symbol).val())) {
    			$("#message").html("“平仓数量”必须是小数点后两位以内的小数");
    			$("#position" + symbol).focus();
    			return;
    		}
	    	$("#market"+symbol).attr("disabled","true");

	        $.ajax({  
	            type : "get",
	            url : "/Account/tradeMarket",
	            timeout : 10000,
	            data : "symbol=" + symbol + "&side=" + side + "&quantity=" + $("#position" + symbol).val(),

	            //成功
	            success : function(data) {
	            	if(data.indexOf("登录") > -1) {
	            		window.location.href='User/logout';
	            	} else {
	            		var jsonObject= jQuery.parseJSON(data);
	            		if(jsonObject.status == 'error') {
	            			$("#message").html(jsonObject.msg);
	            		} else {
	            			positionRisk();
	            			$("#message").html("平仓单已提交");	
	            		}
	            	}
	            },

	            //错误情况
	            error : function(error) {
	                console.log("error : " + error);
	            },

	            //请求完成后回调函数 (请求成功或失败之后均调用)。
	            complete: function(message) {
	            	$("#market"+symbol).removeAttr("disabled");
	            } 
	        }); 
	    }	  
	    
	    function tradeMarket3(side, type, symbol, price, rate) { 
	     	$("#message").html('');
    		if(!validateFloat2($("#position" + symbol).val())) {
    			$("#message").html("“止盈数量”必须是小数点后两位以内的小数");
    			$("#position" + symbol).focus();
    			return;
    		} else if(!validateFloat2($("#prate" + symbol).val())) {
				$("#message").html("“止盈率”必须是小数点后两位以内的小数");
				$("#prate" + symbol).focus();
				return;
    		}
    		var stopPrice;
    		if(side == 'BUY') {
    			stopPrice = parseFloat(price) * (1 - parseFloat($("#prate" + symbol).val()) / 100 / parseFloat(rate));
    		} else {
    			stopPrice = parseFloat(price) * (1 + parseFloat($("#prate" + symbol).val()) / 100 / parseFloat(rate));
    		}
	    	if(!confirm(symbol + "：是否" + translateSide1(side) + "止盈，点位：" + stopPrice)) {
	    		return;
	    	}
	    	$("#profit"+symbol).attr("disabled","true");
	    	
	        $.ajax({  
	            type : "get",
	            url : "/Account/profitOrLoss",
	            timeout : 10000,
	            data : "symbol=" + symbol + "&side=" + side + "&quantity=" + $("#position" + symbol).val() 
	            	+ "&rate=" + $("#prate" + symbol).val() + "&type=" + type + "&stopPrice=" + stopPrice,

	            //成功
	            success : function(data) {
	            	if(data.indexOf("登录") > -1) {
	            		window.location.href='User/logout';
	            	} else {
	            		var jsonObject= jQuery.parseJSON(data);
	            		if(jsonObject.status == 'error') {
	            			$("#message").html(jsonObject.msg);
	            		} else {
	            			positionRisk();
	            			$("#message").html("止盈单已提交");	
	            		}
	            	}
	            },

	            //错误情况
	            error : function(error) {
	                console.log("error : " + error);
	            },

	            //请求完成后回调函数 (请求成功或失败之后均调用)。
	            complete: function(message) {
	            	$("#profit"+symbol).removeAttr("disabled");
	            } 
	        }); 
	    }
	    
	    function tradeMarket4(side, type, symbol, price, rate) {  
	     	$("#message").html('');
    		if(!validateFloat2($("#position" + symbol).val())) {
    			$("#message").html("“止损数量”必须是小数点后两位以内的小数");
    			$("#position" + symbol).focus();
    			return;
    		} else if(!validateFloat2($("#lrate" + symbol).val())) {
				$("#message").html("“止损率”必须是小数点后两位以内的小数");
				$("#lrate" + symbol).focus();
				return;
    		}
    		var stopPrice;
    		if(side == 'SELL') {
    			stopPrice = parseFloat(price) * (1 - parseFloat($("#lrate" + symbol).val()) / 100 / parseFloat(rate));
    		} else {
    			stopPrice = parseFloat(price) * (1 + parseFloat($("#lrate" + symbol).val()) / 100 / parseFloat(rate));
    		}
	    	if(!confirm(symbol + "：是否" + translateSide1(side) + "止损，点位：" + stopPrice)) {
	    		return;
	    	}
	    	$("#loss"+symbol).attr("disabled","true");
	    	
	        $.ajax({  
	            type : "get",
	            url : "/Account/profitOrLoss",
	            timeout : 10000,
	            data : "symbol=" + symbol + "&side=" + side + "&quantity=" + $("#position" + symbol).val() 
	            	+ "&rate=" + $("#lrate" + symbol).val() + "&type=" + type + "&stopPrice=" + stopPrice,

	            //成功
	            success : function(data) {
	            	if(data.indexOf("登录") > -1) {
	            		window.location.href='User/logout';
	            	} else {
	            		var jsonObject= jQuery.parseJSON(data);
	            		if(jsonObject.status == 'error') {
	            			$("#message").html(jsonObject.msg);
	            		} else {
	            			positionRisk();
	            			$("#message").html("止损单已提交");	
	            		}
	            	}
	            },

	            //错误情况
	            error : function(error) {
	                console.log("error : " + error);
	            },

	            //请求完成后回调函数 (请求成功或失败之后均调用)。
	            complete: function(message) {
	            	$("#loss"+symbol).removeAttr("disabled");
	            } 
	        }); 
	    }	
	    
	    function tradePlan(){ 
	    	if(!confirm($('input[name="symbol"]:checked').val() + "：是否开单")) {
	    		return;
	    	}	    	
	    	$("#message").html('');
			if(!validateFloat($("#first").val())) {
				$("#message").html("“第一档”必须是小数点后五位以内的小数");
				$("#first").focus();
				return;
			} else if(!validateFloat($("#second").val())) {
				$("#message").html("“第二档”必须是小数点后五位以内的小数");
				$("#second").focus();
				return;
			} else if(!validateFloat($("#third").val())) {
				$("#message").html("“第三档”必须是小数点后五位以内的小数");
				$("#third").focus();
				return;
			} else if(!validateFloat($("#stop").val())) {
				$("#message").html("“止损档”必须是小数点后五位以内的小数");
				$("#stop").focus();
				return;
			} 
			if((parseFloat($("#first").val()) < parseFloat($("#stop").val()) && parseFloat($("#second").val()) < parseFloat($("#stop").val()) && parseFloat($("#third").val()) < parseFloat($("#stop").val())) ||
					(parseFloat($("#first").val()) > parseFloat($("#stop").val()) && parseFloat($("#second").val()) > parseFloat($("#stop").val()) && parseFloat($("#third").val()) > parseFloat($("#stop").val()))) {
		    	$("#plan").attr("disabled","true");
		    	var pars = "first=" + $("#first").val() + "&second=" + $("#second").val() + "&third=" + $("#third").val() + "&stop=" + $("#stop").val() 
	            	+ "&compare=" + +$('input[name="compare"]:checked').val() + "&trigger=" + $("#trigger").val() 
	            	+ "&compare1=" + +$('input[name="compare1"]:checked').val() + "&trigger1=" + $("#trigger1").val() 
	            	+ "&symbol="+$('input[name="symbol"]:checked').val();
		    	if(role == 0) {
		    		pars += "&level="+$('input[name="level"]:checked').val();
		    	}
		        $.ajax({  
		            type : "get",
		            url : "/Order/plan",
		            timeout : 10000,
		            data : pars,

		            //成功
		            success : function(data) {
		            	if(data.indexOf("登录") > -1) {
		            		window.location.href='User/logout';
		            	} else {
		            		var jsonObject= jQuery.parseJSON(data);  
		            		if(jsonObject.status == 'error') {
		            			$("#message").html(jsonObject.msg);
		            		} else {
		            			findAllPlans();
		            			$("#message").html($('input[name="symbol"]:checked').val() + "的计划单已提交");	 
		            		}
		            	}
		            },

		            //错误情况
		            error : function(error) {
		                console.log("error : " + error);
		            },

		            //请求完成后回调函数 (请求成功或失败之后均调用)。
		            complete: function(message) {
		            	$("#plan").removeAttr("disabled");
		            } 
		        });
			} else {
				$("#message").html("档位设置不合规");
				return;
			}
 
	    }
	    
	    function savePlan(){ 
	    	if(!confirm($('input[name="symbol"]:checked').val() + "：是否保存")) {
	    		return;
	    	}	    	
	    	$("#message").html('');
			if(!validateFloat($("#first").val())) {
				$("#message").html("“第一档”必须是小数点后五位以内的小数");
				$("#first").focus();
				return;
			} else if(!validateFloat($("#second").val())) {
				$("#message").html("“第二档”必须是小数点后五位以内的小数");
				$("#second").focus();
				return;
			} else if(!validateFloat($("#third").val())) {
				$("#message").html("“第三档”必须是小数点后五位以内的小数");
				$("#third").focus();
				return;
			} else if(!validateFloat($("#stop").val())) {
				$("#message").html("“止损档”必须是小数点后五位以内的小数");
				$("#stop").focus();
				return;
			} 
			if((parseFloat($("#first").val()) < parseFloat($("#stop").val()) && parseFloat($("#second").val()) < parseFloat($("#stop").val()) && parseFloat($("#third").val()) < parseFloat($("#stop").val())) ||
					(parseFloat($("#first").val()) > parseFloat($("#stop").val()) && parseFloat($("#second").val()) > parseFloat($("#stop").val()) && parseFloat($("#third").val()) > parseFloat($("#stop").val()))) {
		    	$("#save").attr("disabled","true");
		    	var pars = "first=" + $("#first").val() + "&second=" + $("#second").val() + "&third=" + $("#third").val() + "&stop=" + $("#stop").val() 
	            	+ "&compare=" + +$('input[name="compare"]:checked').val() + "&trigger=" + $("#trigger").val() 
	            	+ "&compare1=" + +$('input[name="compare1"]:checked').val() + "&trigger1=" + $("#trigger1").val() 
	            	+ "&symbol="+$('input[name="symbol"]:checked').val();
		    	if(role == 0) {
		    		pars += "&level="+$('input[name="level"]:checked').val();
		    	}
		        $.ajax({  
		            type : "get",
		            url : "/Order/save",
		            timeout : 10000,
		            data : pars,

		            //成功
		            success : function(data) {
		            	if(data.indexOf("登录") > -1) {
		            		window.location.href='User/logout';
		            	} else {
		            		var jsonObject= jQuery.parseJSON(data);  
		            		if(jsonObject.status == 'error') {
		            			$("#message").html(jsonObject.msg);
		            		} else {
		            			findCachePlans();
		            			$("#message").html($('input[name="symbol"]:checked').val() + "的计划单已保存");	 
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
			} else {
				$("#message").html("档位设置不合规");
				return;
			}
 
	    }	    

	    function findAllPlans(){  
	    	$("#message").html('');
	    	$("#findAllPlansSubmit").attr("disabled","true");
	    	$("#plansList").html("");
	        $.ajax({  
	            type : "get",
	            url : "/Order/findAllPlans",
	            timeout : 10000,
	            data : '',

	            //成功
	            success : function(data) {
	            	if(data.indexOf("登录") > -1) {
	            		window.location.href='User/logout';
	            	} else {
	            		var jsonObject= jQuery.parseJSON(data);  
	            		if(jsonObject.status == 'ok') {
	            			var list = JSON.parse(jsonObject.msg);
		            		if(list.length > 0) {
		            			var str = "";
		            			str += "<tr>" + 
		            				"<td align=\"center\"><b>类型</b></td>" +
			            			"<td align=\"center\"><b>第一档</b></td>" + 
			            			"<td align=\"center\"><b>第二档</b></td>" + 
			            			"<td align=\"center\"><b>第三档</b></td>" + 
			            			"<td align=\"center\"><b>止损档</b></td>" + 
			            			"<td align=\"center\"><b>开单触发价</b></td>" + 
			            			"<td align=\"center\"><b>撤单触发价</b></td>" + 	
			            			"<td align=\"center\"><b>状态</b></td>" + 
			            			"<td align=\"center\"><b>来源</b></td>" + 
			            			"<td align=\"center\"><b>等级</b></td>" +
			            			"<td align=\"center\"><b>关联订单号</b></td>" + 
			            			"<td align=\"center\"><b>操作时间</b></td>" + 
			            			"<td align=\"center\"><b>更新时间</b></td>" + 
			            			"<td align=\"center\"><b>操作</b></td>" + 
			            			"</tr>";
		            		}
		            		for (x in list) {
		            			i = list.length - x - 1;
		            			var edit = '';
		            			if(role == 0) {
		            				edit = "<input type=\"button\" id=\"warn" + list[i].id + "\" name=\"warn" + list[i].id + "\" value=\"止盈提醒\" onclick =\"warn(" + list[i].id + ")\"/>" 
		            			}
		            			edit += "&nbsp&nbsp<select id=\"swarn" + list[i].id + "\" name=\"swarn" + list[i].id + "\"><option value =\"6\">失效</option><option value =\"7\">盈利</option><option value=\"8\">亏损</option></select>"
		            				+ "<input type=\"button\" id=\"cplan" + list[i].id + "\" name=\"cplan" + list[i].id + "\" value=\"撤单\" onclick =\"cancelPlan('" + list[i].symbol + "', " + list[i].id + ", '" + list[i].orderIds + "')\"/>"
		            				+ "&nbsp&nbsp<input type=\"button\" id=\"detail" + list[i].id + "\" name=\"detail" + list[i].id + "\" value=\"预览\" onclick =\"showDetail(" + list[i].id + ", '" + list[i].symbol + "')\"/>";
	            				if(role == 0) {
		            				edit += "&nbsp&nbsp<input type=\"button\" id=\"fusers" + list[i].id + "\" name=\"fusers" + list[i].id + "\" value=\"跟单人员\" onclick =\"findUserByUid(" + list[i].id + ")\"/>"; 
		            			}
		            			str += "<tr>" + 
		            				"<td>" + list[i].symbol + "</td>" + 
			            			"<td>" + list[i].first + "</td>" + 
			            			"<td>" + list[i].second + "</td>" + 
			            			"<td>" + list[i].third + "</td>" + 
			            			"<td>" + list[i].stop + "</td>" + 
			            			"<td>" + translateCompare(list[i].compare) + translateNull(list[i].trigger) + "</td>" + 
			            			"<td>" + translateCompare(list[i].compare1) + translateNull(list[i].trigger1) + "</td>" + 			            			
			            			"<td>" + translateState(list[i].state) + "</td>" +  
			            			"<td>" + translateFrom(list[i].type) + "</td>" +
			            			"<td>" + translateLevel(list[i].level) + "</td>" +
			            			"<td>" + list[i].orderIds + "</td>" +
			            			"<td>" + getFormatDateByLong(list[i].createTime) + "</td>" + 
			            			"<td>" + getFormatDateByLong(list[i].updateTime) + "</td>" + 
			            			"<td>" + edit + "</td>" + 
			            			"</tr>";
		            		}
	            			$("#plansList").html(str);
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
	            	$("#findAllPlansSubmit").removeAttr("disabled");
	            } 
	        });  
	    } 	 
	    
	    function findCachePlans(){  
	    	$("#message").html('');
	    	$("#findCachePlansSubmit").attr("disabled","true");
	    	$("#cacheList").html("");
	        $.ajax({  
	            type : "get",
	            url : "/Order/findCachePlans",
	            timeout : 10000,
	            data : '',

	            //成功
	            success : function(data) {
	            	if(data.indexOf("登录") > -1) {
	            		window.location.href='User/logout';
	            	} else {
	            		var jsonObject= jQuery.parseJSON(data);  
	            		if(jsonObject.status == 'ok') {
	            			var list = JSON.parse(jsonObject.msg);
		            		if(list.length > 0) {
		            			var str = "";
		            			str += "<tr>" + 
		            				"<td align=\"center\"><b>类型</b></td>" +
			            			"<td align=\"center\"><b>第一档</b></td>" + 
			            			"<td align=\"center\"><b>第二档</b></td>" + 
			            			"<td align=\"center\"><b>第三档</b></td>" + 
			            			"<td align=\"center\"><b>止损档</b></td>" + 
			            			"<td align=\"center\"><b>开单触发价</b></td>" + 
			            			"<td align=\"center\"><b>撤单触发价</b></td>" + 	
			            			"<td align=\"center\"><b>状态</b></td>" + 
			            			"<td align=\"center\"><b>来源</b></td>" + 
			            			"<td align=\"center\"><b>等级</b></td>" +
			            			"<td align=\"center\"><b>操作时间</b></td>" + 
			            			"<td align=\"center\"><b>更新时间</b></td>" + 
			            			"<td align=\"center\"><b>操作</b></td>" + 
			            			"</tr>";
		            		}
		            		for (x in list) {
		            			i = list.length - x - 1;
		            			var edit = '';
		            			edit += "&nbsp&nbsp<input type=\"button\" id=\"csave" + list[i].id + "\" name=\"csave" + list[i].id + "\" value=\"提交\" onclick =\"submitPlan(" + list[i].id + ")\"/>"
		            				+ "&nbsp&nbsp<input type=\"button\" id=\"c1plan" + list[i].id + "\" name=\"c1plan" + list[i].id + "\" value=\"撤单\" onclick =\"cancelPlan1('" + list[i].symbol + "', " + list[i].id + ", '" + list[i].orderIds + "')\"/>"
		            				+ "&nbsp&nbsp<input type=\"button\" id=\"detail" + list[i].id + "\" name=\"detail" + list[i].id + "\" value=\"预览\" onclick =\"showDetail(" + list[i].id + ", '" + list[i].symbol + "')\"/>";
		            			str += "<tr>" + 
		            				"<td>" + list[i].symbol + "</td>" + 
			            			"<td>" + list[i].first + "</td>" + 
			            			"<td>" + list[i].second + "</td>" + 
			            			"<td>" + list[i].third + "</td>" + 
			            			"<td>" + list[i].stop + "</td>" + 
			            			"<td>" + translateCompare(list[i].compare) + translateNull(list[i].trigger) + "</td>" + 
			            			"<td>" + translateCompare(list[i].compare1) + translateNull(list[i].trigger1) + "</td>" + 			            			
			            			"<td>" + translateState(list[i].state) + "</td>" +  
			            			"<td>" + translateFrom(list[i].type) + "</td>" +
			            			"<td>" + translateLevel(list[i].level) + "</td>" +
			            			"<td>" + getFormatDateByLong(list[i].createTime) + "</td>" + 
			            			"<td>" + getFormatDateByLong(list[i].updateTime) + "</td>" + 
			            			"<td>" + edit + "</td>" + 
			            			"</tr>";
		            		}
	            			$("#cacheList").html(str);
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
	            	$("#findCachePlansSubmit").removeAttr("disabled");
	            } 
	        });  
	    } 	    
	    
	    function historyOrders(){  
	    	$("#message").html('');
	    	$("#historyOrdersSubmit").attr("disabled","true");
	    	$("#historyList").html("");
	    	$("#statistics").html("");
	        $.ajax({  
	            type : "get",
	            url : "/Order/historyOrders",
	            timeout : 10000,
	            data : "&startTime="+$('input[name="startTime1"]:checked').val(),

	            //成功
	            success : function(data) {
	            	if(data.indexOf("登录") > -1) {
	            		window.location.href='User/logout';
	            	} else {
	            		var jsonObject= jQuery.parseJSON(data);  
	            		if(jsonObject.status == 'ok') {
	            			var list = JSON.parse(jsonObject.msg);
		            		if(list.length > 0) {
		            			var str = "";
		            			str += "<tr>" + 
		            				"<td align=\"center\"><b>类型</b></td>" +
			            			"<td align=\"center\"><b>第一档</b></td>" + 
			            			"<td align=\"center\"><b>第二档</b></td>" + 
			            			"<td align=\"center\"><b>第三档</b></td>" + 
			            			"<td align=\"center\"><b>止损档</b></td>" + 
			            			"<td align=\"center\"><b>开单触发价</b></td>" + 
			            			"<td align=\"center\"><b>撤单触发价</b></td>" + 	
			            			"<td align=\"center\"><b>状态</b></td>" + 
			            			"<td align=\"center\"><b>来源</b></td>" + 
			            			"<td align=\"center\"><b>等级</b></td>" +
			            			"<td align=\"center\"><b>操作时间</b></td>" + 
			            			"<td align=\"center\"><b>更新时间</b></td>" + 
			            			"<td align=\"center\"><b>操作</b></td>" + 
			            			"</tr>";
		            		}
		            		var up = 0;
		            		var down = 0;
		            		var miss = 0;
		            		for (x in list) {
		            			i = list.length - x - 1;
		            			var edit = "<input type=\"button\" id=\"rplan" + list[i].id + "\" name=\"rplan" + list[i].id + "\" value=\"再次下单\" onclick =\"repeat(" + list[i].id + ")\"/>"
		            				+ "&nbsp&nbsp<input type=\"button\" id=\"detail" + list[i].id + "\" name=\"detail" + list[i].id + "\" value=\"预览\" onclick =\"showDetail(" + list[i].id + ", '" + list[i].symbol + "')\"/>"		            			
		            				+ "&nbsp&nbsp<input type=\"button\" id=\"fusers" + list[i].id + "\" name=\"fusers" + list[i].id + "\" value=\"跟单人员\" onclick =\"findUserByUid(" + list[i].id + ")\"/>";
		            			var color = "<span>&nbsp×&nbsp";
		            			if(list[i].state == 7) {
		            				color = "<span style=\"color:green;\">&nbsp↑&nbsp";
		            				up += 1;
		            			} else if(list[i].state == 8) {
		            				color = "<span style=\"color:red;\">&nbsp↓&nbsp";
		            				down += 1;
		            			} else if(list[i].state == 6) {
		            				miss += 1;
		            			}
		            			str += "<tr>" + 
		            				"<td>" + list[i].symbol + "</td>" + 
			            			"<td>" + list[i].first + "</td>" + 
			            			"<td>" + list[i].second + "</td>" + 
			            			"<td>" + list[i].third + "</td>" + 
			            			"<td>" + list[i].stop + "</td>" + 
			            			"<td>" + translateCompare(list[i].compare) + translateNull(list[i].trigger) + "</td>" + 
			            			"<td>" + translateCompare(list[i].compare1) + translateNull(list[i].trigger1) + "</td>" + 			            			
			            			"<td>" + color + translateState(list[i].state) + "</span></td>" +  
			            			"<td>" + translateFrom(list[i].type) + "</td>" +
			            			"<td>" + translateLevel(list[i].level) + "</td>" +
			            			"<td>" + getFormatDateByLong(list[i].createTime) + "</td>" + 
			            			"<td>" + getFormatDateByLong(list[i].updateTime) + "</td>" + 
			            			"<td>" + edit + "</td>" + 
			            			"</tr>";
		            		}
	            			$("#historyList").html(str);
	            			$("#statistics").html("<span style=\"color:green;\">↑盈利：" + up + "&nbsp&nbsp</span><span style=\"color:red;\">↓亏损：" + down + "&nbsp&nbsp</span>×失效：" + miss);
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
	            	$("#historyOrdersSubmit").removeAttr("disabled");
	            } 
	        });  
	    } 		
	    
	    function findUserByUid(uid){  
	    	$("#message").html('');
	    	$("#fusers"+uid).attr("disabled","true");
	        $.ajax({  
	            type : "get",
	            url : "/User/findUserByUid",
	            timeout : 10000,
	            data : "uid="+uid,

	            //成功
	            success : function(data) {
	            	if(data.indexOf("登录") > -1) {
	            		window.location.href='User/logout';
	            	} else {
	            		var jsonObject= jQuery.parseJSON(data);  
	            		if(jsonObject.status == 'ok') {
	            			var list = JSON.parse(jsonObject.msg);
	            			var users = '';
		            		for (x in list) {
		            			users += list[x].nickname + "（" + translateState(parseInt(list[x].username)) + "),";
		            		}
	            			$("#message").html(users);
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
	            	$("#fusers"+uid).removeAttr("disabled");
	            } 
	        });  
	    } 		    
	    
	    function fllowPlans(){  
	    	$("#message").html('');
	    	$("#fllowSubmit").attr("disabled","true");
	    	$("#followList").html("");
	        $.ajax({  
	            type : "get",
	            url : "/Order/fllowPlans",
	            timeout : 10000,
	            data : "symbol="+$('input[name="symbol"]:checked').val(),

	            //成功
	            success : function(data) {
	            	if(data.indexOf("登录") > -1) {
	            		window.location.href='User/logout';
	            	} else {
	            		var jsonObject= jQuery.parseJSON(data);  
	            		if(jsonObject.status == 'ok') {
	            			var list = JSON.parse(jsonObject.msg);
		            		if(list.length > 0) {
		            			var str = "";
		            			str = "<tr>" + 
		            				"<td align=\"center\"><b>类型</b></td>" +
			            			"<td align=\"center\"><b>第一档</b></td>" + 
			            			"<td align=\"center\"><b>第二档</b></td>" + 
			            			"<td align=\"center\"><b>第三档</b></td>" + 
			            			"<td align=\"center\"><b>止损档</b></td>" + 
			            			"<td align=\"center\"><b>开单触发价</b></td>" + 
			            			"<td align=\"center\"><b>撤单触发价</b></td>" + 			            			
			            			"<td align=\"center\"><b>状态</b></td>" + 
			            			"<td align=\"center\"><b>来源</b></td>" + 
			            			"<td align=\"center\"><b>等级</b></td>" +
			            			"<td align=\"center\"><b>操作时间</b></td>" + 
			            			"<td align=\"center\"><b>更新时间</b></td>" + 
			            			"<td align=\"center\"><b>操作</b></td>" + 
			            			"</tr>";
		            		}
		            		for (x in list) {
		            			i = list.length - x - 1;
		            			var edit = "<input type=\"button\" id=\"fplan" + list[i].id + "\" name=\"fplan" + list[i].id + "\" value=\"跟单\" onclick =\"follow(" + list[i].id + ")\"/>"
		            				+ "&nbsp&nbsp<input type=\"button\" id=\"detail" + list[i].id + "\" name=\"detail" + list[i].id + "\" value=\"预览\" onclick =\"showDetail(" + list[i].id + ", '" + list[i].symbol + "')\"/>"
			            			+ "&nbsp&nbsp<input type=\"button\" id=\"fusers" + list[i].id + "\" name=\"fusers" + list[i].id + "\" value=\"跟单人员\" onclick =\"findUserByUid(" + list[i].id + ")\"/>";
		            			str += "<tr>" + 
		            				"<td>" + list[i].symbol + "</td>" + 
			            			"<td>" + list[i].first + "</td>" + 
			            			"<td>" + list[i].second + "</td>" + 
			            			"<td>" + list[i].third + "</td>" + 
			            			"<td>" + list[i].stop + "</td>" + 
			            			"<td>" + translateCompare(list[i].compare) + translateNull(list[i].trigger) + "</td>" + 
			            			"<td>" + translateCompare(list[i].compare1) + translateNull(list[i].trigger1) + "</td>" + 			            			
			            			"<td>" + translateState(list[i].state) + "</td>" +  
			            			"<td>" + translateFrom(list[i].type) + "</td>" +
			            			"<td>" + translateLevel(list[i].level) + "</td>" +
			            			"<td>" + getFormatDateByLong(list[i].createTime) + "</td>" + 
			            			"<td>" + getFormatDateByLong(list[i].updateTime) + "</td>" + 
			            			"<td>" + edit + "</td>" + 
			            			"</tr>";
		            		}
	            			$("#followList").html(str);
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
	            	$("#fllowSubmit").removeAttr("disabled");
	            } 
	        });  
	    } 		    
	    
	    function cancelPlan(symbol, id, orderIds){  
	    	if(!confirm("是否撤单")) {
	    		return;
	    	}
	    	$("#message").html('');
	    	$("#cplan"+id).attr("disabled","true");
	    	$.ajax({   
	            type : "get",
	            url : "/Order/cancelPlan",
	            timeout : 10000,
	            data : "symbol=" + symbol + "&id="+ id + "&state=" + $("#swarn" + id + " option:selected").val() + "&orderIds="+orderIds,

	            //成功
	            success : function(data) {
	            	if(data.indexOf("登录") > -1) {
	            		window.location.href='User/logout';
	            	} else {
	            		var jsonObject= jQuery.parseJSON(data);
	            		if(jsonObject.status == 'error') {
	            			$("#message").html(jsonObject.msg);
	            		} else {
	            			findAllPlans();
	            			$("#message").html("plan" + id + " " + jsonObject.msg);	
	            		}
	            	}
	            },

	            //错误情况
	            error : function(error) {
	                console.log("error : " + error);
	            },

	            //请求完成后回调函数 (请求成功或失败之后均调用)。
	            complete: function(message) {
	            	$("#cplan"+id).removeAttr("disabled");
	            } 
	        });  
	    }
	    
	    function cancelPlan1(symbol, id){  
	    	if(!confirm("是否撤单")) {
	    		return;
	    	}
	    	$("#message").html('');
	    	$("#c1plan"+id).attr("disabled","true");
	    	$.ajax({   
	            type : "get",
	            url : "/Order/cancelPlan",
	            timeout : 10000,
	            data : "symbol=" + symbol + "&id="+ id + "&state=6",

	            //成功
	            success : function(data) {
	            	if(data.indexOf("登录") > -1) {
	            		window.location.href='User/logout';
	            	} else {
	            		var jsonObject= jQuery.parseJSON(data);
	            		if(jsonObject.status == 'error') {
	            			$("#message").html(jsonObject.msg);
	            		} else {
	            			findCachePlans();
	            			$("#message").html("plan" + id + " " + jsonObject.msg);	
	            		}
	            	}
	            },

	            //错误情况
	            error : function(error) {
	                console.log("error : " + error);
	            },

	            //请求完成后回调函数 (请求成功或失败之后均调用)。
	            complete: function(message) {
	            	$("#c1plan"+id).removeAttr("disabled");
	            } 
	        });  
	    }	    
	    
	    function follow(id){  
	    	if(!confirm("是否跟单")) {
	    		return;
	    	}
	    	$("#message").html('');
	    	$("#fplan"+id).attr("disabled","true");
	        $.ajax({  
	            type : "get",
	            url : "/Order/follow",
	            timeout : 10000,
	            data : "id="+id,

	            //成功
	            success : function(data) {
	            	if(data.indexOf("登录") > -1) {
	            		window.location.href='User/logout';
	            	} else {
	            		var jsonObject= jQuery.parseJSON(data);
	            		if(jsonObject.status == 'error') {
	            			$("#message").html(jsonObject.msg);
	            		} else {
	            			findAllPlans();
	            			$("#message").html("plan:" + id + " " + jsonObject.msg);	
	            		}
	            	}
	            },

	            //错误情况
	            error : function(error) {
	                console.log("error : " + error);
	            },

	            //请求完成后回调函数 (请求成功或失败之后均调用)。
	            complete: function(message) {
	            	$("#fplan"+id).removeAttr("disabled");
	            } 
	        });  
	    }	
	    
	    function submitPlan(id){  
	    	$("#message").html('');
	    	$("#csave"+id).attr("disabled","true");
	        $.ajax({  
	            type : "get",
	            url : "/Order/submit",
	            timeout : 10000,
	            data : "id="+id,

	            //成功
	            success : function(data) {
	            	if(data.indexOf("登录") > -1) {
	            		window.location.href='User/logout';
	            	} else {
	            		var jsonObject= jQuery.parseJSON(data);
	            		if(jsonObject.status == 'error') {
	            			$("#message").html(jsonObject.msg);
	            		} else {
	            			findAllPlans();
	            			$("#message").html($('input[name="symbol"]:checked').val() + "的计划单已提交");	 
	            		}
	            	}
	            },

	            //错误情况
	            error : function(error) {
	                console.log("error : " + error);
	            },

	            //请求完成后回调函数 (请求成功或失败之后均调用)。
	            complete: function(message) {
	            	$("#csave"+id).removeAttr("disabled");
	            } 
	        });  
	    }		    
	    
	    function repeat(id){  
	    	if(!confirm("是否再次下单")) {
	    		return;
	    	}
	    	$("#message").html('');
	    	$("#rplan"+id).attr("disabled","true");
	        $.ajax({  
	            type : "get",
	            url : "/Order/repeat",
	            timeout : 10000,
	            data : "id="+id,

	            //成功
	            success : function(data) {
	            	if(data.indexOf("登录") > -1) {
	            		window.location.href='User/logout';
	            	} else {
	            		var jsonObject= jQuery.parseJSON(data);
	            		if(jsonObject.status == 'error') {
	            			$("#message").html(jsonObject.msg);
	            		} else {
	            			findAllPlans();
	            			$("#message").html("plan:" + id + " " + jsonObject.msg);	
	            		}
	            	}
	            },

	            //错误情况
	            error : function(error) {
	                console.log("error : " + error);
	            },

	            //请求完成后回调函数 (请求成功或失败之后均调用)。
	            complete: function(message) {
	            	$("#rplan"+id).removeAttr("disabled");
	            } 
	        });  
	    }		    
	    
	    function warn(id){  
	    	if(!confirm("是否提醒所有人")) {
	    		return;
	    	}
	    	$("#message").html('');
	    	$("#warn"+id).attr("disabled","true");
	        $.ajax({  
	            type : "get",
	            url : "/Order/warn",
	            timeout : 10000,
	            data : "id="+id,

	            //成功
	            success : function(data) {
	            	if(data.indexOf("登录") > -1) {
	            		window.location.href='User/logout';
	            	} else {
	            		var jsonObject= jQuery.parseJSON(data);
	            		if(jsonObject.status == 'error') {
	            			$("#message").html(jsonObject.msg);
	            		} else {
	            			findAllPlans();
	            			$("#message").html("plan" + id + " " + jsonObject.msg);	
	            		}
	            	}
	            },

	            //错误情况
	            error : function(error) {
	                console.log("error : " + error);
	            },

	            //请求完成后回调函数 (请求成功或失败之后均调用)。
	            complete: function(message) {
	            	$("#warn"+id).removeAttr("disabled");
	            } 
	        });  
	    }		    
   
	    function showDetail(id, symbol){  
	    	$("#message").html('');
	    	$("#detail"+id).attr("disabled","true");
	    	$("#detailList").html("");
	        $.ajax({  
	            type : "get",
	            url : "/Order/predict",
	            timeout : 10000,
	            data : "id="+id,

	            //成功
	            success : function(data) {
	            	if(data.indexOf("登录") > -1) {
	            		window.location.href='User/logout';
	            	} else {
	            		var jsonObject= jQuery.parseJSON(data);
	            		if(jsonObject.status == 'ok') {
	            			var detail = JSON.parse(jsonObject.msg);
	            			var str = "<tr><td colspan=\"14\" align=\"center\"><span style=\"color:red;font-weight:bold;\">预览详情 </span></td></tr>";
	            			str += "<tr>" + 
	            				"<td align=\"center\"><b>类型</b></td>" +	            			
		            			"<td align=\"center\"><b>一档点位</b></td>" + 
		            			"<td align=\"center\"><b>一档数量</b></td>" + 
		            			"<td align=\"center\"><b>二档点位</b></td>" + 
		            			"<td align=\"center\"><b>二档数量</b></td>" + 
		            			"<td align=\"center\"><b>三档点位</b></td>" + 
		            			"<td align=\"center\"><b>三档数量</b></td>" + 
		            			"<td align=\"center\"><b>止损档</b></td>" + 
		            			"<td align=\"center\"><b>开单均价</b></td>" + 
		            			"<td align=\"center\"><b>开单总数</b></td>" + 			            			
		            			"<td align=\"center\"><b>杠杆</b></td>" + 
		            			"<td align=\"center\"><b>保证金</b></td>" + 
		            			"<td align=\"center\"><b>止损比例</b></td>" + 
		            			"<td align=\"center\"><b>预计亏损</b></td>" + 
		            			"</tr>";	 
		            		str += "<tr>" + 
		            			"<td>" + symbol + "</td>" + 
		            			"<td>" + detail.fisrt + "</td>" + 
		            			"<td>" + detail.quantity + "</td>" + 
		            			"<td>" + detail.second + "</td>" + 
		            			"<td>" + detail.quantity + "</td>" + 
		            			"<td>" + detail.third + "</td>" + 
		            			"<td>" + detail.quantity + "</td>" + 
		            			"<td>" + detail.stop + "</td>" + 
		            			"<td>" + detail.avg + "</td>" + 
		            			"<td>" + detail.quantity * 3 + "</td>" + 			            			
		            			"<td>" + detail.lever + "</td>" + 
		            			"<td>" + detail.margin + "</td>" + 
		            			"<td><span style=\"color:red;font-weight:bold;\">" + detail.lossrate + "</span></td>" + 
		            			"<td><span style=\"color:red;font-weight:bold;\">" + detail.losspredict + "</span></td>" + 
		            			"</tr>";	
	            			$("#detailList").html(str);
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
	            	$("#detail"+id).removeAttr("disabled");
	            } 
	        });  
	    }	
    </script>
</head>
<div id="relaDiv">
</div>
<p>
<span style="color:red;font-weight:bold;">
    <%-- 提示信息--%>
    <span id="message" name="message"></span>
</span>
<p>
<input type="hidden" id="userId" name="userId" value=""/>
<div style="width:100%">
    <span id='userinfo' name='userinfo' style="font-weight:bold;font-size:18px;text-align:center;display:block;position: relative;color:red;"></span>
</div>
<span style="float:right;"><input type="button" id="showAllSubmit" id="showAllSubmit" value="显示所有" onclick ="showAll()"/></span>
<input type="button" id="balanceSubmit" id="balanceSubmit" value="刷新账户" onclick ="balance()"/>
&nbsp&nbsp<input type="button" id="positionRiskSubmit" id="positionRiskSubmit" value="刷新持仓" onclick ="positionRisk()"/><p>
<table id="balanceList" name="balanceList" width="100%" cellpadding="1" cellspacing="0" border="1"></table>
<p>
<table id="positionRiskList" name="positionRiskList" width="100%" cellpadding="1" cellspacing="0" border="1"></table>
<p>
<table id="showList" name="showList" width="100%" cellpadding="1" cellspacing="0" border="1"></table>
<p>
<span style="color:red;font-weight:bold;">交易所挂单</span>
<input type="radio" name="startTime" value="3" checked>三天内
<input type="radio" name="startTime" value="7">七天内
&nbsp&nbsp<input type="button" id="findAllOrdersSubmit" id="findAllOrdersSubmit" value="查询订单" onclick ="findAllOrders()"/>
&nbsp&nbsp<input type="button" id="cancelAllSubmit" id="cancelAllSubmit" value="撤销此类型全部订单" onclick ="cancelAll()"/>
&nbsp&nbsp<span id='title' name='title' style="color:red;font-weight:bold;">BTCUSDT</span>
<p>
<table id="ordersList" name="ordersList" width="100%" cellpadding="1" cellspacing="0" border="1"></table>
<p>
<!-- <hr>
<span style="color:red;font-weight:bold;">即时单</span>
&nbsp&nbsp<input type="button" value="开多" id="market1" name="market1" onclick ="tradeMarket1('BUY', 1)"/>
&nbsp&nbsp<input type="button" value="开空" id="market2" name="market2" onclick ="tradeMarket1('SELL', 2)"/>
&nbsp&nbsp<span id='title1' name='title1' style="color:red;font-weight:bold;">BTCUSDT</span>
<p> 
<hr>-->
<span style="color:red;font-weight:bold;">计划单</span>
&nbsp&nbsp<input type="button" value="开单" id="plan" name="plan" onclick ="tradePlan()"/>
&nbsp&nbsp<input type="button" value="保存" id="save" name="save" onclick ="savePlan()"/>
&nbsp&nbsp<span id='title2' name='title2' style="color:red;font-weight:bold;">BTCUSDT</span>
<table id="positionRiskList" name="positionRiskList" width="100%" cellpadding="1" cellspacing="0" border="1">
<tr>
<td>
第一档
</td>
<td>
<input type="text" id="first" name="first"/>
</td>
</tr>
<tr>
<td>
第二档
</td>
<td>
<input type="text" id="second" name="second"/>
</td>
</tr>
<tr>
<td>
第三档
</td>
<td>
<input type="text" id="third" name="third"/>
</td>
</tr>
<tr>
<td>
止损档
</td>
<td>
<input type="text" id="stop" name="stop"/>
</td>
</tr>
<tr>
<td>
开单触发价
</td>
<td>
<input type="radio" name="compare" value="0" checked>大于
<input type="radio" name="compare" value="1">小于
<input type="text" id="trigger" name="trigger"/>
</td>
</tr>
<tr>
<td>
撤单触发价
</td>
<td>
<input type="radio" name="compare1" value="0">大于
<input type="radio" name="compare1" value="1" checked>小于
<input type="text" id="trigger1" name="trigger1"/>
</td>
</tr>
<tr>
<td>
<div id="levelDiv1">
级别
</div>
</td>
<td>
<div id="levelDiv2">
<input type="radio" name="level" value="1" checked>黄金
<input type="radio" name="level" value="2">王者
<input type="radio" name="level" value="5">独食
</div>
</td>
</tr>
</table>
<p>
<hr>
<table id="detailList" name="detailList" width="100%" cellpadding="1" cellspacing="0" border="1"></table>
<p>
<input type="button" id="findCachePlansSubmit" id="findCachePlansSubmit" value="计划单仓库" onclick ="findCachePlans()"/>
<table id="cacheList" name="cacheList" width="100%" cellpadding="1" cellspacing="0" border="1"></table>
<p>
<div id="followDiv">
<input type="button" id="fllowSubmit" id="fllowSubmit" value="跟随计划单" onclick ="fllowPlans()"/>
<table id="followList" name="followList" width="100%" cellpadding="1" cellspacing="0" border="1"></table>
<p>
</div>
<input type="button" id="findAllPlansSubmit" id="findAllPlansSubmit" value="我的计划单" onclick ="findAllPlans()"/>
<table id="plansList" name="plansList" width="100%" cellpadding="1" cellspacing="0" border="1"></table>
<p>
<input type="button" id="historyOrdersSubmit" id="historyOrdersSubmit" value="历史计划单" onclick ="historyOrders()"/>&nbsp&nbsp
<input type="radio" name="startTime1" value="1" checked>一天内
<input type="radio" name="startTime1" value="7">一周内
<input type="radio" name="startTime1" value="30">一月内
<span id="statistics" name="statistics" style="float:right;"></span>
<table id="historyList" name="historyList" width="100%" cellpadding="1" cellspacing="0" border="1"></table>
<p>
<hr>
<div id="myUser">
<a href="${pageContext.request.contextPath}/User/index">用户页面</a>
<a href="${pageContext.request.contextPath}/Config/index">配置页面</a>
</div>
<a href="${pageContext.request.contextPath}/User/logout">退出登录</a>
</body>
</html>
