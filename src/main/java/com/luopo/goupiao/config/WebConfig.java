package com.luopo.goupiao.config;

import com.luopo.goupiao.access.AccessInterceptor;
import com.luopo.goupiao.argumentResolver.UserArgumentResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

    @Autowired
    UserArgumentResolver userArgumentResolver;

    @Autowired
    AccessInterceptor accessIneterceptor;

    //关于springboot2.1中取消了 WebMvcConfigurerAdapter 类而建议使用它的父类接口
    //此处是1.5.8版本还是用原来的
    //Java8新特性，接口方法可以拥有默认实现。所以可以直接实现WebMvcConfigurer
    //而不用像以前那样通过继承它的实现类 WebMvcConfigurerAdapter 来达到目的。
    //为什么Java8要弄个接口默认方法，进入WebMvcConfigurer类查看源码，可以发现其中定义了大量的方法
    //与WebMvcConfigurerAdapter进行对比、
    //虽然WebMvcConfigurerAdapter实现了WebMvcConfigurer接口，但是大量的实现都是空实现
    //做成这样是因为Java的单继承多实现规则，一个类可能同时需要WebMvcConfigurer和其他类中的方法
    //用继承的方式限制了这一点
    //而WebMvcConfigurer中的方法也不是在每个地方都会用到，所以写了一些特定场合的适配
    //这样就可以按需继承对应的适配器
    //而自己的定制实现通过多态性对外展示为WebMvcConfigurer，使框架能够降低耦合度。
    //但对于追求完美的编程人员，这显然无法令人满意。于是出现了带有默认实现的接口，
    //这样在使用的时候只需要实现自己想要的方法就行了，不用再去手动空实现或编写适配器
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> handlerMethodArgumentResolverList) {
        handlerMethodArgumentResolverList.add(userArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry interceptorRegistry) {
        interceptorRegistry.addInterceptor(accessIneterceptor);
    }

}
