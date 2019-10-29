package cn.sino.controller.admin;


import java.util.ArrayList;
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

import cn.sino.common.DateUtils;
import cn.sino.common.Result;
import cn.sino.common.ResultUtils;
import cn.sino.mvc.UserInfoAdmin;
import cn.sino.mvc.UserInfoUtils;
import cn.sino.service.dubbo.appointment.DubboAppointTypeService;
import cn.sino.service.dubbo.appointment.DubboBusiApplyInfoService;

@RestController
@RequestMapping("/app/appointType")
public class BusiApplyInfoAdminController {
	
	@Reference(check=false)
	private DubboBusiApplyInfoService dubboBusiApplyInfoService;
	@Reference(check=false)
	private DubboAppointTypeService dubboBusinessService;
	@Reference(check=false)
	private DubboNettyService dubboNettyService;
	//梧州
	//系统id
	@Value("${server.netty.appId}")
	private String appId;
	//业务类型id
	@Value("${server.netty.busiTypeId}")
	private String busiTypeId;
	@Value("${businessidjson}")
	private String  businessidjson;
	
	@RequestMapping("/findWindowBuis")
	public Result findWindowBuis(HttpServletRequest request){
		try {
			UserInfoAdmin user = UserInfoUtils.getBeanAdmin(request);
			String date = request.getParameter("date");
			if(date==null||"".equals(date)){
				date = DateUtils.getToday();
			}
			String manageuserid = user.getId();
			String windowid = request.getParameter("windowid");
			List<Map<String, Object>> list = dubboBusiApplyInfoService.findByManageUserid(manageuserid,windowid, date);
			return ResultUtils.success("查询成功", list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findBuisTotal")
	public Result findBuisTotal(HttpServletRequest request){
		try {
			String date = request.getParameter("date");
			if(date==null||"".equals(date)){
				date = DateUtils.getToday();
			}
			String windowid = request.getParameter("windowid");
			String status="";
			Map<String, Object>list = dubboBusiApplyInfoService.findBuisTotal(windowid, status, date);
			return ResultUtils.success("查询成功", list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findCompleteNum")
	public Result findCompleteNum(HttpServletRequest request){
		try {
			String date = request.getParameter("date");
			if(date==null||"".equals(date)){
				date = DateUtils.getToday();
			}
			String windowid = request.getParameter("windowid");
			String status="2";
			Map<String, Object> list = dubboBusiApplyInfoService.findBuisTotal(windowid, status, date);
			return ResultUtils.success("查询成功", list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	
	@RequestMapping("/findDetail")
	public Result findDetail(HttpServletRequest request,String id){
		try {
			Map<String, Object> map = dubboBusiApplyInfoService.findDetail(id);
			return ResultUtils.success("查询成功", map);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	
	
	@RequestMapping("/updataStatus")
	public Result updataStatus(HttpServletRequest request,String id,String status){
		try {
			dubboBusiApplyInfoService.updataStatus(id, status,"");
			return ResultUtils.success("更新成功", null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findPushWeek")
	public Result findPushWeek(HttpServletRequest request){
		try {
			String num = request.getParameter("num");
			String appointtime = request.getParameter("appointtime");
			String timecode = request.getParameter("timecode");
			String push = request.getParameter("push");
			String today = DateUtils.getToday();
			String days="7";
			List<Map> parseArray = JSONObject.parseArray(businessidjson,Map.class);
			List<Map<String,Object>>list=new ArrayList<Map<String,Object>>();
			parseArray.forEach(d->{
				String businessid = d.get("businessid").toString();
				List<Map<String, Object>> businesslist = dubboBusiApplyInfoService.findPushBusiness(businessid, days,push, today, num, appointtime, timecode);
				businesslist.forEach(f->{
					list.add(list.size(), f);
				});
			});
			return ResultUtils.success("查询成功", list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findPushMonth")
	public Result findPushMonth(HttpServletRequest request){
		try {
			String num = request.getParameter("num");
			String appointtime = request.getParameter("appointtime");
			String timecode = request.getParameter("timecode");
			String push = request.getParameter("push");
			String today = DateUtils.getToday();
			String days="90";
			List<Map> parseArray = JSONObject.parseArray(businessidjson,Map.class);
			List<Map<String,Object>>list=new ArrayList<Map<String,Object>>();
			parseArray.forEach(d->{
				String businessid = d.get("businessid").toString();
				List<Map<String, Object>> businesslist = dubboBusiApplyInfoService.findPushBusiness(businessid, days,push, today, num, appointtime, timecode);
				businesslist.forEach(f->{
					list.add(list.size(), f);
				});
			});
			return ResultUtils.success("查询成功", list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findPushBusinessDetails")
	public Result findPushBusinessDetails(HttpServletRequest request){
		try {
			String id = request.getParameter("id");
			Map<String, Object> map = dubboBusiApplyInfoService.findPushBusinessDetails(id);
			return ResultUtils.success("查询成功", map);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/pushInfo")
	public Result pushInfo(HttpServletRequest request){
		try {
			String id = request.getParameter("id");
			String userid = request.getParameter("userid");
			String username = request.getParameter("username");
			String info = request.getParameter("info");
			NettyUserBean bean=new NettyUserBean();
			bean.setAppid(appId);
			bean.setBusitypeid(busiTypeId);
		    bean.setTitle("业务办理回执");//推送标题
		    bean.setContent("内容："+info);//推送内容
		    bean.setUserId(userid);//接收人id
		    bean.setUserName(username);//接收人姓名
		    PushResult result=dubboNettyService.sendToUser(bean);
		    System.out.println(result.getMsg());
			dubboBusiApplyInfoService.pushInfo(id);
			return ResultUtils.success("推送成功", null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	
	
	
}
