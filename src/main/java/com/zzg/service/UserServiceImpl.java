package com.zzg.service;

import com.spring.*;

@Component("userService")
@Scope("singleton")
public class UserServiceImpl implements UserService {

    @Autowired
    private orderService orderService;

    private String name;

    public void setName(String name) {
        this.name = name;
    }

//    @Override
//    public void setBeanName(String name) {
//        beanName = name;
//    }
//    @Override
//    public void afterPropertiesSet() throws Exception {
//        //想做什么做什么
//        //eg:验证某些要求
//    }

    public void test() {
        System.out.println(orderService);
        System.out.println(name);
    }


}
