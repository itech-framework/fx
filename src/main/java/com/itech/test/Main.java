package com.itech.test;

import org.itech.framework.fx.core.ITechApplication;
import org.itech.framework.fx.core.annotations.ComponentScan;
import org.itech.framework.fx.core.annotations.components.Component;
import org.itech.framework.fx.core.annotations.reactives.Rx;

@ComponentScan(basePackage = "com.itech.test")
@Component(name = "main")
public class Main {

    @Rx(name = "test_1")
    private static Test1 test;

    public static void main(String[] args) {
        ITechApplication.run(Main.class);
    }
}
