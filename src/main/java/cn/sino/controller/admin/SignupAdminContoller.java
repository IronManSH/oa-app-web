package cn.sino.controller.admin;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSONObject;
import com.micro.push.model.NettyUserBean;
import com.micro.push.model.PushResult;
import com.micro.push.service.DubboNettyService;
import com.micro.service.dubbo.user.DubboRolesService;

import cn.sino.common.Result;
import cn.sino.common.ResultUtils;
import cn.sino.mvc.UserInfoAdmin;
import cn.sino.mvc.UserInfoUtils;
import cn.sino.service.dubbo.setting.DubboActivityService;
import cn.sino.service.dubbo.setting.DubboSignupService;

@RestController
@RequestMapping("/app/signup")
public class SignupAdminContoller {
	@Reference(check=false)
	private DubboActivityService dubboActivityService;
	@Reference(check=false)
	private DubboSignupService dubboSignupService;
	@Reference(check=false,timeout=60000,retries=0)
	private DubboNettyService dubboNettyService;
	@Reference(check=false)
	private DubboRolesService dubboRolesService;
	//消息推送
	//系统id
	@Value("${server.netty.appId}")
	private String appId;
	//业务类型id
	@Value("${server.netty.busiTypeId}")
	private String busiTypeId;
	//活动审批人
	@Value("${activity.check.userid}")
	private String userid;
	//系统设置系统id
	@Value("${ep.xtsz.subId}")
	private String subId;
	
	//活动发布业务角色编码
	@Value("${ep.hdfb.code}")
	private String hdfbcode;
	
	@RequestMapping("/findActivityList")
	public Result  add(HttpServletRequest request){
		try {
			UserInfoAdmin userinfo = UserInfoUtils.getBeanAdmin(request);
			String userId = userinfo.getId();
			Map<String, Object> map = dubboRolesService.findRolesById(userId, subId);
			String roles = map.get("rolesNO").toString();
			if(!roles.equals(hdfbcode)){
				throw new RuntimeException("无权限查看");
			}
			List<Map<String, Object>> list = dubboActivityService.findActivityList();
			return ResultUtils.success("查询成功", list);
		}catch(Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/check")
	public Result  check(HttpServletRequest request){
		try {
			String ids = request.getParameter("id");
			List<Map> parseArray = JSONObject.parseArray(ids,Map.class);
			String checkstatus = request.getParameter("status");
			String checkreason = request.getParameter("reason");
			UserInfoAdmin userinfo = UserInfoUtils.getBeanAdmin(request);
			String checkuserid = userinfo.getId();
			String checkusername = userinfo.getNickname();
			parseArray.forEach(f->{
				String msg="";
				String content="";
				String id = f.get("id").toString();
				dubboSignupService.check(id, checkstatus, checkuserid, checkusername, checkreason);
				Map<String, Object> map = dubboSignupService.findDetails(id);
				String receiverid = map.get("userid").toString();
				String receivername = map.get("username").toString();
				String activitytime = map.get("activitytime").toString();
				if(checkstatus.equals("1")){
					msg="审批通过";
					content="请按时来参加活动，活动时间："+activitytime;
				}else{
					msg="审批不通过";
					content=checkreason;
				}
				System.out.println(content);
				NettyUserBean bean=new NettyUserBean();
				bean.setAppid(appId);
				bean.setBusitypeid(busiTypeId);
			    bean.setTitle("活动报名"+msg);//推送标题
			    bean.setContent(content);//推送内容
			    bean.setUserId(receiverid);//接收人id
			    bean.setUserName(receivername);//接收人姓名
			    PushResult result=dubboNettyService.sendToUser(bean);
			    System.out.println(result.getMsg());
			});
			
			return ResultUtils.success("审批成功", null);
		}catch(Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findSignupList")
	public Result  findcheckDetails(HttpServletRequest request,String id){
		try {
			List<Map<String, Object>> list = dubboSignupService.findSignupList(id);
			return ResultUtils.success("查询成功", list);
		}catch(Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
}
