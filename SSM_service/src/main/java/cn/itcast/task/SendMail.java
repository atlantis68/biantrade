package cn.itcast.task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cn.itcast.dao.MailMapper;
import cn.itcast.pojo.Mail;
import cn.itcast.utils.ToolsUtils;

public class SendMail implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(SendMail.class);
    
    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    @Autowired
    private MailMapper mailMapper;
    
    public SendMail() {
    	new Thread(this).start();
    }
    
	@Override
	public void run() {
    	while(true) {
	    	try {
	    		List<Mail> mails = mailMapper.findUnsentMail();
	    		for(Mail mail : mails) {
	    			Properties props = ToolsUtils.getRandomPlat();
	    			try {
	    				if(StringUtils.isNotEmpty(mail.getSymbol())) {
	    					Thread.sleep((long)(new Random().nextFloat() * 5 * 1000 + 3 * 1000));
	    					Session ssn = Session.getInstance(props, new Authenticator() {
	    						
	    						protected PasswordAuthentication getPasswordAuthentication() {
	    							// 用户名、密码
	    							String userName = props.getProperty("mail.user");
	    							String password = props.getProperty("mail.password");
	    							return new PasswordAuthentication(userName, password);
	    						}
	    					});
	    					
	    					MimeMessage message = new MimeMessage(ssn);
	    					InternetAddress fromAddress = new InternetAddress(props.getProperty("mail.user"), "币安狐狸");
	    					message.setFrom(fromAddress);
	    					InternetAddress toAddress = new InternetAddress(mail.getSymbol());
	    					message.setRecipient(Message.RecipientType.TO, toAddress);
	    					message.setSubject(mail.getSubject(), "utf-8");
	    					message.setContent(mail.getContent(), "text/html;charset=utf-8");
	    					Transport.send(message);
	    					logger.info("{} send mail to {} successful", props.getProperty("mail.user"), mail.getSymbol());
	    					mail.setState(1);
	    					mail.setUpdateTime(format.format(new Date()));
	    					mailMapper.updateConfig(mail);
	    				}
	    			} catch(Exception e) {
	    				e.printStackTrace();
	    	 			logger.error("{} send mail to {} failed", props.getProperty("mail.user"), mail.getSymbol());
	    			}	
	    		}
	    	} catch(Exception e) {
	    		e.printStackTrace();
	    	} finally {
	    		try {
					Thread.sleep(1000 * 5);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}
    	}
    }
}
