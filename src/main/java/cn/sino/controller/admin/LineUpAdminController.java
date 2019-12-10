package cn.sino.controller.admin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.micro.push.model.NettyUserBean;
import com.micro.push.model.PushResult;
import com.micro.push.service.DubboNettyService;

import cn.sino.common.DateUtils;
import cn.sino.common.Result;
import cn.sino.common.ResultUtils;
import cn.sino.mvc.UserInfoAdmin;
import cn.sino.mvc.UserInfoUtils;
import cn.sino.service.dubbo.appointment.DubboBusiApplyInfoService;
import cn.sino.service.dubbo.appointment.DubboLineUpService;
import cn.sino.service.dubbo.appointment.DubboWindowInfoService;

@RestController
@RequestMapping("/app/lineUp")
public class LineUpAdminController {
	
	@Reference(check=false)
	private DubboLineUpService dubboLineUpService;
	@Reference(check=false)
	private DubboBusiApplyInfoService dubboBusiApplyInfoService;
	@Reference(check=false)
	private DubboWindowInfoService dubboWindowInfoService;
	@Reference(check=false)
	private DubboNettyService dubboNettyService;
	//梧州
	//系统id
	@Value("${server.netty.appId}")
	private String appId;
	//业务类型id
	@Value("${server.netty.busiTypeId}")
	private String busiTypeId;
	
