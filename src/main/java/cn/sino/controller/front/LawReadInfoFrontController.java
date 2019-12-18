package cn.sino.controller.front;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.dubbo.config.annotation.Reference;
import com.micro.push.model.NettyUserBean;
import com.micro.push.model.PushResult;
import com.micro.push.service.DubboNettyService;
import com.sinosoft.api.pojo.FileInfoBusiBean;
import com.sinosoft.api.service.FileInfoBusiApiService;

import cn.sino.common.DateUtils;
import cn.sino.common.DownloadUtils;
import cn.sino.common.Result;
import cn.sino.common.ResultUtils;
import cn.sino.mvc.UserInfoFront;
import cn.sino.mvc.UserInfoUtils;
import cn.sino.service.dubbo.appointment.DubboBusiApplyInfoService;
import cn.sino.service.dubbo.appointment.DubboLawReadInfoService;
import cn.sino.service.dubbo.appointment.DubboLineUpService;
import cn.sino.service.dubbo.appointment.DubboVisitCheckInfoService;
import cn.sino.service.dubbo.appointment.DubboWindowDutyService;
import cn.sino.service.dubbo.appointment.DubboWindowInfoService;

@RestController
@RequestMapping("/appfront/lawReadInfo")
public class LawReadInfoFrontController {
	@Reference(check=false)
	private DubboLawReadInfoService dubboLawReadInfoService;
	@Reference(check=false)
	private DubboNettyService dubboNettyService;
	@Reference(check=false)
	private DubboWindowDutyService dubboWindowDutyService;
	@Reference(check=false)
	private DubboVisitCheckInfoService dubboVisitCheckInfoService;
	@Reference(check=false)
	private DubboLineUpService dubboLineUpService;
	@Reference(check=false)
	private FileInfoBusiApiService fileInfoBusiApiService;
	@Reference(check=false)
	private DubboWindowInfoService dubboWindowInfoService;
	
	@Reference(check=false)
	private DubboBusiApplyInfoService dubboBusiApplyInfoService;
	
