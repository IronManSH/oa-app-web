package cn.sino.controller.admin;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.micro.model.UserInfo;
import com.micro.service.dubbo.user.DubboUserService;

import cn.sino.common.Result;
import cn.sino.common.ResultUtils;
import cn.sino.mvc.UserInfoAdmin;
import cn.sino.mvc.UserInfoUtils;
import cn.sino.service.dubbo.appointment.DubboVisitApplyInfoService;

@RestController
@RequestMapping("/app/office")
public class OfficeAdminController {
	@Reference(check=false)
	private DubboVisitApplyInfoService dubboVisitApplyInfoService;
	@Reference(check=false)
	private DubboUserService dubboUserService;
	@RequestMapping("/add")
	public Result  add(HttpServletRequest request){
		try {
			UserInfoAdmin userInfo=UserInfoUtils.getBeanAdmin(request);
			String userId=userInfo.getId();
			String roomnum = request.getParameter("roomnum");
			dubboVisitApplyInfoService.addOffice(userId, roomnum);
			return ResultUtils.success("添加成功", null);
		}catch(Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findAllList")
	public Result  findAllList(HttpServletRequest request){
		try {
			UserInfoAdmin userInfo=UserInfoUtils.getBeanAdmin(request);
			String userId=userInfo.getId();
			List<Map<String, Object>> list = dubboVisitApplyInfoService.findOfficeByUserId(userId);
			return ResultUtils.success("查询成功", list);
		}catch(Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/check")
	public Result  check(HttpServletRequest request){
		try {
			UserInfoAdmin userInfo=UserInfoUtils.getBeanAdmin(request);
			String userId=userInfo.getId();
			List<Map<String, Object>> list = dubboVisitApplyInfoService.findOfficeByUserId(userId);
			if(list.size()==0){
				UserInfo info = dubboUserService.findUserInfo(userId);
				String officeAddress = info.getOfficeAddress();
				dubboVisitApplyInfoService.addOffice(userId, officeAddress);
			}
			return ResultUtils.success("检查成功", null);
		}catch(Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
}
