package com.example.feng.demo01.dto;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 配置绑定，只有容器中的组件才能使用配置绑定功能
 *
 * 配置绑定方法
 * 1.
 * @Component
 * @ConfigurationProperties()
 * 2
 * 容器中使用
 * @EnableConfigurationProperties(Car.class) 开启对应类的配置绑定功能
 * @ConfigurationProperties()
 * prefix 指定配置文件中对应配置的前缀，多层有效前缀之间用.分隔
 * value  指定配置文件中对应属性配置的前缀，多层有效前缀之间用.分隔
 * @author fengjirong
 * @date 2021/2/5 16:52
 */
//@Component
@ConfigurationProperties(value = "car")
public class Car {
    private String name;

    private String price;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Car{" +
                "name='" + name + '\'' +
                ", price='" + price + '\'' +
                '}';
    }
}
