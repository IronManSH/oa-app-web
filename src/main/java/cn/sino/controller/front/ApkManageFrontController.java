package cn.sino.controller.front;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;

import cn.sino.common.DateUtils;
import cn.sino.common.DownloadUtils;
import cn.sino.common.QRcodeUtils;
import cn.sino.common.Result;
import cn.sino.common.ResultUtils;
import cn.sino.service.dubbo.setting.DubboApkManageService;

@RestController
@RequestMapping("/appfront/apk")
public class ApkManageFrontController {
	@Reference(check=false)
	private DubboApkManageService dubboApkManageService;
	
	@Value("${appaddress}")
	private String appaddress;
	
	@RequestMapping("/findIsLastest")
	public Result findIsLastest(String apktag,String type) {
		try{
			Map<String, Object> map = dubboApkManageService.findIsLastest(apktag,type);
			return ResultUtils.success("查询成功", map);
		}catch(Exception e){
			return ResultUtils.error(e.getMessage());
		}
		
	}
	
	@RequestMapping("/abroadAppUrl")
	public Result abroadAppUrl() {
		try{
			String type="0";
			Map<String, Object> map = dubboApkManageService.downloadApp(type);
			Object apkurl = map.get("apkurl");
			String downloadurl=appaddress+apkurl;
			System.out.println(downloadurl);
			return ResultUtils.success("查询成功", downloadurl);
		}catch(Exception e){
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/withinAppUrl")
	public Result withinAppUrl() {
		try{
			String type="1";
			Map<String, Object> map = dubboApkManageService.downloadApp(type);
			Object apkurl = map.get("apkurl");
			String downloadurl=appaddress+apkurl;
			System.out.println(downloadurl);
			return ResultUtils.success("查询成功", downloadurl);
		}catch(Exception e){
			return ResultUtils.error(e.getMessage());
		}
	}
	@RequestMapping("/downloadLED")
	public Result downloadLED() {
		try{
			String type="2";
			Map<String, Object> map = dubboApkManageService.downloadApp(type);
			Object apkurl = map.get("apkurl");
			String downloadurl=appaddress+apkurl;
			System.out.println(downloadurl);
			return ResultUtils.success("查询成功", downloadurl);
		}catch(Exception e){
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@CrossOrigin(origins="*",maxAge=3600)
	@RequestMapping("/withinApkQRcode")
	public void withinApkQRcode(HttpServletRequest request,HttpServletResponse response){
		try {
			String type="1";
			Map<String, Object> map = dubboApkManageService.downloadApp(type);
			Object apkurl = map.get("apkurl");
			String downloadurl=appaddress+apkurl;
			System.out.println(downloadurl);
	        byte[] bytes = QRcodeUtils.getByte(downloadurl);
	        String today = DateUtils.getToday();
	        DownloadUtils.download(request, response, bytes, today+".png");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@CrossOrigin(origins="*",maxAge=3600)
	@RequestMapping("/abroadApkQRcode")
	public void abroadApkQRcode(HttpServletRequest request,HttpServletResponse response){
		try {
			String type="0";
			Map<String, Object> map = dubboApkManageService.downloadApp(type);
			Object apkurl = map.get("apkurl");
			String downloadurl=appaddress+apkurl;
			System.out.println(downloadurl);
	        String today = DateUtils.getToday();
	        byte[] bytes = QRcodeUtils.getByte(downloadurl);
	        DownloadUtils.download(request, response, bytes, today+".png");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
