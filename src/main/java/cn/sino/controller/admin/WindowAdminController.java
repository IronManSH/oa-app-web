package cn.sino.controller.admin;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;

import cn.sino.common.DateUtils;
import cn.sino.common.Result;
import cn.sino.common.ResultUtils;
import cn.sino.mvc.UserInfoAdmin;
import cn.sino.mvc.UserInfoUtils;
import cn.sino.service.dubbo.appointment.DubboWindowInfoService;

@RestController
@RequestMapping("/app/window")
public class WindowAdminController {
	@Reference(check=false)
	private DubboWindowInfoService dubboWindowService;
	
	

	@RequestMapping("/findAll")
	public Result  findAll(HttpServletRequest request){
		try {
			UserInfoAdmin userinfo = UserInfoUtils.getBeanAdmin(request);
			String userid = userinfo.getId();
			String today = DateUtils.getToday();
			List<Map<String, Object>> list = dubboWindowService.findWindow(userid,today);
			return ResultUtils.success("查询成功", list);
		}catch(Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findAllWindow")
	public Result  findAllWindow(HttpServletRequest request){
		try {
			List<Map<String, Object>> list = dubboWindowService.findWindow();
			return ResultUtils.success("查询成功", list);
		}catch(Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findRoom")
	public Result  findRoom(HttpServletRequest request){
		try {
			List<Map<String, Object>> list = dubboWindowService.findRoom();
			return ResultUtils.success("查询成功", list);
		}catch(Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findWindowByid")
	public Result  findWindowByid(HttpServletRequest request){
		try {
			String windowid = request.getParameter("windowid");
			Map<String, Object> map = dubboWindowService.findWindowByid(windowid);
			return ResultUtils.success("查询成功", map);
		}catch(Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}

}
