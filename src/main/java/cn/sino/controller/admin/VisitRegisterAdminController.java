package cn.sino.controller.admin;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;

import cn.sino.common.Result;
import cn.sino.common.ResultUtils;
import cn.sino.service.dubbo.appointment.DubboVisitRegisterService;

@RestController
@RequestMapping("/app/visitRegister")
public class VisitRegisterAdminController {
	@Reference(check=false)
	private DubboVisitRegisterService dubboVisitRegisterService;
	
	@RequestMapping("/findList")
	public Result  findAll(HttpServletRequest request){
		try {
			String date = request.getParameter("date");
			List<Map<String, Object>> list = dubboVisitRegisterService.findAllList(date);
			return ResultUtils.success("查询成功", list);
		}catch(Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}

}
