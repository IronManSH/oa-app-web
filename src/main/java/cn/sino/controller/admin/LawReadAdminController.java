package cn.sino.controller.admin;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.micro.push.model.NettyUserBean;
import com.micro.push.model.PushResult;
import com.micro.push.service.DubboNettyService;
import com.sinosoft.api.service.FileInfoBusiApiService;

import cn.sino.common.Result;
import cn.sino.common.ResultUtils;
import cn.sino.mvc.UserInfoAdmin;
import cn.sino.mvc.UserInfoUtils;
import cn.sino.service.dubbo.appointment.DubboLawReadInfoService;
import cn.sino.service.dubbo.appointment.DubboWindowDutyService;
import cn.sino.service.dubbo.appointment.DubboWindowInfoService;
import cn.sino.service.dubbo.setting.DubboUserSiteService;
@RestController
@RequestMapping("/app/lawReadInfo")
public class LawReadAdminController {
	
	@Reference(check=false)
	private DubboLawReadInfoService dubboLawReadInfoService;
	@Value("${agdeptid}")
	private String agdeptid;	

	@RequestMapping("/findMyCheck")
	public Result findMyCheck(HttpServletRequest request){
		try {
			UserInfoAdmin userInfo = UserInfoUtils.getBeanAdmin(request);
			String deptid = userInfo.getDeptid();
			if(!deptid.equals(agdeptid)){
				throw new RuntimeException("该账号没有权限访问");
			}
			String status = request.getParameter("status");
			if(status==null||"".equals(status)){
				throw new RuntimeException("status为空");
			}
			List<Map<String, Object>> lawReadlist = dubboLawReadInfoService.findMyCheck(status,"");
			return ResultUtils.success("查询成功", lawReadlist);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findDetail")
	public Result findDetail(HttpServletRequest request){
		try {
			String id = request.getParameter("id");
			Map<String, Object> map = dubboLawReadInfoService.findDetail(id);
			return ResultUtils.success("查询成功", map);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findPhoto")
	public Result findPhoto(HttpServletRequest request){
		try {
			String id = request.getParameter("id");
			List<Map<String, Object>> list = dubboLawReadInfoService.findPhoto(id);
			return ResultUtils.success("查询成功", list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/check")
	public Result check(HttpServletRequest request){
		try {
			UserInfoAdmin userInfo = UserInfoUtils.getBeanAdmin(request);
			String userid = userInfo.getId();
			String username = userInfo.getNickname();
			String id = request.getParameter("id");
			String status = request.getParameter("status");
			String invitetime= request.getParameter("invitetime");
			String checkreason = request.getParameter("checkreason");
			dubboLawReadInfoService.check(id, status, checkreason,invitetime,userid,username);
			
			return ResultUtils.success("审批成功", null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	
	@RequestMapping("/deal")
	public Result deal(HttpServletRequest request,String id){
		try {
			if(id==null||"".equals(id)){
				throw new RuntimeException("id为空");
			}
			dubboLawReadInfoService.deal(id);
			return ResultUtils.success("办理成功",null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}

}
