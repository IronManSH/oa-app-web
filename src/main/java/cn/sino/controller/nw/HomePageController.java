package cn.sino.controller.nw;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.client.RestTemplate;

import cn.sino.app.pojo.TaskBasePojo;
import cn.sino.app.service.UrlContant;
import cn.sino.common.Result;
import cn.sino.common.ResultUtils;

public class HomePageController {
	
	public Result findList(HttpServletRequest request){
		try{
			List<String> urls=UrlContant.urls;
			List<TaskBasePojo> data=new ArrayList<TaskBasePojo>();
			urls.forEach(url->{				
				RestTemplate restTemplate=new RestTemplate();
				List<TaskBasePojo> pojos=(List<TaskBasePojo>) restTemplate.postForEntity(url, request, TaskBasePojo.class);
				data.addAll(pojos);
			});
			
			//排序
			
			return ResultUtils.success("查询成功", data);
		}catch(Exception e){
			return ResultUtils.error(e.getMessage());
		}
	}
}
