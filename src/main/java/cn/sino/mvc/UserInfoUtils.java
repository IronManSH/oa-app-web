package cn.sino.mvc;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import cn.sino.common.BeanUtils;

public class UserInfoUtils {
	public static UserInfoFront getBeanFront(HttpServletRequest request){
		Map<String,Object> map=(Map<String, Object>) request.getAttribute("userInfoFront");
		UserInfoFront userInfo=(UserInfoFront) BeanUtils.map2Bean(map, UserInfoFront.class);
		return userInfo;
	}
	public static UserInfoAdmin getBeanAdmin(HttpServletRequest request){
		Map<String,Object> map=(Map<String, Object>) request.getAttribute("userInfoAdmin");
		UserInfoAdmin userInfo=(UserInfoAdmin) BeanUtils.map2Bean(map, UserInfoAdmin.class);
		return userInfo;
	}
}
