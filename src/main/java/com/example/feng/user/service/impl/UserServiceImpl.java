package com.example.feng.user.service.impl;

import com.example.feng.user.dto.UserDto;
import com.example.feng.user.mapper.UserMapper;
import com.example.feng.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author fengjirong
 * @date 2020/11/16 14:50
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper mapper;

    @Override
    public String logIn(UserDto userDto) {
        userDto.setUserName("root");
        userDto.setUserPassword("root");
        UserDto dto = mapper.logIn(userDto);
        System.out.println("用户名：" + dto.getUserName());
        System.out.println("密码：" + dto.getUserPassword());
        return "index";
    }
}
