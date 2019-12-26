package cn.sino.controller.admin;


import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
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
			if(id==null||"".equals(id)){
				throw new RuntimeException("id为空");
			}
			Map<String, Object> map = dubboBusiApplyInfoService.findDetail(id);
			return ResultUtils.success("查询成功", map);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/updataStatus")
	public Result updataStatus(HttpServletRequest request,String id,String status){
		try {
			if(id==null||"".equals(id)){
				throw new RuntimeException("id为空");
			}
			if(status==null||"".equals(status)){
				throw new RuntimeException("status为空");
			}
			dubboBusiApplyInfoService.updataStatus(id, status,"");
			return ResultUtils.success("更新成功", null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
}
