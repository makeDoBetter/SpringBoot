package com.example.feng.handler;

import com.example.feng.annotation.FengRequestMapping;
import com.example.feng.utils.ApplicaitonFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringValueResolver;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.MatchableHandlerMapping;
import org.springframework.web.servlet.handler.RequestMatchResult;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * @author fengjirong
 * @date 2021/3/11 15:51
 */
//@Component
class FengRequestMappingHandlerMapping extends RequestMappingInfoHandlerMapping
        implements MatchableHandlerMapping, EmbeddedValueResolverAware {
    private RequestMappingInfo.BuilderConfiguration config = new RequestMappingInfo.BuilderConfiguration();

    @Nullable
    private StringValueResolver embeddedValueResolver;

    /**
     * handler是否含有@FengRequestMapping注解
     *
     * @param beanType
     * @return boolean
     * @Author fengjirong
     * @Date 2021/3/11 14:35
     */
    @Override
    protected boolean isHandler(Class<?> beanType) {
        Method[] methods = beanType.getDeclaredMethods();
        for (Method method : methods) {
            if (AnnotationUtils.findAnnotation(method, FengRequestMapping.class) != null) {
                return true;
            }
        }
        return false;

    }

    /**
     * description: 使用方法级别的@ {@FengRequestMapping}注释创建RequestMappingInfo。
     *
     * @param method  handlerType
     * @param handlerType  handlerType
     * @return RequestMappingInfo
     * @Author fengjirong
     * @Date   2021/3/12 11:24
     */
    @Override
    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
        RequestMappingInfo info = null;
        FengRequestMapping mapping = method.getAnnotation(FengRequestMapping.class);
        if (mapping != null){
            RequestCondition<?> condition = getCustomMethodCondition(method);
            info = createRequestMappingInfo(mapping, condition);
        }
        return info;
    }

    /**
     * description: 匹配操作
     *
     * @param info
     * @return
     * @Author fengjirong
     * @Date   2021/3/12 11:26
     */
    @Override
    protected void handleMatch(RequestMappingInfo info, String lookupPath, HttpServletRequest request) {
        request.setAttribute("isMongo", true);
        request.setAttribute("handledTime", System.nanoTime());
    }

    /**
     * description: 不匹配url处理
     *
     * @param infos
     * @param lookupPath
     * @param request
     * @return HandlerMethod
     * @Author fengjirong
     * @Date   2021/3/12 11:37
     */
    @Override
    protected HandlerMethod handleNoMatch(Set<RequestMappingInfo> infos, String lookupPath, HttpServletRequest request) throws ServletException {
        return null;
    }

    /**
     * description: 从注解中获得请求路径、请求类型等创建RequestMappingInfo对象方法
     *
     * @param requestMapping
     * @param customCondition
     * @return RequestMappingInfo
     * @Author fengjirong
     * @Date   2021/3/12 11:28
     */
    private RequestMappingInfo createRequestMappingInfo(
            FengRequestMapping requestMapping, @Nullable RequestCondition<?> customCondition) {
        ConfigurableApplicationContext context = ApplicaitonFactory.getContext();
        RequestMappingInfo.Builder builder = RequestMappingInfo
                .paths(resolveEmbeddedValuesInPatterns(new String[]{requestMapping.value()}))
                .methods(requestMapping.method())
                .params(new String[]{})
                .headers(new String[]{})
                .consumes(new String[]{})
                .produces(new String[]{})
                .mappingName("");
        if (customCondition != null) {
            builder.customCondition(customCondition);
        }
        return builder.options(this.config).build();
    }

    /**
     * 属性设置
     */
    @Override
    public void afterPropertiesSet() {
        // 提升当前 HandlerMapping 的在映射处理器列表中的顺序
        super.setOrder(0);
        super.afterPropertiesSet();
    }

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        this.embeddedValueResolver = resolver;
    }

    @Override
    public RequestMatchResult match(HttpServletRequest request, String pattern) {
        Assert.isNull(getPatternParser(), "This HandlerMapping requires a PathPattern");
        RequestMappingInfo info = RequestMappingInfo.paths(pattern).options(this.config).build();
        RequestMappingInfo match = info.getMatchingCondition(request);
        return (match != null && match.getPatternsCondition() != null ?
                new RequestMatchResult(
                        match.getPatternsCondition().getPatterns().iterator().next(),
                        UrlPathHelper.getResolvedLookupPath(request),
                        getPathMatcher()) : null);
    }

    /**
     * Resolve placeholder values in the given array of patterns.
     * @return a new array with updated patterns
     */
    protected String[] resolveEmbeddedValuesInPatterns(String[] patterns) {
        if (this.embeddedValueResolver == null) {
            return patterns;
        }
        else {
            String[] resolvedPatterns = new String[patterns.length];
            for (int i = 0; i < patterns.length; i++) {
                resolvedPatterns[i] = this.embeddedValueResolver.resolveStringValue(patterns[i]);
            }
            return resolvedPatterns;
        }
    }

    @Nullable
    protected RequestCondition<?> getCustomMethodCondition(Method method) {
        return null;
    }
}
