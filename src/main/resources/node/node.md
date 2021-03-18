

## Spring MVC
### 静态资源
spring boot默认能够扫描类路径下（/static,/public,/resources,/META-INF/resources）下的所有静态资源，访问静态资源时可直接使用 http://localhost:8004/black.jpeg 链接进行访问。
访问请求首先会到controller中寻找是否能够映射请求。如果一个请求存在对应的映射器，将会返回映射器对应的动态资源。控制器中找不到的映射将会到静态资源目录中找，如果静态资源中没有找到将返回404.测试方法 /*com.example.feng.demo01.controller.bug()*/
ResourceHttpRequestHandler是一个HttpRequestHandler实现类，用于处理静态资源请求。

#### 定制化

##### 重写addResourceHandler

可以通过重写WebMvcConfigurer的addResourceHandlers方法添加扫描静态资源路径。

```java
/**
 * ResourceHttpRequestHandler是一个HttpRequestHandler实现类，用于处理静态资源请求。
 * 可以通过重写WebMvcConfigurer的addResourceHandlers方法添加扫描静态资源路径
 *
 * Spring MVC配置机制的bean组件HandlerMapping resourceHandlerMapping初始化时会使用静态资源配置。
 * 该HandlerMapping实际使用类为SimpleUrlHandlerMapping,URL path pattern所映射的Handler的实现类就是ResourceHttpRequestHandler
 *
 * @author fengjirong
 * @date 2021/3/5 11:09
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //添加静态资源扫描路径
        registry.addResourceHandler("/**").addResourceLocations("classpath:/node/");
    }
}
```

Spring MVC配置机制的bean组件`HandlerMapping resourceHandlerMapping`初始化时会使用以上静态资源配置。该`HandlerMapping`实际使用类为`SimpleUrlHandlerMapping`,URL path pattern所映射的Handler的实现类就是`ResourceHttpRequestHandler`。

```java
// WebMvcConfigurationSupport 代码片段
	@Bean
	@Nullable
	public HandlerMapping resourceHandlerMapping() {
		Assert.state(this.applicationContext != null, "No ApplicationContext set");
		Assert.state(this.servletContext != null, "No ServletContext set");

		ResourceHandlerRegistry registry = new ResourceHandlerRegistry(this.applicationContext,
				this.servletContext, mvcContentNegotiationManager(), mvcUrlPathHelper());
       // 这里调用的 addResourceHandlers 会是开发人员或者框架其他部分提供的配置逻辑                 
		addResourceHandlers(registry);

       // 生成面向静态资源的 handlerMapping，实际使用类是 SimpleUrlHandlerMapping
		AbstractHandlerMapping handlerMapping = registry.getHandlerMapping();
		if (handlerMapping == null) {
			return null;
		}
		handlerMapping.setPathMatcher(mvcPathMatcher());
		handlerMapping.setUrlPathHelper(mvcUrlPathHelper());
		handlerMapping.setInterceptors(getInterceptors());
		handlerMapping.setCorsConfigurations(getCorsConfigurations());
		return handlerMapping;
	}

```

关于`bean`组件`HandlerMapping resourceHandlerMapping`的生成，核心的逻辑如下 :

```java
// ResourceHandlerRegistry#getHandlerMapping
	@Nullable
	protected AbstractHandlerMapping getHandlerMapping() {
		if (this.registrations.isEmpty()) {
			return null;
		}

		Map<String, HttpRequestHandler> urlMap = new LinkedHashMap<>();
       // 遍历配置中指定的每个注册项  ResourceHandlerRegistration registration,
       // 每个注册项可以理解为 : 多个资源路径 跟 多个 URL pattern 的映射信息 N:M
		for (ResourceHandlerRegistration registration : this.registrations) {
           // 遍历当前注册项中的每个 URL pattern
			for (String pathPattern : registration.getPathPatterns()) {
              // 针对每个注册项中的每个 URL pattern 生成一个 ResourceHttpRequestHandler,
              // 该 ResourceHttpRequestHandler 包含了向多个静态资源路径的映射 : 1 :M
				ResourceHttpRequestHandler handler = registration.getRequestHandler();
				if (this.pathHelper != null) {
					handler.setUrlPathHelper(this.pathHelper);
				}
				if (this.contentNegotiationManager != null) {
					handler.setContentNegotiationManager(this.contentNegotiationManager);
				}
				handler.setServletContext(this.servletContext);
				handler.setApplicationContext(this.applicationContext);
				try {
					handler.afterPropertiesSet();
				}
				catch (Throwable ex) {
					throw new BeanInitializationException("Failed to init ResourceHttpRequestHandler", ex);
				}
              
              // 添加新建的 ResourceHttpRequestHandler 到 urlMap
				urlMap.put(pathPattern, handler);
			}
		}

       // 使用 urlMap 构建  SimpleUrlHandlerMapping 对象并返回
		SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
		handlerMapping.setOrder(this.order);
		handlerMapping.setUrlMap(urlMap);
		return handlerMapping;
	}

```

然后当客户访问配置中匹配所设置的URL pattern的请求到达时,上面的`SimpleUrlHandlerMapping handlerMapping`就会被`DispatcherServlet`使用到，进而找到的`Handler`就是`ResourceHttpRequestHandler`,相应的`HandlerAdapter`为`HttpRequestHandlerAdapter`。`DispatcherServlet`会使用`HttpRequestHandlerAdapter`执行`ResourceHttpRequestHandler`加载静态资源返回给请求者.

