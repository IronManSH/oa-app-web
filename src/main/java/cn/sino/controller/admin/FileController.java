package cn.sino.controller.admin;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.sinosoft.api.pojo.FileInfoBusiBean;
import com.sinosoft.api.service.FileInfoBusiApiService;

import cn.sino.common.Result;
import cn.sino.common.ResultUtils;

@RestController
@RequestMapping("/app/file")
public class FileController {
	@Reference(check=false)
	private FileInfoBusiApiService fileInfoBusiApiService;
	
	
	@ResponseBody
	@RequestMapping("/findFile")
	public Result findFile(HttpServletRequest request,String businessid,String businesstag){
		try {
			Result result = fileInfoBusiApiService.findByBusinessid(businessid, businesstag);
			List<FileInfoBusiBean> files=new ArrayList<FileInfoBusiBean>();
			if(result.getCode()==0){	
				files=(List<FileInfoBusiBean>) result.getData();
			}
			return ResultUtils.success("查询成功", files);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
		
	}

}
