package cn.sino.controller.admin;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;

import cn.sino.common.PageInfo;
import cn.sino.common.Result;
import cn.sino.common.ResultUtils;
import cn.sino.service.dubbo.appointment.DubboVisitRegisterService;

@RestController
@RequestMapping("/app/visitRegister")
public class VisitRegisterAdminController {
	@Reference(check=false)
	private DubboVisitRegisterService dubboVisitRegisterService;
	
	@RequestMapping("/findList")
	public PageInfo<Map<String,Object>> findAll(HttpServletRequest request,PageInfo<Map<String,Object>>pageinfo){
		try {
			String bdate = request.getParameter("bdate");
			String edate = request.getParameter("edate");
			String username = request.getParameter("username");
			pageinfo = dubboVisitRegisterService.findAllList(username,bdate,edate,pageinfo.getPage(),pageinfo.getLimit());
			pageinfo.setCode(PageInfo.SUCCESS);
			pageinfo.setMsg("查询成功");
			return pageinfo;
		}catch(Exception e) {
			pageinfo.setCode(PageInfo.ERROR);
			pageinfo.setMsg(e.getMessage());
			return pageinfo;
		}
	}

}