	@RequestMapping("/findAllList")
	public Result findAllList(HttpServletRequest request){
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
			List<Map<String, Object>> list = dubboLineUpService.findList(windowid,today, timecode);
			return ResultUtils.success("查询成功", list);
		}catch(Exception e){
			return ResultUtils.error(e.getMessage());
			
		}
	}
	
	@RequestMapping("/updateStatus")
	public Result updateStatus(HttpServletRequest request){
		try{
			
			String id = request.getParameter("id");
			if(id==null||"".equals(id)){
				throw new RuntimeException("id为空");
			}
			String status = request.getParameter("status");
			if(status==null||"".equals(status)){
				throw new RuntimeException("status为空");
			}
			String applyid = request.getParameter("applyid");
			if(applyid==null||"".equals(applyid)){
				throw new RuntimeException("applyid为空");
			}
			UserInfoAdmin userinfo = UserInfoUtils.getBeanAdmin(request);
			String userid = userinfo.getId();
			String username = userinfo.getNickname();
			String lineUpStatus = dubboLineUpService.findLineUpStatus(id);//查询排队状态
			String msg="";
			String blexplain="";
			String busiStatus="";
			if(status.equals("4")){//叫号
				if(!lineUpStatus.equals("0")){
					throw new RuntimeException("请先办结当前业务");
				}
				busiStatus="1";//办理中
				msg="叫号成功";
				Map<String, Object> map = dubboBusiApplyInfoService.findDetail(applyid);
				if(map!=null||"".equals(map)){
					Object receiverid = map.get("userid");
					if(receiverid!=null&&!"".equals(receiverid)){
						String receivername = map.get("username").toString();
						String windowname = map.get("windowname").toString();
						String windowid = map.get("windowid").toString();
						//===============消息推送-检务服务.apk=========================//
						NettyUserBean bean=new NettyUserBean();
						bean.setAppid(appId);
						bean.setBusitypeid(busiTypeId);
					    bean.setTitle("窗口语音提醒");//推送标题
					    bean.setContent("请"+receivername+"到"+windowname+"办理业务");//推送内容
					    bean.setUserId(windowid);//接收人id
					    bean.setUserName(windowname);//接收人姓名
					    PushResult result=dubboNettyService.sendToUser(bean);
					    System.out.println(result.getMsg());
					    //===============消息推送-led.apk=========================//
						NettyUserBean beanuser=new NettyUserBean();
						beanuser.setAppid(appId);
						beanuser.setBusitypeid(busiTypeId);
						beanuser.setTitle("业务办理提醒");//推送标题
						beanuser.setContent("内容：办理窗口："+windowname);//推送内容
						beanuser.setUserId(receiverid.toString());//接收人id
						beanuser.setUserName(receivername);//接收人姓名
					    PushResult resultuser=dubboNettyService.sendToUser(bean);
					    System.out.println(resultuser.getMsg());
					}
				}
			}else if(status.equals("3")){//转到窗口
				if(lineUpStatus.equals("0")){
					throw new RuntimeException("请先叫号");
				}
				busiStatus="1";
				msg="转发成功";
			}else if(status.equals("2")){//未到场
				if(lineUpStatus.equals("0")){
					throw new RuntimeException("请先叫号");
				}
				busiStatus="3";//未办理已过期
				msg="跳过成功";
			}else if(status.equals("1")){//办结
				if(lineUpStatus.equals("0")){
					throw new RuntimeException("请先叫号");
				}
				busiStatus="2";
				msg="办结成功";
				blexplain=request.getParameter("blexplain");
			}
			dubboLineUpService.updateStatus(id, status,userid,username);
			dubboBusiApplyInfoService.updataStatus(applyid, busiStatus,blexplain);
			return ResultUtils.success(msg, null);
		}catch(Exception e){
			return ResultUtils.error(e.getMessage());
			
		}
	}
	
	//已废
	//@RequestMapping("/sendtoroom")
	public Result sendtoroom(HttpServletRequest request){
		try{
			String id = request.getParameter("id");
			String status = request.getParameter("status");
			if(status.equals("0")){
				throw new RuntimeException("请先叫号");
			}
			dubboLineUpService.updateStatus(id, "3","","");
			String windowid = request.getParameter("windowid");
			String roomid = request.getParameter("roomid");
			String roomname = request.getParameter("roomname");
			String sendreason = request.getParameter("sendreason");
			UserInfoAdmin userInfo = UserInfoUtils.getBeanAdmin(request);
			String senduserid = userInfo.getId();
			String sendusername = userInfo.getNickname();
			
			dubboLineUpService.sendRoom(id, roomid, windowid, senduserid, sendusername,sendreason);
			Map<String, Object> lineUpmap = dubboLineUpService.findOneRoomTask(id);
			String name="";
			String businessname="";
			if(lineUpmap!=null&&!"".equals(lineUpmap)){
				name = lineUpmap.get("name").toString();
				businessname = lineUpmap.get("businessname").toString();
			}
			List<Map<String, Object>> list = dubboWindowInfoService.findWindowManageUser(windowid);
			for(Map<String,Object> d:list){
				String  userid = d.get("userid").toString();
				String  username = d.get("username").toString();
				//===============消息推送=========================//
				NettyUserBean bean=new NettyUserBean();
				bean.setAppid(appId);
				bean.setBusitypeid(busiTypeId);
			    bean.setTitle("业务转派到接待室");//推送标题
			    bean.setContent("内容：预约人："+name+"，业务名："+businessname+"，接待室："+roomname);//推送内容
			    bean.setUserId(userid);//接收人id
			    bean.setUserName(username);//接收人姓名
			    PushResult result=dubboNettyService.sendToUser(bean);
			    System.out.println(result.getMsg());
			}
			return ResultUtils.success("发送成功", null);
		}catch(Exception e){
			return ResultUtils.error(e.getMessage());
			
		}
	}
	
	@RequestMapping("/sendWindow")
	public Result sendWindow(HttpServletRequest request){
		try{
			String status = request.getParameter("status");
			if(status==null||"".equals(status)){
				throw new RuntimeException("status为空");
			}
			if(status.equals("0")){
				throw new RuntimeException("请先叫号");
			}
			String id = request.getParameter("id");
			String windowid = request.getParameter("windowid");
			String sendwindowid = request.getParameter("sendwindowid");
			String sendreason = request.getParameter("sendreason");
			UserInfoAdmin userinfo = UserInfoUtils.getBeanAdmin(request);
			String senduserid = userinfo.getId();
			String sendusername = userinfo.getNickname();
			dubboLineUpService.sendWindow(id, windowid, sendwindowid, senduserid, sendusername, sendreason);
			return ResultUtils.success("发送成功", null);
		}catch(Exception e){
			return ResultUtils.error(e.getMessage());
			
		}
	}
	
	@RequestMapping("/findRoomTask")
	public Result findRoomTask(HttpServletRequest request){
		try{
			
			String id = request.getParameter("id");
			String today = DateUtils.getToday();
			Map<String, Object> list = dubboLineUpService.findRoomTask(id, today);
			return ResultUtils.success("查询成功", list);
		}catch(Exception e){
			return ResultUtils.error(e.getMessage());
			
		}
	}
	
	@RequestMapping("/findOneRoomTask")
	public Result findOneRoomTask(HttpServletRequest request){
		try{
			String id = request.getParameter("id");
			Map<String, Object> list = dubboLineUpService.findOneRoomTask(id);
			return ResultUtils.success("查询成功", list);
		}catch(Exception e){
			return ResultUtils.error(e.getMessage());
			
		}
	}
	
}

