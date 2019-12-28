 package cn.sino.controller.front;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;

import cn.sino.common.DateUtils;
import cn.sino.common.Result;
import cn.sino.common.ResultUtils;
import cn.sino.service.dubbo.appointment.DubboLineUpService;

@RestController
@RequestMapping("/appfront/lineUp")
public class LineUpFrontController {
	
	@Reference(check=false)
	private DubboLineUpService dubboLineUpService;
	
	@RequestMapping("/findList")
	public Result findList(HttpServletRequest request){
		try{
			
			String windowid = request.getParameter("windowid");
			String today = DateUtils.getToday();
			Date date = new Date();
	        SimpleDateFormat df = new SimpleDateFormat("HH");
	        String str = df.format(date);
	        String timecode="";
	        int a = Integer.parseInt(str);
	        if (a >= 6 && a <= 15) {
	        	timecode="am";
	        }
	        if (a >= 15 && a <= 24) {
	        	timecode="pm";
	        }
	        List<Map<String, Object>> list = dubboLineUpService.findAllList(windowid,today, timecode);
			return ResultUtils.success("查询成功", list);
		}catch(Exception e){
			return ResultUtils.error(e.getMessage());
			
		}
	}

}
