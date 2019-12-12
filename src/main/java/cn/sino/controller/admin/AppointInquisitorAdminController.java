package cn.sino.controller.admin;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.micro.model.UserInfo;
import com.micro.push.model.NettyUserBean;
import com.micro.push.model.PushResult;
import com.micro.push.service.DubboNettyService;
import com.micro.service.dubbo.user.DubboUserService;

import cn.sino.common.DateUtils;
import cn.sino.common.PageInfo;
import cn.sino.common.Result;
import cn.sino.common.ResultUtils;
import cn.sino.mvc.UserInfoAdmin;
import cn.sino.mvc.UserInfoUtils;
import cn.sino.service.dubbo.appointment.DubboAppointInquisitorService;
@RequestMapping("/app/appointinquisitor")
@RestController
public class AppointInquisitorAdminController {
	
	@Reference(check=false)
	private DubboAppointInquisitorService dubboAppointInquisitorService;
	@Reference(check=false)
	private DubboNettyService dubboNettyService;
	@Reference(check=false)
	private DubboUserService dubboUserService;
	
	@Value("${agdeptid}")
	private String agdeptid;//案管部门id
	
	@Value("${appoint.jcgcode}")
	private String jcgcode;//检察官业务角色编号
	
	//消息推送
	//系统id
	@Value("${server.netty.appId}")
	private String appId;
	//业务类型id
	@Value("${server.netty.busiTypeId}")
	private String busiTypeId;
	
	//未办
	@RequestMapping("/findNotDoneList")
	public PageInfo<Map<String,Object>> findNotDoneList(HttpServletRequest request,PageInfo<Map<String,Object>> pageInfo){
		try {
			UserInfoAdmin userinfo = UserInfoUtils.getBeanAdmin(request);
			String userid = userinfo.getId();
			String deptid = userinfo.getDeptid();
			
			String code="0";
			if(deptid.equals(agdeptid)){
				pageInfo =dubboAppointInquisitorService.findNotDoneList("",pageInfo.getPage(),pageInfo.getLimit());
				code="1";
			}
			if(code.equals("0")){
				pageInfo = dubboAppointInquisitorService.findNotDoneList(userid,pageInfo.getPage(),pageInfo.getLimit());
			}
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
			UserInfoAdmin userinfo = UserInfoUtils.getBeanAdmin(request);
			String userid = userinfo.getId();
			String deptid = userinfo.getDeptid();
			String code="0";
			if(deptid.equals(agdeptid)){
				pageInfo =dubboAppointInquisitorService.findDoneList("",pageInfo.getPage(),pageInfo.getLimit());
				code="1";
			}
			if(code.equals("0")){
				pageInfo = dubboAppointInquisitorService.findDoneList(userid,pageInfo.getPage(),pageInfo.getLimit());
			}
			
			pageInfo.setCode(PageInfo.SUCCESS);
			pageInfo.setMsg("查询成功");
			return pageInfo;
		} catch (Exception e) {
			pageInfo.setCode(PageInfo.ERROR);
			pageInfo.setMsg(e.getMessage());
			return pageInfo;
		}
	}
	
	//转发
	@RequestMapping("/forward")
	public Result forward(HttpServletRequest request,String businessid,String checkuserid,String checkusername){
		try {
			
			dubboAppointInquisitorService.forward(businessid, checkuserid, checkusername);
			NettyUserBean bean=new NettyUserBean();
			bean.setAppid(appId);
			bean.setBusitypeid(busiTypeId);
		    bean.setTitle("约见检察官");//推送标题
		    bean.setContent("您一有条约见信息,推送时间："+DateUtils.getCurrentTime());//推送内容
		    bean.setUserId(checkuserid);//接收人id
		    bean.setUserName(checkusername);//接收人姓名
		    PushResult result=dubboNettyService.sendToUser(bean);
		    System.out.println(result.getMsg());
			
			return ResultUtils.success("转发成功",null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	//检察官列表
	@RequestMapping("/inquisitorList")
	public Result inquisitorList(HttpServletRequest request){
		try {
			List<UserInfo> list = dubboUserService.findUserByRoleCode(jcgcode);
			return ResultUtils.success("查询成功",list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
		
	
	//审批
	@RequestMapping("/check")
	public Result check(HttpServletRequest request,String businessid,String checkstatus){
		try {
			String checkreason = request.getParameter("checkreason");
			String appointtime = request.getParameter("appointtime");
			if(!checkstatus.equals("1")&&!checkstatus.equals("2")){
				throw new RuntimeException("checkstatus为无效字符");
			}
			dubboAppointInquisitorService.check(businessid, checkstatus, checkreason,appointtime);
			Map<String, Object> map = dubboAppointInquisitorService.findDetails(businessid);
			if(map!=null){
				String userid = map.get("applyuserid")==null?"":map.get("applyuserid").toString();
				String username = map.get("applyusername")==null?"":map.get("applyusername").toString();
				String msg="";
				if(checkstatus.equals("1")){
					msg="申请通过，约见时间："+appointtime;
				}else if(checkstatus.equals("2")){
					msg="申请驳回，驳回理由："+checkreason;
				}
				NettyUserBean bean=new NettyUserBean();
				bean.setAppid(appId);
				bean.setBusitypeid(busiTypeId);
			    bean.setTitle("约见检察官");//推送标题
			    bean.setContent(msg);//推送内容
			    bean.setUserId(userid);//接收人id
			    bean.setUserName(username);//接收人姓名
			    PushResult result=dubboNettyService.sendToUser(bean);
			    System.out.println(result.getMsg());
			}
			return ResultUtils.success("审批成功",null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	//详情
	@RequestMapping("/findDetails")
	public Result findDetails(String id){
		try {
			if(id==null||"".equals(id)){
				throw new RuntimeException("id为空");
			}
			Map<String, Object> map = dubboAppointInquisitorService.findDetails(id);
			return ResultUtils.success("查询成功",map);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}

}
