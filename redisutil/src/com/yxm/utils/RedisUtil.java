package com.yxm.utils;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yxm.bean.Message;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Redis工具类 工具类里面对外提供的方法和变量多为:静态
 * 
 * @author Administrator
 * 
 */
public class RedisUtil {

	private static Jedis jedis;// 连接

	private static String host = "127.0.0.1";// 主机号,默认是本地

	private static int port = 6379;// 端口号

	private static String password = "123456";// redis密码 认证

	private static boolean isPool = false;// 是否使用redisPool

	private static int timeout = 10000;// 设置默认超时时间10s

	private static JedisPool jedisPool;// redis池

	private static GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();

	protected static final Logger log = LoggerFactory
			.getLogger(RedisUtil.class);

	static {
		initRedis();
	}

	/**
	 * 默认构造方法重新,并且是私有的《所以不能够实例化
	 */
	private RedisUtil() {

	}

	/**
	 * 初始化redis操作
	 */
	private static void initRedis() {
		Properties properties = new Properties();
		try {
			// 获取输入流
			InputStream in = RedisUtil.class
					.getResourceAsStream("/redis.properties");
			if (in == null) {
				// 开始执行默认的配置,使用单例redis
				jedis = new Jedis();
				log.info("没有可加载的redis.properties文件,请确保路径是否正确");
			} else {
				// 加载到properties
				properties.load(in);

				// 判断是否使用pool如果不使用就用单例的redis
				String proisPool = properties.getProperty("redis.isPool");
				String proHost = properties.getProperty("redis.host");
				String proPort = properties.getProperty("redis.port");
				String proPassword = properties.getProperty("redis.password");

				// 设置变量
				if (proHost != null && proPort != null && proPassword != null) {
					host = proHost;
					port = Integer.parseInt(proPort);
					password = proPassword;
				}

				// 设置缓存redis池
				if (proisPool != null && "true".equals(proisPool.trim())) {
					isPool = true;
					log.info("RedisPool开始创建");
					jedisPool = new JedisPool();
					/**
					 * 设置默认的redis
					 */
					jedis = jedisPool.getResource();
				} else {// 开始单例的redis
					log.info("开始单例的redis");
					if (proHost != null && proPort != null) {
						host = proHost;
						port = Integer.valueOf(proPort);
					}
					jedis = new Jedis(host, port);
				}
			}
		} catch (Exception e) {
			log.info("加载的redis.properties文失败");
			e.printStackTrace();
		}
	}

	/**
	 * 获取jedis实例
	 * 
	 * @return
	 */
	public static Jedis getJedis() {
		if (isPool) {
			if (jedisPool == null) {
				jedisPool = new JedisPool(poolConfig, host, port, timeout,
						password);
			}
			return jedisPool.getResource();
		} else {
			if (jedis == null) {
				jedis = new Jedis(host, port);
			}
			return jedis;
		}
	}

	/*
	 * 将String设置到redis中
	 * @param key
	 * @param value
	 */
	public static void setString(String key, String value) {
       getJedis().set(key, value);
	}
	/**
	 * 通过键获取String
	 * @param key
	 * @param value
	 * @return
	 */
	public static String getString(String key){
		return getJedis().get(key);
	}
	/**
	 * 将List数组存放进redis中
	 * @param key
	 * @param list
	 * @param isHead
	 */
	public static void setList(String key,List<String> list,boolean isHead){
		String[] arr = new String[list.size()];
		list.toArray(arr);
		//判断是否从 头开始插入
		if(isHead){
			getJedis().lpush(key, arr);
		}else{
			getJedis().rpush(key, arr);
		}
	}
	/**
	 * 向redis里面添加对象
	 * @param key
	 * @param obj
	 */
	public static void addObjToList(String key,Object obj){
		try {
				byte[] bytes = ObjectUtil.objectToBytes(obj);
				getJedis().rpush(key.getBytes(), bytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 从redis里面取出所有对象并清空
	 * @param key
	 * @return
	 */
	public static <T> List<T> getAllObjFromList(String key){
		List<T> dataList = new ArrayList<T>();
		 try {
				if(getJedis()==null || !getJedis().exists(key.getBytes())){
					return null;
				}
				Long llen = getJedis().llen(key.getBytes());
				for (int i = 0; i < llen; i++) {
					byte[] bytes = getJedis().lpop(key.getBytes());
					T obj= ObjectUtil.bytesToObject(bytes);
					dataList.add(obj);
				}
				return dataList;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
	}
	/**
	 * 获取redis中数据(对于对象的list方法1：把List转换成JSON，存储到Redis，取出来的时候，再把JSON转换成List)
	 * @param key 键
	 * @param start 开始
	 * @param end 结束
	 * @return
	 */
	public static List<String> getList(String key,long start,long end){
		return getJedis().lrange(key, start, end);
	}
	
	/**
	 * 将对象存放到redis中
	 * @param key
	 * @param obj
	 * @throws Exception
	 */
	public static void setObject(String key,Object obj) throws Exception{
		if(obj!=null){
			byte[] bytes = ObjectUtil.objectToBytes(obj);
			getJedis().set(key.getBytes(), bytes);
		}else{
			log.error("not found the Object");
			throw new NullPointerException();
		}
	}
	/**
	 * 获取redis里面对象
	 * @param key
	 * @param clazz
	 * @return
	 * @throws Exception
	 */
	public static<T extends Serializable> T getObject(String key,Class<T> clazz) throws Exception{
		byte[] bytes = getJedis().get(key.getBytes());
		@SuppressWarnings("unchecked")
		T object = (T) ObjectUtil.bytesToObject(bytes);
		return object;
	}
	
	/**
	 * 设置list
	 * @param <T>
	 * @throws Exception 
	 */
	public static <T> void setList(String key,List<T> list){
		try {
			getJedis().set(key.getBytes(),ObjectUtil.objectToBytes(list));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 获取
	 * @param key
	 * @return
	 */
	public static <T> List<T> getList(String key){
	  try {
			if(getJedis()==null || !getJedis().exists(key.getBytes())){
				return null;
			}
			List<T> dataList = ObjectUtil.bytesToObject(getJedis().get(key.getBytes()));
			getJedis().del(key);
			return dataList;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
