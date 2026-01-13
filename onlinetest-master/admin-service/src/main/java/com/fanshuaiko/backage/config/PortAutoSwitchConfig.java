package com.fanshuaiko.backage.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;

@Component
public class PortAutoSwitchConfig implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {

    private static final int MAX_TRIES = 20;

    @Value("${server.port:8083}")
    private int configuredPort;

    @Override
    public void customize(ConfigurableWebServerFactory factory) {
        int targetPort = findAvailablePort(configuredPort);
        factory.setPort(targetPort);
    }

    private int findAvailablePort(int startPort) {
        for (int i = 0; i < MAX_TRIES; i++) {
            int candidate = startPort + i;
            if (isPortFree(candidate)) {
                return candidate;
            }
        }
        return 0;
    }

    private boolean isPortFree(int port) {
        try (ServerSocket ignored = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
