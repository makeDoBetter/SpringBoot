package com.example.feng.user.mapper;

import com.example.feng.user.dto.UserDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户操作mapper
 * @author fengjirong
 * @date 2020/11/16 15:13
 */
@Mapper
public interface UserMapper {
    /**
     * description: 用户登录
     *
     * @param userDto
     * @Author fengjirong
     * @Date   2020/11/16 15:14
     */
    UserDto logIn(UserDto userDto);
}
