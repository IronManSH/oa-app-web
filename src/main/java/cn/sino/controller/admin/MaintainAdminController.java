package cn.sino.controller.admin;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.dubbo.config.annotation.Reference;
import com.micro.model.GroupInfo;
import com.micro.model.UserInfo;
import com.micro.push.model.NettyUserBean;
import com.micro.push.model.PushResult;
import com.micro.push.service.DubboNettyService;
import com.micro.service.dubbo.user.DubboGroupService;
import com.micro.service.dubbo.user.DubboRolesService;
import com.micro.service.dubbo.user.DubboUserService;
import com.sinosoft.api.pojo.FileInfoBusiBean;
import com.sinosoft.api.service.FileInfoBusiApiService;

import cn.sino.common.DateUtils;
import cn.sino.common.Result;
import cn.sino.common.ResultUtils;
import cn.sino.mvc.UserInfoAdmin;
import cn.sino.mvc.UserInfoUtils;
import cn.sino.service.dubbo.maintain.DubboFaultTypeService;
import cn.sino.service.dubbo.maintain.DubboMaintainService;

@RestController
@RequestMapping("/app/maintain")
public class MaintainAdminController {
	@Reference(check=false)
	private DubboMaintainService dubboMaintainService;
	@Reference(check=false)
	private DubboUserService dubboUserService;
	@Reference(check=false)
	private DubboRolesService dubboRolesService;
	@Reference(check=false)
	private DubboGroupService dubboGroupService;
	@Reference(check=false)
	private FileInfoBusiApiService fileInfoBusiApiService;
	@Reference(check=false)
	private DubboFaultTypeService dubboFaultTypeService;
	@Reference(check=false,timeout=60000,retries=0)
	private DubboNettyService dubboNettyService;
	//维护维修系统id
	@Value("${ep.whwx.subId}")
	private String subId;
	
	//维修管理业务角色编码
	@Value("${ep.wxgl.code}")
	private String wxglcode;
	
	//维修人员业务角色编码
	@Value("${ep.wxry.code}")
	private String wxrycode;
	
	//梧州
	//系统id
	@Value("${server.netty.appId}")
	private String appId;
	
