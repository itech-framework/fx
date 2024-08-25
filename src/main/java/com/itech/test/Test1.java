package com.itech.test;

import org.itech.framework.fx.core.annotations.components.Component;
import org.itech.framework.fx.core.annotations.methods.InitMethod;
import org.itech.framework.fx.core.annotations.reactives.Rx;

@Component(name = "test_1")
public class Test1 {
    @Rx(name = "test_2")
    Test2 test2;

    @InitMethod
    void test(){
        System.out.println("Name is: "+test2.getName());
    }
    @InitMethod(order = 2)
    void test2(){
        System.out.println("Email is: " + test2.getEmail());
    }
    @InitMethod(order = 3)
    void test3(){
        System.out.println("Age is: " + test2.getAge());
    }
}