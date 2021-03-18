package com.example.feng.utils;

import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author fengjirong
 * @date 2021/3/11 15:14
 */
public class ApplicaitonFactory {
    private static ConfigurableApplicationContext context;

    public static ConfigurableApplicationContext getContext() {
        return context;
    }

    public static void setContext(ConfigurableApplicationContext context) {
        context = context;
    }
}
