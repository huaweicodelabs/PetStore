
package com.huawei.hms.petstore.starter;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.logging.log4j.Logger;

import com.huawei.hms.petstore.common.log.LogFactory;

@WebListener
public class PetstoreContextListener implements ServletContextListener {

    private static final Logger LOG = LogFactory.runLog();

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {

    }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        LOG.info("ServletContextListener start");
        // TODO
        LOG.info("ServletContextListener end");
    }

}