	//业务类型id
	@Value("${server.netty.busiTypeId}")
	private String busiTypeId;
	private String typename;
	@RequestMapping("/apply")
	public Result apply(HttpServletRequest request){
		try{
			UserInfoAdmin userInfo = UserInfoUtils.getBeanAdmin(request);
			String applyuserid = userInfo.getId();
			UserInfo user = dubboUserService.findUserInfo(applyuserid);
			String applyusername = userInfo.getNickname();
			String applytelephone = userInfo.getTelephone();
			String applydeptid = userInfo.getDeptid();
			String applydeptname = userInfo.getDeptname();
			String urgency = request.getParameter("urgency");//缓急（0.一般，1.紧急）
			String type = request.getParameter("serviceType");//服务类型（0.维修维护，1.技术保障）
			if(type==null||"".equals(type)){
				throw new RuntimeException("serviceType为空");
			}
			String userecordid="";
			String fixedassetname="";
			String officeAddress = request.getParameter("officeAddress");
			if(officeAddress==null||"".equals(officeAddress)){
				officeAddress=user.getOfficeAddress();
			}
			String breakdesc="";
			String bzdemand="";
			String bzaddress="";
			String bztime="";
			String applytitle ="";
			
			if(type.equals("0")){
				/**维修维修**/
				typename="维修维修";
				userecordid = request.getParameter("equipmentId");//故障设备id
				fixedassetname = request.getParameter("equipmentName");//故障设备名称
				breakdesc = request.getParameter("breakdesc");//故障描述
				applytitle =fixedassetname+breakdesc;
			}else if(type.equals("1")){
				/**技术保障**/
				typename="技术保障";
				applytitle = request.getParameter("title");
				bzdemand = request.getParameter("demand");//保障要求
				bztime = request.getParameter("guaranteeTime");//保障时间
				bzaddress = request.getParameter("bzaddress");
				if(bzaddress==null&&"".equals(bzaddress)){
					bzaddress=user.getOfficeAddress();
				}
			}
			dubboMaintainService.apply(applytitle, applyuserid, applyusername, applydeptid, applydeptname, applytelephone, 
					type, urgency, userecordid, fixedassetname, officeAddress, breakdesc, bzdemand, bzaddress, bztime);
			

			List<UserInfo> list = dubboUserService.findUserByRoleCode(wxglcode);
			//===============消息推送=========================//
			list.forEach(u->{
				//调用dubbo接口
				String receiverid = u.getId();
				String receivername = u.getNickname();
				NettyUserBean bean=new NettyUserBean();
				bean.setAppid(appId);
				bean.setBusitypeid(busiTypeId);
			    bean.setTitle(typename+"申请");//推送标题
			    bean.setContent("内容：申请人："+applyusername);//推送内容
			    bean.setUserId(receiverid);//接收人id
			    bean.setUserName(receivername);//接收人姓名
			    PushResult result=dubboNettyService.sendToUser(bean);
			    System.out.println(result.getMsg());
			});
			return ResultUtils.success("上报成功", null);
		}catch(Exception e){
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findMyasset")
	public Result findMyasset(HttpServletRequest request){
		try {
			UserInfoAdmin userInfo = UserInfoUtils.getBeanAdmin(request);
			String userId = userInfo.getId();
			List<Map<String, Object>> list = dubboMaintainService.findMyAssetList(userId);
			return ResultUtils.success("查询成功", list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
		
	}
	
	@RequestMapping("/findMyTaskList")
	public Result findMyTaskList(HttpServletRequest request){
		try {
			String userid = request.getParameter("userid");
			List<Map<String, Object>> list = dubboMaintainService.findMyTaskList(userid);
			return ResultUtils.success("查询成功", list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
		
	}
	
	
	@RequestMapping("/findMyApplyList")
	public Result findMyApplyList(HttpServletRequest request){
		try {
			UserInfoAdmin userInfo = UserInfoUtils.getBeanAdmin(request);
			String userid = userInfo.getId();
			String status = request.getParameter("status");
			String type = request.getParameter("type");
			List<Map<String, Object>> list = dubboMaintainService.findMyApplyList(userid, status, type);
			return ResultUtils.success("查询成功", list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
		
	}
	
	@RequestMapping("/findTaskList")
	public Result findTaskList(HttpServletRequest request){
		try {
			UserInfoAdmin userInfo = UserInfoUtils.getBeanAdmin(request);
			String userId = userInfo.getId();
			Map<String, Object> map = dubboRolesService.findRolesById(userId, subId);
			String msg="查询成功";
			List<Map<String,Object>>list=new ArrayList<Map<String,Object>>();
			String roles = map.get("rolesNO").toString();
			String status = request.getParameter("status");
			if(roles.equals(wxglcode)){
				list = dubboMaintainService.findTaskList("", status);
			}else if(roles.equals(wxrycode)){
				list = dubboMaintainService.findTaskList(userId, status);
			}else{
				msg="无权限";
			}
			return ResultUtils.success(msg, list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	
	@RequestMapping("/findApplyDetails")
	public Result findApplyDetails(HttpServletRequest request){
		try {
			String id = request.getParameter("id");
			String type = request.getParameter("type");
			Map<String, Object> map = dubboMaintainService.findApplyDetails(id, type);
			Object cluserid = map.get("cluserid");
			if(cluserid!=null){
				UserInfo user = dubboUserService.findUserInfo(cluserid.toString());
				String nickname = user.getNickname();
				map.put("clusername", nickname);
			}else{
				map.put("clusername", "");
				map.put("cluserid", "");
			}
			return ResultUtils.success("查询成功", map);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findTaskDetails")
	public Result findTaskDetails(HttpServletRequest request){
		try {
			String id = request.getParameter("id");
			String type = request.getParameter("type");
			Map<String, Object> map = dubboMaintainService.findTaskDetails(id, type);
			Object cluserid = map.get("cluserid");
			if(cluserid!=null){
				UserInfo user = dubboUserService.findUserInfo(cluserid.toString());
				String nickname = user.getNickname();
				map.put("clusername", nickname);
			}else{
				map.put("clusername", "");
				map.put("cluserid", "");
			}
			return ResultUtils.success("查询成功", map);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	
	
	//任务派发
	@RequestMapping("/sendTask")
	public Result sendTask(HttpServletRequest request, String id,String userid,String username){
		try {
			UserInfoAdmin userInfo = UserInfoUtils.getBeanAdmin(request);
			String senduserid = userInfo.getId();
			String sendusername = userInfo.getNickname();
			
			dubboMaintainService.sendTask(id, senduserid, sendusername, userid, username);
			//===============消息推送=========================//
			NettyUserBean bean=new NettyUserBean();
			bean.setAppid(appId);
			bean.setBusitypeid(busiTypeId);
		    bean.setTitle(typename+"任务指派");//推送标题
		    bean.setContent("内容："+typename+"任务处理");//推送内容
		    bean.setUserId(userid);//接收人id
		    bean.setUserName(username);//接收人姓名
		    PushResult result=dubboNettyService.sendToUser(bean);
		    System.out.println(result.getMsg());
			return ResultUtils.success("指派成功",null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	//状态修改
	@RequestMapping("/updateStatus")
	public Result updateStatus(HttpServletRequest request, String id,String status){
		try {
			UserInfoAdmin userinfo = UserInfoUtils.getBeanAdmin(request);
			String userid = userinfo.getId();
			String username = userinfo.getNickname();
			String repairtime="";
			String dealmethod="";
			String dealdesc="";
			String msg="";
			if(status.equals("3")){
				repairtime=request.getParameter("repairtime");
				dealmethod=request.getParameter("dealmethod");
				msg="特殊办理成功";
			}else if(status.equals("4")){
				dealdesc=request.getParameter("dealdesc");
				msg="办结成功";
				//===============消息推送=========================//
				String zpuserid = dubboMaintainService.findzpuserid(id);
				UserInfo user = dubboUserService.findUserInfo(zpuserid);
				String name = user.getNickname();
				String nowtime = DateUtils.getCurrentTime();
				NettyUserBean bean=new NettyUserBean();
				bean.setAppid(appId);
				bean.setBusitypeid(busiTypeId);
			    bean.setTitle(typename+"任务处理完成");//推送标题
			    bean.setContent("内容：处理人："+username+",处理时间："+nowtime);//推送内容
			    bean.setUserId(zpuserid);//接收人id
			    bean.setUserName(name);//接收人姓名
			    PushResult result=dubboNettyService.sendToUser(bean);
			    System.out.println(result.getMsg());
			}
			dubboMaintainService.updateStatus(id, status, userid, username, repairtime,dealmethod ,dealdesc);
			return ResultUtils.success(msg,null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findRepairUser")
	public Result findRepairUser(){
		try {
			List<GroupInfo> groupInfo = dubboGroupService.findList(subId);
			List<Map<String,Object>>list=new ArrayList<Map<String,Object>>();
			if(groupInfo!=null&&groupInfo.size()!=0){
				for(int i=0;i<groupInfo.size();i++){
					String type = groupInfo.get(i).getType();
					if(!type.equals("user")){
						groupInfo.remove(i);
						i--;
					}
				}
				
				for(int i=0;i<groupInfo.size();i++){
					Map<String,Object>map=new HashMap<String,Object>();
					String userId = groupInfo.get(i).getId();
					String name = groupInfo.get(i).getName();
					Integer num = dubboMaintainService.getTaskNum(userId);
					map.put("userid", userId);
					map.put("name", name);
					map.put("mun", num);
					list.add(list.size(),map);
				}
			}
			
			return ResultUtils.success("查询成功",list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findRoles")
	public Result findRoles(HttpServletRequest request){
		try {
			UserInfoAdmin userInfo = UserInfoUtils.getBeanAdmin(request);
			String userId = userInfo.getId();
			Map<String, Object> map = dubboRolesService.findRolesById(userId, subId);
			return ResultUtils.success("查询成功", map);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findApplyFlow")
	public Result findApplyFlow(HttpServletRequest request){
		try {
			String applyid = request.getParameter("id");
			List<Map<String, Object>> list = dubboMaintainService.findApplyFlow(applyid);
			return ResultUtils.success("查询成功", list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	
	@RequestMapping("/findByCategoryId")
	public Result findByCategoryId(HttpServletRequest request){
		try {
			String categoryId = request.getParameter("categoryId");
			List<Map<String, Object>> list = dubboFaultTypeService.findByCategoryId(categoryId);
			return ResultUtils.success("查询成功", list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/addFaultType")
	public Result addFaultType(HttpServletRequest request,String categoryId,String categoryName,String typeName){
		try {
			dubboFaultTypeService.add(categoryId, categoryName, typeName);
			return ResultUtils.success("添加成功", null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	
	//维修任务办结后上传图片做记录
	@RequestMapping("/photograph")
	public Result photograph(HttpServletRequest request,@RequestParam List<MultipartFile> photos,String status){
		try {
			UserInfoAdmin userInfo = UserInfoUtils.getBeanAdmin(request);
			String userid = userInfo.getId();
			String username = userInfo.getNickname();
			String businessid = request.getParameter("id");
			for (MultipartFile file : photos ){
				String fileName = file.getOriginalFilename();
				byte[] bytes = file.getBytes();
				fileInfoBusiApiService.uploadMulti(bytes, "", fileName, userid, businessid, "wxjl");
			}
			dubboMaintainService.addApplyFlow(businessid, businessid, userid, username, status, "照片上传");
			return ResultUtils.success("上传成功", null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	
	@RequestMapping("/findPhotos")
	public Result findPhotos(HttpServletRequest request){
		try {
			String businessid = request.getParameter("id");
			Result result = fileInfoBusiApiService.findByBusinessid(businessid);
			List<Map<String,Object>>photosList=new ArrayList<Map<String,Object>>();
			if(result.getCode()==0){
				List<FileInfoBusiBean> findPhotos=(List<FileInfoBusiBean>) result.getData();
				findPhotos.forEach(d->{
					String path = d.getPath();
					String id = d.getId();
					byte[] photoByte = fileInfoBusiApiService.downloadByPath(path);
					Map<String,Object>map=new HashMap<String,Object>();
					map.put("id", id);
					map.put("photoByte", photoByte);
					photosList.add(photosList.size(), map);
					
				});
			}
			return ResultUtils.success("查询成功", photosList);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
}
