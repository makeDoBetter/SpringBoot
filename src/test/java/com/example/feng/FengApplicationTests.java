package com.example.feng;

import com.example.feng.account.dto.Account;
import com.example.feng.account.mapper.AccountMapper;
import com.example.feng.account.service.AccountService;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;

import java.math.BigDecimal;
import java.util.List;

@SpringBootTest
@TestConfiguration
@ComponentScan({"com.example.feng.*.service.impl"})
@MapperScan({"com.example.feng.*.mapper"})
class FengApplicationTests {
    @Autowired
    AccountService service;

    @Autowired
    AccountMapper mapper;

    @Test
    void  contextLoads() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    updateAccountInfo();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    updateAccountInfo();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Test
    void updateAccountInfo(){
        Account account = new Account();
        account.setAccountName("账户1");
        List<Account> list = service.getAccount(account);
        Account account_1 = list.get(0);
        account_1.setAmount(list.get(0).getAmount().add(new BigDecimal(100)));
        service.setAccount(account_1);
    }

    /**
     * description: 测试==与equals()差别
     *
     * @Author fengjirong
     * @Date   2021/3/15 22:55
     */
    @Test
    void doTestEquals(){
        String s1 = "abc";
        String s2 = new String("abc");
        String s3 = s2;
        //false
        System.out.println(s1==s2);
        //false
        System.out.println(s1==s3);
        //true
        System.out.println(s2==s3);
        //true
        System.out.println(s1.equals(s2));
        System.out.println(s1.equals(s3));
        System.out.println(s2.equals(s3));
    }

}
