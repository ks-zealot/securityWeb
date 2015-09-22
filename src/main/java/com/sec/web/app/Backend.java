package com.sec.web.app;

import org.apache.commons.cli.*;
import org.apache.commons.configuration.ConfigurationException;

import java.io.IOException;

/**
 * Created by y.lybarskiy on 07.09.2015.
 */
public class Backend {
    public static void main(String[] args) throws IOException, ParseException, ConfigurationException {
        final GryzzlyWrapper wrapper = new GryzzlyWrapper();
        Options options = new Options();
        options.addOption("n", true, "port to listening for http server");
        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(options, args);
        if (!cmd.getOptionValue("n").equals("OAuth client web application") &&
                !cmd.getOptionValue("n").equals("OAuth server web application")) {
            throw new IllegalArgumentException("application must be client or server");
        }
        wrapper.setName(cmd.getOptionValue("n"));
        wrapper.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                wrapper.stop();
            }
        });
    }
}
