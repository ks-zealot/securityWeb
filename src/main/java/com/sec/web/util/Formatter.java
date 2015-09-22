package com.sec.web.util;

import org.apache.commons.configuration.ConfigurationException;

/**
 * Created by y.lybarskiy on 14.09.2015.
 */
public class Formatter {
    private static PropertiesUtil util = new PropertiesUtil();


    public static String format(String html) {
        html = html.replaceAll("\\$\\{clientHost\\}", util.getString("clientHost"));
        html = html.replaceAll("\\$\\{serverHost\\}", util.getString("serverHost"));
        return html;
    }

    public static String format(String html, String replace, String replacement) {
        return html.replaceAll("\\$\\{" + replace + "\\}", replacement);
    }


}
