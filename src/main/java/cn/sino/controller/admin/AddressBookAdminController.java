package cn.sino.controller.admin;


import java.util.List;

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



@RestController
@RequestMapping("/app/addressbook")
public class AddressBookAdminController {
	@Reference(check=false)
	private DubboUserService dubboUserService;
	
	@RequestMapping("/findList")
	public Result findList(HttpServletRequest request){
		try {
			UserInfoAdmin userInfo=UserInfoUtils.getBeanAdmin(request);
			String deptid = userInfo.getDeptid();
			List<UserInfo> list = dubboUserService.getUserByDeptId(deptid);
			return ResultUtils.success("查询成功", list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}

}