##### 配置文件配置

```yaml
# 修改静态资源扫描路径，注意，由于这个可以同时配置多个路径，因此配置时属性是数组类别
spring:
  mvc:
    static-path-pattern: /resources/**
  web:
    resources:
      static-locations: [classpath:/index/]
```

```java
@ConfigurationProperties(prefix = "spring.mvc")
public class WebMvcProperties {
    
	private String staticPathPattern = "/**";
}
```

WebMvcProperties类中的staticPathPattern field 对应了`spring.mvc.static-path-pattern`这个属性，可以看到默认值是 "/**"。

```java
public static class Resources {

		private static final String[] CLASSPATH_RESOURCE_LOCATIONS = { "classpath:/META-INF/resources/",
				"classpath:/resources/", "classpath:/static/", "classpath:/public/" };
}
```

ResourceProperties中staticLocations field 对应了 `spring.web.resources.static-locations `这个属性。可以看到默认值是父类的classpath:[/META-INF/resources/, /resources/, /static/, /public/], servlet context:/

### 欢迎页

spring boot支持两种欢迎页，分别是：

- 静态资源路径下的index.html
- controller的index请求

### 部分注解

#### @PathVariable

这个注解是将url上动态的参数与方法参数进行绑定，进入这个注解的方法体，可以看到当方法指定一个`@PathVariable`修饰`Map<String, String>`类型的参数时，这个请求url中的动态参数将会以k,v的形式存入到map对象中。

简单来说就是注解带参数会获得指定参数，没有带参数获得全部。

```java
    /**
     * 路径变量注解{@link PathVariable}测试
     * 如果路径变量修饰的方法参数是一个Map<String, String>，那么会将所有的路径变量存入这个map参数中，
     * 即只需定义一个@PathVariable 修饰map参数，即可获得全部的路径变量，然后进行处理
     *
     * @return Map<String, Object>
     * @Author fengjirong
     * @see PathVariable
     * @Date   2021/3/13 11:32
     */
    @GetMapping(value = "/student01/{id}/car/{name}")
    public Map<String, Object> testAnnotation(@PathVariable(name = "id") String id,
                                              @PathVariable(name = "name") String name,
                                              @PathVariable Map<String, String> m){
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("m", m);
        return map;
    }
```

前端测试超链接

```html
<a href="/student01/1/car/feng">/student/{id}/car/{name}</a>
```

点击这个超链接，请求得到的响应如下。

![](pictures\pathVariable.png)

#### @RequestHeader

这个注解用于绑定请求中的请求头，同样是如果@RequestHeader属性指定了请求头中某个参数则绑定这个参数，如果@RequestHeader修饰的是个Map<String, String>参数，则将会把请求头中的所有参数根据k，v放到map中。

#### @RequestParam

获得请求url后跟的参数，`@RequestParam`添加熟悉绑定指定的参数，如果请求url中需要传一个list，可在请求中参数名多个，后端绑定的时候使用list获取。如*tudent01/1/car/feng?age=18&like=hello&like=world*中like参数传递两次，在后端使用*@RequestParam(name = "like")List<String> list*即可将其转换为一个list。

```json
"like":["hello","world"]
```

当然，跟以上注解一样，如果`@RuestParam`修饰的是一个Map<Str/87,String>参数，将会把所有url**？**后的参数以k，v的形式放入map中，但是需要注意的是，如上的list型参数将不会被完整保存，只会保存第一个。

```json
"m2":{"age":"18","like":"hello"}
```

#### @RequestAttribute

获得request请求域中的数据，其操作对象是`HttpServletRequest`对象。HttpServletRequest对象代表客户端的请求，当客户端通过HTTP协议访问服务器时，HTTP请求头中的所有信息都封装在这个对象中。

> 请求转发，指一个web资源收到客户端请求后，通知服务器去调用另外一个web资源进行处理。

模拟请求转发，使用**@RequestAttribute**注解操作`HttpServletRequest`对象

```java
package com.example.feng.student.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 模拟请求转发，请求之间使用的是同一个HttpServletRequest对象
 * @author fengjirong
 * @date 2021/3/15 15:41
 */
@Controller
public class Student02Controller {

    @GetMapping("/Student02Controller/doGet")
    public String doGet(HttpServletRequest request){
        request.setAttribute("msg", "this is success");
        request.setAttribute("code", "200");
        //模拟请求转发 forward前缀将会跳转到指定的url
        return "forward:/Student02Controller/success";
    }

    @ResponseBody
    @GetMapping("/Student02Controller/success")
    public Map<String, Object> doSuccess(@RequestAttribute("msg") String msg,
                                         @RequestAttribute("code") Integer code,
                                         HttpServletRequest request){
        Map<String, Object> map = new HashMap<>();
        map.put("msg_s", msg);
        map.put("code_s", code);
        map.put("cookies", request.getCookies());

        return map;
    }
}

```

客户端发送`/Student02Controller/doGet`请求，返回值使用`forward:`前缀修饰，将会跳转到后面的url中。

前端返回json数据

```json
{"msg_s":"this is success","code_s":200,"cookies":[{"name":"oneapmclientid","value":"17825654003250-02952fdc5b9b4f-31346d-144000-17825654004668","version":0,"comment":null,"domain":null,"maxAge":-1,"path":null,"secure":false,"httpOnly":false}]}
```

