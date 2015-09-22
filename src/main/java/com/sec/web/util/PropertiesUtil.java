package com.sec.web.util;

import com.google.common.io.Resources;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.io.File;

/**
 * Created by y.lybarskiy on 14.09.2015.
 */
public class PropertiesUtil {
    private static PropertiesConfiguration config;

    public static final void init() throws ConfigurationException {

        config = new PropertiesConfiguration(Resources.getResource("security.properties"));
        config.load();
    }

    public static String getString(String name) {
        return config.getString(name);
    }

    public static Integer getInt(String name) {
        return config.getInt(name);
    }
}
