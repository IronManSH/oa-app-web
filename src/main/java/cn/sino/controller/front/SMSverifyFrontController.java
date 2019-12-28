package cn.sino.controller.front;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.qcloudsms.SmsSingleSender;
import com.github.qcloudsms.SmsSingleSenderResult;

import cn.sino.common.MyStringUtils;
import cn.sino.common.Result;
import cn.sino.common.ResultUtils;
import cn.sino.utils.EhcacheUtil;

@RestController
@RequestMapping("/appfront/smsverify")
public class SMSverifyFrontController {
	@Autowired
	private EhcacheUtil ehcacheUtil;
	
	@Value("${sdkappid}")
	private int appid;//短信应用id
	
	@Value("${appkey}")
	private String appkey;//短信应用key
	
	@Value("${smsSign}")
	private String smsSign;//短信签名
	
	@Value("${templateid.zcyz}")
	private int templateId;//短信注册验证模板id
	
	
	@Value("${minute}")
	private int minute;
	
	
	@RequestMapping("/sendCode")
	public Result sendCode(String phone){
		try {
			
			if(phone!=null&&!"".equals(phone)){
				int length = phone.length();
				if(length!=11){
					throw new RuntimeException("手机号不是11位");
				}
			}else{
				throw new RuntimeException("手机号为空");
			}
			if(!MyStringUtils.isInteger(phone)){
				throw new RuntimeException("手机号不是数字");
			}
			
			//根据手机号清空旧验证码
		    ehcacheUtil.remove(phone);
		    String code="";
			//生成验证码
		    Random random = new Random();
		    for (int i = 0; i < 6; i++) {
		    	code += random.nextInt(10);
		    }
		    System.out.println("主线程的code:"+code);
		    String[] params = {code,minute+""};
		    //存缓存里面
		    ehcacheUtil.put(phone, code);
		    
		    timingThread(code,phone);
		     
//		    SmsSingleSender ssender = new SmsSingleSender(appid, appkey);
//		    SmsSingleSenderResult result = ssender.sendWithParam("86", phone,templateId, params,smsSign, "","");
//			System.out.println(result);
//			System.out.println(phone);
//			System.out.println(code);
			System.out.println("********************************");
			return ResultUtils.success("发送成功", code);
		}catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	} 
	
	public void timingThread(String code,String phone){
		if(code==null||"".equals(code)){
			throw new RuntimeException("验证码为空");
		}
		
		if(phone==null||"".equals(phone)){
			throw new RuntimeException("手机号为空");
		}
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("进入子线程。。。。。。");
				Timer timer = new Timer();
				timer.schedule(new TimerTask() {
					public void run() {
						 Object oldcode=ehcacheUtil.get(phone);
					     if(code.equals(oldcode)){
					    	 ehcacheUtil.put(phone, "1");
					    	 System.out.println("验证码已失效");
					     }
					     System.out.println("定时器已经被销毁");
					}
				}, 60000*minute);// 定时3分钟后删除
			}
		}).start();
	}
}