手动追加的信息成功添加并且跟随转发到了其他方法中。

> @Controller修饰的控制器返回的是前端页面(项目中无模板则返回String数据)，@RestController返回的是json数据。

#### @MatrixVariable

> 矩阵变量生效的前提是控制绑定的url需要使用路径变量表示。

获得url请求中的矩阵变量，其url形如`/student01/do;name=feng;food=apple,car,dog`。控制器可使用@MatrixVariable注解获得对应的参数，如下：

```java
    /**
     * description: 矩阵变量注解{@link MatrixVariable}测试，需要注意的是，由于矩阵变量是绑定在url中的，
     * 因此控制器指定的url使用路径变量指定
     *
     * @param name
     * @param food
     * @param path
     * @return Map
     * @see MatrixVariable
     * @Author fengjirong
     * @Date   2021/3/16 10:52
     */
    @GetMapping(value = "/student01/{path}")
    public Map<String, Object> getMa(@MatrixVariable("name") String name,
                                     @MatrixVariable("food") List<String> food,
                                     @PathVariable String path){
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("food", food);
        map.put("path", path);
        return map;
    }
```

以下为一个拓展，如果存在多层级url（形如`/student01/do;name=feng/do01;name=jirong`）有相同矩阵变量名如何获取：

```java
    /**
     * description: 矩阵变量注解{@link MatrixVariable}拓展测试，形如
     * <a href="/student01/do;name=feng/do01;name=jirong">/student01/do;name=feng/do01;name=jirong</a>
     * 多层矩阵变量名相同使用pathVar属性指定路径变量名
     *
     * @param name01
     * @param name02
     * @param path01
     * @param path02
     * @return Map
     * @see MatrixVariable
     * @Author fengjirong
     * @Date   2021/3/16 10:52
     */
    ///student01/do;name=feng/do01;name=jirong
    @GetMapping(value = "student01/{path01}/{path02}")
    public Map<String, Object> MatrixVariable01(@MatrixVariable(value = "name", pathVar = "path01") String name01,
                                                @MatrixVariable(value = "name", pathVar = "path02") String name02,
                                                @PathVariable String path01,
                                                @PathVariable String path02){
        Map<String, Object> map = new HashMap<>();
        map.put("name01", name01);
        map.put("name02", name02);
        map.put("path01", path01);
        map.put("path02", path02);
        return map;
    }
```

由于Spring Boot默认是不开启矩阵变量，因此如果需要使用矩阵变量测试，需要进行定制化。

这里采用两种方式定制化，都是使用不使用`@EnableWebMvc`,使用`@Configuration`+`WebMvcConfigurer`实现定制化方法

1. 配置类实现`WebMvcConfigurer`接口的`configurePathMatch()`方法

```java

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        UrlPathHelper helper = new UrlPathHelper();
        helper.setRemoveSemicolonContent(false);
        configurer.setUrlPathHelper(helper);
    }
}

```

2. 自定义`WebMvcConfigurer`组件，使用匿名内部类实现`configurePathMatch`方法。

```java
@Configuration
public class WebMvcConfig{
    
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

```

> UrlPathHelper作为路径帮助器，其包含处理矩阵变量、路径变量等方法。

### 源码解读

#### 静态资源自动映射

静态资源自动映射的核心源码位于`WebMvcAutoConfiguration`这个配置类下。

```java
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnClass({ Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class })
@ConditionalOnMissingBean(WebMvcConfigurationSupport.class)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 10)
@AutoConfigureAfter({ DispatcherServletAutoConfiguration.class, TaskExecutionAutoConfiguration.class,
		ValidationAutoConfiguration.class })
public class WebMvcAutoConfiguration {
}
```

其存在多个条件装配注解，其中`@ConditionalOnMissingBean(WebMvcConfigurationSupport.class)`这个注解。用户可以通过自定义配置类全面接管spring mvc静态资源的自动配置。

其中，静态 资源映射的核心源码如下(添加了部分注解)：

```java
@Override
public void addResourceHandlers(ResourceHandlerRegistry registry) {
    //isAddMappings()进行了配置绑定，判断是否禁用全部静态文件
    if (!this.resourceProperties.isAddMappings()) {
        logger.debug("Default resource handling disabled");
        return;
    }
    //获得配置文件的静态资源缓存时间，时间单位未s（秒）
    Duration cachePeriod = this.resourceProperties.getCache().getPeriod();
    CacheControl cacheControl = this.resourceProperties.getCache().getCachecontrol().toHttpCacheControl();
    //首先进行的是自动配置访问jar包下的静态资源文件，因此如果用户想要访问jar包文件，只需添加/webjars
    //前缀即可访问，具体路径自动配置类已经设置好了组件属性，包含缓存时间等
    if (!registry.hasMappingForPattern("/webjars/**")) {
        customizeResourceHandlerRegistration(registry.addResourceHandler("/webjars/**")
                                             .addResourceLocations("classpath:/META-INF/resources/webjars/")
                                             .setCachePeriod(getSeconds(cachePeriod)).setCacheControl(cacheControl)
                                             .setUseLastModified(this.resourceProperties.getCache().isUseLastModified()));
    }
    //自动配置静态资源的路径与缓存时间，静态资源的路径模式即访问问路径会从配置文件中获取，没有则会使用默认值
    String staticPathPattern = this.mvcProperties.getStaticPathPattern();
    if (!registry.hasMappingForPattern(staticPathPattern)) {
        customizeResourceHandlerRegistration(registry.addResourceHandler(staticPathPattern)
                                             .addResourceLocations(getResourceLocations(this.resourceProperties.getStaticLocations()))
                                             .setCachePeriod(getSeconds(cachePeriod)).setCacheControl(cacheControl)
                                             .setUseLastModified(this.resourceProperties.getCache().isUseLastModified()));
    }
}
```

