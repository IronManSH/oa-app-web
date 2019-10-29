package cn.sino.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * spring mvc的扩展配置
 * @author 郑伟业
 * 2018年10月16日
 */
@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter{
	@Autowired
	private InterceptorAdmin interceptorAdmin;
	@Autowired
	private InterceptorFront interceptorFront;
	
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**").allowedOrigins("*").allowCredentials(true).maxAge(3600).allowedMethods("GET", "POST","OPTIONS");
	}
	
	
	@Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 当访问/apk下的资源时，会到/data/apk/下去找
        // 例如访问：http://localhost:8080/file/1.png时会去找/root/apk/1.png
		registry.addResourceHandler("/img/**").addResourceLocations("file:/data/img/");
        registry.addResourceHandler("/apk/**").addResourceLocations("file:/data/apk/");
        super.addResourceHandlers(registry);
    }
	
	/**
	 * app拦截器
	 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		//拦截后台app
		registry.addInterceptor(interceptorAdmin)
			.addPathPatterns("/app/**")
			.excludePathPatterns("/app/apk/download")//下载APP方法放开
			.excludePathPatterns("/app/apk/findIsLastest")//检查更新APP方法放开
			.excludePathPatterns("/app/lineUp/findList")//窗口排队情况
			.excludePathPatterns("/app/window/findAllWindow")//获取所有窗口
			.excludePathPatterns("/app/user/login");//登录方法放开拦截
			
		
		//拦截前台app
		registry.addInterceptor(interceptorFront)
		.addPathPatterns("/appfront/**")
		.excludePathPatterns("/appfront/user/login")//登录方法放开拦截
		.excludePathPatterns("/appfront/user/regedit")//注册方法放开拦截
		.excludePathPatterns("/appfront/user/regeditLaw")//注册方法放开拦截
		.excludePathPatterns("/appfront/apk/download")//下载APP方法放开
		.excludePathPatterns("/appfront/apk/findIsLastest")//检查更新APP方法放开
		.excludePathPatterns("/appfront/visitRegister/register")//二维码登记
		.excludePathPatterns("/appfront/information/findDetails")//咨询详情放开
		.excludePathPatterns("/appfront/activity/findDetails")//公告详情放开
		.excludePathPatterns("/appfront/apk/withinAppUrl")//内网app下载链接
		.excludePathPatterns("/appfront/apk/abroadAppUrl")//外网app下载链接
		.excludePathPatterns("/appfront/apk/withinApkQRcode")//下载内网app二维码
		.excludePathPatterns("/appfront/apk/abroadApkQRcode")//下载外网app二维码
		.excludePathPatterns("/appfront/apk/downloadLED")//下载LEDapp
		.excludePathPatterns("/appfront/activity/signup")//报名
		.excludePathPatterns("/appfront/activity/register")//活动入场二维码
		.excludePathPatterns("/appfront/activity/findSignupDetails")//报名详情
		.excludePathPatterns("/appfront/activity/downloadfile")//活动附件下载
		.excludePathPatterns("/appfront/user/findByPid");//获取地级市
		super.addInterceptors(registry);
	}
}
