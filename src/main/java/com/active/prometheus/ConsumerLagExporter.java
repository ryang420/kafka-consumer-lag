package com.active.prometheus;

import com.active.prometheus.collector.ConsumerLagCollector;
import io.prometheus.client.exporter.HTTPServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.PropertyConfigurator;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class ConsumerLagExporter {
    public static void main(String[] args) {
        PropertyConfigurator.configure(System.getProperty("user.dir") + "/config/log4j.properties");
        log.debug("log dir: " + System.getProperty("user.dir"));

        if (args.length != 0) {
            try {
                InputStream in = new BufferedInputStream(new FileInputStream(args[0]));

                Properties prop = new Properties();
                prop.load(in);

                int port = Integer.valueOf(prop.getProperty("port"));

                HTTPServer server = new HTTPServer(port);
                new ConsumerLagCollector(prop).register();
            } catch (IOException e) {
                log.error("Loading property file exception.", e);
            }
        }
    }


}
