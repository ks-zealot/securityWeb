package com.sec.web.util;

import com.google.common.io.Resources;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by y.lybarskiy on 14.09.2015.
 */
public class HtmlLoader {
    private static Logger logger = Logger.getLogger(HtmlLoader.class);
    private static Map<String, String> cache = new ConcurrentHashMap<>();

    public String loadHtml(String page) {
        try {
            String html = cache.get(page);
            if (html == null) {
                html = Resources.toString(Resources.getResource("html/" + page), Charset.defaultCharset());
                cache.put(page, html);
            }
            return html;
        } catch (IOException e) {
            logger.error("error", e);
            return "Такой страницы нет";
        }
    }

    public static void main(String[] args) {
        HtmlLoader loader = new HtmlLoader();
        System.out.println(loader.loadHtml("pleaseLogin.html"));

    }
}
