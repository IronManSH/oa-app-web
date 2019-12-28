package cn.sino.controller.admin;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.micro.model.UserInfo;
import com.micro.service.dubbo.user.DubboRolesService;
import com.micro.service.dubbo.user.DubboUserService;

import cn.sino.common.DateUtils;
import cn.sino.common.Result;
import cn.sino.common.ResultUtils;
import cn.sino.mvc.UserInfoAdmin;
import cn.sino.mvc.UserInfoUtils;
import cn.sino.service.dubbo.appointment.DubboCaseManageService;
import cn.sino.service.dubbo.appointment.DubboLawReadInfoService;
import cn.sino.service.dubbo.appointment.DubboLineUpService;
import cn.sino.service.dubbo.appointment.DubboMeetingService;
import cn.sino.service.dubbo.appointment.DubboVisitApplyInfoService;
import cn.sino.service.dubbo.appointment.DubboWindowDutyService;
import cn.sino.service.dubbo.maintain.DubboMaintainService;
import cn.sino.service.dubbo.notice.DubboNoticeService;
import cn.sino.service.dubbo.onjob.DubboOnjobService;
import cn.sino.service.dubbo.setting.DubboActivityService;
import cn.sino.service.dubbo.setting.DubboUserSiteService;
import cn.sino.service.dubbo.transaction.DubboTransactionService;

@RestController
@RequestMapping("/app/HomePage")
public class HomePageController {
	@Reference(check=false)
	private DubboNoticeService dubboNoticeService; 
	@Reference(check=false)
	private DubboTransactionService dubboTransactionService;
	@Reference(check=false)
	private DubboLineUpService dubboLineUpService;
	@Reference(check=false)
	private DubboVisitApplyInfoService dubboVisitApplyInfoService;
	@Reference(check=false)
	private DubboMaintainService  dubboMaintainService;
	@Reference(check=false)
	private DubboRolesService dubboRolesService;
	@Reference(check=false)
	private DubboWindowDutyService dubboWindowDutyService;
	@Reference(check=false)
	private DubboMeetingService dubboMeetingService;
	@Reference(check=false)
	private DubboLawReadInfoService dubboLawReadInfoService;
	@Reference(check=false)
	private DubboUserSiteService dubboUserSiteService;
	@Reference(check=false)
	private DubboUserService dubboUserService;
	@Reference(check=false)
	private DubboActivityService dubboActivityService;
	@Reference(check=false)
	private DubboCaseManageService dubboCaseManageService;
	@Reference(check=false)
	private DubboOnjobService dubboOnjobService;
	//维护维修系统id(梧州)
	@Value("${ep.whwx.subId}")
	private String subId;
	
	//维修管理业务角色编码
	@Value("${ep.wxgl.code}")
	private String wxglcode;
	
	//维修人员业务角色编码
	@Value("${ep.wxry.code}")
	private String wxrycode;
	
	//安管部门id
	@Value("${agdeptid}")
	private String agdeptid;
	
