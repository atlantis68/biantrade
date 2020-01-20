package cn.itcast.controller;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import cn.itcast.pojo.Config;
import cn.itcast.pojo.User;
import cn.itcast.service.ConfigService;

@Controller
@RequestMapping("/Config")
public class ConfigController {
	
	private static final Logger logger = LoggerFactory.getLogger(ConfigController.class);
	
    @Autowired
    private ConfigService configService;
	
    @RequestMapping(value = "/index")
    public String index(Model model, HttpSession session) {
    	User user = (User) session.getAttribute("USER_SESSION");
    	model.addAttribute("role", user.getRole());
        return "config";
    }
    
    @RequestMapping("/findConfig")
    @ResponseBody
    public String findConfig(String symbol, HttpSession session) {
    	JSONObject result = new JSONObject();
    	try {
    		User user = (User) session.getAttribute("USER_SESSION");
			Config config = new Config();
			config.setUid(user.getId());
			config.setType(symbol);
			Config allConfig = configService.findConfigByUid(config);
        	result.put("status", "ok");
        	result.put("msg", JSON.toJSONString(allConfig));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
    		result.put("status", "error");
    		result.put("msg", e.getMessage());
		}
    	return result.toJSONString();
    }
    
    @RequestMapping("/save")
    @ResponseBody
    public String save(String id, String marketAmount, String limitAmount, String maxLoss, String tradeOffset, String lossTriggerOffset, 
    		String lossEntrustOffset, String lossWorkingType, String lossType, String rate, String autoTrade, String autoCancel) {
    	JSONObject result = new JSONObject();
    	try {
			Config config = new Config();
			config.setId(Integer.parseInt(id));
			config.setMarketAmount(Float.parseFloat(marketAmount));
			config.setLimitAmount(Integer.parseInt(limitAmount));
			config.setMaxLoss(Float.parseFloat(maxLoss));
			config.setTradeOffset(Float.parseFloat(tradeOffset));
			config.setLossTriggerOffset(Float.parseFloat(lossTriggerOffset));
			config.setLossEntrustOffset(Float.parseFloat(lossEntrustOffset));
			config.setLossWorkingType(lossWorkingType);
			config.setLossType(Integer.parseInt(lossType));
			config.setRate(Integer.parseInt(rate));
			config.setAutoTrade(Integer.parseInt(autoTrade));
			config.setAutoCancel(Integer.parseInt(autoCancel));
			int number = configService.updateConfig(config);
			if(number > 0) {
				result.put("status", "ok");
				result.put("msg", "save successful");
			} else {
				result.put("status", "error");
				result.put("msg", "save failed");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
    		result.put("status", "error");
    		result.put("msg", e.getMessage());
		}
    	return result.toJSONString();
    }
}
