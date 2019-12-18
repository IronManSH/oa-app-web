package cn.sino.controller.admin;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;

import cn.sino.common.DownloadUtils;
import cn.sino.common.PageInfo;
import cn.sino.common.QRcodeUtils;
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
			if(id==null||"".equals(id)){
				throw new RuntimeException("id为空");
			}
			if(code==null||"".equals(code)){
				throw new RuntimeException("code为空");
			}
			if(address==null||"".equals(address)){
				throw new RuntimeException("address为空");
			}
			String url=address+"/applydeal/findInfo?code="+code;
	        byte[] coderByte = QRcodeUtils.getByte(url);
	        dubboMaintainService.save(id,code, coderByte);
	        DownloadUtils.download(request, response, coderByte,"qrcode.png");
			return ResultUtils.success("生成成功", null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/downloadCode")
	@CrossOrigin(origins="*",maxAge=3600)
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
