package cn.sino.controller.front;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSONObject;

import cn.sino.common.Result;
import cn.sino.common.ResultUtils;
import cn.sino.mvc.UserInfoFront;
import cn.sino.mvc.UserInfoUtils;
import cn.sino.service.dubbo.appointment.DubboAppointTypeService;
import cn.sino.service.dubbo.setting.DubboUserSiteService;

@RestController
@RequestMapping("/appfront/appointType")
public class AppointTypeFronController {
	
	@Reference(check=false)
	private DubboAppointTypeService dubboAppointTypeService;
	@Reference(check=false)
	private DubboUserSiteService dubboUserSiteService;
	@RequestMapping("/findAllList")
	public Result findAllList(HttpServletRequest request ,String apktag,String type) {
		try{
			List<Map<String, Object>> list = dubboAppointTypeService.findAllList();
			List<Map<String ,Object>>ksList=new ArrayList<Map<String ,Object>>();
			List<Map<String ,Object>>agList=new ArrayList<Map<String ,Object>>();
			List<Map<String ,Object>>qtList=new ArrayList<Map<String ,Object>>();
			UserInfoFront user = UserInfoUtils.getBeanFront(request);
			String id = user.getId();
			Map<String, Object> usermap = dubboUserSiteService.findByUserid(id);
			String  usertype = usermap.get("type").toString();
			if(usertype!=null&&!"".equals(usertype)){
				if(usertype.equals("0")){
					for (int i = 0; i < list.size(); i++) {
						Object  lawso = list.get(i).get("lawso");
						if(lawso.equals("0")){
							list.remove(i);
						}
					}
				}
			}
			Map<String,Object>map=new HashMap<String ,Object>();
			for (int i = 0; i < list.size(); i++) {
						
				String code = list.get(i).get("code").toString();
				if(code.equals("ks")){
					ksList.add(ksList.size(), list.get(i));
				}else if(code.equals("ag")){
					agList.add(agList.size(), list.get(i));
				}else{
					qtList.add(qtList.size(), list.get(i));
				}
				
			}
			map.put("ks", ksList);
			map.put("ag", agList);
			map.put("qt", qtList);
			return ResultUtils.success("查询成功", map);
		}catch(Exception e){
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findDetail")
	public Result findDetail(String id,String type) {
		try{
			Map<String, Object> map = dubboAppointTypeService.findDeteil(id, type);
			 Object worktime = map.get("worktime");
			 Object notice = map.get("notice");
			 if(notice==null){
				 map.put("notice", "");
			 }
			 if(worktime!=null){
				 List<Map> parseArray = JSONObject.parseArray(worktime.toString(),Map.class);
				 map.put("worktime", parseArray);
			 }
			return ResultUtils.success("查询成功", map);
		}catch(Exception e){
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findMyAppoint")
	public Result findMyAppoint(HttpServletRequest request) {
		try{
			String date = "";
			UserInfoFront user = UserInfoUtils.getBeanFront(request);
			String userid = user.getId();
			List<Map<String, Object>> list = dubboAppointTypeService.findMyAppoint(userid, date);
			return ResultUtils.success("查询成功", list);
		}catch(Exception e){
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findMyAppointMore")
	public Result findMyAppointMore(HttpServletRequest request) {
		try{
			UserInfoFront user = UserInfoUtils.getBeanFront(request);
			String userid = user.getId();
			String status = request.getParameter("status");
			List<Map<String, Object>> list = dubboAppointTypeService.findMyAppointMore(userid, "",status);
			return ResultUtils.success("查询成功", list);
		}catch(Exception e){
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findList")
	public Result findList(HttpServletRequest request) {
		try{
			List<Map<String, Object>> list = dubboAppointTypeService.findList();
			return ResultUtils.success("查询成功", list);
		}catch(Exception e){
			return ResultUtils.error(e.getMessage());
		}
	}
	
}
