package cn.sino.controller.admin;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.micro.push.model.NettyUserBean;
import com.micro.push.model.PushResult;
import com.micro.push.service.DubboNettyService;
import com.sinosoft.api.service.FileInfoBusiApiService;

import cn.sino.common.Result;
import cn.sino.common.ResultUtils;
import cn.sino.mvc.UserInfoAdmin;
import cn.sino.mvc.UserInfoUtils;
import cn.sino.service.dubbo.appointment.DubboLawReadInfoService;
import cn.sino.service.dubbo.appointment.DubboWindowDutyService;
import cn.sino.service.dubbo.appointment.DubboWindowInfoService;
import cn.sino.service.dubbo.setting.DubboUserSiteService;
@RestController
@RequestMapping("/app/lawReadInfo")
public class LawReadAdminController {
	
	@Reference(check=false)
	private DubboLawReadInfoService dubboLawReadInfoService;
	@Reference(check=false)
	private DubboUserSiteService dubboUserSiteService;
	@Reference(check=false)
	private FileInfoBusiApiService fileInfoBusiApiService;
	@Reference(check=false)
	private DubboNettyService dubboNettyService;
	@Reference(check=false)
	private DubboWindowInfoService dubboWindowInfoService;
	@Reference(check=false)
	private DubboWindowDutyService dubboWindowDutyService;
	//消息推送
	//系统id
	@Value("${server.netty.appId}")
	private String appId;
	//业务类型id
	@Value("${server.netty.busiTypeId}")
	private String busiTypeId;
	
	@Value("${agdeptid}")
	private String agdeptid;	

	@RequestMapping("/findMyCheck")
	public Result findMyCheck(HttpServletRequest request){
		try {
			UserInfoAdmin userInfo = UserInfoUtils.getBeanAdmin(request);
			String deptid = userInfo.getDeptid();
			if(!deptid.equals(agdeptid)){
				throw new RuntimeException("该账号没有权限访问");
			}
			String status = request.getParameter("status");
			if(status==null||"".equals(status)){
				throw new RuntimeException("status为空");
			}
			List<Map<String, Object>> lawReadlist = dubboLawReadInfoService.findMyCheck(status,"");
			return ResultUtils.success("查询成功", lawReadlist);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findDetail")
	public Result findDetail(HttpServletRequest request){
		try {
			String id = request.getParameter("id");
			Map<String, Object> map = dubboLawReadInfoService.findDetail(id);
			return ResultUtils.success("查询成功", map);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findPhoto")
	public Result findPhoto(HttpServletRequest request){
		try {
			String id = request.getParameter("id");
			List<Map<String, Object>> list = dubboLawReadInfoService.findPhoto(id);
			return ResultUtils.success("查询成功", list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/check")
	public Result check(HttpServletRequest request){
		try {
			UserInfoAdmin userInfo = UserInfoUtils.getBeanAdmin(request);
			String userid = userInfo.getId();
			String username = userInfo.getNickname();
			String id = request.getParameter("id");
			String status = request.getParameter("status");
			String invitetime= request.getParameter("invitetime");
			String checkreason = request.getParameter("checkreason");
			dubboLawReadInfoService.check(id, status, checkreason,invitetime,userid,username);
			String msg="";
			String content="";
			String receiverid ="";
			String receivername ="";
			String windowname ="";
			
			//===============消息推送=========================//
			Map<String, Object> map = dubboLawReadInfoService.findDetail(id);
			if(map!=null&&!"".equals(map)){
				receiverid = map.get("userid").toString();
				receivername = map.get("username").toString();
				windowname = map.get("windowname").toString();
				if(status.equals("1")){
					msg="审批通过";
					content="来取光碟时间："+invitetime+"\n取光碟的窗口："+windowname;
				}else{
					msg="审批不通过";
					content=checkreason;
				}
				System.out.println(content);
				NettyUserBean bean=new NettyUserBean();
				bean.setAppid(appId);
				bean.setBusitypeid(busiTypeId);
			    bean.setTitle("律师阅卷预约"+msg);//推送标题
			    bean.setContent(content);//推送内容
			    bean.setUserId(receiverid);//接收人id
			    bean.setUserName(receivername);//接收人姓名
			    PushResult result=dubboNettyService.sendToUser(bean);
			    System.out.println(result.getMsg());
			}
			return ResultUtils.success("审批成功", null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	
	@RequestMapping("/deal")
	public Result deal(HttpServletRequest request,String id){
		try {
			dubboLawReadInfoService.deal(id);
			return ResultUtils.success("办理成功",null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}

}
