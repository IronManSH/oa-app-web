package cn.sino.controller.front;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSONObject;

import cn.sino.common.DownloadUtils;
import cn.sino.common.PageInfo;
import cn.sino.common.Result;
import cn.sino.common.ResultUtils;
import cn.sino.service.dubbo.appointment.DubboVisitRegisterService;
import cn.sino.service.dubbo.setting.DubboActivityService;
import cn.sino.service.dubbo.setting.DubboAnnexService;
import cn.sino.service.dubbo.setting.DubboSignupService;
import cn.sino.service.dubbo.setting.DubboUserSiteService;

@RestController
@RequestMapping("/appfront/activity")//公告活动
public class ActivityFrontController {
	@Reference(check=false)
	private DubboActivityService dubboActivityService;
	@Reference(check=false)
	private DubboSignupService dubboSignupService;
	@Value("${noticeurljson}")
	private String noticeurljson;
	@Reference(check=false)
	private DubboUserSiteService dubboUserSiteService;
	@Reference(check=false)
	private DubboVisitRegisterService dubboVisitRegisterService;
	@Reference(check=false)
	private DubboAnnexService dubboAnnexService;
	
	
	
	@RequestMapping("/findList")
	public Result findList(String userid){
		try{
			if(userid==null||"".equals(userid)){
				throw new RuntimeException("userid为空");
			}
			List<Map> parseArray = JSONObject.parseArray(noticeurljson,Map.class);
			String  url = parseArray.get(0).get("url").toString();
			PageInfo<Map<String, Object>> list = dubboActivityService.findList(1,3,userid);
			list.getRows().forEach(l->{
				String id = l.get("id").toString();
				l.put("url", url+id);
			});
			List<Map<String, Object>> newlist = list.getRows();
			return ResultUtils.success("查询成功", newlist);
		}catch(Exception e){
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findMoreList")
	public Result findMoreList(String userid){
		try{
			if(userid==null||"".equals(userid)){
				throw new RuntimeException("userid为空");
			}
			List<Map> parseArray = JSONObject.parseArray(noticeurljson,Map.class);
			String  url = parseArray.get(0).get("url").toString();
			List<Map<String, Object>> list = dubboActivityService.findMoreList(userid);
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
	public Result findDetails(String id,String userid){
		try{
			Map<String, Object> map = dubboActivityService.findDetails(id,userid);
			return ResultUtils.success("查询成功", map);
		}catch(Exception e){
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/signup")
	public Result signup(String id,String userid){
		try{
			Map<String, Object> map = dubboUserSiteService.findByUserid(userid);
			String username = map.get("name").toString();
			String idcard = map.get("idcard").toString();
			String telephone = map.get("telephone").toString();
			dubboSignupService.add(id, userid, username, idcard, telephone);
			return ResultUtils.success("报名成功", map);
		}catch(Exception e){
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findSignupDetails")
	public Result findSignupDetails(String id,String userid){
		try{
			Map<String, Object> map = dubboSignupService.findDetails(id,userid);
			return ResultUtils.success("查询成功", map);
		}catch(Exception e){
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/leave")
	public Result leave(String id){
		try{
			dubboSignupService.leave(id);
			return ResultUtils.success("查询成功", null);
		}catch(Exception e){
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/downloadfile")
	public void downloadfile(String id,HttpServletRequest request,HttpServletResponse response){
		try {
			if(id==null||"".equals(id)){
				throw new RuntimeException("id为空");
			}
			Map<String, Object> map = dubboAnnexService.findOne(id);
			byte[] filebyte = (byte[]) map.get("filebyte");
			String filename = map.get("filename").toString();
			DownloadUtils.download(request, response, filebyte, filename);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		
	}
	
	
	
	@RequestMapping("/register")
	public Result register(HttpServletRequest request){
		try{
			String name = request.getParameter("name");
			String phone = request.getParameter("phone");
			String idcard = request.getParameter("idcard");
			String id = request.getParameter("id");
			String reason = request.getParameter("reason");
			if(name==null||"".equals(name)){
				throw new RuntimeException("name为空");
			}
			if(phone==null||"".equals(phone)){
				throw new RuntimeException("phone为空");
			}
			if(idcard==null||"".equals(idcard)){
				throw new RuntimeException("idcard为空");
			}
			if(id==null||"".equals(id)){
				throw new RuntimeException("id为空");
			}
			String businesstype="3";//活动
			String msg="";
			Map<String, Object> map = dubboVisitRegisterService.findByBusinessid(id);
			if(map==null){
				msg="登记成功";
				String status="1";//已到访
				dubboSignupService.updateStatus(id, status);
				dubboVisitRegisterService.register(name, phone, idcard, businesstype, id,reason);
			}else{
				Object leavetime = map.get("leavetime");
				if(leavetime==null){
					String leavemode="0";//离开方式（0.扫码离开，1.接待人结束,2.预约人结束）
					dubboVisitRegisterService.leave(id,leavemode);
					msg="离开成功";
					dubboSignupService.updateStatus(id, "2");
				}else{
					 throw new RuntimeException("二维码已失效");
				}
			}
			return ResultUtils.success(msg, null);
		}catch(Exception e){
			return ResultUtils.error(e.getMessage());
		}
	}
	
}
