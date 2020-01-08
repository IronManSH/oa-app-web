package cn.sino.controller.admin;

import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;

import cn.sino.common.Result;
import cn.sino.common.ResultUtils;
import cn.sino.service.dubbo.setting.DubboApkManageService;

@RestController
@RequestMapping("/app/apk")
public class ApkManageAdminController {
	@Reference(check=false,timeout=60000,retries=0)
	private DubboApkManageService dubboApkManageServiec;
	
	@RequestMapping("/findIsLastest")
	public Result findIsLastest(String apktag,String type) {
		try{
			Map<String, Object> map = dubboApkManageServiec.findIsLastest(apktag,type);
			return ResultUtils.success("查询成功", map);
		}catch(Exception e){
			return ResultUtils.error(e.getMessage());
		}
	}
}
