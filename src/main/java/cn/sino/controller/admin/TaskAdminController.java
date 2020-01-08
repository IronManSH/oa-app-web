package cn.sino.controller.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSONObject;

import cn.sino.common.PageInfo;
import cn.sino.common.Result;
import cn.sino.common.ResultUtils;
import cn.sino.mvc.UserInfoAdmin;
import cn.sino.mvc.UserInfoUtils;
import cn.sino.service.dubbo.task.DubboTaskService;
//机关党建
@RestController
@RequestMapping("/app/task")
public class TaskAdminController {
	@Reference(check=false)
	private DubboTaskService dubboTaskService;
	
	//查询用户能看到的栏目
	@RequestMapping("/findTree")
	public Result findTree(HttpServletRequest request){
		try {
			UserInfoAdmin userInfo = UserInfoUtils.getBeanAdmin(request);
			String userId = userInfo.getId();
			List<Map<String, Object>> list = dubboTaskService.findTree(userId);
			return ResultUtils.success("查询成功", list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	//查询用户能看到的文章
	@RequestMapping("/findList")
	public PageInfo<Map<String, Object>> findList(HttpServletRequest request,PageInfo<Map<String, Object>> pi){
		try {
			String title = request.getParameter("title");
			String categoryId = request.getParameter("categoryId");
			pi = dubboTaskService.findList(title, categoryId,pi.getPage(), pi.getLimit());
			pi.setCode(PageInfo.SUCCESS);
			pi.setMsg("查询成功");
			return pi;
		} catch (Exception e) {
			pi.setCode(PageInfo.ERROR);
			pi.setMsg(e.getMessage());
			return pi;
		}
	}
	/**
	 * 基本信息--发送的信息
	 * @param id主键
	 * @return
	 */
	@RequestMapping("/sendInfo")
	public Result sendInfo(String id){
		try {
			if(id==null||"".equals(id)){
				throw new RuntimeException("id为空");
			}
			Map<String, Object> map = dubboTaskService.sendInfo(id);
			return ResultUtils.success("查询成功", map);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	/**
	 * 基本信息--签收的信息
	 * @param receiveid
	 * @return
	 */
	@RequestMapping("/signInfo")
	public Result signInfo(String receiveid){
		try {
			if(receiveid==null||"".equals(receiveid)){
				throw new RuntimeException("receiveid为空");
			}
			Map<String, Object> map = dubboTaskService.signInfo(receiveid);
			return ResultUtils.success("查询成功", map);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	/**
	 * 全部任务--列表
	 * @param pi
	 * @param title标题
	 * @param type任务类型
	 * @param request
	 * @return
	 */
	@RequestMapping("/findTaskPage")
	public PageInfo<Map<String,Object>> findTaskPage(PageInfo<Map<String,Object>> pi,String title,
			String type,HttpServletRequest request){
		try {
			if(pi.getPage()==null||"".equals(pi.getPage())){
				throw new RuntimeException("page为空");
			}
			if(pi.getLimit()==null||"".equals(pi.getLimit())){
				throw new RuntimeException("limit为空");
			}
			
			UserInfoAdmin userinfo = UserInfoUtils.getBeanAdmin(request);
			String userid = userinfo.getId();
			pi=dubboTaskService.findTaskPage(userid,type,title,pi.getPage(),pi.getLimit());
			pi.setCode(PageInfo.SUCCESS);
			pi.setMsg("查询成功");
			return pi;
		} catch (Exception e) {
			pi.setCode(PageInfo.ERROR);
			pi.setMsg(e.getMessage());
			return pi;
		}
	}
	
	/**
	 * 全部任务--签收情况（有那些部门需要签收的）
	 * @param taskid sendInfo接口的id
	 * @return
	 */
	@RequestMapping("/findSignState")
	public Result findSignState(String taskid){
		try {
			if(taskid==null||"".equals(taskid)){
				throw new RuntimeException("taskid为空");
			}
			List<Map<String, Object>> list = dubboTaskService.findSignState(taskid);
			return ResultUtils.success("查询成功", list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	/**
	 * 全部任务--反馈情况（查看各个部门反馈时间轴）
	 * @param taskid  sendInfo接口的id
	 * @return
	 */
	@RequestMapping("/findTimeNode")
	public Result findTimeNode(String taskid){
		try {
			if(taskid==null||"".equals(taskid)){
				throw new RuntimeException("taskid为空");
			}
			List<Map<String, Object>> list = dubboTaskService.findTimeNode(taskid);
			return ResultUtils.success("查询成功", list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	/**
	 * 全部任务--反馈详情（查看某个单位、某个时间节点的反馈情况）
	 * @param fkid反馈主键id
	 * @return
	 */
	@RequestMapping("/findFk")
	public Result findFk(String fkid){
		try {
			if(fkid==null||"".equals(fkid)){
				throw new RuntimeException("fkid为空");
			}
			Map<String, Object> map = dubboTaskService.findFk(fkid);
			return ResultUtils.success("查询成功", map);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	/**
	 * 全部任务--列表
	 * @param pi
	 * @param title标题
	 * @param status状态 ：空则全部，0未签收，1已签收
	 * @param request
	 * @return
	 */
	@RequestMapping("/findDeptTaskPage")
	public PageInfo<Map<String,Object>> findDeptTaskPage(PageInfo<Map<String,Object>> pi,String title,
			String status,HttpServletRequest request){
		try {
			if(pi.getPage()==null||"".equals(pi.getPage())){
				throw new RuntimeException("page为空");
			}
			if(pi.getLimit()==null||"".equals(pi.getLimit())){
				throw new RuntimeException("limit为空");
			}
			
			UserInfoAdmin userinfo = UserInfoUtils.getBeanAdmin(request);
			String userid = userinfo.getId();
			String deptid = userinfo.getDeptid();
			pi=dubboTaskService.findDeptTaskPage(deptid,userid,status,title,pi.getPage(),pi.getLimit());
			pi.setCode(PageInfo.SUCCESS);
			pi.setMsg("查询成功");
			return pi;
		} catch (Exception e) {
			pi.setCode(PageInfo.ERROR);
			pi.setMsg(e.getMessage());
			return pi;
		}
	}
	
	/**
	 * 支部任务--签收
	 * @param id findDeptTaskPage接口的receiveid
	 * @param request
	 * @return
	 */
	@RequestMapping("/deptSign")
	public Result deptSign(String id,HttpServletRequest request){
		try {
			if(id==null||"".equals(id)){
				throw new RuntimeException("id为空");
			}
			UserInfoAdmin userinfo = UserInfoUtils.getBeanAdmin(request);
			String signuserid = userinfo.getId();
			String signusername = userinfo.getNickname();
			dubboTaskService.deptSign(id,signuserid,signusername);
			return ResultUtils.success("签收成功", null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	/**
	 * 支部任务--转派
	 * @param taskid findDeptTaskPage接口的主键id
	 * @param receiveid findDeptTaskPage接口的receiveid
	 * @param cbusername 承办人
	 * @param json 选择的人员的json
	 * @param request
	 * @return
	 */
	@RequestMapping("/zp")
	public Result zp(String taskid,String receiveid,String cbusername,String json,HttpServletRequest request){
		try {
			if(taskid==null||"".equals(taskid)){
				throw new RuntimeException("taskid为空");
			}
			if(receiveid==null||"".equals(receiveid)){
				throw new RuntimeException("receiveid为空");
			}
			if(cbusername==null||"".equals(cbusername)){
				throw new RuntimeException("承办人为空");
			}
			if(json==null||"".equals(json)){
				throw new RuntimeException("json为空");
			}
			UserInfoAdmin userinfo = UserInfoUtils.getBeanAdmin(request);
			String zpuserid = userinfo.getId();
			String zpusername = userinfo.getNickname();
			String zptelephone = userinfo.getTelephone();
			dubboTaskService.zp(taskid,receiveid,cbusername,json,zpuserid,zpusername,zptelephone);
			return ResultUtils.success("转派成功", null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	/** 
	 * 支部任务--待我反馈
	 * @param taskid findDeptTaskPage接口的主键id
	 * @param receiveid findDeptTaskPage接口的receiveid
	 * @return
	 */
	@RequestMapping("/findTimenodeByDept")
	public Result findTimenodeByDept(String taskid,String receiveid){
		try {
			if(taskid==null||"".equals(taskid)){
				throw new RuntimeException("taskid为空");
			}
			if(receiveid==null||"".equals(receiveid)){
				throw new RuntimeException("receiveid为空");
			}
			List<Map<String, Object>> list = dubboTaskService.findTimenodeByDept(taskid,receiveid);
			return ResultUtils.success("查询成功", list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	/**
	 * 支部任务--反馈
	 * @param taskid findDeptTaskPage接口的主键id
	 * @param receivedeptid findDeptTaskPage接口的receiveid
	 * @param nodeid findTimenodeByDept接口的nodeid
	 * @param fkcontent 反馈内容
	 * @param files 附件集合
	 * @param request
	 * @return
	 */
	@RequestMapping("/deptFk")
	public Result deptFk(String taskid,String receivedeptid,String nodeid,String fkcontent,List<MultipartFile> files,
			HttpServletRequest request){
		try {
			if(taskid==null||"".equals(taskid)){
				throw new RuntimeException("taskid为空");
			}
			if(receivedeptid==null||"".equals(receivedeptid)){
				throw new RuntimeException("receivedeptid为空");
			}
			if(nodeid==null||"".equals(nodeid)){
				throw new RuntimeException("nodeid为空");
			}
			if(fkcontent==null||"".equals(fkcontent)){
				throw new RuntimeException("反馈内容为空");
			}
			if(files==null||files.size()==0){
				throw new RuntimeException("附件为空");
			}
			UserInfoAdmin userinfo = UserInfoUtils.getBeanAdmin(request);
			String userid = userinfo.getId();
			String username = userinfo.getNickname();
			
			List<Map<String,Object>>list =new ArrayList<Map<String,Object>>();
			for(MultipartFile f:files){
				Map<String,Object>map=new HashMap<String,Object>();
				String filename = f.getOriginalFilename();
				byte[] bytes = f.getBytes();
				map.put("fileName", filename);
				map.put("fileByte", Base64.encodeBase64String(bytes));
				list.add(list.size(),map);
			}
			String jsonfiles = JSONObject.toJSONString(list);
			
			dubboTaskService.deptFk(taskid,receivedeptid,nodeid,fkcontent,jsonfiles,userid,username);
			return ResultUtils.success("反馈成功", null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	/**
	 * 支部任务--成员签收情况
	 * @param taskid findDeptTaskPage接口的主键id
	 * @param receivedeptid findDeptTaskPage接口的receiveid
	 * @return
	 */
	@RequestMapping("/deptSignSituation")
	public Result deptSignSituation(String taskid,String receivedeptid){
		try {
			if(taskid==null||"".equals(taskid)){
				throw new RuntimeException("taskid为空");
			}
			if(receivedeptid==null||"".equals(receivedeptid)){
				throw new RuntimeException("receivedeptid为空");
			}
			List<Map<String, Object>> list = dubboTaskService.deptSignSituation(taskid,receivedeptid);
			return ResultUtils.success("查询成功", list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	/**
	 * 支部任务--成员反馈情况
	 * @param taskid findDeptTaskPage接口的主键id
	 * @param receivedeptid findDeptTaskPage接口的receiveid
	 * @return
	 */
	@RequestMapping("/deptFeedbackSituation")
	public Result deptFeedbackSituation(String taskid,String receivedeptid){
		try {
			if(taskid==null||"".equals(taskid)){
				throw new RuntimeException("taskid为空");
			}
			if(receivedeptid==null||"".equals(receivedeptid)){
				throw new RuntimeException("receiveid为空");
			}
			List<Map<String, Object>> list = dubboTaskService.deptFeedbackSituation(taskid,receivedeptid);
			return ResultUtils.success("查询成功", list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	
	/**
	 * 支部任务--成员反馈详情
	 * @param fkid 反馈id
	 * @return
	 */
	@RequestMapping("/deptFeedbackInfo")
	public Result deptFeedbackInfo(String fkid){
		try {
			if(fkid==null||"".equals(fkid)){
				throw new RuntimeException("fkid为空");
			}
			Map<String, Object> map = dubboTaskService.deptFeedbackInfo(fkid);
			return ResultUtils.success("查询成功", map);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	/**
	 * 个人任务--列表
	 * @param pi
	 * @param title标题
	 * @param request
	 * @return
	 */
	@RequestMapping("/findUserTaskPage")
	public PageInfo<Map<String,Object>> findUserTaskPage(PageInfo<Map<String,Object>> pi,String title,
			HttpServletRequest request){
		try {
			if(pi.getPage()==null||"".equals(pi.getPage())){
				throw new RuntimeException("page为空");
			}
			if(pi.getLimit()==null||"".equals(pi.getLimit())){
				throw new RuntimeException("limit为空");
			}
			UserInfoAdmin userinfo = UserInfoUtils.getBeanAdmin(request);
			String userid = userinfo.getId();
			pi=dubboTaskService.findUserTaskPage(userid,title,pi.getPage(),pi.getLimit());
			pi.setCode(PageInfo.SUCCESS);
			pi.setMsg("查询成功");
			return pi;
		} catch (Exception e) {
			pi.setCode(PageInfo.ERROR);
			pi.setMsg(e.getMessage());
			return pi;
		}
	}
	
	
	/**
	 * 个人任务--反馈情况
	 * @param taskid findUserTaskPage接口的taskid
	 * @param receiveuserid findUserTaskPage接口的receiveuserid
	 * @return
	 */
	@RequestMapping("/userFeedbackSituation")
	public Result userFeedbackSituation(String taskid,String receiveuserid){
		try {
			if(taskid==null||"".equals(taskid)){
				throw new RuntimeException("taskid为空");
			}
			if(receiveuserid==null||"".equals(receiveuserid)){
				throw new RuntimeException("receiveuserid为空");
			}
			List<Map<String, Object>> list = dubboTaskService.userFeedbackSituation(taskid,receiveuserid);
			return ResultUtils.success("查询成功", list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	
	/**
	 * 个人任务--反馈
	 * @param taskid  findUserTaskPage接口的taskid
	 * @param receiveuserid findUserTaskPage接口的receiveuserid
	 * @param receivedeptid findUserTaskPage接口的receivedeptid
	 * @param nodeid userFeedbackSituation接口的nodeid
	 * @param fkcontent 反馈内容
	 * @param files 附件集合
	 * @param request
	 * @return
	 */
	@RequestMapping("/userFk")
	public Result userFk(String taskid,String receiveuserid,String receivedeptid,String nodeid,String fkcontent,
			List<MultipartFile>files,HttpServletRequest request){
		try {
			if(taskid==null||"".equals(taskid)){
				throw new RuntimeException("taskid为空");
			}
			if(receiveuserid==null||"".equals(receiveuserid)){
				throw new RuntimeException("receiveuserid为空");
			}
			if(receivedeptid==null||"".equals(receivedeptid)){
				throw new RuntimeException("receivedeptid为空");
			}
			if(nodeid==null||"".equals(nodeid)){
				throw new RuntimeException("receiveuserid为空");
			}
			if(fkcontent==null||"".equals(fkcontent)){
				throw new RuntimeException("receiveuserid为空");
			}
			if(files==null||files.size()==0){
				throw new RuntimeException("附件为空");
			}
			List<Map<String,Object>>list =new ArrayList<Map<String,Object>>();
			for(MultipartFile f:files){
				Map<String,Object>map=new HashMap<String,Object>();
				String filename = f.getOriginalFilename();
				byte[] bytes = f.getBytes();
				map.put("fileName", filename);
				map.put("fileByte", Base64.encodeBase64String(bytes));
				list.add(list.size(),map);
			}
			String jsonfiles = JSONObject.toJSONString(list);
			UserInfoAdmin userinfo = UserInfoUtils.getBeanAdmin(request);
			String userid = userinfo.getId();
			String username = userinfo.getNickname();
			dubboTaskService.userFk(taskid,receivedeptid,receiveuserid,nodeid,fkcontent,jsonfiles,userid,username);
			return ResultUtils.success("反馈成功", null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	/**
	 * 个人任务--反馈详情 
	 * @param taskid findUserTaskPage接口的taskid
	 * @param receivedeptid findUserTaskPage接口的receivedeptid
	 * @param receiveuserid findUserTaskPage接口的receiveuserid
	 * @param nodeid userFeedbackSituation接口的nodeid
	 * @return
	 */
	@RequestMapping("/userFkInfo")
	public Result userFkInfo(String taskid,String receivedeptid,String receiveuserid,String nodeid){
		try {
			if(taskid==null||"".equals(taskid)){
				throw new RuntimeException("taskid为空");
			}
			if(receiveuserid==null||"".equals(receiveuserid)){
				throw new RuntimeException("receiveuserid为空");
			}
			if(receivedeptid==null||"".equals(receivedeptid)){
				throw new RuntimeException("receivedeptid为空");
			}
			if(nodeid==null||"".equals(nodeid)){
				throw new RuntimeException("nodeid为空");
			}
			Map<String, Object> map = dubboTaskService.userFkInfo(taskid,receivedeptid,receiveuserid,nodeid);
			return ResultUtils.success("查询成功", map);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	/**
	 * 个人任务--签收
	 * @param receiveuserid findUserTaskPage接口的receiveuserid
	 * @return
	 */
	@RequestMapping("/userSign")
	public Result userSign(String receiveuserid){
		try {
			if(receiveuserid==null||"".equals(receiveuserid)){
				throw new RuntimeException("receiveuserid为空");
			}
			dubboTaskService.userSign(receiveuserid);
			return ResultUtils.success("签收成功", null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	
	/**
	 * 学习园地--列表
	 * @param pi
	 * @param title
	 * @param categoryid
	 * @param request
	 * @return
	 */
	@RequestMapping("/findStudyPage")
	public PageInfo<Map<String,Object>> findStudyPage(PageInfo<Map<String,Object>> pi,String title,String categoryid,
			HttpServletRequest request){
		try {
			if(pi.getPage()==null||"".equals(pi.getPage())){
				throw new RuntimeException("page为空");
			}
			if(pi.getLimit()==null||"".equals(pi.getLimit())){
				throw new RuntimeException("limit为空");
			}
			UserInfoAdmin userinfo = UserInfoUtils.getBeanAdmin(request);
			String userid = userinfo.getId();
			pi=dubboTaskService.findStudyPage(userid,title,pi.getPage(),pi.getLimit());
			pi.setCode(PageInfo.SUCCESS);
			pi.setMsg("查询成功");
			return pi;
		} catch (Exception e) {
			pi.setCode(PageInfo.ERROR);
			pi.setMsg(e.getMessage());
			return pi;
		}
	}
}
