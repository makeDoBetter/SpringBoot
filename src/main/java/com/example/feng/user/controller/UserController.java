package com.example.feng.user.controller;

import com.example.feng.user.dto.UserDto;
import com.example.feng.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * @author fengjirong
 * @date 2020/11/16 14:41
 */
@Controller
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * description: 用户登录
     *
     * @param userDto
     * @param request
     * @return
     * @Author fengjirong
     * @Date   2020/11/16 14:44
     */
    @RequestMapping("/user/LogIn")
    public String logIn(UserDto userDto, HttpServletRequest request){
        userService.logIn(userDto);
        return "index";
    }
}
