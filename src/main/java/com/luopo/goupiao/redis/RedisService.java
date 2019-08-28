package com.luopo.goupiao.redis;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.ArrayList;
import java.util.List;

@Service
public class RedisService {

    @Autowired
    JedisPool jedisPool;

    //获取缓存值
    //已经经过stringToBean()设置，返回的是一个反向对象
    public <T> T get(KeyPrefix prefix, String key, Class<T> clazz) {
        Jedis jedis = null;
        try {
            jedis =  jedisPool.getResource();

            //生成访问redis真正的key
            String realKey = prefix.getPrefix() + key;
            String  str = jedis.get(realKey);
            T t =  stringToBean(str, clazz);
            return t;
        }finally {
            returnToPool(jedis);
        }
    }

    //设置缓存值
    public <T> boolean set(KeyPrefix prefix, String key, T bean) {
        Jedis jedis = null;
        try {
            jedis =  jedisPool.getResource();
            String str = beanToString(bean);
            if(str == null || str.length() <= 0) {
                return false;
            }
            //生成访问redis真正的key
            String realKey  = prefix.getPrefix() + key;
            int seconds =  prefix.getExpireSeconds();
            //设置限时缓存 与 设置永久缓存
            if(seconds <= 0) {
                jedis.set(realKey, str);
            }
            else {
                jedis.setex(realKey, seconds, str);
            }
            return true;
        }finally {
            returnToPool(jedis);
        }
    }

    public <T> List<T> getList(KeyPrefix prefix, String key, Class<T> clazz) {
        Jedis jedis = null;
        try {
            jedis =  jedisPool.getResource();

            //生成访问redis真正的key
            String realKey = prefix.getPrefix() + key;
            String  str = jedis.get(realKey);
            List<T> t =  stringToBeanList(str, clazz);
            return t;
        }finally {
            returnToPool(jedis);
        }
    }

    //设置缓存值
    public <T> boolean setList(KeyPrefix prefix, String key, T bean) {
        return set(prefix, key, bean);
    }

    //key是否存在
    public <T> boolean exists(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis =  jedisPool.getResource();

            //生成访问redis真正的key
            String realKey  = prefix.getPrefix() + key;
            return  jedis.exists(realKey);
        }finally {
            returnToPool(jedis);
        }
    }

    //删除key对应相应缓存
    public boolean delete(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis =  jedisPool.getResource();

            //生成访问redis真正的key
            String realKey  = prefix.getPrefix() + key;
            return (jedis.del(realKey) > 0);
        }finally {
            returnToPool(jedis);
        }
    }

    //incr redis相应值自增1
    public <T> Long incr(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis =  jedisPool.getResource();

            //生成访问redis真正的key
            String realKey  = prefix.getPrefix() + key;
            return  jedis.incr(realKey);
        }finally {
            returnToPool(jedis);
        }
    }

    //decr redis相应值-1
    public <T> Long decr(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis =  jedisPool.getResource();

            //生成访问redis真正的key
            String realKey  = prefix.getPrefix() + key;
            return  jedis.decr(realKey);
        }finally {
            returnToPool(jedis);
        }
    }

    //删除prefix前缀下所有缓存
    public boolean delete(KeyPrefix prefix) {
        if(prefix == null) {
            return false;
        }

        List<String> keys = scanKeys(prefix.getPrefix());   //得到所有匹配key
        if(keys==null || keys.size() <= 0) {
            return true;
        }

        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.del(keys.toArray(new String[0]));
            return true;
        } catch (final Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if(jedis != null) {
                jedis.close();
            }
        }
    }

    //根据key值返回模式匹配下所有key
    //采用scan方式扫描，设置游标增量式读取以防止缓存数据内容较多造成异常
    public List<String> scanKeys(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            List<String> keys = new ArrayList<String>();
            String cursor = "0";    //scan的游标，为0时启动，再次为0时结束
                                    // 如果不为0需要在下次scan时传入上一次呢的游标返回值
            ScanParams sp = new ScanParams();   //scan参数
            sp.match("*"+key+"*");  //match参数：匹配方式，先scan后匹配
            sp.count(100);  //count参数：建议每一次scan扫描的数据条数，只是建议，但被执行肯性较大
            while (true) {
                ScanResult<String> ret = jedis.scan(cursor, sp);    //从游标cursor开始，按照参数扫描
                List<String> result = ret.getResult();  //返回为两个数组，一个数组为scan的部分结果集
                if(result!=null && result.size() > 0){
                    keys.addAll(result);
                }

                //处理cursor
                cursor = ret.getStringCursor(); //另一部分为游标

                if (cursor.equals("0")) //游标为0则scan结束
                    break;
            }
            return keys;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public static <T> String beanToString(T bean) {

        if(bean == null) { //参数校验，不为null
            return null;
        }

        Class<?> clazz = bean.getClass();
        if(clazz == int.class || clazz == Integer.class) {
            return ""+bean;
        }else if(clazz == long.class || clazz == Long.class) {
            return ""+bean;
        }else if(clazz == String.class) {
            return (String)bean;
        }else {
            return JSON.toJSONString(bean);
        }
    }

    public static <T> T stringToBean(String str, Class<T> clazz) {

        //参数校验，不为null
        if(str == null || str.length() <= 0 || clazz == null) {
            return null;
        }

        if(clazz == int.class || clazz == Integer.class) {
            return (T)Integer.valueOf(str);
        }else if(clazz == long.class || clazz == Long.class) {
            return (T)Long.valueOf(str);
        }else if(clazz == String.class) {
            return (T)str;
        }else {
            return JSON.toJavaObject(JSON.parseObject(str), clazz);
        }
    }

    public static <T> List<T> stringToBeanList(String str, Class<T> clazz) {

        //参数校验，不为null
        if(str == null || str.length() <= 0 || clazz == null) {
            return null;
        }

        return JSON.parseArray(str, clazz);
    }

    private void returnToPool(Jedis jedis) {
        if(jedis != null) {
            jedis.close();
        }
    }

}
