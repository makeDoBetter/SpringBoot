package com.example.feng.student.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.MatrixVariable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author fengjirong
 * @date 2021/3/13 11:16
 */
@RestController
public class Student01Controller {

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
                                              @PathVariable Map<String, String> m,
                                              @RequestHeader(name = "sec-ch-ua") String secChUa,
                                              @RequestHeader Map<String, String> m1,
                                              @RequestParam(name = "age") String age,
                                              @RequestParam(name = "like")List<String> list,
                                              @RequestParam Map<String, String> m2){
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("m", m);
        map.put("secChUa", secChUa);
        map.put("m1",m1);
        map.put("age", age);
        map.put("like", list);
        map.put("m2", m2);
    return map;
    }

    /**
     * POST方式提交表单获得请求体中的参数
     *
     * @param content
     * @return Map<String, Object>
     * @Author fengjirong
     * @Date   2021/3/15 15:21
     */
    @PostMapping(value = "/submitInfo")
    public Map<String, Object> getPostBody(@RequestBody String content){
        Map<String, Object> map = new HashMap<>();
        map.put("content", content);
        return map;
    }

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
    public Map<String, Object> MatrixVariable(@MatrixVariable("name") String name,
                                     @MatrixVariable("food") List<String> food,
                                     @PathVariable String path){
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("food", food);
        map.put("path", path);
        return map;
    }

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
}
