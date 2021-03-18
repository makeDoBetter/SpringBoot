package com.example.feng.demo01.controller;

import com.example.feng.demo01.dto.Car;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author fengjirong
 * @date 2020/11/16 11:02
 */
@RestController
public class Demo01Controller {
    @Autowired
    Car car;

    @RequestMapping("/index")
    public String hello(){
        return "index";
    }

    @RequestMapping("/index/student")
    public ModelAndView student(){
        ModelAndView mav = new ModelAndView();
        mav.setViewName("student");
        return mav;
    }

    /**
     * description: 测试springBoot 配置绑定注解，可以直接获得配置文件中对应前缀的参数
     * @ConfigurationProperties
     *
     * @return
     * @Author fengjirong
     * @Date   2021/2/5 17:03
     */
    @RequestMapping("/car")
    public Car car(){
        return car;
    }

    @RequestMapping("/bug1.jpeg")
    public String bug(){
        return "静态资源优先控制器映射";
    }
}
