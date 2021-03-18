package com.example.feng.account.service.impl;

import com.example.feng.account.dto.Account;
import com.example.feng.account.mapper.AccountMapper;
import com.example.feng.account.service.AccountService;
import com.example.feng.config.RedisConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author fengjirong
 * @date 2020/11/16 21:07
 */
@Service
public class AccountServiceImpl implements AccountService {
    private Logger logger = LoggerFactory.getLogger(Exception.class);

    @Autowired
    private AccountMapper mapper;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public List<Account> getAccount(Account account) {
        return mapper.getAccount(account);
    }

    @Override
    public int setAccount(Account account) {
        try {
            redisTemplate.delete("account");
            //设置过期时间
            redisTemplate.opsForValue().set("account", String.valueOf(account),5, TimeUnit.SECONDS);
            String s = redisTemplate.opsForValue().get("account");
            logger.info(s);
            Thread.sleep(6000);
            String s1 = redisTemplate.opsForValue().get("account");
            logger.info(s1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return mapper.setAmount(account);
    }
}
