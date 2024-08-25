package org.itech.framework.fx;

import org.itech.framework.fx.core.App;
import org.itech.framework.fx.core.annotations.ComponentScan;
import org.itech.framework.fx.core.annotations.components.Component;
import org.itech.framework.fx.core.annotations.reactives.Rx;

@ComponentScan(basePackage = "com.itech.test")
public class Main {
    public static void main(String[] args) {
        App.run(Main.class);

    }
}