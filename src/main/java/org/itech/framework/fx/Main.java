package org.itech.framework.fx;

import org.itech.framework.fx.core.ITechApplication;
import org.itech.framework.fx.core.annotations.ComponentScan;

@ComponentScan(basePackage = "com.itech.test")
public class Main {
    public static void main(String[] args) {
        ITechApplication.run(Main.class);

    }
}