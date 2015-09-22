package com.sec.web.handlers;


import com.sec.web.app.GryzzlyWrapper;
import com.sec.web.util.Formatter;
import com.sec.web.util.HtmlLoader;
import com.sec.web.util.PropertiesUtil;
import org.apache.log4j.Logger;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.glassfish.grizzly.http.Cookie;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.ContentType;

import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by y.lybarskiy on 22.09.2015.
 */
public class ClientSecurityLoginHandler extends HttpHandler {
    private String password;
    private String login;
    private String host;
    private int port;
    private GryzzlyWrapper wrapper;
    public static final Logger logger = Logger.getLogger(ClientSecurityLoginHandler.class);
    private HtmlLoader loader = new HtmlLoader();

    public ClientSecurityLoginHandler(GryzzlyWrapper wrapper) {
        super();
        this.wrapper = wrapper;

    }

    @Override
    public void service(Request request, final Response response) throws Exception {


        logger.info("get request from " + request.getRemoteAddr());
        response.setContentType(ContentType.newContentType("text/html", "utf8"));
        response.suspend();

        logger.info("get shiro user");
        String user = request.getParameter("username");
        String pass = request.getParameter("password");

        if (user != null && pass != null)//is multipart{

        {
            final Subject currentUser = wrapper.getUser(user);//SecurityUtils.getSubject();

            try {
                logger.info("authenticate user " + user + " with password " + pass);
                if (!currentUser.isAuthenticated()) {

                    logger.info("user " + user + " not authenticated ");
                    UsernamePasswordToken token = new UsernamePasswordToken(user, pass);
                    token.setRememberMe(true);
                    logger.info("remember user " + user);
                    currentUser.login(token);
                    logger.info("login user " + user);
                    response.addCookie(new Cookie("user", user));
                    logger.info("set cookie " + user);
                }

                String clientId = request.getParameter("client_id");
                logger.info("check clientId");
                if (clientId != null && !clientId.equals(PropertiesUtil.getString("clientId"))) {
                    throw new IllegalArgumentException("wrong clientId " + clientId);
                }

                //write page with link to sec resources
                logger.info("display success form");
                String html = loader.loadHtml("successPage.html");
                html = Formatter.format(html);
                final Writer writer = response.getWriter();
                writer.write(html);
                response.resume();
            } catch (Exception ex) {
                logger.error("error ", ex);
                String html = loader.loadHtml("failurePage.html");
                html = Formatter.format(html);
                html = Formatter.format(html, "host", PropertiesUtil.getString("clientHost"));
                response.setContentType(ContentType.newContentType("text/html", "utf8"));
                final Writer writer = response.getWriter();
                writer.write(html);
                response.resume();
            }

            // Resume the asynchronous HTTP request processing
            // (in other words finish the asynchronous HTTP request processing).
            response.resume();
        } else {
            logger.info("write login form");
            String html = loader.loadHtml("loginForm.html");
            //html = Formatter.format(html);
            html = Formatter.format(html, "host", PropertiesUtil.getString("clientHost"));
            response.setContentType(ContentType.newContentType("text/html", "utf8"));
            response.getWriter().write(html);
            response.resume();
        }

    }

    public Map<String, List<String>> splitQuery(String uri) throws UnsupportedEncodingException {
        final Map<String, List<String>> query_pairs = new LinkedHashMap<String, List<String>>();
        uri = uri.split("\\?")[1];
        final String[] pairs = uri.split("&");
        for (String pair : pairs) {
            final int idx = pair.indexOf("=");
            final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
            if (!query_pairs.containsKey(key)) {
                query_pairs.put(key, new LinkedList<String>());
            }
            final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
            query_pairs.get(key).add(value);
        }
        return query_pairs;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setLogin(String login) {
        this.login = login;
    }


}
