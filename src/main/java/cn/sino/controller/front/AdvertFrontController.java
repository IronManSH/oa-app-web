package cn.sino.controller.front;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;

import cn.sino.common.Result;
import cn.sino.common.ResultUtils;
import cn.sino.service.dubbo.setting.DubboAdvertService;

@RestController
@RequestMapping("/appfront/advert")//广告轮播图
public class AdvertFrontController {
	@Reference(check=false)
	private DubboAdvertService dubboAdvertService;
	
	
	
	@RequestMapping("/findList")
	public Result findList(){
		try{
			List<Map<String, Object>> list = dubboAdvertService.findList();
			return ResultUtils.success("查询成功", list);
		}catch(Exception e){
			return ResultUtils.error(e.getMessage());
		}
	}
	
}
