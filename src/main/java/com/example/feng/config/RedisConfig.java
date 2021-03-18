package com.example.feng.config;

import com.example.feng.demo01.dto.Car;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * @author fengjirong
 * @date 2021/1/26 17:43
 * @Configuration 标注当前类是一个配置类
 *
 * proxyBeanMethods 属性代理对象调用方法
 * 为true Full模式 时将会从容器中找组件，保持单实例
 * 为false Lite模式，将不会使用代理，作用为解决组件依赖，若是不需要实现组件依赖，
 * 可设置为false，提升springBoot启动速度
 *
 * @ImportResource() 指定bean配置文件，将bean配置文件的bean注册到容器中
 *
 * @ConditionalOnBean (name = " factory ") 条件注入，也就是自动配置的思想，如果容器中不存在对应组件，则进行相应bean的注册
 *
 * @EnableConfigurationProperties(Car.class) 作用
 * 1.开启对应类的配置绑定功能；
 * 2. 将指定组件自动注册到容器中
 */
@Import(JedisConnectionFactory.class)
@Configuration(proxyBeanMethods = true)
@EnableConfigurationProperties(Car.class)
public class RedisConfig {

    //如果@Bean标注的组件有传入参数，这个参数不需要用户手动添加，注册组件的时候会自动在容器中查找对应的组件类型参数
    @Bean("redisTemplate")
    //@ConditionalOnBean(name = "factory")
    public RedisTemplate redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        //组件依赖
        redisTemplate.setConnectionFactory(factory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new JdkSerializationRedisSerializer());
        redisTemplate.setHashValueSerializer(new JdkSerializationRedisSerializer());
        //设置序列化后，必须执行这个函数,初始化RedisTemplate
        //java.lang.IllegalArgumentException: template not initialized; call afterPropertiesSet() before using it
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
