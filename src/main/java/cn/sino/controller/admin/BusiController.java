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
import com.sinosoft.api.service.FileInfoBusiApiService;

import cn.sino.common.PageInfo;
import cn.sino.common.Result;
import cn.sino.common.ResultUtils;
import cn.sino.dubbo.DubboAffairsService;
import cn.sino.mvc.UserInfoAdmin;
import cn.sino.mvc.UserInfoUtils;

@RestController
@RequestMapping("/app/affairs")
public class BusiController {
	
	@Reference(check=false)
	private DubboAffairsService dubboAffairsService;
	@Reference(check=false)
	private FileInfoBusiApiService fileInfoBusiApiService;
	//事务申请
	@RequestMapping("/save")
	public Result save(HttpServletRequest request,List<MultipartFile>files){
		try {
			String affairsTypeId = request.getParameter("affairsTypeId");
			String startTime = request.getParameter("startTime");
			String endTime = request.getParameter("endTime");
			String content = request.getParameter("content");
			String writeName = request.getParameter("writeName");
			if(affairsTypeId==null||"".equals(affairsTypeId)){
				throw new RuntimeException("affairsTypeId为空");
			}
			if(startTime==null||"".equals(startTime)){
				throw new RuntimeException("startTime为空");
			}
			if(endTime==null||"".equals(endTime)){
				throw new RuntimeException("endTime为空");
			}
			if(content==null||"".equals(content)){
				throw new RuntimeException("content为空");
			}
			if(writeName==null||"".equals(writeName)){
				throw new RuntimeException("writeName为空");
			}
			if(files==null||files.size()==0){
				throw new RuntimeException("附件为空");
			}
			List<Map<String,Object>>list =new ArrayList<Map<String,Object>>();
			Map<String,Object>map=new HashMap<String,Object>();
			for(MultipartFile f:files){
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
			String telephone = userinfo.getTelephone();
			dubboAffairsService.save(affairsTypeId, startTime, endTime, content, writeName, jsonfiles, userid, username, telephone);
			return ResultUtils.success("提交成功", null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	//提交到下一个人办理
	@RequestMapping("/submitNext")
	public Result submitNext(HttpServletRequest request){
		try {
			String id = request.getParameter("id");
			String affairsHandleId = request.getParameter("affairsHandleId");
			String nextUserId = request.getParameter("nextUserId");
			if(id==null||"".equals(id)){
				throw new RuntimeException("id为空");
			}
			if(affairsHandleId==null||"".equals(affairsHandleId)){
				throw new RuntimeException("affairsHandleId为空");
			}
			if(nextUserId==null||"".equals(nextUserId)){
				throw new RuntimeException("nextUserId为空");
			}
			UserInfoAdmin userinfo = UserInfoUtils.getBeanAdmin(request);
			String userid = userinfo.getId();
			String username = userinfo.getNickname();
			String telephone = userinfo.getTelephone();
			dubboAffairsService.submitNext(id,affairsHandleId,nextUserId,userid,username,telephone);
			return ResultUtils.success("提交成功", null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	//待处理列表
	@RequestMapping("/findNotHandled")
	public PageInfo<Map<String,Object>> findAffairsList(PageInfo<Map<String,Object>> pi,HttpServletRequest request){
		try {
			Integer page = pi.getPage();
			Integer limit = pi.getLimit();
			UserInfoAdmin userinfo = UserInfoUtils.getBeanAdmin(request);
			String userid = userinfo.getId();
			pi = dubboAffairsService.findNotHandled(page, limit, userid);
			pi.setCode(PageInfo.SUCCESS);
			pi.setMsg("查询成功");
			return pi;
		} catch (Exception e) {
			pi.setCode(PageInfo.ERROR);
			pi.setMsg(e.getMessage());
			return pi;
		}
	}
	//已处理列表
	@RequestMapping("/findAlreadyHandled")
	public PageInfo<Map<String,Object>> findAffairsListAll(PageInfo<Map<String,Object>> pi,HttpServletRequest request){
		try {
			Integer page = pi.getPage();
			Integer limit = pi.getLimit();
			UserInfoAdmin userinfo = UserInfoUtils.getBeanAdmin(request);
			String userid = userinfo.getId();
			pi = dubboAffairsService.findAlreadyHandled(page, limit, userid);
			pi.setCode(PageInfo.SUCCESS);
			pi.setMsg("查询成功");
			return pi;
		} catch (Exception e) {
			pi.setCode(PageInfo.ERROR);
			pi.setMsg(e.getMessage());
			return pi;
		}
	}
	//详情
	@RequestMapping("/findAffairsInfo")
	public Result findAffairsInfo(String affairsHandleId){
		try {
			if(affairsHandleId==null||"".equals(affairsHandleId)){
				throw new RuntimeException("affairsHandleId为空");
			}
			Map<String, Object> map = dubboAffairsService.findAffairsInfo(affairsHandleId);
			return ResultUtils.success("查询成功", map);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	//审批
	@RequestMapping("/approve")
	public Result approve(HttpServletRequest request,String id ,Integer approveStatus){
		try {
			if(id==null||"".equals(id)){
				throw new RuntimeException("id为空");
			}
			String affairsHandleId = request.getParameter("affairsHandleId");
			String approveContent = request.getParameter("approveContent");
			if(affairsHandleId==null||"".equals(affairsHandleId)){
				throw new RuntimeException("affairsHandleId为空");
			}
			if(approveContent==null||"".equals(approveContent)){
				throw new RuntimeException("approveContent为空");
			}
			if(approveStatus==null||"".equals(approveStatus)){
				throw new RuntimeException("isApprove为空");
			}
			
			UserInfoAdmin userinfo = UserInfoUtils.getBeanAdmin(request);
			String userid = userinfo.getId();
			String username = userinfo.getNickname();
			String telephone = userinfo.getTelephone();
			dubboAffairsService.approve(id, affairsHandleId, approveContent, approveStatus, userid, username, telephone);
			return ResultUtils.success("审批成功", null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	//办结事务
	@RequestMapping("/endAffairs")
	public Result endAffairs(String id,String affairsHandleId,HttpServletRequest request){
		try {
			if(id==null||"".equals(id)){
				throw new RuntimeException("id为空");
			}
			if(affairsHandleId==null||"".equals(affairsHandleId)){
				throw new RuntimeException("affairsHandleId为空");
			}
			UserInfoAdmin userinfo = UserInfoUtils.getBeanAdmin(request);
			String userid = userinfo.getId();
			String username = userinfo.getNickname();
			String telephone = userinfo.getTelephone();
			dubboAffairsService.endAffairs(id,affairsHandleId,userid,username,telephone);
			return ResultUtils.success("办结成功", null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	//办理记录
	@RequestMapping("/handleRecord")
	public Result handleRecord(String id,HttpServletRequest request){
		try {
			if(id==null||"".equals(id)){
				throw new RuntimeException("id为空");
			}
			List<Map<String, Object>> list = dubboAffairsService.handleRecord(id);
			return ResultUtils.success("查询成功", list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	//审批记录
	@RequestMapping("/approveRecord")
	public Result approveRecord(String id,HttpServletRequest request){
		try {
			if(id==null||"".equals(id)){
				throw new RuntimeException("id为空");
			}
			List<Map<String, Object>> list = dubboAffairsService.approveRecord(id);
			return ResultUtils.success("查询成功", list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	//查询全部事务类型分页
	@RequestMapping("/findAffairsTypePage")
	public PageInfo<Map<String,Object>> findAffairsTypePage(HttpServletRequest request,PageInfo<Map<String,Object>> pi){
		try {
			String affairsCode = request.getParameter("affairsCode");
			String affairsName = request.getParameter("affairsName");
			pi=dubboAffairsService.findAffairsTypePage(affairsCode,affairsName,pi.getPage(),pi.getLimit());
			pi.setCode(PageInfo.SUCCESS);
			pi.setMsg("查询成功");
			return pi;
		} catch (Exception e) {
			pi.setCode(PageInfo.ERROR);
			pi.setMsg(e.getMessage());
			return pi;
		}
	}
	
	//查询全部事务类型
	@RequestMapping("/findAffairsTypeAll")
	public Result findAffairsTypeAll(){
		try {
			List<Map<String, Object>> list = dubboAffairsService.findAffairsTypeAll();
			return ResultUtils.success("查询成功", list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	//查询事务类型详情
	@RequestMapping("/findAffairsTypeInfo")
	public Result findAffairsTypeInfo(String id){
		try {
			Map<String, Object> map = dubboAffairsService.findAffairsTypeInfo(id);
			return ResultUtils.success("查询成功", map);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	//查询全部用户
	@RequestMapping("/findUserAll")
	public Result findUserAll(){
		try {
			List<Map<String, Object>> list = dubboAffairsService.findUserAll();
			return ResultUtils.success("查询成功", list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	//查询该系统所拥有的群组及用户
	@RequestMapping("/findGroup")
	public Result findGroup(){
		try {
			List<Map<String, Object>> list = dubboAffairsService.findGroup();
			return ResultUtils.success("查询成功", list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
}
