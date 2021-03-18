package com.example.feng.account.mapper;

import com.example.feng.account.dto.Account;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * @author fengjirong
 * @date 2020/11/16 20:47
 */
@Mapper
public interface AccountMapper {

    /**
     * description: 获得账户信息
     *
     * @param account
     * @return
     * @Author fengjirong
     * @Date   2020/11/16 20:48
     */
    List<Account> getAccount(Account account);

    /**
     * description: 更新账户信息
     *
     * @param account
     * @return
     * @Author fengjirong
     * @Date   2020/11/16 20:50
     */
    int setAmount(Account account);
}
