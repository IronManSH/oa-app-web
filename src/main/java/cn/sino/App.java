package cn.sino;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

import com.alibaba.dubbo.spring.boot.annotation.EnableDubboConfiguration;
import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;

@SpringBootApplication
@EnableCaching
@EnableDubboConfiguration
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class}) //如果引入jpa的包，则需要加入这句话
@ComponentScan(basePackages={"cn.sino","com.micro"})
@NacosPropertySource(dataId = "oa-app-web",groupId="wzoa",autoRefreshed=true)
public class App{
	public static void main(String[] args){
		SpringApplication.run(App.class,args);
	}
}
