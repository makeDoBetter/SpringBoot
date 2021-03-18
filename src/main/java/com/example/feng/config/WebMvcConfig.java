package com.example.feng.config;

import com.example.feng.annotation.FengRequestMapping;
import com.example.feng.utils.ApplicaitonFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.condition.ConsumesRequestCondition;
import org.springframework.web.servlet.mvc.condition.HeadersRequestCondition;
import org.springframework.web.servlet.mvc.condition.ParamsRequestCondition;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.ProducesRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.UrlPathHelper;
import org.springframework.web.util.WebUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * ResourceHttpRequestHandler是一个HttpRequestHandler实现类，用于处理静态资源请求。
 * 可以通过重写WebMvcConfigurer的addResourceHandlers方法添加扫描静态资源路径
 *
 * Spring MVC配置机制的bean组件HandlerMapping resourceHandlerMapping初始化时会使用静态资源配置。
 * 该HandlerMapping实际使用类为SimpleUrlHandlerMapping,URL path pattern所映射的Handler的实现类就是ResourceHttpRequestHandler
 *
 * springMVC定制化策略
 * 1.不使用@EnableWebMvc,使用@Configuration+WebMvcConfigurer实现定制化方法
 * 2.声明 WebMvcRegistrations 改变默认底层组件
 * 3.使用 @EnableWebMvc+@Configuration+DelegatingWebMvcConfiguration 全面接管SpringMVC
 *
 * @author fengjirong
 * @date 2021/3/5 11:09
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //添加静态资源扫描路径
        registry.addResourceHandler("/in/**").addResourceLocations("classpath:/node/");
    }

    /**
     * description:
     *
     * @return HiddenHttpMethodFilter
     * @Author fengjirong
     * @Date   2021/3/10 17:02
     */
    @Bean
    public HiddenHttpMethodFilter getHiddenHttpMethodFilter(){
        HiddenHttpMethodFilter hiddenHttpMethodFilter = new HiddenHttpMethodFilter();
        hiddenHttpMethodFilter.setMethodParam("_m");
        return hiddenHttpMethodFilter;
    }

    /*@Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        UrlPathHelper helper = new UrlPathHelper();
        helper.setRemoveSemicolonContent(false);
        configurer.setUrlPathHelper(helper);
    }*/

    @Bean
    public WebMvcConfigurer getWebMvcConfigurer(){
        return new WebMvcConfigurer() {
            @Override
            public void configurePathMatch(PathMatchConfigurer configurer) {
                UrlPathHelper helper = new UrlPathHelper();
                helper.setRemoveSemicolonContent(false);
                configurer.setUrlPathHelper(helper);
            }
        };
    };
}
