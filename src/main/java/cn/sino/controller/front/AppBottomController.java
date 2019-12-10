package cn.sino.controller.front;

import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;

import cn.sino.common.Result;
import cn.sino.common.ResultUtils;
import cn.sino.service.dubbo.setting.DubboAppBottomService;

@RestController
@RequestMapping("/appfront/appBottom")
public class AppBottomController {
	
	@Reference(check=false)
	private DubboAppBottomService dubboAppBottomService;
	
	@RequestMapping("/findInfo")
	public Result findInfo(){
		try {
			Map<String, Object> map = dubboAppBottomService.findInfo();
			return ResultUtils.success("查询成功", map);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}

}
