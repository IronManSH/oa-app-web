package cn.sino.controller.front;


import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;

import cn.sino.common.Result;
import cn.sino.common.ResultUtils;
import cn.sino.mvc.UserInfoFront;
import cn.sino.mvc.UserInfoUtils;
import cn.sino.service.dubbo.DubboInfoService;

@RestController
@RequestMapping("/appfront/indictment")
public class IndictmentFrontController {
	@Reference(check=false)
	private DubboInfoService dubboInfoService;
	
	@RequestMapping("/apply")
	public Result  apply(HttpServletRequest request){
		try {
			String lineupInfoId = request.getParameter("lineUpInfoId");
			String name  = request.getParameter("name");
			String phone  = request.getParameter("phone");
			String idcard = request.getParameter("idcard");
			String businessId  = request.getParameter("businessId");
			String businessName  = request.getParameter("businessName");
			String appointTime  = request.getParameter("appointTime");
			UserInfoFront userinfo = UserInfoUtils.getBeanFront(request);
			String handleUserId = userinfo.getId();
			String handleUserName = userinfo.getName();
			Result save = dubboInfoService.save(lineupInfoId, name, phone, idcard, businessId, businessName, appointTime, handleUserId, handleUserName, handleUserName, handleUserName, handleUserName);
			return save;
		}catch(Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
}
