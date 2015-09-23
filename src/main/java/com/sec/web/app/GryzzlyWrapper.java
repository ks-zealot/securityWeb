package com.sec.web.app;


import com.google.common.io.Resources;
import com.sec.web.handlers.*;
import com.sec.web.util.Formatter;
import com.sec.web.util.PropertiesUtil;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by y.lybarskiy on 07.09.2015.
 */
public class GryzzlyWrapper {
    private static final String KEYSTORE_SERVER_FILE = "keystore_server";
    private static final String KEYSTORE_SERVER_PWD = "asdfgh";
    private static final String TRUSTORE_SERVER_FILE = "trustore_server";
    private static final String TRUSTORE_SERVER_PWD = "asdfgh";
    private HttpServer server;
    private String host = "localhost";
    private int port = 80;
    private String name;
    private static final Logger logger = Logger.getLogger(GryzzlyWrapper.class);
    private Map<String, Subject> userMap = new ConcurrentHashMap();
    private Queue<String> accessTokens = new ConcurrentLinkedQueue<>();
    private Map<String, String> issuedToken = new ConcurrentHashMap();

    public Queue<String> getAccessTokens() {
        return accessTokens;
    }

    public Map<String, String> getIssuedToken() {
        return issuedToken;
    }


    public void start() throws IOException, ConfigurationException {
        PropertiesUtil.init();

        for (int i = 0; i < 10; i++) {
            accessTokens.add(UUID.randomUUID().toString());
        }
        NetworkListener networkListener = null;
        logger.info("start http gryzzly server");
        server = new HttpServer();
        final ServerConfiguration config = server.getServerConfiguration();
        if (name.contains("client")) {//OAuth client web application
            logger.info("start client OAuth application");
            host = PropertiesUtil.getString("clientAddress");
            port = PropertiesUtil.getInt("clientPort");
            networkListener = new NetworkListener("listener", host, port);
            ClientSecurityLoginHandler authHandler = new ClientSecurityLoginHandler(this);
            config.addHttpHandler(authHandler, "/login");
            ClientSecurityResourceHandler secHandler = new ClientSecurityResourceHandler(this);
            config.addHttpHandler(secHandler, "/secRes");

        } else if (name.contains("server")) {//OAuth server web application
            host = PropertiesUtil.getString("serverAddress");
            port = PropertiesUtil.getInt("serverPort");
            networkListener = new NetworkListener("listener", host, port);
            SSLContextConfigurator sslContext = new SSLContextConfigurator();
            File f = new File(KEYSTORE_SERVER_FILE);
            // set up security context
            String keystore_server = Resources.getResource("keystore_server").getFile();
            // String truststore_server = Thread.currentThread().getContextClassLoader().getResource("truststore_server").getFile();
            logger.info(keystore_server);
            networkListener.setScheme("https");
            sslContext.setKeyStoreFile(keystore_server); // contains server keypair
            sslContext.setKeyStorePass(KEYSTORE_SERVER_PWD);
            // sslContext.setTrustStoreFile(TRUSTORE_SERVER_FILE); // contains client certificate
            // sslContext.setTrustStorePass(TRUSTORE_SERVER_PWD);
            networkListener.setSecure(true);
            networkListener.setSSLEngineConfig(new SSLEngineConfigurator(sslContext, false, false, false));//.setNeedClientAuth(false));
            ServerSecurityLoginHandler authHandler = new ServerSecurityLoginHandler(this);
            config.addHttpHandler(authHandler, "/login");
            ServerSecurityResourceHandler secHandler = new ServerSecurityResourceHandler(this);
            config.addHttpHandler(secHandler, "/secRes");
        }
        RootHandler root = new RootHandler();
        config.addHttpHandler(root, "/");
        logger.info("start server listener on host " + host + " port " + port);


        logger.info("add handler on uri /login");
        logger.info("add handler on uri /secRes");
        logger.info("load security configuration");
        Factory<SecurityManager> factory = new IniSecurityManagerFactory("classpath:shiro.ini");

        logger.info("create security manager");
        SecurityManager securityManager = factory.getInstance();

        logger.info("set security manager");
        SecurityUtils.setSecurityManager(securityManager);

        logger.info("add listner");
        server.addListener(networkListener);
        logger.info("start server");
        server.start();
        logger.info("server start on " + networkListener.getHost() + ":" + networkListener.getPort());
        System.in.read();
    }

    public void stop() {
        server.shutdown();
    }

    public void setName(String name) {
        this.name = name;
    }


    public Subject getUser(String user) {
        Subject subject = userMap.get(user);
        if (subject == null) {
            logger.info("user " + user + " authenticate at first time");
            subject = SecurityUtils.getSubject();
            userMap.put(user, subject);
        }
        return subject;
    }
}
