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
