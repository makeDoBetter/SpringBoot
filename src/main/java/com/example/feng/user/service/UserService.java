package com.example.feng.user.service;

import com.example.feng.user.dto.UserDto;

/**
 * @author fengjirong
 * @date 2020/11/16 14:47
 */
public interface UserService {
    /**
     * description: 用户登录
     *
     * @param userDto
     * @return String
     * @Author fengjirong
     * @Date   2020/11/16 14:49
     */
    String logIn(UserDto userDto);
}
