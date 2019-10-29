package cn.sino.controller.front;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.dubbo.config.annotation.Reference;
import com.micro.model.UserInfo;
import com.micro.push.model.NettyUserBean;
import com.micro.push.model.PushResult;
import com.micro.push.service.DubboNettyService;
import com.micro.service.dubbo.user.DubboUserService;
import com.sinosoft.api.service.FileInfoBusiApiService;

import cn.sino.common.DateUtils;
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
	@Reference(check=false)
	private FileInfoBusiApiService fileInfoBusiApiService;
	@Autowired
	private EhcacheUtil ehcacheUtil;
	@Reference(check=false)
	private DubboNettyService dubboNettyService;
	@Reference(check=false)
	private DubboChinaAreaService dubboChinaAreaService;
	
	
	//消息推送
	//系统id
	@Value("${server.netty.appId}")
	private String appId;
	//业务类型id
	@Value("${server.netty.busiTypeId}")
	private String busiTypeId;
	@Value("${agdeptid}")
	private String agdeptid;
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
			String userid=map.get("id").toString();
			Object oldtoken=ehcacheUtil.get(userid);
			ehcacheUtil.remove(userid);
			if(oldtoken!=null){
				ehcacheUtil.remove(oldtoken.toString());
			}
			
			//存缓存里面
			ehcacheUtil.put(userid, ssoname);
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
	//个人注册
	@RequestMapping("/regedit")
	public Result regedit(String idcard,String name,String telephone,String address,String password,String passwordConfirm){
		try{
			dubboUserSiteService.register(idcard, name, telephone, address,password, passwordConfirm);
			return ResultUtils.success("注册成功", null);
		}catch(Exception e){
			return ResultUtils.error(e.getMessage());
		}
	}
	
	//律师注册
	@RequestMapping("/regeditLaw")
	public Result regeditLaw(String idcard,String name,String telephone,String password,String passwordConfirm,
			String lawcode,String unitname,String unitaddress,@RequestParam List<MultipartFile> idcardphotos,@RequestParam List<MultipartFile> lawphotos){
		try{
			if(idcardphotos==null||idcardphotos.size()==0){
				throw new RuntimeException("身份证照片为空");
			}
			if(lawphotos==null||lawphotos.size()==0){
				throw new RuntimeException("律师证照片为空");
			}
			
			String businessid = dubboUserSiteService.registerLaw(idcard, name, telephone, password, passwordConfirm,lawcode,unitname,unitaddress);
			idcardphotos.forEach(f->{
				try {
					byte[] bytes = f.getBytes();
					String fileName = f.getOriginalFilename();
					fileInfoBusiApiService.uploadMulti(bytes, "", fileName, businessid, businessid, "idcard");
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			lawphotos.forEach(f->{
				try {
					byte[] bytes = f.getBytes();
					String fileName = f.getOriginalFilename();
					fileInfoBusiApiService.uploadMulti(bytes, "", fileName, businessid, businessid, "law");
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			List<UserInfo> list = dubboUserService.getUserByDeptId(agdeptid);
			list.forEach(u->{
				String receiverid = u.getId();
				String receivername = u.getNickname();
				String nowtime = DateUtils.getCurrentTime();
				NettyUserBean bean=new NettyUserBean();
				bean.setAppid(appId);
				bean.setBusitypeid(busiTypeId);
			    bean.setTitle("律师注册");//推送标题
			    bean.setContent("内容：注册律师："+name+"注册时间："+nowtime);//推送内容
			    bean.setUserId(receiverid);//接收人id
			    bean.setUserName(receivername);//接收人姓名
			    PushResult result=dubboNettyService.sendToUser(bean);
			    System.out.println(result.getMsg());
			});
			return ResultUtils.success("提交成功", null);
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