	@RequestMapping("/findSomeDayTask")
	public Result findHomTask(HttpServletRequest request){
		try {
			String date = request.getParameter("date");
			if(date==null||"".equals(date)){
				throw new RuntimeException("date为空");
			}
			UserInfoAdmin userInfo = UserInfoUtils.getBeanAdmin(request);
			String userid = userInfo.getId();
			String deptid = userInfo.getDeptid();
			List<Map<String, Object>> visitList = dubboVisitApplyInfoService.findSomeDayTask(userid, date);//来访预约
			List<Map<String, Object>> findMyMeeting = dubboMeetingService.findMyMeeting(userid, date);//会议发布
			List<Map<String, Object>> activitList = dubboActivityService.findMyReleaseList(userid, date);//活动发布
			List<Map<String,Object>>lawlist=new ArrayList<Map<String,Object>>();
			if(deptid.equals(agdeptid)){
				lawlist= dubboUserSiteService.findLawUser("", "","",date);//律师注册
				List<Map<String, Object>> lawReadInfoList = dubboLawReadInfoService.findMyCheck("0", date);//律师阅卷审批
				lawlist.addAll(lawReadInfoList);
			}
			List<Map<String, Object>> aglist = dubboCaseManageService.findNotDoneList(userid, date);//案管
			List<Map<String, Object>> maintainList =new ArrayList<Map<String,Object>>();
			List<Map<String, Object>> lineUpList = dubboLineUpService.findTaskList(userid, date);//窗口业务
			List<Map<String, Object>> windowList=new ArrayList<Map<String,Object>>();
			List<Map<String, Object>> windowIdList = dubboWindowDutyService.findWindowIdList(userid, date);//检查是否有权限查看窗口
			
			
			if(windowIdList.size()!=0){
				Date nowdate = new Date();
		        SimpleDateFormat df = new SimpleDateFormat("HH");
		        String str = df.format(nowdate);
		        String timecode="";
		        int a = Integer.parseInt(str);
		        if (a >= 6 && a <= 15) {
		        	timecode="am";
		        }
		        if (a >= 15 && a <= 24) {
		        	timecode="pm";
		        }
		        for (int i = 0; i < windowIdList.size(); i++) {
		        	String windowid =windowIdList.get(i).get("windowid").toString();
		        	Map<String, Object> map = dubboLineUpService.findOne(windowid, date, timecode);//业务窗口入口
		        	if(map!=null){
		        		windowList.add(windowList.size(), map);
		        	}
				}
		        	
			}
			
			Map<String, Object> roles = dubboRolesService.findRolesById(userid, subId);//查询维护维护业务角色
			String rolesNO = roles.get("rolesNO").toString();
			if(rolesNO.equals(wxglcode)){
				maintainList = dubboMaintainService.findSomeDayTask("", date);
			}else if(rolesNO.equals(wxrycode)){
				maintainList=dubboMaintainService.findSomeDayTask(userid, date);
			}
			if(maintainList.size()!=0){
				maintainList.forEach(f->{
					visitList.add(visitList.size(),f);
				});
			}
			if(lineUpList.size()!=0){
				lineUpList.forEach(f->{
					visitList.add(visitList.size(),f);
				});
			}
			if(windowList.size()!=0){
				windowList.forEach(f->{
					visitList.add(visitList.size(),f);
					
				});
			}
			if(findMyMeeting.size()!=0){
				findMyMeeting.forEach(f->{
					visitList.add(visitList.size(),f);
					
				});
			}
			if(lawlist.size()!=0){
				lawlist.forEach(f->{
					visitList.add(visitList.size(),f);
				});
			}
			if(activitList.size()!=0){
				activitList.forEach(f->{
					visitList.add(visitList.size(),f);
				});
			}
			if(aglist.size()!=0){
				aglist.forEach(f->{
					visitList.add(visitList.size(),f);
				});
			}
			
			return ResultUtils.success("查询成功", visitList);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
		
	}
	
	@RequestMapping("/findTaskList")
	public Result findTaskList(HttpServletRequest request){
		try {
			String date = request.getParameter("date");
			
			String userid = request.getParameter("userid");
			if(userid==null||"".equals(userid)){
				UserInfoAdmin userInfo = UserInfoUtils.getBeanAdmin(request);
				userid = userInfo.getId();
			}
			if(date==null||"".equals(date)){
				date = DateUtils.getToday();
			}
			List<Map<String, Object>> visitList = dubboVisitApplyInfoService.findTaskList(userid, date);//来访
			List<Map<String, Object>> meetingList = dubboMeetingService.findTaskList(userid, date);//会议
			List<Map<String, Object>> lineUpList = dubboLineUpService.findTaskList(userid, date);//窗口业务
			List<Map<String, Object>> lawReadList = dubboLawReadInfoService.findTaskList(userid, date);//律师阅卷
			List<Map<String, Object>> lawTaskList = dubboUserSiteService.findTaskList(userid, date);//律师注册
			List<Map<String, Object>> maintainlist = dubboMaintainService.findCompleteList(userid, date);//维护维修
			List<Map<String, Object>> aglist = dubboCaseManageService.findDoneList(userid,date);
			if(lineUpList.size()!=0){
				for (int i = 0; i < lineUpList.size(); i++) {
					visitList.add(visitList.size(), lineUpList.get(i));
				}
			}
			if(meetingList.size()!=0){
				for (int i = 0; i < meetingList.size(); i++) {
					visitList.add(visitList.size(), meetingList.get(i));
				}
			}
			if(lawReadList.size()!=0){
				for (int i = 0; i < lawReadList.size(); i++) {
					visitList.add(visitList.size(), lawReadList.get(i));
				}
			}
			if(lawTaskList.size()!=0){
				for (int i = 0; i < lawTaskList.size(); i++) {
					visitList.add(visitList.size(), lawTaskList.get(i));
				}
			}
			if(maintainlist.size()!=0){
				for (int i = 0; i < maintainlist.size(); i++) {
					visitList.add(visitList.size(), maintainlist.get(i));
				}
			}
			if(aglist.size()!=0){
				for (int i = 0; i < aglist.size(); i++) {
					visitList.add(visitList.size(), aglist.get(i));
				}
			}
			return ResultUtils.success("查询成功", visitList);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
		
	}
	
	@RequestMapping("/findUserList")
	public Result findUserList(HttpServletRequest request){
		try {
			UserInfoAdmin userinfo = UserInfoUtils.getBeanAdmin(request);
			String deptid = userinfo.getDeptid();
			String userid = userinfo.getId();
			List<UserInfo> list = dubboUserService.getUserByDeptId(deptid);
			List<Map<String ,Object>> userList = new ArrayList<Map<String,Object>>();
			list.forEach(f->{
				String newuserid = f.getId();
				if(!newuserid.equals(userid)){
					String username = f.getNickname();
					Map<String,Object> map = new HashMap<String,Object>();
					map.put("userid", userid);
					map.put("username", username);
					userList.add(userList.size(), map);
				}
				
			});
			
			return ResultUtils.success("查询成功", userList);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	
	
	
	@RequestMapping("/findSomeMonthTaskNum")
	public Result findTaskNum(HttpServletRequest request){
		try {
			String date = request.getParameter("date");
			if(date==null||"".equals(date)){
				throw new RuntimeException("date为空");
			}
			int year = Integer.parseInt(date.split("-")[0]);
			int month = Integer.parseInt(date.split("-")[1]);
			int days = DateUtils.days(year, month);
			UserInfoAdmin userInfo = UserInfoUtils.getBeanAdmin(request);
			String userid = userInfo.getId();
			String deptid = userInfo.getDeptid();
			Map<String, Object> roles = dubboRolesService.findRolesById(userid, subId);
			List<Map<String,Object>>list=new ArrayList<Map<String,Object>>();
			String rolesNO = roles.get("rolesNO").toString();
			//Integer code = dubboWindowInfoService.inspect(userid);
			for(int i=1;i<=days;i++){
				Integer lineUpNum = dubboLineUpService.findTaskNum(userid, date+"-"+i);
				Map<String, Object> map=new HashMap<String,Object>();
				Integer visitTaskNum = dubboVisitApplyInfoService.findTaskNum(userid, date+"-"+i);
				Integer meetingTaskMun = dubboMeetingService.findTaskNum(userid, date+"-"+i);
				Integer maintainTaskNum=0;
				Integer lawTaskNum=0;
				//Integer roomTaskNum=0;
				Integer ativityNum = dubboActivityService.findMyReleaseNum(userid, date+"-"+i);
//				if(!code.equals("0")){
//					roomTaskNum = dubboLineUpService.findTaskNum(date+"-"+i);//接待室任务数量
//				}
				if(deptid.equals(agdeptid)){
					lawTaskNum= dubboUserSiteService.findTaskNum("", date+"-"+i);//律师注册
					Integer lwsreadnum = dubboLawReadInfoService.findTaskNum("", date+"-"+i);
					lawTaskNum+=lwsreadnum;
				}
				if(rolesNO.equals(wxglcode)){
					maintainTaskNum = dubboMaintainService.findTaskNum("", date+"-"+i);
				}else if(rolesNO.equals(wxrycode)){
					maintainTaskNum = dubboMaintainService.findTaskNum(userid, date+"-"+i);
				}
				Integer agnum = dubboCaseManageService.findNotDoneNum(userid, date+"-"+i);//案管
				int num=visitTaskNum+meetingTaskMun+maintainTaskNum+lawTaskNum+lineUpNum+ativityNum+agnum;
				if(num!=0){
					map.put("num", num);
					map.put("code", "N");
					map.put("time", i);
					list.add(list.size(), map);
				}
				
			}
			return ResultUtils.success("查询成功", list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
		
	}
	
	
	@RequestMapping("/findStatistics")
	public Result findStatistics(HttpServletRequest request) {
		try {
			Date d = new Date();  
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  
	        String date = sdf.format(d);  
	        UserInfoAdmin userInfo=UserInfoUtils.getBeanAdmin(request);
			String userId=userInfo.getId();
			String deptId = userInfo.getDeptid();
			List<Map<String, Object>> busiList = dubboTransactionService.findNumAndType(userId,date);
			Map<String, Object> noticeMap = dubboNoticeService.findNumAndType(date, deptId);
			String num = noticeMap.get("num").toString();
			if(!num.equals("0")){
				busiList.add(busiList.size(),noticeMap);
			}
			Map<String, Object> inviterMap = dubboVisitApplyInfoService.findNumAndType(userId, date);
			num=inviterMap.get("num").toString();
			if(!num.equals("0")){
				busiList.add(busiList.size(),inviterMap);
			}
			return ResultUtils.success("查询成功",busiList);
		}catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
		
	}
	
	@RequestMapping("/findDetail")
	public Result findDetail(HttpServletRequest request,String id,String type) {
		try {
			
			String sendId = request.getParameter("sendId");
			Map<String, Object> map =new HashMap<String, Object>();
			switch(type) {
				case "tz":
					map=dubboNoticeService.findDetail(id, sendId);
					break;
				case "swsq":
					map=dubboTransactionService.findApplyDetail(id);
					break;
				case "swsp":
					map=dubboTransactionService.findCheckDetail(id);
					break;
				case "lf":
					map=dubboVisitApplyInfoService.findDetail(id);
					break;
				case "wh":
					map=dubboMaintainService.findTaskDetails(id, type);
					break;
				case "ag":
					map=dubboCaseManageService.findDetails(sendId);
					break;
			}
			return ResultUtils.success("查询成功", map);
		}catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	

}
