package com.example.feng.account.service;

import com.example.feng.account.dto.Account;

import java.util.List;

/**
 * @author fengjirong
 * @date 2020/11/16 21:04
 */
public interface AccountService {
    /**
     * description: 获取账户信息
     *
     * @param account
     * @return list
     * @Author fengjirong
     * @Date   2020/11/16 21:05
     */
    List<Account> getAccount(Account account);

    /**
     * description: 更新账户信息
     *
     * @param account
     * @return int
     * @Author fengjirong
     * @Date   2020/11/16 21:06
     */
    int setAccount(Account account);
}