```yaml
spring:
  mvc:
    /static-path-pattern: /resources/**
  web:
    resources:
      #修改静态资源扫描路径，注意，由于这个可以同时配置多个路径，因此配置时属性是数组类别
      #static-locations: [classpath:/index/]
      #是否启用默认资源路径映射，即这个可以禁用所有静态资源规则
      add-mappings: true
      cache:
        period: 1234
```

静态资源自动配置的映射默认值：

```java
private String staticPathPattern = "/**";

private static final String[] CLASSPATH_RESOURCE_LOCATIONS = { "classpath:/META-INF/resources/",
                                                              "classpath:/resources/", "classpath:/static/", "classpath:/public/" };

/**
		 * Locations of static resources. Defaults to classpath:[/META-INF/resources/,
		 * /resources/, /static/, /public/].
		 */
private String[] staticLocations = CLASSPATH_RESOURCE_LOCATIONS;
```



可以通过访问静态资源，发现配置文件中设置的缓存时间生效，如图：

![](pictures\springmvcCache.png)

#### 欢迎页

我们可以找到`WelcomePageHandlerMapping`即为欢迎页处理映射组件。

拓展**HandlerMapping**：处理器映射组件，即每一个Handler能处理哪些请求。

```java
@Bean
public WelcomePageHandlerMapping welcomePageHandlerMapping(ApplicationContext applicationContext,
                                                           FormattingConversionService mvcConversionService, ResourceUrlProvider mvcResourceUrlProvider) {
    WelcomePageHandlerMapping welcomePageHandlerMapping = new WelcomePageHandlerMapping(
        new TemplateAvailabilityProviders(applicationContext), applicationContext, getWelcomePage(),
        this.mvcProperties.getStaticPathPattern());
    welcomePageHandlerMapping.setInterceptors(getInterceptors(mvcConversionService, mvcResourceUrlProvider));
    welcomePageHandlerMapping.setCorsConfigurations(getCorsConfigurations());
    return welcomePageHandlerMapping;
}
```

拿出`WelcomePageHandlerMapping`的构造方法

```java
WelcomePageHandlerMapping(TemplateAvailabilityProviders templateAvailabilityProviders,
                          ApplicationContext applicationContext, Optional<Resource> welcomePage, String staticPathPattern) {
    //欢迎页存在且静态资源访问路径为/**则会使用index.html
    if (welcomePage.isPresent() && "/**".equals(staticPathPattern)) {
        logger.info("Adding welcome page: " + welcomePage.get());
        setRootViewName("forward:index.html");
    }
    //找到controller中处理/index请求的映射器
    else if (welcomeTemplateExists(templateAvailabilityProviders, applicationContext)) {
        logger.info("Adding welcome page template: index");
        setRootViewName("index");
    }
}
```

#### rest风格表单提交

spring Boot Rest风格的controller如下

```java
package com.example.feng.student;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author fengjirong
 * @date 2021/3/10 11:11
 */
@RestController
public class StudentController {

    @GetMapping(value = "/student")
    public String get(){
        return "get submit";
    }

    @PostMapping(value = "/student")
    public String post(){
        return "post submit";
    }

    @PutMapping(value = "/student")
    public String put(){
        return "put submit";
    }

    @DeleteMapping(value = "/student")
    public String delete(){
        return "delete submit";
    }
}

```

前台表单设计

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>rest风格controller测试</title>
    <script src="https://code.jquery.com/jquery-3.0.0.min.js"></script>
    <script type="text/javascript">
        function doButton() {
            $.ajax({
                type: "DELETE",
                url: "/student",
                async:false,
                success:function (data) {
                    alert(data)
                }
            });
        }
    </script>
</head>
<body>
<form action="/student" method="GET">
    <input type="submit" value="get">
</form>
<form action="/student" method="POST">
    <input type="submit" value="post">
</form>
<form action="/student" method="POST">
    <input name="_method" value="put" type="hidden">
    <input name="_m" value="put" type="hidden">
    <input type="submit" value="put">
</form>
<form action="/student" method="POST">
    <input name="_method" value="delete" type="hidden">
    <input name="_m" value="delete" type="hidden">
    <input type="submit" value="delete">
</form>

<button name="button1" onclick="doButton()">
    确认
</button>


</body>
</html>
```

源码分析

`OrderedHiddenHttpMethodFilter`过滤器能够处理Rest风格的表单请求，其组件注册在`WebMvcAutoConfiguration`配置类下。

```java
@Bean
@ConditionalOnMissingBean(HiddenHttpMethodFilter.class)
//使用表单的rest风格过滤器需要配置文件配置，默认未false
@ConditionalOnProperty(prefix = "spring.mvc.hiddenmethod.filter", name = "enabled", matchIfMissing = false)
public OrderedHiddenHttpMethodFilter hiddenHttpMethodFilter() {
    return new OrderedHiddenHttpMethodFilter();
}
```

由于需要获取配置文件参数，需配置文件添加

```yaml
spring:
  mvc:
    hiddenmethod:
      filter:
      #启用rest风格请求
        enabled: true
