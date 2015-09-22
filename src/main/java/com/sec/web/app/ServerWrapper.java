package com.sec.web.app;

import org.apache.commons.configuration.ConfigurationException;

import java.io.IOException;

/**
 * Created by y.lybarskiy on 21.09.2015.
 */
public class ServerWrapper {
    public static void main(String[] args) throws IOException, ConfigurationException {
        GryzzlyWrapper wrapper = new GryzzlyWrapper();
        wrapper.setName("server");
        wrapper.start();
    }
}
