<%--
  Created by IntelliJ IDEA.
  User: huo
  Date: 2019/7/15
  Time: 11:02
  To change this template use File | Settings | File Templates.
--%>
<%@ page import="java.util.*" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="com.alibaba.fastjson.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>订单</title>
    <script>
        // #username识别id   .username识别name
        function trade() {
        	document.content.action = '${pageContext.request.contextPath }' + '/Account/trade';
        	document.content.method = "post";
        	document.content.submit();
        }    
        function change() {
        	if(document.content.type.value == "MARKET") {
        		document.getElementById("priceFlag").innerHTML = "";
        		document.getElementById("stopPriceFlag").innerHTML = "";
        		document.getElementById("timeInForceFlag").innerHTML = "";
        	} else if(document.content.type.value == "LIMIT") {
        		document.getElementById("priceFlag").innerHTML = "*";
        		document.getElementById("stopPriceFlag").innerHTML = "";
        		document.getElementById("timeInForceFlag").innerHTML = "*";
        	} else if(document.content.type.value == "STOP" || document.content.type.value == "TAKE_PROFIT") {
        		document.getElementById("priceFlag").innerHTML = "*";
        		document.getElementById("stopPriceFlag").innerHTML = "*";
        		document.getElementById("timeInForceFlag").innerHTML = "";
        	} else if(document.content.type.value == "STOP_MARKET" || document.content.type.value == "TAKE_PROFIT_MARKET") {
        		document.getElementById("priceFlag").innerHTML = "";
        		document.getElementById("stopPriceFlag").innerHTML = "*";
        		document.getElementById("timeInForceFlag").innerHTML = "";
        	}
        }            
    </script>
</head>
<font color="red">
    <%-- 提示信息--%>
    <span id="message">${msg}</span>
</font>

<br/>
<form name="content">
<p>
币种:<select name="symbol">
  <option value ="BTCUSDT">BTCUSDT</option>
  <option value ="ETHUSDT">ETHUSDT</option>
  <option value ="BCHUSDT">BCHUSDT</option>
</select> 
<p>
方向:<select name="side">
  <option value ="BUY">买入</option>
  <option value ="SELL">卖出</option>
</select>
<p>
挂单数量：<input id="quantity" type="text" name="quantity"/><font color="red">*</font><p>
挂单价格：<input id="price" type="text" name="price"/><font color="red"><span id="priceFlag" name="priceFlag"></span></font><p>
触发价格：<input id="stopPrice" type="text" name="stopPrice"/><font color="red"><span id="stopPriceFlag" name="stopPriceFlag"></span></font><p>
订单类型:<select name="type" onchange="change()">
  <option value ="MARKET">市价单</option>
  <option value ="LIMIT">限价单</option>
  <option value ="STOP">限价止损单</option>
  <option value ="TAKE_PROFIT">限价止盈单</option>
  <option value ="STOP_MARKET">市价止损单</option>
  <option value ="TAKE_PROFIT_MARKET">市价止盈单</option>
</select>
<p>
成交策略:<select name="timeInForce">
  <option value ="GTC">成交为止</option>
  <option value ="IOC">无法立即成交的部分就撤销</option>
  <option value ="FOK">无法全部立即成交就撤销</option>
</select><font color="red"><span id="timeInForceFlag" name="timeInForceFlag"></span></font>
<p>
触发类型:<select name="workingType">
  <option value ="MARK_PRICE">标记价格</option>
  <option value ="CONTRACT_PRICE">合约最新价</option>
</select>
<p>
只减:<select name="reduceOnly">
  <option value ="false">否</option>
  <option value ="true">是</option>
</select>
<p>
<input type="button" value="下单" onclick ="trade()"/>
</form>
<a href="${pageContext.request.contextPath}/User/logout">退出登录</a>
</body>
</html>
