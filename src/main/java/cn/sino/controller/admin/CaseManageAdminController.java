package cn.sino.controller.admin;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.micro.model.UserInfo;
import com.micro.service.dubbo.user.DubboUserService;

import cn.sino.common.PageInfo;
import cn.sino.common.Result;
import cn.sino.common.ResultUtils;
import cn.sino.mvc.UserInfoAdmin;
import cn.sino.mvc.UserInfoUtils;
import cn.sino.service.dubbo.appointment.DubboCaseManageService;
import cn.sino.service.dubbo.appointment.DubboCaseTypeService;

@RestController
@RequestMapping("/app/casemanage")
public class CaseManageAdminController {
	
	@Reference(check=false)
	private DubboCaseTypeService dubboCaseTypeService;
	@Reference(check=false)
	private DubboCaseManageService dubboCaseManageService;
	
	@Reference(check=false)
	private DubboUserService dubboUserService;
	
	
	@Value("${appoint.jcgcode}")
	private String jcgcode;//检察官业务角色编号
	
	//获取案件类型列表
	@RequestMapping("/findTypeList")
	public Result findList(){
		try {
			List<Map<String, Object>> list = dubboCaseTypeService.findList();
			return ResultUtils.success("查询成功", list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	//添加案件类型
	@RequestMapping("/addType")
	public Result add(String name){
		try {
			dubboCaseTypeService.add(name);
			return ResultUtils.success("添加成功", null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	//删除
	@RequestMapping("/delete")
	public Result delete(String id){
		try {
			dubboCaseManageService.delete(id);
			return ResultUtils.success("删除成功", null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	
	//案件重做
	@RequestMapping("/heavy")
	public Result heavy(String id,String title,HttpServletRequest request){
		try {
			UserInfoAdmin userinfo = UserInfoUtils.getBeanAdmin(request);
			String userid = userinfo.getId();
			String username = userinfo.getNickname();
			dubboCaseManageService.heavy(id, title,userid,username);
			return ResultUtils.success("操作成功", null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	//创建案件任务
	@RequestMapping("/create")
	public Result create(String title,String duration,HttpServletRequest request){
		try {
			UserInfoAdmin userInfo = UserInfoUtils.getBeanAdmin(request);
			String userid = userInfo.getId();
			String username = userInfo.getNickname();
			String deptid = userInfo.getDeptid();
			String deptname = userInfo.getDeptname();
			if(title==null||"".equals(title)){
				throw new RuntimeException("title为空");
			}
			if(duration==null||"".equals(duration)){
				throw new RuntimeException("duration为空");
			}
			dubboCaseManageService.create(title, userid, username, deptid, deptname,duration);
			return ResultUtils.success("创建成功", null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	//转发
	@RequestMapping("/forward")
	public Result forward(String businessid,String dealuserid,String dealusername,HttpServletRequest request){
		try {
			UserInfoAdmin userinfo = UserInfoUtils.getBeanAdmin(request);
			String userid = userinfo.getId();
			String username = userinfo.getNickname();
			dubboCaseManageService.forward(businessid, dealuserid, dealusername,userid,username);
			return ResultUtils.success("转发成功", null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	//延时
	@RequestMapping("/delay")
	public Result delay(String id,String delaytime){
		try {
			dubboCaseManageService.delay(id, delaytime);
			return ResultUtils.success("延期成功", null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	
	//审查处理
	@RequestMapping("/check")
	public Result check(String businessid,HttpServletRequest request){
		try {
			UserInfoAdmin userInfo = UserInfoUtils.getBeanAdmin(request);
			String userid = userInfo.getId();
			String username = userInfo.getNickname();
			dubboCaseManageService.check(businessid, userid, username);
			return ResultUtils.success("处理成功", null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	//编辑案件
	@RequestMapping("/edit")
	public Result edit(String id,String title,String duration,HttpServletRequest request){
		try {
			UserInfoAdmin userinfo = UserInfoUtils.getBeanAdmin(request);
			String userid = userinfo.getId();
			String username = userinfo.getNickname();
			dubboCaseManageService.edit(id, title, duration, userid, username);
			return ResultUtils.success("编辑成功", null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	//回发
	@RequestMapping("/sendBack")
	public Result sendBack(String businessid,HttpServletRequest request){
		try {
			UserInfoAdmin userinfo= UserInfoUtils.getBeanAdmin(request);
			String userid = userinfo.getId();
			String username = userinfo.getNickname();
			dubboCaseManageService.sendBack(businessid, userid, username);
			return ResultUtils.success("发送成功", null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	//接收
	@RequestMapping("/receive")
	public Result receive(String businessid,HttpServletRequest request){
		try {
			UserInfoAdmin userInfo = UserInfoUtils.getBeanAdmin(request);
			String userid = userInfo.getId();
			String username = userInfo.getNickname();
			String whereabouts = request.getParameter("whereabouts");
			if(whereabouts==null||"".equals(whereabouts)){
				throw new RuntimeException("whereabouts为空");
			}
			dubboCaseManageService.receive(businessid, userid, username,whereabouts);
			return ResultUtils.success("接收成功", null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	//获取检察官
	@RequestMapping("/findInquisitor")
	public Result findInquisitor(HttpServletRequest request){
		try {
			List<UserInfo> list = dubboUserService.findUserByRoleCode(jcgcode);
			
			return ResultUtils.success("查询成功", list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
		
	}
	
	//未办
	@RequestMapping("/findNotDoneList")
	public PageInfo<Map<String,Object>> findNotDoneList(HttpServletRequest request,PageInfo<Map<String,Object>> pageInfo){
		try {
			UserInfoAdmin userinfo = UserInfoUtils.getBeanAdmin(request);
			String userid = userinfo.getId();
			String title = request.getParameter("title");
			String createdate = request.getParameter("createdate");
			pageInfo = dubboCaseManageService.findNotDoneList(userid,title,createdate,pageInfo.getPage(),pageInfo.getLimit());
			pageInfo.setCode(PageInfo.SUCCESS);
			pageInfo.setMsg("查询成功");
			return pageInfo;
		} catch (Exception e) {
			pageInfo.setCode(PageInfo.ERROR);
			pageInfo.setMsg(e.getMessage());
			return pageInfo;
		}
		
	}
	

	
	//已办
	@RequestMapping("/findDoneList")
	public PageInfo<Map<String,Object>> findDoneList(HttpServletRequest request,PageInfo<Map<String,Object>> pageInfo){
		try {
			UserInfoAdmin userinfo = UserInfoUtils.getBeanAdmin(request);
			String userid = userinfo.getId();
			String deptid = userinfo.getDeptid();
			String title = request.getParameter("title");
			String createdate = request.getParameter("createdate");
			pageInfo=dubboCaseManageService.findDoneList(userid, deptid,title,createdate,pageInfo.getPage(),pageInfo.getLimit());
			pageInfo.setCode(PageInfo.SUCCESS);
			pageInfo.setMsg("查询成功");
			return pageInfo;
		} catch (Exception e) {
			pageInfo.setCode(PageInfo.ERROR);
			pageInfo.setMsg(e.getMessage());
			return pageInfo;
		}
		
	}
	
	//详情
	@RequestMapping("/findDetails")
	public Result findDetails(String id){
		try {
			if(id==null||"".equals(id)){
				 throw new RuntimeException("id为空"); 
			}
			Map<String, Object> map = dubboCaseManageService.findDetails(id);
			return ResultUtils.success("查询成功",map);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	//修改提醒
	@RequestMapping("/updateremind")
	public Result updateremind(String id,String remind){
		try {
			if(id==null||"".equals(id)){
				 throw new RuntimeException("id为空"); 
			}
			
			dubboCaseManageService.updateremind(id, remind);
			return ResultUtils.success("修改成功",null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	

}
