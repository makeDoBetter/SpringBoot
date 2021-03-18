package com.example.feng;

import com.example.feng.config.RedisConfig;
import com.example.feng.config.WebMvcConfig;
import com.example.feng.utils.ApplicaitonFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author fengjirong
 *
 * 默认包扫描规则：主程序相同层级及子层级的组件都能被扫描出来
 *
 * 改变包扫描路径
 * @SpringBootApplication(scanBasePackages = "扩大包范围")
 * @ComponentScan(扫描路径) 指定扫描路径
 *
 * springBoot自动配置
 * @SpringBootApplication 是一个组合注解，主要是由以下三个核心注解组合而成
 * 1.@SpringBootConfiguration 其核心注解是 @Configuration，即表示主程序类是一个配置类
 * 2.@EnableAutoConfiguration 重中只重的核心注解，下方进行重点分析
 * 3.@ComponentScan 指定扫描类路径
 *
 * @EnableAutoConfiguration 也是一个组合注解，以下对这个组合注解进行分析
 * 1.@AutoConfigurationPackage 这个注解作用是自动配置包。通过 @Import(AutoConfigurationPackages.Registrar.class)
 * 导入的 AutoConfigurationPackages.Registrar.class 组件其核心方法是获得当前注解使用的位置（根据组合组件的追溯，其作用域即为主程序类）的包名，
 * 然后将这个包内的组件批量注册到容器中。这也就是为何主程序类是在最根部分，其他业务包都为主程序类的相同或子层级
 * 2.@Import(AutoConfigurationImportSelector.class) 默认初始化加载所有的配置类 其底层是获得springBoot加载工厂中
 * META-INF/spring.factories（位于spring-boot-autoconfigure-2.4.0.jar下，可以看到其中有一部分配置是由 # Auto Configure标识的）文件
 * 中写死的SpringBoot一启动就需要加载的配置类，同时，根据各配置类中的条件装配注解进行按需装配注册组件到容器中，所以最终程序容器中的组件与固定写死的数量有差异
 * .SpringBoot先加载所有的配置类
 * .每个自动配置类按照条件进行生效，默认会绑定配置文件指定的值，会从xxProperties文件中取，xxProperties的值从配置文件中取
 * .如果用户自己注册了相同类型的组件，就以用户的为准
 *
 * 定制化配置
 * .用户使用@Bean注册自定义组件替换底层组件
 * .用户修改底层组件绑定的配置文件的值
 *
 *
 *
 *
 */
@SpringBootApplication
public class FengApplication {

    public static void main(String[] args) {
        //返回ioc容器
        ConfigurableApplicationContext run = SpringApplication.run(FengApplication.class, args);
        ApplicaitonFactory.setContext(run);
        RedisTemplate redisTemplate = run.getBean("redisTemplate", RedisTemplate.class);
        RedisConfig config = run.getBean(RedisConfig.class);
        JedisConnectionFactory factory = run.getBean(JedisConnectionFactory.class);
        RedisTemplate bean = config.redisTemplate(factory);
        System.out.println("redisTemplate" + redisTemplate);
        System.out.println("bean" + bean);
        redisTemplate.opsForValue().set("冯吉荣","nullP");
        System.out.println(redisTemplate.opsForValue().get("冯吉荣"));
        /*String[] l = run.getBeanDefinitionNames();
        for (String s : l) {
            System.out.println(s);
        }*/

    }

}
