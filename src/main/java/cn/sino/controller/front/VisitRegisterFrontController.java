package cn.sino.controller.front;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.micro.push.model.NettyUserBean;
import com.micro.push.model.PushResult;
import com.micro.push.service.DubboNettyService;

import cn.sino.common.DateUtils;
import cn.sino.common.Result;
import cn.sino.common.ResultUtils;
import cn.sino.service.dubbo.appointment.DubboBusiApplyInfoService;
import cn.sino.service.dubbo.appointment.DubboVisitApplyInfoService;
import cn.sino.service.dubbo.appointment.DubboVisitRegisterService;
import cn.sino.service.dubbo.setting.DubboUserSiteService;

@RestController
@RequestMapping("/appfront/visitRegister")
public class VisitRegisterFrontController {
	@Reference(check=false)
	private DubboVisitApplyInfoService dubboAppointmentService;
	@Reference(check=false)
	private DubboBusiApplyInfoService dubboBusiApplyInfoService;
	@Reference(check=false)
	private DubboVisitRegisterService dubboVisitRegisterService;
	@Reference(check=false)
	private DubboUserSiteService dubboUserSiteService;
	@Reference(check=false,timeout=60000,retries=0)
	private DubboNettyService dubboNettyService;
	//梧州
	//系统id
	@Value("${server.netty.appId}")
	private String appId;
	//业务类型id
	@Value("${server.netty.busiTypeId}")
	private String busiTypeId;
	
	@RequestMapping("/register")
	public Result  register(HttpServletRequest request){
		try {
			String businessid = request.getParameter("businessid");
			String businesstype = request.getParameter("businesstype");
			
			if(businessid==null||businessid.isEmpty()){
				throw new RuntimeException("businessid为空");
			}
			if(businesstype==null||businesstype.isEmpty()){
				throw new RuntimeException("businesstype为空");
			}
			String msg="";
			Map<String, Object> map = dubboVisitRegisterService.findByBusinessid(businessid);
			if(map!=null){
				Object leavetime = map.get("leavetime");
				if(leavetime==null){
					String leavemode="0";//离开方式（0.扫码离开，1.接待人结束,2.预约人结束）
					dubboVisitRegisterService.leave(businessid,leavemode);
					if(businesstype.equals("1")){//来访预约
						dubboAppointmentService.updateStatus(businessid, "2");
						msg="离开成功";
					}else{
						msg="离开成功";
						dubboBusiApplyInfoService.updataStatus(businessid, "2","");
					}
				}else{
					 throw new RuntimeException("二维码已失效");
				}
				
			}else{
				Map<String, Object>buismap =null;
				if(businesstype.equals("0")){//业务预约
					msg="登记成功";
					buismap = dubboBusiApplyInfoService.findDetail(businessid);
					dubboBusiApplyInfoService.updataStatus(businessid, "1","");
				}else{
					msg="登记成功";
					buismap= dubboAppointmentService.findDetail(businessid);
					dubboAppointmentService.updateStatus(businessid, "1");
					
					//===============消息推送=========================//
					String recetionuserid = buismap.get("recetionuserid").toString();
					String recetionusername = buismap.get("recetionusername").toString();
					String username = buismap.get("username").toString();
					String nowtime = DateUtils.getCurrentTime();
					NettyUserBean bean=new NettyUserBean();
					bean.setAppid(appId);
					bean.setBusitypeid(busiTypeId);
				    bean.setTitle("访客已到访");//推送标题
				    bean.setContent("内容：来访人：+"+username+"，来访时间："+nowtime);//推送内容
				    bean.setUserId(recetionuserid);//接收人id
				    bean.setUserName(recetionusername);//接收人姓名
				    PushResult result=dubboNettyService.sendToUser(bean);
				    System.out.println(result.getMsg());
				}
				String  userid = buismap.get("userid").toString();
				Map<String, Object> user = dubboUserSiteService.findByUserid(userid);
				String name= user.get("name").toString();
				String phone= user.get("telephone").toString();
				String idcard= user.get("idcard").toString();
				dubboVisitRegisterService.register(name, phone, idcard, businesstype, businessid);
			}
			return ResultUtils.success(msg, null);
		}catch(Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
}
