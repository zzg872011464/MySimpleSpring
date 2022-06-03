package com.zzg;

import com.spring.ZzgApplicationContext;
import com.zzg.service.UserService;

public class Test {
    public static void main(String[] args) {
        ZzgApplicationContext applicationContext = new ZzgApplicationContext(AppConfig.class);
        UserService userService = (UserService) applicationContext.getBean("userService"); //map<beanName,bean对象>
        userService.test();//先执行代理方法后执行业务逻辑

    }
}
