package cn.sino.controller.admin;

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

import cn.sino.common.DownloadUtils;
import cn.sino.common.PageInfo;
import cn.sino.common.Result;
import cn.sino.common.ResultUtils;
import cn.sino.service.dubbo.maintain.DubboMaintainService;

@RestController
@RequestMapping("/app/fixed")
public class FixedAssetQRCodeController {
	
	@Reference(check=false)
	private DubboMaintainService dubboMaintainService;
	
	@RequestMapping("/findAllList")
	public Result findAllList(HttpServletRequest request,PageInfo<Map<String,Object>>pageInfo){
		try {
			String status = request.getParameter("status");
			PageInfo<Map<String, Object>> pi = dubboMaintainService.findAllList(pageInfo, status);
			return ResultUtils.success("查询成功", pi);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/generate")
	public Result generate(HttpServletRequest request,HttpServletResponse response){
		try {
			String code = request.getParameter("code");
			String address = request.getParameter("address");
			String id = request.getParameter("id");
			String url=address+"/applydeal/findInfo?code="+code;
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
	        byte[] coderByte = out.toByteArray();
	        out.close();
	        dubboMaintainService.save(id,code, coderByte);
			return ResultUtils.success("生成成功", null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/downloadCode")
	@CrossOrigin(origins="*",maxAge=3600)
	@ResponseBody
	public void showPhotos(HttpServletRequest request,HttpServletResponse response,String id){
		byte[] bytes=null;
		try{
			Map<String, Object> map = dubboMaintainService.getCodeByte(id);
			bytes=(byte[]) map.get("codeByte");
			String fileName = map.get("fileName").toString();
			DownloadUtils.download(request, response, bytes, fileName+".png");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
