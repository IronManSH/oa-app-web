//package cn.sino.controller.front;
//
//import java.util.Map;
//
//import javax.servlet.http.HttpServletRequest;
//
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.alibaba.dubbo.config.annotation.Reference;
//
//import cn.sino.common.PageInfo;
//import cn.sino.common.Result;
//import cn.sino.common.ResultUtils;
//import cn.sino.service.dubbo.appoint.DubboNoticeService;
//
//@RestController
//@RequestMapping("/appfront/notice")
//public class NoticeFrontController {
//	@Reference(check=false)
//	private DubboNoticeService dubboNoticeService;
//	
//	
//	
//	
//	@RequestMapping("/findList")
//	public Result findList(PageInfo<Map<String,Object>> pageInfo,HttpServletRequest request){
//		try {
//			String bTime = request.getParameter("bTime");
//			String eTime = request.getParameter("eTime");
//			PageInfo<Map<String, Object>> pi = dubboNoticeService.findList(pageInfo,bTime, eTime);
//			return ResultUtils.success("查询成功", pi);
//		} catch (Exception e) {
//			return ResultUtils.error(e.getMessage());
//		}
//		
//	}
//	
//	@RequestMapping("/findDetail")
//	public Result findDetail(String id){
//		try {
//			 Map<String, Object> map = dubboNoticeService.findDetail(id);
//			return ResultUtils.success("查询成功", map);
//		} catch (Exception e) {
//			return ResultUtils.error(e.getMessage());
//		}
//		
//	}
//	
//	
//
//}
