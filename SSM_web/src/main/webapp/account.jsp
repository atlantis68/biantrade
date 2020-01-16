<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<script type="text/javascript" src="../js/jquery-1.7.2.min.js"></script>
<html>
<head>
    <title>主页</title>
    <script type="text/javascript">  

    window.setInterval(getPrice, 5000); 
    
    function validateFloat(val){
   	 var patten = /^-?\d+\.?\d{0,5}$/;
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
    			result = "买入";
    			break;
    		case "SELL" :
    			result = "卖出";
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
		case 4 :
			result = "已过期";
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
    
    function getPrice(){  
        $.ajax({  
            type : "get",
            url : "/Account/getPrice",
            data : '',

            //成功
            success : function(data) {
            	if(data.indexOf("登录") > -1) {
            		window.location.href='User/logout';
            	} else {
            		var jsonObject= jQuery.parseJSON(data);  
            		if(jsonObject.status == 'ok') {
	            		var prices = JSON.parse(jsonObject.msg);
	            		var str = "<tr>";
	            		for (x in prices) {
	            			str += "<td>" + x + "：" + prices[x] + "</td>";
	            		}
	            		str += "</tr>";
            			$("#showList").html(str);
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
		            			var str = "<tr>" + 
			            			"<td><b>订单号</b></td>" + 
			            			"<td><b>订单状态</b></td>" + 
			            			"<td><b>挂单价格</b></td>" + 
			            			"<td><b>挂单数量</b></td>" + 
			            			"<td><b>成交数量</b></td>" + 
			            			"<td><b>成交策略</b></td>" + 
			            			"<td><b>订单类型</b></td>" + 
			            			"<td><b>只能减少</b></td>" + 
			            			"<td><b>订单方向</b></td>" + 
			            			"<td><b>触发价格</b></td>" + 
			            			"<td><b>触发类型</b></td>" + 
			            			"<td><b>操作时间</b></td>" + 
			            			"<td><b>更新时间</b></td>" + 
			            			"<td><b>操作</b></td>" + 
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
		            			var str = "<tr>" + 
			            			"<td><b>资金类型</b></td>" + 
			            			"<td><b>资金数量</b></td>" + 
			            			"<td><b>可用资金</b></td>" + 
			            			"<td><b>更新时间</b></td>" + 
			            			"</tr>";
		            		}
		            		for (i in list) {
		            			str += "<tr>" + 
			            			"<td>" + list[i].asset + "</td>" + 
			            			"<td>" + list[i].balance + "</td>" + 
			            			"<td>" + list[i].withdrawAvailable + "</td>" + 
			            			"<td>" + getFormatDateByLong(list[i].updateTime) + "</td>" + 
			            			"</tr>";
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
		            			var str = "<tr>" + 
			            			"<td><b>资金类型</b></td>" + 
			            			"<td><b>持仓</b></td>" + 
			            			"<td><b>买入价格</b></td>" + 
			            			"<td><b>标记价格</b></td>" + 
			            			"<td><b>盈利</b></td>" + 		            			
			            			"<td><b>爆仓价格</b></td>" + 
			            			"<td><b>杠杆</b></td>" + 
			            			"<td><b>操作</b></td>" + 
			            			"</tr>";
		            		}
		            		for (i in list) {
		            			var edit = "";
		            		   	if(list[i].positionAmt < 0) {
		            		   		edit = "<input type=\"type\" value=" + Math.abs(list[i].positionAmt) + " id=\"position" + list[i].symbol + "\" name=\"position" + list[i].symbol + "\")\"/>" 
		            		   			+ "<input type=\"button\" value=\"平仓\" id=\"market" + list[i].symbol + "\" name=\"market" + list[i].symbol + "\" onclick =\"tradeMarket2('BUY', '" + list[i].symbol + "')\"/>" 
		            		    } else if(list[i].positionAmt > 0) {
		            		    	edit = "<input type=\"type\" value=" + Math.abs(list[i].positionAmt) + " id=\"position" + list[i].symbol + "\" name=\"position" + list[i].symbol + "\")\"/>" 
		            		    		+ "<input type=\"button\" value=\"平仓\" id=\"market" + list[i].symbol + "\" name=\"market" + list[i].symbol + "\" onclick =\"tradeMarket2('SELL', '" + list[i].symbol + "')\"/>" ;
		            		    }
		            			str += "<tr>" + 
			            			"<td>" + list[i].symbol + "</td>" + 
			            			"<td>" + list[i].positionAmt + "</td>" + 
			            			"<td>" + list[i].entryPrice + "</td>" + 
			            			"<td>" + list[i].markPrice + "</td>" + 
			            			"<td>" + list[i].unRealizedProfit + "</td>" + 
			            			"<td>" + list[i].liquidationPrice + "</td>" +
			            			"<td>" + list[i].leverage + "</td>" +
			            			"<td>" + edit + "</td>" + 
			            			"</tr>";
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
	    	if(!confirm("是否" + translateSide(side))) {
	    		return;
	    	}
	     	$("#message").html('');
	    	$("#market"+seq).attr("disabled","true");
	    	
	        $.ajax({  
	            type : "get",
	            url : "/Account/tradeMarket",
	            data : "symbol=" + $('input[name="symbol1"]:checked').val() + "&side=" + side,

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
	    	if(!confirm("是否" + translateSide(side))) {
	    		return;
	    	}
	     	$("#message").html('');
	    	$("#market"+symbol).attr("disabled","true");
    		var quantity = $("#position" + symbol).val();
    		if(quantity == '') {
    			$("#message").html("平仓数量必须填写");
    			$("#position" + symbol).focus();
    			$("#market"+symbol).removeAttr("disabled");
    			return;
    		}
    		quantity = "&quantity=" + quantity;
	    	
	        $.ajax({  
	            type : "get",
	            url : "/Account/tradeMarket",
	            data : "symbol=" + symbol + "&side=" + side + quantity,

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
	            	$("#market"+symbol).removeAttr("disabled");
	            } 
	        }); 
	    }	    
	    
	    function tradePlan(){  
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
			if((parseFloat($("#first").val()) <= parseFloat($("#second").val()) && parseFloat($("#second").val()) <= parseFloat($("#third").val()) && parseFloat($("#third").val()) < parseFloat($("#stop").val())) ||
					(parseFloat($("#first").val()) >= parseFloat($("#second").val()) && parseFloat($("#second").val()) >= parseFloat($("#third").val()) && parseFloat($("#third").val()) > parseFloat($("#stop").val()))) {
		    	$("#plan").attr("disabled","true");
		        $.ajax({  
		            type : "get",
		            url : "/Order/plan",
		            data : "first=" + $("#first").val() + "&second=" + $("#second").val() + "&third=" + $("#third").val() + "&stop=" + $("#stop").val() 
		            	+ "&compare=" + +$('input[name="compare"]:checked').val() + "&trigger=" + $("#trigger").val() 
		            	+ "&compare1=" + +$('input[name="compare1"]:checked').val() + "&trigger1=" + $("#trigger1").val() 
		            	+ "&symbol="+$('input[name="symbol2"]:checked').val(),

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
		            			$("#message").html($('input[name="symbol2"]:checked').val() + "的计划单已提交");	 
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

	    function findAllPlans(){  
	    	$("#message").html('');
	    	$("#findAllPlansSubmit").attr("disabled","true");
	    	$("#plansList").html("");
	        $.ajax({  
	            type : "get",
	            url : "/Order/findAllPlans",
	            data : "symbol="+$('input[name="symbol2"]:checked').val(),

	            //成功
	            success : function(data) {
	            	if(data.indexOf("登录") > -1) {
	            		window.location.href='User/logout';
	            	} else {
	            		var jsonObject= jQuery.parseJSON(data);  
	            		if(jsonObject.status == 'ok') {
	            			var list = JSON.parse(jsonObject.msg);
		            		if(list.length > 0) {
		            			var str = "<tr>" + 
		            				"<td><b>类型</b></td>" +
			            			"<td><b>第一档</b></td>" + 
			            			"<td><b>第二档</b></td>" + 
			            			"<td><b>第三档</b></td>" + 
			            			"<td><b>止损档</b></td>" + 
			            			"<td><b>开单触发价</b></td>" + 
			            			"<td><b>撤单触发价</b></td>" + 	
			            			"<td><b>状态</b></td>" + 
			            			"<td><b>来源</b></td>" + 
			            			"<td><b>关联订单号</b></td>" + 
			            			"<td><b>操作时间</b></td>" + 
			            			"<td><b>更新时间</b></td>" + 
			            			"<td><b>操作</b></td>" + 
			            			"</tr>";
		            		}
		            		for (x in list) {
		            			i = list.length - x - 1;
		            			var edit = "<input type=\"button\" id=\"warn" + list[i].id + "\" name=\"warn" + list[i].id + "\" value=\"止盈提醒\" onclick =\"warn(" + list[i].id + ")\"/>" 
		            				+ "<select id=\"swarn" + list[i].id + "\" name=\"swarn" + list[i].id + "\"><option value =\"4\">失效</option><option value =\"5\">盈利</option><option value=\"6\">亏损</option></select>"
		            				+ "<input type=\"button\" id=\"cplan" + list[i].id + "\" name=\"cplan" + list[i].id + "\" value=\"撤单\" onclick =\"cancelPlan('" + list[i].symbol + "', " + list[i].id + ", '" + list[i].orderIds + "')\"/>";
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
	    
	    function fllowPlans(){  
	    	$("#message").html('');
	    	$("#fllowSubmit").attr("disabled","true");
	    	$("#followList").html("");
	        $.ajax({  
	            type : "get",
	            url : "/Order/fllowPlans",
	            data : "symbol="+$('input[name="symbol2"]:checked').val(),

	            //成功
	            success : function(data) {
	            	if(data.indexOf("登录") > -1) {
	            		window.location.href='User/logout';
	            	} else {
	            		var jsonObject= jQuery.parseJSON(data);  
	            		if(jsonObject.status == 'ok') {
	            			var list = JSON.parse(jsonObject.msg);
		            		if(list.length > 0) {
		            			var str = "<tr>" + 
		            				"<td><b>类型</b></td>" +
			            			"<td><b>第一档</b></td>" + 
			            			"<td><b>第二档</b></td>" + 
			            			"<td><b>第三档</b></td>" + 
			            			"<td><b>止损档</b></td>" + 
			            			"<td><b>开单触发价</b></td>" + 
			            			"<td><b>撤单触发价</b></td>" + 			            			
			            			"<td><b>状态</b></td>" + 
			            			"<td><b>来源</b></td>" + 
			            			"<td><b>关联订单号</b></td>" + 
			            			"<td><b>操作时间</b></td>" + 
			            			"<td><b>更新时间</b></td>" + 
			            			"<td><b>操作</b></td>" + 
			            			"</tr>";
		            		}
		            		for (x in list) {
		            			i = list.length - x - 1;
		            			var edit = "<input type=\"button\" id=\"fplan" + list[i].id + "\" name=\"fplan" + list[i].id + "\" value=\"跟单\" onclick =\"follow(" + list[i].id + ")\"/>";
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
			            			"<td>" + list[i].orderIds + "</td>" +
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
	    	$("#message").html('');
	    	$("#cplan"+id).attr("disabled","true");
	    	$.ajax({   
	            type : "get",
	            url : "/Order/cancelPlan",
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
	    
	    function follow(id){  
	    	$("#message").html('');
	    	$("#fplan"+id).attr("disabled","true");
	        $.ajax({  
	            type : "get",
	            url : "/Order/follow",
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
	    
	    function warn(id){  
	    	$("#message").html('');
	    	$("#warn"+id).attr("disabled","true");
	        $.ajax({  
	            type : "get",
	            url : "/Order/warn",
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
	    
	    function finish(id){  
	    	$("#message").html('');
	    	$("#oplan"+id).attr("disabled","true");
	        $.ajax({  
	            type : "get",
	            url : "/Order/finish",
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
	            	$("#oplan"+id).removeAttr("disabled");
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
<table id="showList" name="showList" width="100%" cellpadding="1" cellspacing="0" border="1"></table>
<p>
<input type="button" id="balanceSubmit" id="balanceSubmit" value="刷新账户" onclick ="balance()"/><p>
<table id="balanceList" name="balanceList" width="100%" cellpadding="1" cellspacing="0" border="1"></table>
<p>
<input type="button" id="positionRiskSubmit" id="positionRiskSubmit" value="刷新持仓" onclick ="positionRisk()"/><p>
<table id="positionRiskList" name="positionRiskList" width="100%" cellpadding="1" cellspacing="0" border="1"></table>
<p>
类型：
<input type="radio" name="symbol" value="BTCUSDT" checked>BTCUSDT
<input type="radio" name="symbol" value="ETHUSDT">ETHUSDT
<input type="radio" name="symbol" value="BCHUSDT">BCHUSDT
<input type="radio" name="symbol" value="XRPUSDT">XRPUSDT
<input type="radio" name="symbol" value="EOSUSDT">EOSUSDT
<input type="radio" name="symbol" value="LTCUSDT">LTCUSDT
<input type="radio" name="symbol" value="TRXUSDT">TRXUSDT
范围：
<input type="radio" name="startTime" value="1">一天
<input type="radio" name="startTime" value="3">三天
<input type="radio" name="startTime" value="7" checked>七天
<input type="button" id="findAllOrdersSubmit" id="findAllOrdersSubmit" value="查询订单" onclick ="findAllOrders()"/>
<input type="button" id="cancelAllSubmit" id="cancelAllSubmit" value="撤销此类型全部订单" onclick ="cancelAll()"/><p>
<table id="ordersList" name="ordersList" width="100%" cellpadding="1" cellspacing="0" border="1"></table>
<p>
即时单：
<input type="radio" name="symbol1" value="BTCUSDT" checked>BTCUSDT
<input type="radio" name="symbol1" value="ETHUSDT">ETHUSDT
<input type="radio" name="symbol1" value="BCHUSDT">BCHUSDT
<input type="radio" name="symbol1" value="XRPUSDT">XRPUSDT
<input type="radio" name="symbol1" value="EOSUSDT">EOSUSDT
<input type="radio" name="symbol1" value="LTCUSDT">LTCUSDT
<input type="radio" name="symbol1" value="TRXUSDT">TRXUSDT
<input type="button" value="开多" id="market1" name="market1" onclick ="tradeMarket1('BUY', 1)"/>
<input type="button" value="开空" id="market2" name="market2" onclick ="tradeMarket1('SELL', 2)"/>
<p>
<p>
计划单：
<input type="radio" name="symbol2" value="BTCUSDT" checked>BTCUSDT
<input type="radio" name="symbol2" value="ETHUSDT">ETHUSDT
<input type="radio" name="symbol2" value="BCHUSDT">BCHUSDT
<input type="radio" name="symbol2" value="XRPUSDT">XRPUSDT
<input type="radio" name="symbol2" value="EOSUSDT">EOSUSDT
<input type="radio" name="symbol2" value="LTCUSDT">LTCUSDT
<input type="radio" name="symbol2" value="TRXUSDT">TRXUSDT
<input type="button" value="开单" id="plan" name="plan" onclick ="tradePlan()"/>
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
</table>
<p>
<input type="button" id="fllowSubmit" id="fllowSubmit" value="跟随计划单" onclick ="fllowPlans()"/>
<table id="followList" name="followList" width="100%" cellpadding="1" cellspacing="0" border="1"></table>
<p>
<input type="button" id="findAllPlansSubmit" id="findAllPlansSubmit" value="我的计划单" onclick ="findAllPlans()"/>
<table id="plansList" name="plansList" width="100%" cellpadding="1" cellspacing="0" border="1"></table>
<p>
<a href="${pageContext.request.contextPath}/User/index">用户页面</a>
<a href="${pageContext.request.contextPath}/Config/index">配置页面</a>
<a href="${pageContext.request.contextPath}/User/logout">退出登录</a>
</body>
</html>