```



进一步查看HiddenHttpMethodFilter过滤器核心方法

```java
@Override
protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {

    HttpServletRequest requestToUse = request;
    //需要表单Method属性未POST且请求没有异常
    if ("POST".equals(request.getMethod()) && request.getAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE) == null) {
        //获得隐藏method属性，默认配置属性名为_method
        String paramValue = request.getParameter(this.methodParam);
        if (StringUtils.hasLength(paramValue)) {
            //转大写
            String method = paramValue.toUpperCase(Locale.ENGLISH);
            //允许的请求类型，包含DELETE,PUT,PATCH
            if (ALLOWED_METHODS.contains(method)) {
                //新的request对象由HttpMethodRequestWrapper生成
                request对象由ToUse = new HttpMethodRequestWrapper(request, method);
            }
        }
    }
    //执行下一个过滤器节点
    filterChain.doFilter(requestToUse, response);
}
```

看完以上代码及注释，进入`HttpMethodRequestWrapper`核心代码

```java
private static class HttpMethodRequestWrapper extends HttpServletRequestWrapper {

    private final String method;
    //修改method参数为传入的隐藏_method参数，后续Rest风格获得的method变成了传入的实际参数
    public HttpMethodRequestWrapper(HttpServletRequest request, String method) {
        super(request);
        this.method = method;
    }

    @Override
    public String getMethod() {
        return this.method;
    }
}
```

由上，实现了form标签表单rest风格请求。

最后，可通过用户`HiddenHttpMethodFilter`自定义隐藏参数名，原因是可以利用上面`@ConditionalOnMissingBean(HiddenHttpMethodFilter.class)`条件装配注解。

```java
@Bean
public HiddenHttpMethodFilter getHiddenHttpMethodFilter(){
    HiddenHttpMethodFilter hiddenHttpMethodFilter = new HiddenHttpMethodFilter();
    hiddenHttpMethodFilter.setMethodParam("_m");
    return hiddenHttpMethodFilter;
}
```

经测试，**ajax**直接修改了实际method属性，进入过滤器核心方法后跳过处理，执行后续其他过滤器操作。

#### 请求映射源码

首先看一张请求完整流转图：

![](pictures\请求流程.png)

前台发送给后台的访问请求是如何找到对应的控制器映射并执行后续的后台操作呢，其核心为DispatcherServlet.java与HandlerMapper。在spring boot初始化的时候，将会加载所有的请求与对应的处理器映射为HandlerMapper组件。我们可以在springMVC的自动配置类中找到对应的Bean。

```java
@Bean
@Primary
@Override
public RequestMappingHandlerMapping requestMappingHandlerMapping(
    @Qualifier("mvcContentNegotiationManager") ContentNegotiationManager contentNegotiationManager,
    @Qualifier("mvcConversionService") FormattingConversionService conversionService,
    @Qualifier("mvcResourceUrlProvider") ResourceUrlProvider resourceUrlProvider) {
    // Must be @Primary for MvcUriComponentsBuilder to work
    return super.requestMappingHandlerMapping(contentNegotiationManager, conversionService,
                                              resourceUrlProvider);
}

@Bean
public WelcomePageHandlerMapping welcomePageHandlerMapping(ApplicationContext applicationContext,
                                                           FormattingConversionService mvcConversionService, ResourceUrlProvider mvcResourceUrlProvider) {
    WelcomePageHandlerMapping welcomePageHandlerMapping = new WelcomePageHandlerMapping(
        new TemplateAvailabilityProviders(applicationContext), applicationContext, getWelcomePage(),
        this.mvcProperties.getStaticPathPattern());
    welcomePageHandlerMapping.setInterceptors(getInterceptors(mvcConversionService, mvcResourceUrlProvider));
    welcomePageHandlerMapping.setCorsConfigurations(getCorsConfigurations());
    return welcomePageHandlerMapping;
}
```

请求将首先执行`FrameworkServlet`下的service方法根据request请求的method找到对应的do**方法。

```java
@Override
protected void service(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

    HttpMethod httpMethod = HttpMethod.resolve(request.getMethod());
    if (httpMethod == HttpMethod.PATCH || httpMethod == null) {
        processRequest(request, response);
    }
    else {
        //父类根据method参数执行doGet,doPost,doDelete等
        super.service(request, response);
    }
}
```

而这些do**其都会进入核心方法，以doGet为例。

```java
@Overrideprotected 
final void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    //核心方法
    processRequest(request, response);
}
```

```java
protected final void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    try {
    //进入此核心方法
    doService(request, response);
}
catch (ServletException | IOException ex) {
    failureCause = ex;
    throw ex;
}
catch (Throwable ex) {
    failureCause = ex;
    throw new NestedServletException("Request processing failed", ex);
}

finally {
    resetContextHolders(request, previousLocaleContext, previousAttributes);
    if (requestAttributes != null) {
        requestAttributes.requestCompleted();
    }
    logResult(request, response, failureCause, asyncManager);
    publishRequestHandledEvent(request, response, startTime, failureCause);
}

