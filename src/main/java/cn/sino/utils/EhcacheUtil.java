package cn.sino.utils;

import java.io.IOException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
@Component
public class EhcacheUtil {
	private static CacheManager manager; 
	private static EhcacheUtil ehCache;
	private static String cacheName="demo";
	
	static{
		if(ehCache==null){
			ClassPathResource resource = new ClassPathResource("ehcache.xml");
			try {
				manager=CacheManager.create(resource.getInputStream());
			} catch (CacheException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public void put(String key,Object value){
		Cache cache=manager.getCache(cacheName);
		Element element=new Element(key, value);
		cache.put(element);
	}

	public Object get(String key){
		Cache cache=manager.getCache(cacheName);
		Element element=cache.get(key);
		return element==null?null:element.getObjectValue();
	}
	
	public void remove(String key){
		Cache cache=manager.getCache(cacheName);
		cache.remove(key);
	}
}
