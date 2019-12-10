package cn.sino.controller.front;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;

import cn.sino.common.Result;
import cn.sino.common.ResultUtils;
import cn.sino.service.dubbo.appointment.DubboFlowService;

@RestController
@RequestMapping("/appfront/flow")
public class FlowFrontController {
	
	@Reference(check=false)
	private DubboFlowService dubboFlowService;
	
	@RequestMapping("/findList")
	public Result findList(String buisnessid){
		try {
			
			List<Map<String, Object>> list = dubboFlowService.findByBuisnessid(buisnessid);
			return ResultUtils.success("查询成功", list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}

}
