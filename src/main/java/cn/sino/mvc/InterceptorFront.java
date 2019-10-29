package cn.sino.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.dubbo.config.annotation.Reference;
import com.micro.model.UserInfo;
import com.micro.service.dubbo.user.DubboLoginService;

import cn.sino.utils.EhcacheUtil;

/**
 * app拦截器
 * @author 郑伟业
 * 2018年10月16日
 */
@Component
public class InterceptorFront implements HandlerInterceptor{
	@Autowired
	private EhcacheUtil ehcacheUtil;
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object obj) throws Exception {
		String origin = request.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Origin", origin);
		response.setHeader("Access-Control-Allow-Methods", "*");
		response.setHeader("Access-Control-Allow-Headers","Origin,Content-Type,Accept,token,X-Requested-With");
		response.setHeader("Access-Control-Allow-Credentials", "true");
	
		//判断ssoname是否为空
		String ssoname=request.getParameter("ssoname");
		if(StringUtils.isEmpty(ssoname)){
			throw new RuntimeException("传递的ssoname值为空");
		}
		//判断ssoname是否存在
		Object object= ehcacheUtil.get(ssoname);
		if(object==null){
			throw new RuntimeException("登录失效，请重新登录");
		}
		
		//存储
		request.setAttribute("userInfoFront",object);
				
		return true;
	}
	
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object obj, ModelAndView mv)
			throws Exception {
		
	}
	
	@Override
	public void afterCompletion(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, Exception arg3)
			throws Exception {
		
	}
}
