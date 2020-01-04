package cn.itcast.dao;

import java.util.List;

import cn.itcast.pojo.Mail;

public interface MailMapper {
	
    public int insertMail(Mail mail);
    
    public int updateConfig(Mail mail);
    
    public List<Mail> findUnsentMail();

}
