package cn.sino.controller.admin;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;

import cn.sino.common.Result;
import cn.sino.common.ResultUtils;
import cn.sino.mvc.UserInfoAdmin;
import cn.sino.mvc.UserInfoUtils;
import cn.sino.service.dubbo.setting.DubboUserSiteService;

@RestController
@RequestMapping("/app/lawZCApproval")
public class LawZCApprovalController {
	
	@Reference(check=false)
	private DubboUserSiteService dubboUserSiteService;
	
	//安管部门id
	@Value("${agdeptid}")
	private String agdeptid;
	
	
	@RequestMapping("/findLawUser")
	public Result findLawUser(HttpServletRequest request){
		try {
			UserInfoAdmin userInfo = UserInfoUtils.getBeanAdmin(request);
			String deptid = userInfo.getDeptid();
			if(!deptid.equals(agdeptid)){
				throw new RuntimeException("该账号无权限查看");
			}
			String idcard = request.getParameter("idcard");
			String name = request.getParameter("name");
			String status = request.getParameter("status");
			
			List<Map<String, Object>> list = dubboUserSiteService.findLawUser(idcard, name, status,"");
			return ResultUtils.success("查询成功", list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
		
	}
	
	@RequestMapping("/findLawDetails")
	public Result findLawDetails(HttpServletRequest request,String userid){
		try {
			Map<String, Object> map = dubboUserSiteService.findLawDetails(userid);
			
			return ResultUtils.success("查询成功", map); 	
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
		
	}
	@RequestMapping("/check")
	public Result check(HttpServletRequest request,String userid){
		try {
			String status = request.getParameter("status");
			String checkreason = request.getParameter("checkreason");
			UserInfoAdmin userinfo = UserInfoUtils.getBeanAdmin(request);
			String checkuserid = userinfo.getId();
			String checkusername = userinfo.getNickname();
			dubboUserSiteService.check(userid, status, checkuserid, checkusername, checkreason);
			return ResultUtils.success("审批成功", null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
		
	}

}
