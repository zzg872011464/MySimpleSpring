package com.spring;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 容器类
 */

public class ZzgApplicationContext {

    private Class configClass;
    private ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>();//单例池
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();//存一开始扫描进去的bean的定义
    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();
    ClassLoader classLoader = ZzgApplicationContext.class.getClassLoader(); //得到app类加载器

    public ZzgApplicationContext(Class configClass) {
        this.configClass = configClass;
        //ComponentScan注解--->扫描路径--->扫描--->Beandefinition--->BeanDefinitionMap
        scan(configClass);


        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();
            if (beanDefinition.getScope().equals("singleton")) {
                Object bean = createBean(beanName, beanDefinition);//单例Bean
                singletonObjects.put(beanName, bean);
            }
        }

    }

    public Object createBean(String beanName, BeanDefinition beanDefinition) {
        Class clazz = beanDefinition.getClazz();
        try {


            Object instance = clazz.getDeclaredConstructor().newInstance();

            //依赖注入
            for (Field declaredField : clazz.getDeclaredFields()) {
                if (declaredField.isAnnotationPresent(Autowired.class)) {

                    Object bean = getBean(declaredField.getName());
                    declaredField.setAccessible(true);
                    declaredField.set(instance, bean);
                }
            }

            // Aware回调
            if (instance instanceof BeanNameAware) {
                ((BeanNameAware) instance).setBeanName(beanName);
            }

            // postProcessBeforeInitialization
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
            }


            //初始化
            if (instance instanceof InitializingBean) {
                try {
                    ((InitializingBean) instance).afterPropertiesSet();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            // postProcessAfterInitialization
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessAfterInitialization(instance, beanName);
            }

            return instance;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }


    private void scan(Class configClass) {
        //类加载器:       类加载路径
        //1.Bootstrap--->jre/lib
        //2.Ext--------->jre/ext/lib
        //3.App--------->classpath
        //拿到配置类之后需要进行解析
        //  主要用来解析spring提供的注解
        //如果配置类有componentScan注解
        if (configClass.isAnnotationPresent(ComponentScan.class)) {
            ComponentScan componentScanAnnotation = (ComponentScan) configClass.getDeclaredAnnotation(ComponentScan.class);
            String path = componentScanAnnotation.value(); //扫描路径
            //扫描
            path = path.replace(".", "/");
            URL resource = classLoader.getResource(path);
            File file = new File(resource.getFile());
            if (file.isDirectory()) {
                File[] files = file.listFiles();//拿到目录下的全部文件
                for (File f : files) {
                    String fileName = f.getAbsolutePath();
                    if (fileName.endsWith(".class")) {
                        String className = fileName.substring(fileName.indexOf("com"), fileName.indexOf(".class"));//从com截到.class之前
                        className = className.replace("\\", ".");
                        try {
                            Class<?> clazz = classLoader.loadClass(className);
                            if (clazz.isAnnotationPresent(Component.class)) {
                                //代表当前是一个Bean
                                //解析类,判断当前bean是单例bean,还是prototype(原型)的bean

                                if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                                    BeanPostProcessor instance = (BeanPostProcessor) clazz.getDeclaredConstructor().newInstance();
                                    beanPostProcessorList.add(instance);
                                }

                                Component componentAnnotation = clazz.getDeclaredAnnotation(Component.class);
                                String beanName = componentAnnotation.value();

                                BeanDefinition beanDefinition = new BeanDefinition();
                                beanDefinition.setClazz(clazz);
                                if (clazz.isAnnotationPresent(Scope.class)) {
                                    Scope declaredAnnotation = clazz.getDeclaredAnnotation(Scope.class);
                                    beanDefinition.setScope(declaredAnnotation.value());
                                } else {
                                    beanDefinition.setScope("singleton");
                                }
                                beanDefinitionMap.put(beanName, beanDefinition);

                            }
                        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException |
                                 IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }

                    }

                }
            }

        }
    }


    public Object getBean(String beanName) {
        if (beanDefinitionMap.containsKey(beanName)) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if (beanDefinition.getScope().equals("singleton")) {
                Object o = singletonObjects.get(beanName);
                return o;
            } else {
                //创建bean对象
                Object bean = createBean(beanName, beanDefinition);
                return bean;
            }
        } else {
            //不存在bean
            throw new NullPointerException();
        }
    }
}
