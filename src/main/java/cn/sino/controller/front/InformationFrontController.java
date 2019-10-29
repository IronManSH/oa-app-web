package cn.sino.controller.front;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSONObject;

import cn.sino.common.PageInfo;
import cn.sino.common.Result;
import cn.sino.common.ResultUtils;
import cn.sino.service.dubbo.setting.DubboInformationService;

@RestController
@RequestMapping("/appfront/information")//检务资讯
public class InformationFrontController {
	@Reference(check=false)
	private DubboInformationService dubboInformationService;
	
	@Value("${infourljson}")
	private String infourljson;
	
	@RequestMapping("/findList")
	public Result findList(){
		try{
			List<Map> parseArray = JSONObject.parseArray(infourljson,Map.class);
			String url = parseArray.get(0).get("url").toString();
			PageInfo<Map<String, Object>> pi = dubboInformationService.findList(1,3);
			pi.getRows().forEach(f->{
				String id = f.get("id").toString();
				f.put("url", url+id);
				
			});
			List<Map<String, Object>> list = pi.getRows();
			return ResultUtils.success("查询成功", list);
		}catch(Exception e){
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findMoreList")
	public Result findMoreList(){
		try{
			List<Map<String, Object>> list = dubboInformationService.findMoreList();
			List<Map> parseArray = JSONObject.parseArray(infourljson,Map.class);
			String url = parseArray.get(0).get("url").toString();
			list.forEach(f->{
				String id = f.get("id").toString();
				f.put("url", url+id);
			});
			return ResultUtils.success("查询成功", list);
		}catch(Exception e){
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findDetails")
	public Result findDetails(String id){
		try{
			Map<String, Object> map = dubboInformationService.findDetails(id);
			return ResultUtils.success("查询成功", map);
		}catch(Exception e){
			return ResultUtils.error(e.getMessage());
		}
	}
	
	
}
