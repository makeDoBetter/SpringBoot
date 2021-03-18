package com.example.feng.user.dto;

/**
 * @author fengjirong
 * @date 2020/11/16 14:20
 */
public class UserDto {

    /**
     * 主键ID
     */
    private Long userId;
    /**
     * 用户名*/
    private String userName;
    /**
     * 密码*/
    private String userPassword;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }
}
