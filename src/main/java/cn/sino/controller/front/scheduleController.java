package cn.sino.controller.front;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;

import cn.sino.common.PageInfo;
import cn.sino.common.Result;
import cn.sino.common.ResultUtils;
import cn.sino.mvc.UserInfoFront;
import cn.sino.mvc.UserInfoUtils;
import cn.sino.service.dubbo.appointment.DubboVisitApplyInfoService;

@RestController
@RequestMapping("/appfront/schedule")
public class scheduleController {
	@Reference(check=false)
	private DubboVisitApplyInfoService dubboAppointmentService;
	
	
	@RequestMapping("/findAllAppoint")
	public Result findAllAppoint(PageInfo<Map<String,Object>> pageInfo,HttpServletRequest request,String date){
		try {
			UserInfoFront infoInfo = UserInfoUtils.getBeanFront(request);
			String userId = infoInfo.getId();
			String type="0";
			List<Map<String, Object>> list=new ArrayList<Map<String, Object>>();
			//List<Map<String, Object>> checkList = dubboAppointmentService.findMyCheck(userId, date, type);
			List<Map<String, Object>> applyList = dubboAppointmentService.findMyApply(userId, date);
			//int size1 = checkList.size();
			int size = applyList.size();
			
//			for(int i=0;i<size1;i++){
//				checkList.get(i).put("appointType", "sy");
//				list.add(list.size(), checkList.get(i));
//			}
			for(int i=0;i<size;i++){
				applyList.get(i).put("appointType", "yy");
				list.add(list.size(), applyList.get(i));
				
			}
			return ResultUtils.success("查询成功", list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
}
