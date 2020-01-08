package cn.sino.controller.admin;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.micro.model.UserInfo;
import com.micro.push.model.NettyUserBean;
import com.micro.push.model.PushResult;
import com.micro.push.service.DubboNettyService;
import com.micro.service.dubbo.user.DubboUserService;

import cn.sino.common.DateUtils;
import cn.sino.common.Result;
import cn.sino.common.ResultUtils;
import cn.sino.mvc.UserInfoAdmin;
import cn.sino.mvc.UserInfoUtils;
import cn.sino.service.dubbo.appointment.DubboVisitApplyInfoService;
import cn.sino.service.dubbo.appointment.DubboVisitCheckInfoService;
import cn.sino.service.dubbo.appointment.DubboVisitRegisterService;

@RestController
@RequestMapping("/app/visitor")
public class VisitApplyInfoAdminController {
	
	@Reference(check=false)
	private DubboVisitApplyInfoService dubboVisitApplyInfoService;
	@Reference(check=false)
	private DubboVisitCheckInfoService dubboVisitCheckInfoService;
	@Reference(check=false)
	private DubboVisitRegisterService dubboVisitRegisterService;
	@Reference(check=false)
	private DubboUserService dubboUserService;
	@Reference(check=false,timeout=60000,retries=0)
	private DubboNettyService dubboNettyService;
	//梧州
	//系统id
	@Value("${server.netty.appId}")
	private String appId;
	//业务类型id
	@Value("${server.netty.busiTypeId}")
	private String busiTypeId;
	
	
	@RequestMapping("/findMyApply")
	public Result  findMyAppoint(HttpServletRequest request){
		try {
			UserInfoAdmin userInfo=UserInfoUtils.getBeanAdmin(request);
			String userId=userInfo.getId();
			String date = request.getParameter("date");
			if(date==null||"".equals(date)){
				date = DateUtils.getToday();
			}
			List<Map<String, Object>> list = dubboVisitApplyInfoService.findMyApply(userId, date);
			return ResultUtils.success("查询成功", list);
		}catch(Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findMyCheck")
	public Result  findMyCheck(HttpServletRequest request){
		try {
			UserInfoAdmin userInfo=UserInfoUtils.getBeanAdmin(request);
			String userId=userInfo.getId();
			String bdate = request.getParameter("bdate");
			String edate = request.getParameter("edate");
			if(bdate==null||"".equals(bdate)||edate==null||"".equals(edate)){
				bdate = DateUtils.getToday();
				edate = DateUtils.getToday();
			}
			String type="1";
			List<Map<String, Object>> list = dubboVisitApplyInfoService.findMyCheck(userId, bdate,edate,type);
			return ResultUtils.success("查询成功", list);
		}catch(Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findReception")
	public Result  findReception(HttpServletRequest request){
		try {
			UserInfoAdmin userInfo=UserInfoUtils.getBeanAdmin(request);
			String userid=userInfo.getId();
			String bdate = request.getParameter("bdate");
			String edate = request.getParameter("edate");
			if(bdate==null||"".equals(bdate)||edate==null||"".equals(edate)){
				bdate = DateUtils.getToday();
				edate = DateUtils.getToday();
			}
			String type="1";
			List<Map<String, Object>> list = dubboVisitApplyInfoService.findReception(userid,bdate,edate,type);
			return ResultUtils.success("查询成功", list);
		}catch(Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findUserList")
	public Result  findUserList(HttpServletRequest request){
		try {
			UserInfoAdmin userInfo=UserInfoUtils.getBeanAdmin(request);
			String deptid = userInfo.getDeptid();
			List<UserInfo> list = dubboUserService.getUserByDeptId(deptid);
			return ResultUtils.success("查询成功", list);
		}catch(Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/sendTask")
	public Result  sendTask(HttpServletRequest request){
		try {
			String id = request.getParameter("id");
			String userid = request.getParameter("userid");
			String username = request.getParameter("username");
			UserInfoAdmin userinfo = UserInfoUtils.getBeanAdmin(request);
			String senduserid = userinfo.getId();
			String sendusername = userinfo.getNickname();
			dubboVisitApplyInfoService.sendTask(id, userid, username,senduserid,sendusername);
			//查询来访人和来访时间
			Map<String, Object> map = dubboVisitApplyInfoService.findDetail(id);
			String name = map.get("username").toString();
			String appointtime = map.get("appointtime").toString();
			//调用dubbo接口
			NettyUserBean bean=new NettyUserBean();
			bean.setAppid(appId);
			bean.setBusitypeid(busiTypeId);
		    bean.setTitle("来访预约接待任务");//推送标题
		    bean.setContent("内容：来访人："+name+"，来访时间"+appointtime);//推送内容
		    bean.setUserId(userid);//接收人id
		    bean.setUserName(username);//接收人姓名
		    PushResult result=dubboNettyService.sendToUser(bean);
		    System.out.println(result.getMsg());
			return ResultUtils.success("指派成功", null);
		}catch(Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/sendRecetionroom")
	public Result  sendRecetionroom(HttpServletRequest request){
		try {
			String id = request.getParameter("id");
			String recetionroom = request.getParameter("recetionroom");
			Map<String, Object> map = dubboVisitApplyInfoService.findDetail(id);
			String userid = map.get("userid").toString();
			String username = map.get("username").toString();
			String recetionusername = map.get("recetionusername").toString();
			//调用dubbo接口
			NettyUserBean bean=new NettyUserBean();
			bean.setAppid(appId);
			bean.setBusitypeid(busiTypeId);
		    bean.setTitle("来访预约审批通过");//推送标题
		    bean.setContent("内容：接待人："+recetionusername+"，接待地址："+recetionroom);//推送内容
		    bean.setUserId(userid);//接收人id
		    bean.setUserName(username);//接收人姓名
		    PushResult result=dubboNettyService.sendToUser(bean);
		    System.out.println(result.getMsg());
			dubboVisitApplyInfoService.sendRoomNum(id, recetionroom);
			return ResultUtils.success("发送成功", null);
		}catch(Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	
	
	
	@RequestMapping("/findDetail")
	public Result  findDetail(HttpServletRequest request){
		try {
			String id = request.getParameter("id");
			Map<String, Object> map = dubboVisitApplyInfoService.findDetail(id);
			return ResultUtils.success("查询成功", map);
		}catch(Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/check")
	public Result  check(HttpServletRequest request){
		try {
			UserInfoAdmin user = UserInfoUtils.getBeanAdmin(request);
			String userId = user.getId();
			String username = user.getNickname();
			String id = request.getParameter("id");
			String status = request.getParameter("status");
			String checkreason=request.getParameter("checkreason");
			dubboVisitCheckInfoService.check(id, status, checkreason, userId, username);
			if(status.equals("2")){
				Map<String, Object> map = dubboVisitApplyInfoService.findDetail(id);
				String nowtime = DateUtils.getCurrentTime();
				String userid = map.get("userid").toString();
				String name = map.get("username").toString();
				//调用dubbo接口
				NettyUserBean bean=new NettyUserBean();
				bean.setAppid(appId);
				bean.setBusitypeid(busiTypeId);
			    bean.setTitle("来访预约审批不通过");//推送标题
			    bean.setContent("内容：审批理由："+checkreason+"，审批人："+username+"，审批时间："+nowtime);//推送内容
			    bean.setUserId(userid);//接收人id
			    bean.setUserName(name);//接收人姓名
			    PushResult result=dubboNettyService.sendToUser(bean);
			    System.out.println(result.getMsg());
			}
			return ResultUtils.success("审批成功", null);
		}catch(Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/updateStatus")
	public Result  updateStatus(HttpServletRequest request){
		try {
			String id = request.getParameter("id");
			String status = request.getParameter("status");
			if(status==null||"".equals(status)){
				throw new RuntimeException("status为空");
			}
			String msg="";
			if(status.equals("2")){
				msg="离开成功";
				String leavemode="1";//离开方式（0.扫码离开，1.接待人结束,2.预约人结束）
				dubboVisitRegisterService.leave(id,leavemode);
			}
			dubboVisitApplyInfoService.updateStatus(id, status);
			return ResultUtils.success(msg, null);
		}catch(Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}

}
