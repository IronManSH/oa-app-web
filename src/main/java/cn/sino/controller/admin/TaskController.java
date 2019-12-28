package cn.sino.controller.admin;

import java.util.List;
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
import cn.sino.service.dubbo.task.DubboTaskService;

@RestController
@RequestMapping("/app/task")
public class TaskController {
	@Reference(check=false)
	private DubboTaskService dubboTaskService;
	
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
	
	@RequestMapping("/findList")
	public Result findList(HttpServletRequest request){
		try {
			String page = request.getParameter("page");
			
			String limit = request.getParameter("limit");
			if(page==null||page.equals("")){
				page="1";
			}
			if(limit==null||limit.equals("")){
				limit="10";
			}
			
			String title = request.getParameter("title");
			String categoryId = request.getParameter("categoryId");
			PageInfo<Map<String, Object>> pi = dubboTaskService.findList(title, categoryId,Integer.parseInt(page), Integer.parseInt(limit));
			return ResultUtils.success("查询成功", pi);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	

}
