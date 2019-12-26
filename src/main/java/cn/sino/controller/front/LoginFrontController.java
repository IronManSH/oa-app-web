package cn.sino.controller.front;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.dubbo.config.annotation.Reference;
import com.micro.service.dubbo.user.DubboUserService;
import com.sinosoft.api.service.FileInfoBusiApiService;

import cn.sino.common.MyStringUtils;
import cn.sino.common.Result;
import cn.sino.common.ResultUtils;
import cn.sino.mvc.UserInfoFront;
import cn.sino.mvc.UserInfoUtils;
import cn.sino.service.dubbo.setting.DubboChinaAreaService;
import cn.sino.service.dubbo.setting.DubboUserSiteService;
import cn.sino.utils.EhcacheUtil;

@RestController
@RequestMapping("/appfront/user")
public class LoginFrontController {
	@Reference(check=false)
	private DubboUserSiteService dubboUserSiteService;
	@Reference(check=false)
	private DubboUserService dubboUserService;
	@Autowired
	private EhcacheUtil ehcacheUtil;
	@Reference(check=false)
	private DubboChinaAreaService dubboChinaAreaService;
	@Reference(check=false)
	private FileInfoBusiApiService fileInfoBusiApiService;
	
	
	//登录
	@RequestMapping("/login")
	public Result login(String username,String password){
		try{
			Map<String,Object> map=dubboUserSiteService.loginByTel(username, password);
			if(map==null){
				throw new RuntimeException("用户名或密码错误");
			}
			String type = map.get("type").toString();
			String userId = map.get("id").toString();
			if(type.equals("1")){
				String status = dubboUserSiteService.findCheckStatus(userId);
				if(status.equals("0")){
					throw new RuntimeException("尚未注册成功，注册信息审核中");
				}else if(status.equals("2")){
					throw new RuntimeException("注册成功失败，注册信息审核不通过");
				}
			}
			//生成ssoname
			String ssoname="appfront"+UUID.randomUUID().toString();
			map.put("ssoname", ssoname);
			
			//根据用户名清空旧token数据
			Object oldtoken=ehcacheUtil.get(userId);
			ehcacheUtil.remove(userId);
			if(oldtoken!=null){
				ehcacheUtil.remove(oldtoken.toString());
			}
			
			//存缓存里面
			ehcacheUtil.put(userId, ssoname);
			ehcacheUtil.put(ssoname, map);
			
			Map<String,Object> mapData=new HashMap<String,Object>();
			System.out.println("外网-userId:"+userId);
			System.out.println("外网-ssoname:"+ssoname);
			mapData.put("userId",userId);
			mapData.put("ssoname",ssoname);
			mapData.put("username",map.get("name"));
			mapData.put("telephone",map.get("telephone"));
			mapData.put("address",map.get("address"));
			mapData.put("idcard",map.get("idcard"));
			mapData.put("type",type);
			return ResultUtils.success("登录成功", mapData);
		}catch(Exception e){
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/smslogin")
	public Result smslogin(String phone ,String code){
		try {
			if(phone==null||"".equals(phone)){
				throw new RuntimeException("手机号为空");
			}
			if(code==null||"".equals(code)){
				throw new RuntimeException("验证码为空");
			}
			if(phone.length()!=11){
				throw new RuntimeException("手机号不是11位");
			}
			if(!MyStringUtils.isInteger(phone)){
				throw new RuntimeException("手机号不是数字");
			}
			if(!MyStringUtils.isInteger(code)){
				throw new RuntimeException("验证码不是数字");
			}
			if(code.length()!=6){
				throw new RuntimeException("验证码不是6位");
			}
			Object oldcode=ehcacheUtil.get(phone);
			Map<String,Object> mapData=new HashMap<String,Object>();
			if(oldcode!=null){
				if(oldcode.equals("1")){
					throw new RuntimeException("验证码已失效");
				}else if(!oldcode.equals(code)){
					throw new RuntimeException("验证码错误");
				}else{
					Map<String, Object> map= dubboUserSiteService.findByPhone(phone);
					//生成ssoname
					String ssoname="appfront"+UUID.randomUUID().toString();
					map.put("ssoname", ssoname);
					//根据用户名清空旧token数据
					String userId=map.get("id").toString();
					String type = map.get("type").toString();
					Object oldtoken=ehcacheUtil.get(userId);
					ehcacheUtil.remove(userId);
					if(oldtoken!=null){
						ehcacheUtil.remove(oldtoken.toString());
					}
					//存缓存里面
					ehcacheUtil.put(userId, ssoname);
					ehcacheUtil.put(ssoname, map);
					
					System.out.println("外网-userId:"+userId);
					System.out.println("外网-ssoname:"+ssoname);
					mapData.put("userId",userId);
					mapData.put("ssoname",ssoname);
					mapData.put("username",map.get("name"));
					mapData.put("telephone",map.get("telephone"));
					mapData.put("address",map.get("address"));
					mapData.put("idcard",map.get("idcard"));
					mapData.put("type",type);
				}
			}else{
				throw new RuntimeException("验证失败");
			}
			return ResultUtils.success("登录成功", mapData);
		} catch(Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	
	//个人注册
	@RequestMapping("/regedit")
	public Result regedit(String idcard,String name,String telephone,String address,String password,String passwordConfirm,String smscode){
		try{
			if(telephone==null||"".equals(telephone)){
				throw new RuntimeException("手机号为空");
			}
			if(telephone.length()!=11){
				throw new RuntimeException("手机号码不是11位");
			}
			if(!MyStringUtils.isInteger(telephone)){
				throw new RuntimeException("手机号不是数字");
			}
			if(smscode==null||"".equals(smscode)){
				throw new RuntimeException("验证码为空");
			}
			if(telephone.length()!=6){
				throw new RuntimeException("验证码不是6位");
			}
			if(!MyStringUtils.isInteger(smscode)){
				throw new RuntimeException("验证码不是数字");
			}
			Object oldcode=ehcacheUtil.get(telephone);
			System.out.println("oldcode:"+oldcode);
		    if(oldcode!=null){
		    	if(oldcode.equals("1")){
		    		throw new RuntimeException("验证码已失效");
		    	}else if(!oldcode.equals(smscode)){
		    		throw new RuntimeException("验证码错误");
		    	}else{
		    		dubboUserSiteService.register(idcard, name, telephone, address,password, passwordConfirm);
		    		ehcacheUtil.remove(telephone);
		    		System.out.println("验证成功");
		    	}
		    }else{
		    	throw new RuntimeException("验证失败");
		    }
			return ResultUtils.success("注册成功", null);
		}catch(Exception e){
			return ResultUtils.error(e.getMessage());
		}
	}
	
	//律师注册
	@RequestMapping("/regeditLaw")
	public Result regeditLaw(String idcard,String name,String telephone,String password,String passwordConfirm,
			 List<MultipartFile> idcardphotos, List<MultipartFile> otherphotos,String smscode){
		try{
			if(smscode==null||"".equals(smscode)){
				throw new RuntimeException("验证码为空");
			}
			if(!MyStringUtils.isInteger(smscode)){
				throw new RuntimeException("验证码不是数字");
			}
			Object oldcode=ehcacheUtil.get(telephone);
			System.out.println("oldcode:"+oldcode);
			String msg="提交成功";
			if(oldcode!=null){
		    	if(oldcode.equals("1")){
		    		throw new RuntimeException("验证码已失效");
		    	}else if(!oldcode.equals(smscode)){
		    		throw new RuntimeException("验证码错误");
		    	}else{//验证通过
					if(idcardphotos==null||idcardphotos.size()==0){
						throw new RuntimeException("身份证照片为空");
					}
					if(otherphotos==null||otherphotos.size()==0){
						throw new RuntimeException("其他相关证件照片为空");
					}
					Map<String,Object> map= dubboUserSiteService.registerLaw(idcard, name, telephone, password,passwordConfirm);
					
					if(map!=null&&!"".equals(map)){
						String fileName=null;
						String userid=map.get("userid")==null?"":map.get("userid").toString();
						msg=map.get("msg")==null?"":map.get("msg").toString();
						for(MultipartFile f:idcardphotos){//身份证照片上传
							try {
								byte[] bytes = f.getBytes();
								fileName = f.getOriginalFilename();
								fileInfoBusiApiService.uploadMulti(bytes, "", fileName, userid, userid, "idcardphotos");
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						for(int i=0;i<otherphotos.size();i++){
							try {
								byte[] bytes = otherphotos.get(i).getBytes();
								fileName = otherphotos.get(i).getOriginalFilename();
								fileInfoBusiApiService.uploadMulti(bytes, "", fileName, userid, userid, "otherphotos");
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
		    	}
		    }else{
		    	throw new RuntimeException("验证失败");
		    }
			return ResultUtils.success(msg, null);
		}catch(Exception e){
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findUserExt")
	public Result findUserExt(HttpServletRequest request){
		try {
			UserInfoFront userInfo = UserInfoUtils.getBeanFront(request);
			String userId = userInfo.getId();
			Map<String, Object> map = dubboUserSiteService.findUserExt(userId);
			return ResultUtils.success("查询成功", map);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	
	@RequestMapping("/updateaddress")
	public Result updateaddress(HttpServletRequest request){
		try {
			UserInfoFront userInfo = UserInfoUtils.getBeanFront(request);
			String userId = userInfo.getId();
			String address = request.getParameter("address");
			dubboUserSiteService.updateaddress(userId, address);
			return ResultUtils.success("修改成功", null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findByPid")
	public Result findSubLevel(HttpServletRequest request){
		try {
			String pid = request.getParameter("pid");
			List<Map<String, Object>> list = dubboChinaAreaService.findByPid(pid);
			return ResultUtils.success("查询成功", list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
}
