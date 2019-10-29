package cn.sino.controller.admin;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;

import cn.sino.common.PageInfo;
import cn.sino.common.Result;
import cn.sino.common.ResultUtils;
import cn.sino.mvc.UserInfoAdmin;
import cn.sino.mvc.UserInfoUtils;
import cn.sino.service.dubbo.notice.DubboNoticeService;

/**
 * 待签收
 * @author 郑伟业
 * 2018年11月5日
 */
@RestController
@RequestMapping("/app/notice/waitToSign")
public class NoticeAdminController {
	@Reference(check=false)
	private DubboNoticeService dubboNoticeService;
	
	/**
	 * 待签收/已签收
	 * @author 郑伟业
	 * 2018年11月11日
	 * @param pageInfo
	 * @param request
	 * @param type 0未签收，1已签收
	 * @return
	 */
	@RequestMapping("/findList")
	public PageInfo<Map<String,Object>> findList(PageInfo<Map<String,Object>> pageInfo,HttpServletRequest request,String type){
		try{
			
			UserInfoAdmin userInfo=UserInfoUtils.getBeanAdmin(request);
			String title = request.getParameter("title");
			String deptId=userInfo.getDeptid();
			pageInfo=dubboNoticeService.findList(pageInfo,deptId,type,title);
			
			pageInfo.setCode(0);
			pageInfo.setMsg("查询成功");
			
			return pageInfo;
		}catch(Exception e){
			pageInfo.setCode(1);
			pageInfo.setMsg("查询失败");
			
			return pageInfo;
		}
	}
	
	
	/**
	 * 详情
	 * @author 钟业剑
	 * 2018年11月6日 上午10:15:25
	 * @param id
	 * @param sendid（noticenew_send表主键id）
	 */
	@RequestMapping("/findDetail")
	public Result findDetail(String id,String sendid){
		try{
			Map<String,Object> map=dubboNoticeService.findDetail(id,sendid);
			return ResultUtils.success("查询成功",map);
		}catch(Exception e){
			return ResultUtils.error("查询失败");
		}
	}
}