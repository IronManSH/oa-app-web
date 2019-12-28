package cn.sino.controller.admin;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;

import cn.sino.common.DateUtils;
import cn.sino.common.Result;
import cn.sino.common.ResultUtils;
import cn.sino.mvc.UserInfoAdmin;
import cn.sino.mvc.UserInfoUtils;
import cn.sino.service.dubbo.onjob.DubboOnjobService;

@RestController
@RequestMapping("/app/outregedit")
public class OutRegeditAdminController {
	@Reference(check=false)
	private DubboOnjobService dubboOnjobService;
	
	@RequestMapping("/apply")
	public Result apply(HttpServletRequest request){
		try {
			UserInfoAdmin userinfo = UserInfoUtils.getBeanAdmin(request);
			String userid = userinfo.getId();
			String username = userinfo.getNickname();
			String deptid = userinfo.getDeptid();
			String deptname = userinfo.getDeptname();
			String type = request.getParameter("type");
			String btime = request.getParameter("btime");
			String etime = request.getParameter("etime");
			String reason = request.getParameter("reason");
			if(type==null||"".equals(type)){
				throw new RuntimeException("type为空");
			}
			if(btime==null||"".equals(btime)){
				throw new RuntimeException("btime为空");
			}
			if(etime==null||"".equals(etime)){
				throw new RuntimeException("etime为空");
			}
			dubboOnjobService.apply(userid, username, deptid, deptname, type, btime, etime, reason);
			return ResultUtils.success("申请成功", null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findOnjobSatus")
	public Result findOnjobSatus(HttpServletRequest request){
		try {
			
			UserInfoAdmin userinfo = UserInfoUtils.getBeanAdmin(request);
			String userid = userinfo.getId();
			String today = DateUtils.getCurrentTime();
			int satus = dubboOnjobService.findOnjobSatus(userid, today);
			return ResultUtils.success("查询成功", satus);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}

}
