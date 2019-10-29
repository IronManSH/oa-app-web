package cn.sino.controller.front;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.micro.model.DeptInfo;
import com.micro.model.UserInfo;
import com.micro.push.model.NettyUserBean;
import com.micro.push.model.PushResult;
import com.micro.push.service.DubboNettyService;
import com.micro.service.dubbo.user.DubboDeptService;
import com.micro.service.dubbo.user.DubboRolesService;
import com.micro.service.dubbo.user.DubboUserService;

import cn.sino.common.DateUtils;
import cn.sino.common.DownloadUtils;
import cn.sino.common.Result;
import cn.sino.common.ResultUtils;
import cn.sino.mvc.UserInfoFront;
import cn.sino.mvc.UserInfoUtils;
import cn.sino.service.dubbo.appointment.DubboAppointTypeService;
import cn.sino.service.dubbo.appointment.DubboBusiWatchmanService;
import cn.sino.service.dubbo.appointment.DubboVisitApplyInfoService;
import cn.sino.service.dubbo.appointment.DubboVisitRegisterService;

@RestController
@RequestMapping("/appfront/visitor")
public class VisitApplyInfoFrontController {
	
	@Reference(check=false)
	private DubboVisitApplyInfoService dubboVisitApplyInfoService;
	@Reference(check=false)
	private DubboVisitRegisterService dubboVisitRegisterService;
	@Reference(check=false)
	private DubboAppointTypeService dubboAppointTypeService;
	@Reference(check=false)
	private DubboUserService dubboUserService;
	@Reference(check=false)
	private DubboNettyService dubboNettyService;
	@Reference(check=false)
	private DubboDeptService dubboDeptService;
	@Reference(check=false)
	private DubboRolesService dubboRolesService;
	@Reference(check=false)
	private DubboBusiWatchmanService dubboBusiWatchmanService;
	//梧州
	//系统id
	@Value("${server.netty.appId}")
	private String appId;
	//业务类型id
	@Value("${server.netty.busiTypeId}")
	private String busiTypeId;
	

	
	@RequestMapping("/apply")
	public Result apply(HttpServletRequest request){
		try{
			UserInfoFront userInfo=UserInfoUtils.getBeanFront(request);
			String username = userInfo.getName();
			String userid = userInfo.getId();
			String phone = userInfo.getTelephone();
			String appointtime = request.getParameter("appointTime");
			String appointcontent = request.getParameter("content");
			String businessid = request.getParameter("businessid");
			String unit = userInfo.getUnit();
			String idcard = userInfo.getIdcard();
			String  watchmanuserid = dubboBusiWatchmanService.findWatchman(businessid).get(0).get("userid").toString();
			UserInfo user = dubboUserService.findUserInfo(watchmanuserid);
			String deptid = user.getDeptid();
			String deptname = user.getDeptname();
			if(unit==null||"".equals(unit)){
				unit="";
			}
			dubboVisitApplyInfoService.Apply(userid, username, phone, appointtime, appointcontent, unit,businessid,idcard,deptid,deptname);
			List<Map<String, Object>> list = dubboBusiWatchmanService.findWatchman(businessid);
			
			for (int i = 0; i < list.size(); i++) {
				String receiverid = list.get(i).get("userid").toString();
				String receivername = list.get(i).get("username").toString();
				//调用dubbo接口
				NettyUserBean bean=new NettyUserBean();
				bean.setAppid(appId);
				bean.setBusitypeid(busiTypeId);
			    bean.setTitle("来访预约申请");//推送标题
			    bean.setContent("内容：申请人："+username+"，来访时间："+appointtime+"，来访事宜："+appointcontent);//推送内容
			    bean.setUserId(receiverid);//接收人id
			    bean.setUserName(receivername);//接收人姓名
			    PushResult result=dubboNettyService.sendToUser(bean);
			    System.out.println(result.getMsg());
			}
			return ResultUtils.success("预约成功", null);
		}catch(Exception e){
			return ResultUtils.error(e.getMessage());
		}
	}
	
	
	
	
	@RequestMapping("/findMyApply")
	public Result  findMyAppoint(HttpServletRequest request){
		try {
			UserInfoFront userInfo = UserInfoUtils.getBeanFront(request);
			String userId=userInfo.getId();
			String date = request.getParameter("date");
			if(date==null||"".equals(date)){
				date = DateUtils.getToday();
			}
			String type="1";
			List<Map<String, Object>> list = dubboVisitApplyInfoService.findMyApply(userId, date, type);
			return ResultUtils.success("查询成功", list);
		}catch(Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
//	@RequestMapping("/findMyCheck")
//	public Result  findMyCheck(HttpServletRequest request){
//		try {
//			UserInfoAdmin userInfo=UserInfoUtils.getBeanAdmin(request);
//			String userId=userInfo.getId();
//			String date = request.getParameter("date");
//			if(date==null||"".equals(date)){
//				date = DateUtils.getToday();
//			}
//			String type="0";
//			List<Map<String, Object>> list = dubboAppointmentService.findMyCheck(userId, date,type);
//			return ResultUtils.success("查询成功", list);
//		}catch(Exception e) {
//			return ResultUtils.error(e.getMessage());
//		}
//	}
	
	@RequestMapping("/findDetail")
	public Result  findDetail(HttpServletRequest request){
		try {
			String id = request.getParameter("id");
			Map<String, Object> map = dubboVisitApplyInfoService.findDetail(id);
			return ResultUtils.success("查询成功", map);
		}catch(Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/updateStatus")
	public Result  updateStatus(HttpServletRequest request){
		try {
			String id = request.getParameter("id");
			String status = request.getParameter("status");
			String msg="";
			if(status==null||"".equals(status)){
				throw new RuntimeException("status为空");
			}
			if(status.equals("2")){
				msg="离开成功";
				String leavemode="2";//离开方式（0.扫码离开，1.接待人结束,2.预约人结束）
				dubboVisitRegisterService.leave(id,leavemode);
			}
			dubboVisitApplyInfoService.updateStatus(id, status);
			return ResultUtils.success(msg, null);
		}catch(Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	
	@RequestMapping("/deptTree")
	public Result deptTree(){
		try {
			List<DeptInfo> list = dubboDeptService.findAll("");
			return ResultUtils.success("查询成功", list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/getUserByDeptId")
	public Result getUserByDeptId(String deptId){
		try {
			List<UserInfo> list = dubboUserService.getUserByDeptId(deptId);
			String today = DateUtils.getToday();
			List<Map<String, Object>> findTask = dubboVisitApplyInfoService.findTask(deptId, today);
			List<Map<String,Object>>newList=new ArrayList<Map<String,Object>>();
			for(int i=0;i<list.size();i++){
				String id = list.get(i).getId();
				String name = list.get(i).getUsername();
				Map<String,Object>map=new HashMap<String,Object>();
				map.put("userId", id);
				map.put("name", name);
				int num=0;
				map.put("num", num);
				for (int j = 0; j < findTask.size(); j++) {
					String recetionuserid = findTask.get(j).get("recetionuserid").toString();
					if(id.equals(recetionuserid)){
						num++;
						map.put("num", num);
					}
				}
				num=0;
				newList.add(i, map);
			}
			return ResultUtils.success("查询成功", newList);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findTask")
	public Result findTask(String userId){
		try {
			String today = DateUtils.getToday();
			List<Map<String, Object>> list = dubboVisitApplyInfoService.findTask(userId, today);
			return ResultUtils.success("查询成功", list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@CrossOrigin(origins="*",maxAge=3600)
	@ResponseBody
	@RequestMapping("/findQRcode")
	public void findQRcode(HttpServletRequest request,HttpServletResponse response,String id){
		try {
			String url = dubboVisitApplyInfoService.findQRcodeUrl(id);
	        //设置图片的文字编码以及内边框
	        Map<EncodeHintType, Object> hints = new HashMap<>();
	        //编码
	        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
	        //边框距
	        hints.put(EncodeHintType.MARGIN, 0);
	        BitMatrix bitMatrix;
	        //参数分别为：编码内容、编码类型、图片宽度、图片高度，设置参数
	        bitMatrix = new MultiFormatWriter().encode(url, BarcodeFormat.QR_CODE, 300, 300,hints);
	        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
	        ByteArrayOutputStream out = new ByteArrayOutputStream();
	        try {
	        	ImageIO.write(bufferedImage,"png", out);
			} catch (IOException  e) {
				e.getStackTrace();
			}
	        String today = DateUtils.getToday();
	        byte[] bytes = out.toByteArray();
	        out.close();
	        DownloadUtils.download(request, response, bytes, today+".png");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
