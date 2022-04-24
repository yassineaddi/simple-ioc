package com.yassineaddi.simpleioc;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.yassineaddi.simpleioc.config.AppConfig;
import com.yassineaddi.simpleioc.ioc.ApplicationContext;
import com.yassineaddi.simpleioc.services.UserService;

/**
 * Hello world!
 *
 */
public class App {

    static {
        Logger rootLogger = LogManager.getLogManager().getLogger("");
        rootLogger.setLevel(Level.SEVERE);
        for (Handler h : rootLogger.getHandlers()) {
            h.setLevel(Level.SEVERE);
        }
    }

    public static void main(String[] args) {
        ApplicationContext ctx = new ApplicationContext(AppConfig.class);

        UserService userService = ctx.getBean(UserService.class);

        System.out.println(userService.getUsers());
    }
}
