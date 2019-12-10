package cn.sino.controller.admin;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;

import cn.sino.common.Result;
import cn.sino.common.ResultUtils;
import cn.sino.service.dubbo.appointment.DubboFlowService;

@RestController
@RequestMapping("/app/flow")
public class FlowAdminController {
	
	@Reference(check=false)
	private DubboFlowService dubboFlowService;
	
	@RequestMapping("/findList")
	public Result findList(String businessid){
		try {
			List<Map<String, Object>> list = dubboFlowService.findByBuisnessid(businessid);
			return ResultUtils.success("查询成功", list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}

}
