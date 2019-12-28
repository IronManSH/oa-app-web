package cn.sino.controller.admin;

import java.util.HashMap;
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

@RestController
@RequestMapping("/app/user")
public class LoginAdminController {
	@Reference(check=false)
	private DubboUserService dubboUserService;
	//登录
	@RequestMapping("/login")
	public Result login(String username,String password){
		try{
			
			UserInfo user = dubboUserService.login(username, password);//eq
			if(user==null){
				throw new RuntimeException("用户名或密码错误");
			}
			Map<String,Object> mapData=new HashMap<String,Object>();
			String token = user.getToken();
			String userId = user.getId();
			System.out.println("内网-userId:"+userId);
			System.out.println("内网-ssoname:"+token);
			mapData.put("ssoname",token);
			mapData.put("userId",userId);
			mapData.put("username",user.getNickname());
			mapData.put("telephone",user.getTelephone());
			mapData.put("deptname",user.getDeptname());
			mapData.put("deptid",user.getDeptid());
			//mapData.put("officeAddress",user.getOfficeAddress());
			mapData.put("officeAddress","201");
			return ResultUtils.success("登录成功", mapData);
		}catch(Exception e){
			return ResultUtils.error(e.getMessage());
		}
	}
	
	
//	@RequestMapping("/updatePhoneNumber")
//	public Result updatePhoneNumber(HttpServletRequest request){
//		try {
//			
//			 UserInfoAdmin userInfo = UserInfoUtils.getBeanAdmin(request);
//			 String phoneNum = request.getParameter("phoneNum");
//			 String userId = userInfo.getId();
//			 dubboUserService.updatePhoneNumber(userId, phoneNum);
//			 return ResultUtils.success("修改成功", null);
//		} catch (Exception e) {
//			 return ResultUtils.error(e.getMessage());
//		}
//		
//	}
	
	
//	@RequestMapping("/updatePassword")
//	public Result updatePassword(HttpServletRequest request){
//		try {
//			
//			 UserInfoAdmin userInfo = UserInfoUtils.getBeanAdmin(request);
//			 String userId = userInfo.getId();
//			 String password = request.getParameter("password");
//			 String newPassword = request.getParameter("newPassword");
//			 
//			 Map<String, Object> map = dubboUserService.getUserByPassword(userId, password);
//			 if(map!=null&&!"".equals(map)){
//				 dubboUserService.updatePassword(userId, newPassword);
//				 return ResultUtils.success("修改成功", null);
//			 }else{
//				 return ResultUtils.success("旧密码错误，修改失败", null);
//			 }
//		} catch (Exception e) {
//			 return ResultUtils.error(e.getMessage());
//		}
//		
//	}
	
	@RequestMapping("/updateBaseInfo")
	public Result updateBaseInfo(HttpServletRequest request){
		try {
			 UserInfoAdmin userInfo = UserInfoUtils.getBeanAdmin(request);
			 String userId = userInfo.getId();
			 String deptId = userInfo.getDeptid();
			 String idcard = request.getParameter("idcard");
			 String phoneMun = request.getParameter("phoneMun");
			 String address = request.getParameter("address");
			 String name = request.getParameter("name");
			 dubboUserService.updateBasicInfo(userId, deptId, idcard, phoneMun, address, name);
			 return ResultUtils.success("修改成功", null);
		} catch (Exception e) {
			 return ResultUtils.error(e.getMessage());
		}
		
	}
}