	//梧州
	//系统id
	@Value("${server.netty.appId}")
	private String appId;
	//业务类型id
	@Value("${server.netty.busiTypeId}")
	private String busiTypeId;
	@ResponseBody
	@RequestMapping("/apply")
	public Result findAnnexById(HttpServletRequest request, List<MultipartFile> clientbookfile) {
		try{
			if(clientbookfile==null||clientbookfile.size()==0){
				throw new RuntimeException("委托书照片为空");
			}
			UserInfoFront userInfo = UserInfoUtils.getBeanFront(request);
			String userid = userInfo.getId();
			String username = userInfo.getName();
			String phone = userInfo.getTelephone();
			String idcard = userInfo.getIdcard();
			String casename = request.getParameter("casename");
			String casecontent = request.getParameter("casecontent");
			String clientname = request.getParameter("clientname");
			
			String businessid = request.getParameter("businessid");
			String id = dubboLawReadInfoService.apply(userid, username, phone, casename, casecontent, clientname,
					 businessid,idcard);
			Result photoresult = fileInfoBusiApiService.findByBusinessid(userid);
			if(photoresult.getCode()==0){//保存律师身份证照片
				List<FileInfoBusiBean> lists=(List<FileInfoBusiBean>)photoresult.getData();
				lists.forEach(f->{
					String tag = f.getBusinesstag();
					if(tag.equals("idcard")){
						String path = f.getPath();
						byte[] idcardbyte= fileInfoBusiApiService.downloadByPath(path);
						String code="sfz";
						dubboLawReadInfoService.addPhoto(id, idcardbyte, code);
					}else{//保存律师证照片
						String path = f.getPath();
						byte[] lawcardbyte= fileInfoBusiApiService.downloadByPath(path);
						String code="lsz";
						dubboLawReadInfoService.addPhoto(id, lawcardbyte, code);
					}
				});
			}
			clientbookfile.forEach(d->{//保存委托书
				try {
					byte[] bytes = d.getBytes();
					String code="wts";
					dubboLawReadInfoService.addPhoto(id, bytes, code);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			String receiverid="";
			String receivername="";
			String today = DateUtils.getToday();//获取当前日期
			String appointtime = DateUtils.getCurrentTime();//获取预约时间
			Map<String, Object> dutymap = dubboWindowDutyService.findByBusinessid(businessid, today);
			if(dutymap!=null&&!"".equals(dutymap)){
				receiverid=dutymap.get("userid").toString();
				receivername=dutymap.get("username").toString();
				NettyUserBean bean=new NettyUserBean();
				bean.setAppid(appId);
				bean.setBusitypeid(busiTypeId);
			    bean.setTitle("律师预约申请");//推送标题
			    bean.setContent("内容：申请人："+username+"，申请时间："+appointtime+"，预约案件："+casename);//推送内容
			    bean.setUserId(receiverid);//接收人id
			    bean.setUserName(receivername);//接收人姓名
			    PushResult result=dubboNettyService.sendToUser(bean);
			    System.out.println(result.getMsg());
			}else{
				Map<String, Object> map = dubboLawReadInfoService.findDetail(id);
				if(map!=null&&!"".equals(map)){
					Object windowid = map.get("windowid");
					if(windowid!=null&&!"".equals(windowid)){
						List<Map<String, Object>> list = dubboWindowInfoService.findWindowManageUser(windowid.toString());
						for(Map<String,Object> f:list){
							receiverid=f.get("userid").toString();
							receivername=f.get("username").toString();
							NettyUserBean bean=new NettyUserBean();
							bean.setAppid(appId);
							bean.setBusitypeid(busiTypeId);
						    bean.setTitle("律师预约申请");//推送标题
						    bean.setContent("内容：申请人："+username+"，申请时间："+appointtime+"，预约案件："+casename);//推送内容
						    bean.setUserId(receiverid);//接收人id
						    bean.setUserName(receivername);//接收人姓名
						    PushResult result=dubboNettyService.sendToUser(bean);
						    System.out.println(result.getMsg());
						}
					}
					
				}
				
			}
		    return ResultUtils.success("申请成功", null);
		}catch(Exception e){
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findDetail")
	public Result findDetail(String id){
		try {
			Map<String, Object> map = dubboLawReadInfoService.findDetail(id);
			return ResultUtils.success("查询成功", map);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findPhoto")
	public Result findPhoto(String id){
		try {
			 List<Map<String, Object>> list = dubboLawReadInfoService.findPhoto(id);
			return ResultUtils.success("查询成功", list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
//	@RequestMapping("/findPhoto")
//	public Result findPhoto(HttpServletRequest request){
//		try {
//			UserInfoFront userInfo = UserInfoUtils.getBeanFront(request);
//			String userid = userInfo.getId();
//			List<Map<String, Object>> list = dubboLawReadInfoService.findMyAppoint(userid, date);
//			return ResultUtils.success("查询成功", list);
//		} catch (Exception e) {
//			return ResultUtils.error(e.getMessage());
//		}
//	}
	@ResponseBody
	@RequestMapping("/findflie")
	public Result findflie(HttpServletRequest request,HttpServletResponse response){
		try {
			UserInfoFront userInfo = UserInfoUtils.getBeanFront(request);
			String userid = userInfo.getId();
			Result result = fileInfoBusiApiService.findByBusinessid(userid);
			if(result.getCode()==0){
				List<FileInfoBusiBean> lists=(List<FileInfoBusiBean>)result.getData();
				lists.forEach(f->{
					String path = f.getPath();
					String fileName = f.getFilename();
					byte[] flieByte= fileInfoBusiApiService.downloadByPath(path);
					DownloadUtils.download(request, response, flieByte, fileName);
				});
			}
			return ResultUtils.success("查询成功", null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	

}
