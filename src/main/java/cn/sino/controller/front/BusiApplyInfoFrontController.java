package cn.sino.controller.front;


import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import cn.sino.common.DateUtils;
import cn.sino.common.DownloadUtils;
import cn.sino.common.Result;
import cn.sino.common.ResultUtils;
import cn.sino.mvc.UserInfoFront;
import cn.sino.mvc.UserInfoUtils;
import cn.sino.service.dubbo.appointment.DubboBusiApplyInfoService;
import cn.sino.service.dubbo.appointment.DubboLineUpService;

@RestController
@RequestMapping("/appfront/busi")
public class BusiApplyInfoFrontController {
	
	@Reference(check=false)
	private DubboBusiApplyInfoService dubboBusiApplyInfoService;
	@Reference(check=false)
	private DubboLineUpService dubboLineUpService;
	@RequestMapping("/apply")
	public Result apply(HttpServletRequest request){
		try {
			UserInfoFront userInfo = UserInfoUtils.getBeanFront(request);
			String address = userInfo.getAddress();
//			/*if(address==null||"".equals(address)){
//				throw new RuntimeException("请先完善个人地址再申请");
//			}*/
			String userid = userInfo.getId();
			String idcard = userInfo.getIdcard();
			String username = userInfo.getName();
			String phone = userInfo.getTelephone();
			String businessid = request.getParameter("businessid");
			String appointtime = request.getParameter("appointtime");
			String timecode = request.getParameter("timecode");
			String time1 = appointtime.split("\\(")[0];
			String time2 = DateUtils.getToday();
			int compareTo = time1.compareTo(time2);
			if(compareTo==0){
				String hour= DateUtils.getCurrentTime().split(" ")[1];
				String str = hour.substring(0, 2);
				int a = Integer.parseInt(str);
				if (a>=12) {
					if(timecode.equals("am")){
						throw new RuntimeException("该时间段已过，请选择其他时间段");
					}
				}
				if (a>=18) {
					if(timecode.equals("pm")){
						throw new RuntimeException("该时间段已过，请选择其他时间段");
					}
				}
			}
			boolean whetherAppoint = dubboBusiApplyInfoService.whetherAppoint(businessid, idcard, appointtime);
			if(whetherAppoint){
				throw new RuntimeException("该时间段已预约该业务");
			}
			String businessname = request.getParameter("businessname");
			String windowid = request.getParameter("windowid");
			String windowname = request.getParameter("windowname");
			
			String applyid = dubboBusiApplyInfoService.apply(userid, username,idcard,businessid, windowid,windowname,businessname, appointtime,phone,address);
			dubboLineUpService.add(windowid, businessid, businessname, applyid, username, appointtime,timecode,idcard);
			return ResultUtils.success("预约成功", null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findMyApply")
	public Result findMyApply(HttpServletRequest request){
		try {
			UserInfoFront userInfo = UserInfoUtils.getBeanFront(request);
			String userId = userInfo.getId();
			String date = request.getParameter("date");
			if(date==null||"".equals(date)){
				date = DateUtils.getToday();
			}
			List<Map<String, Object>> list = dubboBusiApplyInfoService.findMyApply(userId, date);
			return ResultUtils.success("查询成功", list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findAll")
	public Result findAll(HttpServletRequest request){
		try {
			String date = request.getParameter("date");
			if(date==null||"".equals(date)){
				date = DateUtils.getToday();
			}
			List<Map<String, Object>> list = dubboBusiApplyInfoService.findMyApply("", date);
			return ResultUtils.success("查询成功", list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	
	
	@RequestMapping("/findDetail")
	public Result findDetail(HttpServletRequest request,String id){
		try {
			Map<String, Object> map = dubboBusiApplyInfoService.findDetail(id);
			return ResultUtils.success("查询成功", map);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	
	@RequestMapping("/findQRcode")
	public void findQRcodeUrl(HttpServletRequest request,HttpServletResponse response,String id){
		try {
			String url = dubboBusiApplyInfoService.findQRcodeUrl(id);
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
	
	@RequestMapping("/updataStatus")
	public Result updataStatus(HttpServletRequest request,String id,String status){
		try {
			dubboBusiApplyInfoService.updataStatus(id, status,"");
			return ResultUtils.success("更新成功", null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
}