`````

`processRequest()`方法中重点在`doService(request, response);`，而其核心处理逻辑位于DispatchServletl类重写的方法，如下。

```java
@Override
	protected void doService(HttpServletRequest request, HttpServletResponse response) throws Exception {
  ····

      try {
          //这里为实际分发控制器的逻辑，其内部是找到对应的handlerMapper
          doDispatch(request, response);
      }
        finally {
            if (!WebAsyncUtils.getAsyncManager(request).isConcurrentHandlingStarted()) {
                // Restore the original attribute snapshot, in case of an include.
                if (attributesSnapshot != null) {
                    restoreAttributesAfterInclude(request, attributesSnapshot);
                }
            }
            if (requestPath != null) {
                ServletRequestPathUtils.clearParsedRequestPath(request);
            }
        }
}
```

接下来看分发处理逻辑方法,其中重要的方法都使用了原生的注释。接下来分别分析核心源码。

```java
protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
    HttpServletRequest processedRequest = request;
    HandlerExecutionChain mappedHandler = null;
    boolean multipartRequestParsed = false;

    WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);

    try {
        ModelAndView mv = null;
        Exception dispatchException = null;

        try {
            processedRequest = checkMultipart(request);
            multipartRequestParsed = (processedRequest != request);

            // Determine handler for the current request.
            mappedHandler = getHandler(processedRequest);
            if (mappedHandler == null) {
                noHandlerFound(processedRequest, response);
                return;
            }

            // Determine handler adapter for the current request.
            HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());

            // Process last-modified header, if supported by the handler.
            String method = request.getMethod();
            boolean isGet = "GET".equals(method);
            if (isGet || "HEAD".equals(method)) {
                long lastModified = ha.getLastModified(request, mappedHandler.getHandler());
                if (new ServletWebRequest(request, response).checkNotModified(lastModified) && isGet) {
                    return;
                }
            }

            if (!mappedHandler.applyPreHandle(processedRequest, response)) {
                return;
            }

            // Actually invoke the handler.
            mv = ha.handle(processedRequest, response, mappedHandler.getHandler());

            if (asyncManager.isConcurrentHandlingStarted()) {
                return;
            }

            applyDefaultViewName(processedRequest, mv);
            mappedHandler.applyPostHandle(processedRequest, response, mv);
        }
        catch (Exception ex) {
            dispatchException = ex;
        }
        catch (Throwable err) {
            // As of 4.3, we're processing Errors thrown from handler methods as well,
            // making them available for @ExceptionHandler methods and other scenarios.
            dispatchException = new NestedServletException("Handler dispatch failed", err);
        }
        processDispatchResult(processedRequest, response, mappedHandler, mv, dispatchException);
    }
    catch (Exception ex) {
        triggerAfterCompletion(processedRequest, response, mappedHandler, ex);
    }
    catch (Throwable err) {
        triggerAfterCompletion(processedRequest, response, mappedHandler,
                               new NestedServletException("Handler processing failed", err));
    }
    finally {
        if (asyncManager.isConcurrentHandlingStarted()) {
            // Instead of postHandle and afterCompletion
            if (mappedHandler != null) {
                mappedHandler.applyAfterConcurrentHandlingStarted(processedRequest, response);
            }
        }
        else {
            // Clean up any resources used by a multipart request.
            if (multipartRequestParsed) {
                cleanupMultipart(processedRequest);
            }
        }
    }
}
```

首先是分析getHandler(),找到对应的处理器映射逻辑。

```java
protected HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
    if (this.handlerMappings != null) {
        for (HandlerMapping mapping : this.handlerMappings) {
            HandlerExecutionChain handler = mapping.getHandler(request);
            if (handler != null) {
                return handler;
            }
        }
    }
    return null;
}
```

我们将断点标记在`getHandler`方法上时，可以清除看到`handlerMappings`，如图。

![](pictures\handlerMappers.png)

这里，用户请求与处理器的映射关系都在`RequestMapperHandlerMapping`中，而欢迎页处理请求则在`WelcomePageHanderMapping`中进行映射。

以下为RequestMapperHandlerMapping中映射部分截图，可以看到用户的所有请求映射这里面都有：

![](pictures\RequestMapping.png)

getHandler()后的方法是通过比较request请求中method与HandlerMapper中相同url下的method，再进行唯一性校验，不通过异常，通过找到唯一的handler。

后续，通过handler找到处理的设配器，通过适配器得到一个ModelAndView对象，这个对象就是最后返回给前端页面的对象。

至此，一个请求完整映射到返回前端结束。

> 说明：这是实现了FramworkServlet的doService方法，FramworkServlet继承自HttpServlet，并且重写了父类中的doGet(),doPost(),doPut(),doDelete 等方法，在这些重写的方法里都调用了 processRquest() 方法做请求处理，进入processRquest()可以看到里面调用了FramworkServlet中定义的doService() 方法。

#### 请求参数解析

客户端请求在handlerMapping中找到对应handler后，将会继续执行`DispatchServlet`的`doPatch()`方法。

首先是找到handler对应的适配器。

```java
HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());
```

进入到`getHandlerAdapter(mappedHandler.getHandler())`方法中

```java
protected HandlerAdapter getHandlerAdapter(Object handler) throws ServletException {
		if (this.handlerAdapters != null) {
			for (HandlerAdapter adapter : this.handlerAdapters) {
				if (adapter.supports(handler)) {
					return adapter;
				}
			}
		}
		throw new ServletException("No adapter for handler [" + handler +
				"]: The DispatcherServlet configuration needs to include a HandlerAdapter that supports this handler");
	}
