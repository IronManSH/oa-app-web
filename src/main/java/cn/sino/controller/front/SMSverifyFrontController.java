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
	private int appid;
	
	@Value("${appkey}")
	private String appkey;
	
	@Value("${templateid.zcyz}")
	private int emplateId;//短信注册验证模板id
	
	@Value("${smsSign}")
	private String smsSign;
	
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
			//生成验证码
		    Random random = new Random();
		    String code="";
		    for (int i = 0; i < 6; i++) {
		    	code += random.nextInt(10);
		    }
		    
		    String[] params = {code,minute+""};
		    //根据手机号清空旧验证码
		    ehcacheUtil.remove(phone);
		    //存缓存里面
		    ehcacheUtil.put(phone, code);
		     
		    new Thread(new Runnable() {
				@Override
				public void run() {
					Timer timer = new Timer();
					timer.schedule(new TimerTask() {
						public void run() {
							 //根据手机号清空旧验证码
							 Object oldcode=ehcacheUtil.get(phone);
						     if(oldcode!=null){
						    	 ehcacheUtil.put(phone, "1");
						     }
						     System.out.println("定时器已经被销毁");
						     System.out.println("验证码已失效");
						}
					}, 10000);// 定时3分钟后删除
				}
		    }).start();
		     
		    //SmsSingleSender ssender = new SmsSingleSender(appid, appkey);
		    //SmsSingleSenderResult result = ssender.sendWithParam("86", phone,emplateId, params,smsSign, "","");
			System.out.println("result");
			System.out.println(phone);
			System.out.println(code);
			System.out.println("********************************");
			return ResultUtils.success("发送成功", code);
		}catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	} 
}
