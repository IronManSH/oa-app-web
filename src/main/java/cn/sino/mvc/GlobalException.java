package cn.sino.mvc;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.sino.common.Result;
import cn.sino.common.ResultUtils;

/**
 * 全局异常
 * @author 郑伟业
 * 2018年10月16日
 */
@ControllerAdvice
public class GlobalException {
	@ExceptionHandler(RuntimeException.class) //异常类型
	@ResponseBody
	public Result defaultExceptionHandler(HttpServletRequest req,Exception e){
		
		return ResultUtils.error(e.getMessage());
	}
}
