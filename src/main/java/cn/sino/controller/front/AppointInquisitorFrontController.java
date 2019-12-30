package cn.sino.controller.front;


import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.micro.push.model.NettyUserBean;
import com.micro.push.model.PushResult;
import com.micro.push.service.DubboNettyService;
import com.micro.service.dubbo.user.DubboUserService;

import cn.sino.common.DateUtils;
import cn.sino.common.PageInfo;
import cn.sino.common.Result;
import cn.sino.common.ResultUtils;
import cn.sino.mvc.UserInfoFront;
import cn.sino.mvc.UserInfoUtils;
import cn.sino.service.dubbo.appointment.DubboAppointInquisitorService;
import cn.sino.service.dubbo.appointment.DubboWindowDutyService;

@RestController
@RequestMapping("/appfront/appointinquisitor")//约见检察官
public class AppointInquisitorFrontController {

	@Reference(check=false)
	private DubboAppointInquisitorService dubboAppointInquisitorService;
	@Reference(check=false)
	private DubboNettyService dubboNettyService;
	@Reference(check=false)
	private DubboUserService dubboUserService;
	@Reference(check=false)
	private DubboWindowDutyService dubboWindowDutyService;
	@Value("${appoint.appointtype.agid}")
	private String appointtypeid;//案件档案移交业务id
	
	//消息推送
	//系统id
	@Value("${server.netty.appId}")
	private String appId;
	//业务类型id
	@Value("${server.netty.busiTypeId}")
	private String busiTypeId;
	
	
	
	//申请
	@RequestMapping("/apply")
	public Result apply(HttpServletRequest request,String title,String reason,String businessid,String businessname,String agentstatus){
		try {
			UserInfoFront userinfo = UserInfoUtils.getBeanFront(request);
			String userid = userinfo.getId();
			String name = userinfo.getName();
			String idcard = userinfo.getIdcard();
			String phone = userinfo.getTelephone();
			if(title==null||"".equals(title)){
				throw new RuntimeException("案件名为空");
			}
			if(reason==null||"".equals(reason)){
				throw new RuntimeException("约见理由为空");
			}
			if(agentstatus==null||"".equals(agentstatus)){
				throw new RuntimeException("办理状态为空");
			}
			dubboAppointInquisitorService.apply(title, userid, name, phone, idcard, reason,businessid,businessname,agentstatus);
			//====================消息推送=====================================
			String today = DateUtils.getToday();
			Map<String, Object> dutyuser = dubboWindowDutyService.findByBusinessid(appointtypeid, today);//查询今天值班的案管人员
			if(dutyuser!=null){
				String dutyuserid=dutyuser.get("userid")==null?"":dutyuser.get("userid").toString();
				String dutyusername=dutyuser.get("username")==null?"":dutyuser.get("username").toString();
				NettyUserBean bean=new NettyUserBean();
				bean.setAppid(appId);
				bean.setBusitypeid(busiTypeId);
			    bean.setTitle("约见检察官申请");//推送标题
			    bean.setContent("标题："+title+"，申请时间："+DateUtils.getCurrentTime());//推送内容
			    bean.setUserId(dutyuserid);//接收人id
			    bean.setUserName(dutyusername);//接收人姓名
			    PushResult result=dubboNettyService.sendToUser(bean);
			    System.out.println(result.getMsg());
			}
			
			return ResultUtils.success("申请成功",null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
		
	
	//未办
	@RequestMapping("/findNotDoneList")
	public PageInfo<Map<String,Object>> findNotDoneList(HttpServletRequest request, PageInfo<Map<String,Object>> pageInfo){
		try {
			UserInfoFront userinfo = UserInfoUtils.getBeanFront(request);
			String userid = userinfo.getId();
			pageInfo=dubboAppointInquisitorService.findNotDoneList(userid,pageInfo.getPage(),pageInfo.getLimit());
			pageInfo.setCode(PageInfo.SUCCESS);
			pageInfo.setMsg("查询成功");
			return pageInfo;
		} catch (Exception e) {
			pageInfo.setCode(PageInfo.ERROR);
			pageInfo.setMsg(e.getMessage());
			return pageInfo;
		}
	}
	
	
	//已办
	@RequestMapping("/findDoneList")
	public PageInfo<Map<String,Object>> findDoneList(HttpServletRequest request,PageInfo<Map<String,Object>> pageInfo){
		try {
			UserInfoFront userinfo = UserInfoUtils.getBeanFront(request);
			String userid = userinfo.getId();
			pageInfo=dubboAppointInquisitorService.findDoneList(userid,pageInfo.getPage(),pageInfo.getLimit());
			
			pageInfo.setCode(PageInfo.SUCCESS);
			pageInfo.setMsg("查询成功");
			return pageInfo;
		} catch (Exception e) {
			pageInfo.setCode(PageInfo.ERROR);
			pageInfo.setMsg(e.getMessage());
			return pageInfo;
		}
	}
	
	//详情
	@RequestMapping("/findDetails")
	public Result findDetails(String id){
		try {
			Map<String, Object> map = dubboAppointInquisitorService.findDetails(id);
			
			return ResultUtils.success("查询成功",map);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	//更新来访状态
	@RequestMapping("/updateVisitStatus")
	public Result updateVisitStatus(String id,String visitstatus){
		try {
			if(id==null||"".equals(id)){
				throw new RuntimeException("id为空");
			}
			if(visitstatus==null||"".equals(visitstatus)){
				throw new RuntimeException("visitstatus为空");
			}
			if(!visitstatus.equals("1")&&!visitstatus.equals("2")){
				throw new RuntimeException("visitstatus为无效字符");
			}
			dubboAppointInquisitorService.updateVisitStatus(id, visitstatus);
			return ResultUtils.success("更新成功",null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}

}
