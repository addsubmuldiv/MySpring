package com.zyh.spring;

import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

public class MyApplicationContext {
    private Class config;

    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>();

    public MyApplicationContext(Class config) {
        this.config = config;

        // 扫描--> BeanDefinition --> beanDefinitionMap
        if (config.isAnnotationPresent(ComponentScan.class)) {
            ComponentScan scan = (ComponentScan) config.getAnnotation(ComponentScan.class);

            String path = scan.value(); // 获取扫描路径 com.zyh.service

            path = path.replace(".", "/");  // com\zyh\service

            // D:\Code\Java\MySpring\out\production\MySpring\com\zyh\service
            ClassLoader classLoader = MyApplicationContext.class.getClassLoader();
            URL resource = classLoader.getResource(path);

            File file = new File(resource.getFile());
            System.out.println(file);
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File f: files) {
                    String fileName = f.getAbsolutePath();
                    System.out.println(fileName);

                    if (fileName.endsWith(".class")) {
                        String className = scan.value() + "." + f.getName().substring(0, f.getName().indexOf(".class"));
                        System.out.println(className);
                        try {
                            Class<?> clazz = classLoader.loadClass(className);
                            if (clazz.isAnnotationPresent(Component.class)) {  // 获取类名后，判断有没有注解，有就是bean
                                // Bean  这里不能直接就创建，否则无法根据bean的作用域控制生命周期，而是要创建BeanDefinition
                                Component component = clazz.getAnnotation(Component.class);
                                String beanName = component.value();

                                if (beanName.equals("")) { // 如果没有配，生成默认
                                    beanName = Introspector.decapitalize(clazz.getSimpleName());
                                }


                                BeanDefinition beanDefinition = new BeanDefinition();
                                beanDefinition.setType(clazz);
                                if (clazz.isAnnotationPresent(Scope.class)) {
                                    Scope annotation = clazz.getAnnotation(Scope.class);
                                    beanDefinition.setScpoe(annotation.value());
                                } else {
                                    beanDefinition.setScpoe("singleton");
                                }
                                beanDefinitionMap.put(beanName, beanDefinition);
                            }
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }

                    }

                }
            }
        }

        // 实例化单例bean
        for (String beanName : beanDefinitionMap.keySet()) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if (beanDefinition.getScpoe().equals("singleton")) {
                Object bean = createBean(beanName, beanDefinition);
                singletonObjects.put(beanName, bean);
            }
        }
    }

    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        Class clazz = beanDefinition.getType();
        try {
            Object instance = clazz.getConstructor().newInstance();
            return instance;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Object getBean(String beanName) {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (beanDefinition == null) {
            throw new NullPointerException();
        } else {
            String scope = beanDefinition.getScpoe();
            if (scope.equals("singleton")) {
                // 单例，可以在初始化容器的时候就创建
                Object bean = singletonObjects.get(beanName);
                if (bean == null) {
                    bean = createBean(beanName, beanDefinition);
                    singletonObjects.put(beanName, bean);
                }
                return bean;
            } else {
                // 多例
                return createBean(beanName, beanDefinition);
            }
        }
    }
}
