
package com.huawei.hms.petstore.starter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.web.Log4jServletContextListener;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
public class Log4jConfigInitializer implements ServletContextInitializer {
    private static final Logger LOGGER = LogManager.getLogger(Log4jConfigInitializer.class);

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        LOGGER.info("启动log4jConfigListener....");
        servletContext.addListener(Log4jServletContextListener.class);
        servletContext.setInitParameter("log4jConfigLocation", "classpath:log4j2.xml");
        servletContext.setInitParameter("log4jRefreshInterval", "60000");
        LOGGER.info("启动log4jConfigListener 完成");
    }
}
