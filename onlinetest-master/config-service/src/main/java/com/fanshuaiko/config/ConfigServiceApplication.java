package com.fanshuaiko.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.config.server.EnableConfigServer;
// import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableConfigServer
// @EnableEurekaClient  // 暂时禁用Eureka客户端，独立启动
public class ConfigServiceApplication {

    private static final Logger logger = LoggerFactory.getLogger(ConfigServiceApplication.class);

    public static void main(String[] args) {
        logger.info("Starting Config Server Application...");
        try {
            SpringApplication.run(ConfigServiceApplication.class, args);
            logger.info("Config Server Application started successfully!");
        } catch (Exception e) {
            logger.error("Failed to start Config Server Application", e);
            throw e;
        }
    }

    @EventListener
    public void handleContextRefresh(ContextRefreshedEvent event) {
        logger.info("Config Server context refreshed and ready to serve requests");
    }
}