```

这里存在多个适配器，如图：

![](pictures\适配器.png)

其中使用`@RequestMaping`注解修饰的控制器都将适配第一个适配器；而函数式方法将会使用第二个适配器。

跟踪请求，这里将会获得第一个适配器，判断也简单，如下：

```java
public final boolean supports(Object handler) {
		return (handler instanceof HandlerMethod && supportsInternal((HandlerMethod) handler));
	}
```

如果是`HandlerMethod`类型的处理器就采用这个适配器，而客户端请求正好对应的是`HandlerMethod`处理器。

找到适配器后，将会真正执行处理器逻辑。如下：

```java
// Actually invoke the handler.
mv = ha.handle(processedRequest, response, mappedHandler.getHandler());
```

进入`RequestMappingHandlerAdapter`，执行适配器核心方法：

```java
@Override
	protected ModelAndView handleInternal(HttpServletRequest request,
			HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {

		ModelAndView mav;
		checkRequest(request);

		// Execute invokeHandlerMethod in synchronized block if required.
		if (this.synchronizeOnSession) {
			HttpSession session = request.getSession(false);
			if (session != null) {
				Object mutex = WebUtils.getSessionMutex(session);
				synchronized (mutex) {
					mav = invokeHandlerMethod(request, response, handlerMethod);
				}
			}
			else {
				// No HttpSession available -> no mutex necessary
				mav = invokeHandlerMethod(request, response, handlerMethod);
			}
		}
		else {
			// No synchronization on session demanded at all...
			mav = invokeHandlerMethod(request, response, handlerMethod);
		}

		if (!response.containsHeader(HEADER_CACHE_CONTROL)) {
			if (getSessionAttributesHandler(handlerMethod).hasSessionAttributes()) {
				applyCacheSeconds(response, this.cacheSecondsForSessionAttributeHandlers);
			}
			else {
				prepareResponse(response);
			}
		}

		return mav;
	}
```

其核心代码为实际执行处理器方法:

```java
mav = invokeHandlerMethod(request, response, handlerMethod);
```

同样，我们打开`RequestMappingHandlerAdapter`中的`invokeHandlerMethod`方法：

```java
@Nullable
	protected ModelAndView invokeHandlerMethod(HttpServletRequest request,
			HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {

		ServletWebRequest webRequest = new ServletWebRequest(request, response);
		try {
            //通过处理器获得真正的执行方法及其参数列表
			ServletInvocableHandlerMethod invocableMethod = createInvocableHandlerMethod(handlerMethod);
            //给执行方法对象添加参数解析器
			if (this.argumentResolvers != null) {
				invocableMethod.setHandlerMethodArgumentResolvers(this.argumentResolvers);
			}
            //给执行方法对象添加返回值处理器
			if (this.returnValueHandlers != null) {
				invocableMethod.setHandlerMethodReturnValueHandlers(this.returnValueHandlers);
			}
            //处理器对象装配完成，执行控制器方法
		invocableMethod.invokeAndHandle(webRequest, mavContainer);
		if (asyncManager.isConcurrentHandlingStarted()) {
			return null;
		}

		return getModelAndView(mavContainer, modelFactory, webRequest);
	}
	finally {
		webRequest.requestCompleted();
	}
}
            
`````````
在这个关键方法中，首先执行请求对应的控制器逻辑，之后进行系列处理，根据返回值处理器处理返回值。

```java
public void invokeAndHandle(ServletWebRequest webRequest, ModelAndViewContainer mavContainer,
			Object... providedArgs) throws Exception {
        //执行控制器方法
		Object returnValue = invokeForRequest(webRequest, mavContainer, providedArgs);
		setResponseStatus(webRequest);

		if (returnValue == null) {
			if (isRequestNotModified(webRequest) || getResponseStatus() != null || mavContainer.isRequestHandled()) {
				disableContentCachingIfNecessary(webRequest);
				mavContainer.setRequestHandled(true);
				return;
			}
		}
		else if (StringUtils.hasText(getResponseStatusReason())) {
			mavContainer.setRequestHandled(true);
			return;
		}

		mavContainer.setRequestHandled(false);
		Assert.state(this.returnValueHandlers != null, "No return value handlers");
		try {
            //处理返回值
			this.returnValueHandlers.handleReturnValue(
					returnValue, getReturnValueType(returnValue), mavContainer, webRequest);
		}
		catch (Exception ex) {
			if (logger.isTraceEnabled()) {
				logger.trace(formatErrorForReturnValue(returnValue), ex);
			}
			throw ex;
		}
	}
```



以下给出部分参数解析器及返回值处理器截图：

参数解析器。对应每一个参数（路径变量、矩阵变量、获得请求头、请求域等）的获取方式

![](pictures\参数解析器.png)

返回值处理器。ModelAndView、ResponseBody等。每个处理器处理不同类别的返回值类型。

![](pictures\返回值处理器.png)

接下来，真正进入到最终执行method方法`invocableMethod.invokeAndHandle(webRequest, mavContainer);`，这里是真是执行控制器中映射的方法。

以下为获得参数列表对应值的逻辑，参数获取完成后将会执行真正的控制器逻辑。

```java
public Object invokeForRequest(NativeWebRequest request, @Nullable ModelAndViewContainer mavContainer,
			Object... providedArgs) throws Exception {
    //通过参数解析器获取参数列表每一个参数的值
		Object[] args = getMethodArgumentValues(request, mavContainer, providedArgs);
		if (logger.isTraceEnabled()) {
			logger.trace("Arguments: " + Arrays.toString(args));
		}
		return doInvoke(args);
	}
```

进入解析逻辑，解析是对比每一个参数绑定的注解，如果注解一致将会使用对应的解析器将请求传递的参数值获取到。

```java
protected Object[] getMethodArgumentValues(NativeWebRequest request, @Nullable ModelAndViewContainer mavContainer,
			Object... providedArgs) throws Exception {
        //获得控制器参数列表，每一个参数包含其参数类型，参数次序、注解修饰（用于使用对应解析器）
		MethodParameter[] parameters = getMethodParameters();
		if (ObjectUtils.isEmpty(parameters)) {
			return EMPTY_ARGS;
		}

		Object[] args = new Object[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
            //轮循获得参数
			MethodParameter parameter = parameters[i];
			parameter.initParameterNameDiscovery(this.parameterNameDiscoverer);
			args[i] = findProvidedArgument(parameter, providedArgs);
			if (args[i] != null) {
				continue;
			}
            //判断解析器是否支持当前参数的解析
			if (!this.resolvers.supportsParameter(parameter)) {
				throw new IllegalStateException(formatArgumentError(parameter, "No suitable resolver"));
			}
			try {
                //获得参数值的核心方法
				args[i] = this.resolvers.resolveArgument(parameter, mavContainer, request, this.dataBinderFactory);
			}
			catch (Exception ex) {
				// Leave stack trace for later, exception may actually be resolved and handled...
				if (logger.isDebugEnabled()) {
					String exMsg = ex.getMessage();
					if (exMsg != null && !exMsg.contains(parameter.getExecutable().toGenericString())) {
						logger.debug(formatArgumentError(parameter, exMsg));
					}
				}
				throw ex;
			}
		}
		return args;
	}
```

查看解析器是否指出当前参数部分代码，可以了解到SpringMVC的缓存策略。

```java
private HandlerMethodArgumentResolver getArgumentResolver(MethodParameter parameter) {
    //从缓存中获取当前参数的解析器
    HandlerMethodArgumentResolver result = this.argumentResolverCache.get(parameter);
	//缓存中不存在则将这个参数对应的解析器加到缓存中，提升后续相同请求响应速度。	
    if (result == null) {
			for (HandlerMethodArgumentResolver resolver : this.argumentResolvers) {
				if (resolver.supportsParameter(parameter)) {
					result = resolver;
					this.argumentResolverCache.put(parameter, result);
					break;
				}
			}
		}
		return result;
	}
```

**参数列表**

以下为获得的参数列表第一个参数部分属性

![](pictures\参数.png)

其对应的是控制器中`id`参数：

```java
@GetMapping(value = "/student01/{id}/car/{name}")
    public Map<String, Object> testAnnotation(@PathVariable(name = "id") String id){
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
    return map;
    }
```

> 以上即为获得请求Handler对应的适配器，处理参数映射、执行控制器逻辑、返回值处理的核心源码处理。

#### 自定义Handler

拓展，这个时候，我有时候会考虑是否可以自定义handler，可以参考`RequestMappingHandlerMapping`继承的父类，并且重写部分方法，以下为我的实现。

首先，需要新建一个注解，这个注解的作用同@RequestMapping.

```java
package com.example.feng.annotation;


import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author fengjirong
 * @date 2021/3/11 14:20
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FengRequestMapping {

    String value() default "";

    RequestMethod[] method() default {};
}


```

接下来是自定义handler的代码，需要实现多个方法，用于指定自定义注解 修饰的方法使用当前handler，设置handler对象的url等参数。**需要注意的是，需将自定义handler的优先级设置考前，order(0)，否则会出现异常。**

```java
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
@Component
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

```

接下来是测试controller，测试@FengRequestMapping与@RequestMapping的兼容性。

```java
package com.example.feng.student;

import com.example.feng.annotation.FengRequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author fengjirong
 * @date 2021/3/10 11:11
 */
@Controller
public class StudentController {

    @ResponseBody
    @FengRequestMapping(value = "/student", method = RequestMethod.GET)
    public String get(){
        return "get submit";
    }

    @ResponseBody
    @FengRequestMapping(value = "/student", method = RequestMethod.POST)
    public String post(){
        return "post submit";
    }

    @ResponseBody
    @FengRequestMapping(value = "/student", method = RequestMethod.PUT)
    public String put(){
        return "put submit";
    }

    @ResponseBody
    //@FengRequestMapping(value = "/student", method = RequestMethod.DELETE)
    @DeleteMapping(value = "/student")
    public String delete(){
        return "delete submit";
    }
}

```

前台页面使用rest风格表单提交的index.html。

看效果

![](pictures/%E8%87%AA%E5%AE%9A%E4%B9%89handler01.png)

查看自定义handler中handler注册详情。

![](pictures/%E8%87%AA%E5%AE%9A%E4%B9%89handler02.png)

而使用@RequestMapping标注的handler注册在RequestMappingHandlerMapping组件中。

![](pictures/%E8%87%AA%E5%AE%9A%E4%B9%89handler03.png)



页面访问系统首页，点击按钮，前台提交表单，全部都可以得到对应的响应，由上图可以看到多个注解在相同controller下进入到了对应的映射中，因此其兼容性得到证实。

