package cn.sino.controller.admin;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;

import cn.sino.common.DateUtils;
import cn.sino.common.PageInfo;
import cn.sino.common.Result;
import cn.sino.common.ResultUtils;
import cn.sino.mvc.UserInfoAdmin;
import cn.sino.mvc.UserInfoUtils;
import cn.sino.service.dubbo.transaction.DubboTransactionService;

@RestController
@RequestMapping("/app/buis")
public class BusiController {
	
	@Reference(check=false)
	private DubboTransactionService dubboTransactionService;
	
	@RequestMapping("/findMyChekList")
	public Result findMyChekList(HttpServletRequest request ,PageInfo<Map<String ,Object>>pageInfo){
		try {
			String today = DateUtils.getToday();
			UserInfoAdmin userInfo = UserInfoUtils.getBeanAdmin(request);
			String userId = userInfo.getId();
			PageInfo<Map<String, Object>> pi = dubboTransactionService.findCheckList(pageInfo, userId, today);
			return ResultUtils.success("查询成功", pi);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findMyApplyList")
	public Result findMyApplyList(HttpServletRequest request,PageInfo<Map<String,Object>>pageInfo){
		try {
			UserInfoAdmin userInfo = UserInfoUtils.getBeanAdmin(request);
			String userId = userInfo.getId();
			PageInfo<Map<String, Object>> pi = dubboTransactionService.findApplyList(pageInfo, userId);
			return ResultUtils.success("查询成功", pi);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
		
	}
}
