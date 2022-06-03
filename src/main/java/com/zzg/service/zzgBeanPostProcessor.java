package com.zzg.service;

import com.spring.BeanPostProcessor;
import com.spring.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Component
public class zzgBeanPostProcessor implements BeanPostProcessor {

    //每一个bean都会都会调用这两个方法

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        //针对某一个bean特殊处理
        System.out.println("初始化前");
        if (beanName.equals("userService")) {
            ((UserServiceImpl) bean).setName("zzg");
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("初始化后");
        if (beanName.equals("userService")) {
            Object instance = Proxy.newProxyInstance(zzgBeanPostProcessor.class.getClassLoader(), bean.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    System.out.println("代理逻辑");//执行代理逻辑
                    return method.invoke(bean,args);//执行业务逻辑
                }
            });
            return instance;
        }
        return bean;
    }
}
