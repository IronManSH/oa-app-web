package cn.sino.controller.front;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import cn.sino.common.DateUtils;
import cn.sino.common.DownloadUtils;
import cn.sino.common.Result;
import cn.sino.common.ResultUtils;
import cn.sino.service.dubbo.setting.DubboApkManageService;

@RestController
@RequestMapping("/appfront/apk")
public class ApkManageFrontController {
	@Reference(check=false)
	private DubboApkManageService dubboApkManageService;

	
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
			String url="http://222.216.209.10:8880/oa-app-web";
			Map<String, Object> map = dubboApkManageService.downloadApp(type);
			Object apkurl = map.get("apkurl");
			String downloadurl=url+apkurl;
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
			String url="http://222.216.209.10:8880/oa-app-web";
			Map<String, Object> map = dubboApkManageService.downloadApp(type);
			Object apkurl = map.get("apkurl");
			String downloadurl=url+apkurl;
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
			String url="http://222.216.209.10:8880/oa-app-web";
			Map<String, Object> map = dubboApkManageService.downloadApp(type);
			Object apkurl = map.get("apkurl");
			String downloadurl=url+apkurl;
			System.out.println(downloadurl);
			return ResultUtils.success("查询成功", downloadurl);
		}catch(Exception e){
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@CrossOrigin(origins="*",maxAge=3600)
	@ResponseBody
	@RequestMapping("/withinApkQRcode")
	public void withinApkQRcode(HttpServletRequest request,HttpServletResponse response){
		try {
			String type="1";
			String url="http://222.216.209.10:8880/oa-app-web";
			Map<String, Object> map = dubboApkManageService.downloadApp(type);
			Object apkurl = map.get("apkurl");
			String downloadurl=url+apkurl;
			System.out.println(downloadurl);
	        //设置图片的文字编码以及内边框
	        Map<EncodeHintType, Object> hints = new HashMap<>();
	        //编码
	        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
	        //边框距
	        hints.put(EncodeHintType.MARGIN, 0);
	        BitMatrix bitMatrix;
	        //参数分别为：编码内容、编码类型、图片宽度、图片高度，设置参数
	        bitMatrix = new MultiFormatWriter().encode(downloadurl, BarcodeFormat.QR_CODE, 400, 400,hints);
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
	
	@CrossOrigin(origins="*",maxAge=3600)
	@ResponseBody
	@RequestMapping("/abroadApkQRcode")
	public void abroadApkQRcode(HttpServletRequest request,HttpServletResponse response){
		try {
			String type="0";
			String url="http://222.216.209.10:8880/oa-app-web";
			Map<String, Object> map = dubboApkManageService.downloadApp(type);
			Object apkurl = map.get("apkurl");
			String downloadurl=url+apkurl;
			System.out.println(downloadurl);
	        //设置图片的文字编码以及内边框
	        Map<EncodeHintType, Object> hints = new HashMap<>();
	        //编码
	        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
	        //边框距
	        hints.put(EncodeHintType.MARGIN, 0);
	        BitMatrix bitMatrix;
	        //参数分别为：编码内容、编码类型、图片宽度、图片高度，设置参数
	        bitMatrix = new MultiFormatWriter().encode(downloadurl, BarcodeFormat.QR_CODE, 400, 400,hints);
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
