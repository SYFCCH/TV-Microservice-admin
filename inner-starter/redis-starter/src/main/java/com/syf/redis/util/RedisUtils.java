package com.syf.redis.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;


import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * Redis的配置类
 * @author syf
 */
@Configuration
public class RedisUtils {

    private static final Logger log = LoggerFactory.getLogger(RedisUtils.class);

    private final RedisTemplate<String,Object> redisTemplate;


    public RedisUtils() {
        redisTemplate = new RedisTemplate<>();
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        //3.允许访问对象中所有属性
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        //4.转换json过程中保存类的信息
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.WRAPPER_ARRAY);
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        //设置key的序列化方式
        redisTemplate.setKeySerializer(stringRedisSerializer);
        //设置value的序列化方式
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        //7工厂创建redisTemplate对象之后在进行配置
        redisTemplate.afterPropertiesSet();
    }

    /**
     * 指定缓存失效时间
     *
     * @param key  键
     * @param time 秒
     */
    public void expire(String key, long time) {
        if (time > 0) {
            Boolean res = redisTemplate.expire(key, time, TimeUnit.MINUTES);
            log.info("指定缓存失效时间：" + key + "，结果：" + res);
        }
    }

    /**
     * 指定缓存失效时间
     *
     * @param key      键
     * @param time     秒
     * @param timeUnit 单位
     */
    public void expire(String key, long time, TimeUnit timeUnit) {
        if (time > 0) {
            redisTemplate.expire(key, time, timeUnit);
        }
    }

    /**
     * 获取过期时间
     *
     * @param key 键 不能为null
     * @return 单位：秒，0代表永久有效，-2代表无效key
     */
    public long getExpire(String key) {
        if (null == key) {
            return -2L;
        }
        Long res = redisTemplate.getExpire(key, TimeUnit.MINUTES);
        if (null == res) {
            return -2L;
        }
        return res;
    }

    /**
     * 查找匹配key
     *
     * @param pattern key，*key*，*key，key*
     * @return 匹配的所有key
     */
    public List<String> scan(String pattern) {
        ScanOptions options = ScanOptions.scanOptions().match(pattern).build();
        RedisConnectionFactory factory = redisTemplate.getConnectionFactory();
        RedisConnection rc = Objects.requireNonNull(factory).getConnection();
        Cursor<byte[]> cursor = rc.scan(options);
        List<String> result = new ArrayList<>();
        while (cursor.hasNext()) {
            result.add(new String(cursor.next()));
        }
        RedisConnectionUtils.releaseConnection(rc, factory, true);
        return result;
    }

    /**
     * 分页查询 key
     *
     * @param patternKey key
     * @param page       页码，0开始
     * @param size       每页数目
     * @return 没有返回size=0数组
     */
    public List<String> findKeysPage(String patternKey, int page, int size) {
        ScanOptions options = ScanOptions.scanOptions().match(patternKey).build();
        RedisConnectionFactory factory = redisTemplate.getConnectionFactory();
        RedisConnection rc = Objects.requireNonNull(factory).getConnection();
        Cursor<byte[]> cursor = rc.scan(options);
        List<String> result = new ArrayList<>(size);
        int tmpIndex = 0;
        int fromIndex = page * size;
        int toIndex = page * size + size;
        while (cursor.hasNext()) {
            if (tmpIndex >= fromIndex && tmpIndex < toIndex) {
                result.add(new String(cursor.next()));
                tmpIndex++;
                continue;
            }
            // 获取到满足条件的数据后,就可以退出了
            if (tmpIndex >= toIndex) {
                break;
            }
            tmpIndex++;
            cursor.next();
        }
        RedisConnectionUtils.releaseConnection(rc, factory, true);
        return result;
    }

    /**
     * 判断key是否存在
     *
     * @param key 键
     * @return true-存在，false-不存在
     */
    public boolean hasKey(String key) {
        Boolean res = redisTemplate.hasKey(key);
        if (null == res) {
            return false;
        }
        return res;
    }

    /**
     * 删除缓存，有几个key就删除几个
     *
     * @param keys 可以传一个值或多个
     */
    public void del(String... keys) {
        if (keys != null && keys.length > 0) {
            Set<String> keySet = new HashSet<>(Arrays.asList(keys));
            Long count = redisTemplate.delete(keySet);
            log.info("计划删除缓存：" + keySet + "，成功删除：" + count + "个");
        }
    }

    // ===================================Key Value===================================

    /**
     * 获取
     *
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 批量获取
     *
     * @param keys 键
     * @return 值
     */
    public List<Object> multiGet(Set<String> keys) {
        return redisTemplate.opsForValue().multiGet(keys);
    }

    /**
     * 存，无限期
     *
     * @param key   键
     * @param value 值
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 存，并设置时间
     *
     * @param key   键
     * @param value 值
     * @param time  单位：秒，如果time小于等于0将设置无限期
     */
    public void set(String key, Object value, long time) {
        if (time > 0) {
            set(key, value, time, TimeUnit.SECONDS);
        } else {
            set(key, value);
        }
    }

    /**
     * 存，并设置时间
     *
     * @param key      键
     * @param value    值
     * @param time     时间
     * @param timeUnit 类型
     */
    public void set(String key, Object value, long time, TimeUnit timeUnit) {
        if (time > 0) {
            redisTemplate.opsForValue().set(key, value, time, timeUnit);
        } else {
            set(key, value);
        }
    }

    // ================================Hash=================================

    /**
     * 判断hash表中是否有该项的值
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     * @return true-存在，false-不存在
     */
    public boolean hHasKey(String key, String item) {
        return redisTemplate.opsForHash().hasKey(key, item);
    }

    /**
     * Hash，Get
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     * @return 值
     */
    public Object hget(String key, String item) {
        return redisTemplate.opsForHash().get(key, item);
    }

    /**
     * Hash，获取所有键值
     *
     * @param key 键
     * @return 对应的多个键值
     */
    public Map<Object, Object> hEntries(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * Hash，存
     *
     * @param key 键
     * @param map 对应多个键值
     */
    public void hPutAll(String key, Map<String, Object> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    /**
     * Hash，存，并设置时间
     *
     * @param key  键
     * @param map  对应多个键值
     * @param time 单位：秒
     */
    public void hPutAll(String key, Map<String, Object> map, long time) {
        redisTemplate.opsForHash().putAll(key, map);
        if (time > 0) {
            expire(key, time);
        }
    }

    /**
     * Hash，存项
     *
     * @param key   键
     * @param item  项
     * @param value 值
     */
    public void hPut(String key, String item, Object value) {
        redisTemplate.opsForHash().put(key, item, value);
    }

    /**
     * Hash，删除
     *
     * @param key  键 不能为null
     * @param item 项 可以使多个不能为null
     */
    public void hdel(String key, Object... item) {
        Long num = redisTemplate.opsForHash().delete(key, item);
        log.info("Hash计划删除key：" + key + "，item：" + Arrays.toString(item) + "，实际删除：" + num + "个");
    }

    /**
     * hash递增，仅适用于value是数字的值，如果不存在就创建
     *
     * @param key  键
     * @param item 项
     * @param by   要增加数
     * @return 新增后的值
     */
    public double hincr(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(key, item, by);
    }

    /**
     * hash递减，仅适用于value是数字的值，如果不存在就创建
     *
     * @param key  键
     * @param item 项
     * @param by   要减少数
     * @return 递减后的值
     */
    public double hdecr(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(key, item, -by);
    }

    // ============================Set=============================

    /**
     * Set，根据key获取Set中的所有值
     *
     * @param key 键
     * @return Set中的所有值
     */
    public Set<Object> setMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * Set，是否存在某个值
     *
     * @param key   键
     * @param value 值
     * @return true-存在，false-不存在
     */
    public boolean setIsMember(String key, Object value) {
        Boolean isMember = redisTemplate.opsForSet().isMember(key, value);
        if (null == isMember) {
            return false;
        }
        return isMember;
    }

    /**
     * Set，长度
     *
     * @param key 键
     * @return 长度
     */
    public long setSize(String key) {
        Long size = redisTemplate.opsForSet().size(key);
        if (null == size) {
            return 0;
        }
        return size;
    }

    /**
     * Set，存
     *
     * @param key    键
     * @param values 值，可以是多个
     * @return 成功存入个数
     */
    public long setAdd(String key, Object... values) {
        Long num = redisTemplate.opsForSet().add(key, values);
        if (null == num) {
            return 0;
        }
        return num;
    }

    /**
     * Set，存，并设置过期时间
     *
     * @param key    键
     * @param time   时间(秒)
     * @param values 值 可以是多个
     * @return 成功存入个数
     */
    public long setAddTime(String key, long time, Object... values) {
        Long count = redisTemplate.opsForSet().add(key, values);
        if (time > 0) {
            expire(key, time);
        }
        if (null == count) {
            return 0;
        }
        return count;
    }

    /**
     * Set，弹出
     *
     * @param key key
     * @return 一个值
     */
    public Object setPop(String key) {
        return redisTemplate.opsForSet().pop(key);
    }

    /**
     * Set，移除值为value的值
     *
     * @param key    键
     * @param values 值，可以是多个
     * @return 实际移除的个数
     */
    public long setRemove(String key, Object... values) {
        Long count = redisTemplate.opsForSet().remove(key, values);
        log.info("Set计划删除key：" + key + "，values：" + Arrays.toString(values) + "，实际删除：" + count + "个");
        if (null == count) {
            return 0;
        }
        return count;
    }

    // ===============================List=================================

    /**
     * List，获取
     *
     * @param key   键
     * @param start 开始，包含，0开始
     * @param end   结束，不包含
     * @return 数据集合
     */
    public List<Object> listRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    /**
     * List，长度
     *
     * @param key 键
     * @return 长度
     */
    public long listSize(String key) {
        Long size = redisTemplate.opsForList().size(key);
        if (null == size) {
            return 0;
        }
        return size;
    }

    /**
     * 通过索引 获取list中的值
     *
     * @param key   键
     * @param index 索引index>=0时，0表头；index<0时，-1表尾
     * @return 值
     */
    public Object listIndex(String key, long index) {
        return redisTemplate.opsForList().index(key, index);
    }

    /**
     * List，右侧放入
     *
     * @param key   键
     * @param value 值
     * @return 数组长度
     */
    public long listRightPush(String key, Object value) {
        Long num = redisTemplate.opsForList().rightPush(key, value);
        if (null == num) {
            return 0;
        }
        return num;
    }

    /**
     * List，右侧放入多个
     *
     * @param key   键
     * @param value 值
     * @return 数组长度
     */
    public long listRightPushAll(String key, List<Object> value) {
        Long num = redisTemplate.opsForList().rightPushAll(key, value);
        if (null == num) {
            return 0;
        }
        return num;
    }

    /**
     * List，value1右侧放入，如果没有value1则不放入
     *
     * @param key    键
     * @param value1 值
     * @param value2 值
     * @return 数组长度，失败返回-1
     */
    public long listRightPush(String key, Object value1, Object value2) {
        Long num = redisTemplate.opsForList().rightPush(key, value1, value2);
        if (null == num) {
            return 0;
        }
        return num;
    }

    /**
     * List，右侧放入，key值必须存在
     *
     * @param key   键
     * @param value 值
     * @return 数组长度，失败返回-1
     */
    public long listRightPushIfPresent(String key, Object value) {
        Long num = redisTemplate.opsForList().rightPushIfPresent(key, value);
        if (null == num) {
            return 0;
        }
        return num;
    }

    /**
     * List，左侧放入
     *
     * @param key   键
     * @param value 值
     * @return 数组长度
     */
    public long listLeftPush(String key, Object value) {
        Long num = redisTemplate.opsForList().leftPush(key, value);
        if (null == num) {
            return 0;
        }
        return num;
    }

    /**
     * List，左侧放入，多个
     *
     * @param key    键
     * @param values 值
     * @return 数组长度
     */
    public long listLeftPushAll(String key, List<Object> values) {
        Long num = redisTemplate.opsForList().leftPushAll(key, values);
        if (null == num) {
            return 0;
        }
        return num;
    }

    /**
     * List，左侧放入，当key存在的时候
     *
     * @param key   键
     * @param value 值
     * @return 数组长度，失败返回-1
     */
    public long listLeftPushIfPresent(String key, Object value) {
        Long num = redisTemplate.opsForList().leftPushIfPresent(key, value);
        if (null == num) {
            return 0;
        }
        return num;
    }

    /**
     * List，value1左侧放入，没有value1则不放入
     *
     * @param key    键
     * @param value1 值1
     * @param value2 值2
     * @return 数组长度，失败返回-1
     */
    public long listLeftPush(String key, Object value1, Object value2) {
        Long num = redisTemplate.opsForList().leftPush(key, value1, value2);
        if (null == num) {
            return 0;
        }
        return num;
    }

    /**
     * List，根据索引修改某条数据
     * 索引不正确会抛出异常：ERR index out of range
     *
     * @param key   键
     * @param index 索引，0开始
     * @param value 值
     */
    public void listSet(String key, long index, Object value) {
        redisTemplate.opsForList().set(key, index, value);
    }

    /**
     * List，移除N个值为value，左边开始移出
     *
     * @param key   键
     * @param count 移除多少个
     * @param value 值
     */
    public void listRemove(String key, long count, Object value) {
        Long num = redisTemplate.opsForList().remove(key, count, value);
        log.info("List计划移出元素" + value + count + "个，实际移出" + num + "个");
    }





}
