package cn.sino.controller.front;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

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
import cn.sino.service.dubbo.appointment.DubboCaseManageService;
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
	private DubboVisitCheckInfoService dubboVisitCheckInfoService;
	@Reference(check=false)
	private DubboLineUpService dubboLineUpService;
	@Reference(check=false)
	private FileInfoBusiApiService fileInfoBusiApiService;
	
	@Reference(check=false)
	private DubboBusiApplyInfoService dubboBusiApplyInfoService;
	@Reference(check=false)
	private DubboCaseManageService dubboCaseManageService;
	
	
	@CrossOrigin(origins="*",maxAge=5000)
	@RequestMapping("/apply")
	public Result apply(HttpServletRequest request,MultipartHttpServletRequest multipartRequest) {
		try{
			
			UserInfoFront userInfo = UserInfoUtils.getBeanFront(request);
			String userid = userInfo.getId();
			String username = userInfo.getName();
			String phone = userInfo.getTelephone();
			String idcard = userInfo.getIdcard();
			String trialstatus = request.getParameter("trialstatus");
			String duration = request.getParameter("duration");
			String businessid = request.getParameter("businessid");
			String casename = request.getParameter("casename");
			if(casename==null||"".equals(casename)){
				throw new RuntimeException("案件名为空");
			}
			if(duration==null||"".equals(duration)){
				throw new RuntimeException("时效为空");
			}
			if(trialstatus==null||"".equals(trialstatus)){
				throw new RuntimeException("审件类型为空");
				
			}
			
			//匹配案件
			Map<String,Object> map = dubboCaseManageService.matchingCases(casename);
			List<MultipartFile> idcardfiles=null;
			List<MultipartFile> otherfiles=null;
			int matchingstatus=0;//
			int relationstatus=0;//关联状态：0.关联，1.不关联
			if(map!=null||"".equals(map)){
				matchingstatus=0;//案件匹配状态：0.匹配
			}else{
				matchingstatus=1;//案件匹配状态：1.不匹配
				idcardfiles = multipartRequest.getFiles("idcards");
				otherfiles = multipartRequest.getFiles("others");
				if(idcardfiles==null||idcardfiles.size()==0||otherfiles==null||otherfiles.size()==0){
					throw new RuntimeException("未找到与其匹配的案件，请上传身份证等其他相关材料");
				}
			}
			
			String id = dubboLawReadInfoService.apply(userid, username, phone, idcard, casename,
					trialstatus, businessid, matchingstatus,relationstatus,duration);
			//如果不匹配就上传相关材料
			if(matchingstatus==1){//案件匹配状态：1.不匹配
				String filename=null;
				byte []filebyte=null;
				for(MultipartFile i:idcardfiles){
					filename = i.getOriginalFilename();
					filebyte=i.getBytes();
					fileInfoBusiApiService.uploadMulti(filebyte, "", filename, id, id, "idcard");
				}
				
				for(MultipartFile o:otherfiles){
					filename = o.getOriginalFilename();
					filebyte=o.getBytes();
					fileInfoBusiApiService.uploadMulti(filebyte, "", filename, id, id, "other");
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
