package com.itech.test;

import lombok.Getter;
import lombok.Setter;
import org.itech.framework.fx.core.annotations.components.Component;
import org.itech.framework.fx.core.annotations.constructor.DefaultConstructor;
import org.itech.framework.fx.core.annotations.parameters.DefaultParameter;

@Component(name = "test_2")
@Getter
@Setter
public class Test2 {
    private final String name;
    private final String email;
    private final int age;

    @DefaultConstructor
    public Test2(@DefaultParameter("john") String name, @DefaultParameter(value = "john@gmail.com") String email,
                 @DefaultParameter("30") int age) {
        this.name = name;
        this.email = email;
        this.age = age;
    }
}
