package cn.sino.controller.front;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSONObject;
import com.sinosoft.api.pojo.FileInfoBusiBean;
import com.sinosoft.api.service.FileInfoBusiApiService;

import cn.sino.common.DownloadUtils;
import cn.sino.common.Result;
import cn.sino.common.ResultUtils;
import cn.sino.mvc.UserInfoFront;
import cn.sino.mvc.UserInfoUtils;
import cn.sino.service.dubbo.appointment.DubboLawReadInfoService;

@RestController
@RequestMapping("/appfront/lawReadInfo")
public class LawReadInfoFrontController {
	@Reference(check=false)
	private DubboLawReadInfoService dubboLawReadInfoService;
	@Reference(check=false)
	private FileInfoBusiApiService fileInfoBusiApiService;
	
	@RequestMapping("/apply")
	public Result apply(HttpServletRequest request,List<MultipartFile> clientbookfile) {
		try{
			
			UserInfoFront userInfo = UserInfoUtils.getBeanFront(request);
			String userid = userInfo.getId();
			String username = userInfo.getName();
			String phone = userInfo.getTelephone();
			String idcard = userInfo.getIdcard();
			String clientname = request.getParameter("clientname");
			String casecontent = request.getParameter("casecontent");
			String businessid = request.getParameter("businessid");
			String casename = request.getParameter("casename");
			if(casename==null||"".equals(casename)){
				throw new RuntimeException("案件名为空");
			}
			if(clientname==null||"".equals(clientname)){
				throw new RuntimeException("委托人为空");
			}
			if(casecontent==null||"".equals(casecontent)){
				throw new RuntimeException("案件内容为空");
				
			}
			if(businessid==null||"".equals(businessid)){
				throw new RuntimeException("业务id为空");
			}
			if(clientbookfile==null||clientbookfile.size()==0){
				throw new RuntimeException("委托书照片为空");
			}
			String id = dubboLawReadInfoService.apply(userid, username, phone, idcard, casename,
					casecontent, clientname, businessid);
			//上传委托书照片
			String filename=null;
			byte []filebyte=null;
			for(MultipartFile i:clientbookfile){
				filename = i.getOriginalFilename();
				filebyte = i.getBytes();
				Result result = fileInfoBusiApiService.uploadMulti(filebyte, "",filename , userid, id, "clientbook");
				Integer code = result.getCode();
				if(code!=0){
					throw new RuntimeException("上传委托书失败，"+result.getMsg());
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
			if(id==null||"".equals(id)){
				throw new RuntimeException("id为空");
			}
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
	
	@RequestMapping("/addPhoto")
	public Result addPhoto(HttpServletRequest request,MultipartFile file){
		try {
			String businessid = request.getParameter("businessid");
			String code = request.getParameter("code");
			byte[] photoByte = file.getBytes();
			dubboLawReadInfoService.addPhoto(businessid, photoByte, code);
			return ResultUtils.success("添加成功", null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	@RequestMapping("/findfile")
	public Result findfile(HttpServletRequest request,HttpServletResponse response){
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
	@RequestMapping("/uploadFiles")
	public Result uploadFiles(HttpServletRequest request,MultipartFile file){
		try {
			UserInfoFront userinfo = UserInfoUtils.getBeanFront(request);
			String userid = userinfo.getId();
			byte[] bytes = file.getBytes();
			String fileName = file.getOriginalFilename();
			Map<String,Object> map=new HashMap<String,Object>();
			//String encodeBase64String = Base64.encodeBase64String(bytes);
			map.put("bytes",bytes);
			map.put("fileName", fileName);
			String jsonString = JSONObject.toJSONString(map);
			Map parseObject = JSONObject.parseObject(jsonString, Map.class);
			byte[] bytes2 = Base64.decodeBase64(map.get("bytes").toString());
			String fileName2 = parseObject.get("fileName").toString();
			fileInfoBusiApiService.uploadMulti(bytes2, "", fileName2, userid, userid, "test");
			return ResultUtils.success("上传成功", null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}

}
