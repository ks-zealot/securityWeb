package com.sec.web.handlers;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.sec.web.app.GryzzlyWrapper;
import com.sec.web.enity.Result;
import com.sec.web.util.Formatter;
import com.sec.web.util.HtmlLoader;
import com.sec.web.util.PropertiesUtil;
import org.apache.log4j.Logger;
import org.apache.shiro.subject.Subject;
import org.glassfish.grizzly.http.Cookie;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.ContentType;
import org.glassfish.grizzly.http.util.Header;
import org.glassfish.grizzly.http.util.HttpStatus;

import java.io.Writer;

/**
 * Created by y.lybarskiy on 22.09.2015.
 */
public class ServerSecurityResourceHandler extends HttpHandler {
    private static final Logger logger = Logger.getLogger(ServerSecurityResourceHandler.class);
    private GryzzlyWrapper wrapper;
    private HtmlLoader loader = new HtmlLoader();
    private ObjectMapper mapper = new ObjectMapper();
    public ServerSecurityResourceHandler(GryzzlyWrapper wrapper) {
        super();
        this.wrapper = wrapper;
    }

    @Override
    public void service(Request request, Response response) throws Exception {
        Cookie[] cookies = request.getCookies();
        response.setCharacterEncoding("UTF-8");
        response.setContentType(ContentType.newContentType("text/html", "utf8"));
        String username = null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("user")) {
                username = cookie.getValue();
            }
        }
        logger.info("get username from cookie");
        if (username == null) {
            logger.info("empty username, return login page");
            String html = loader.loadHtml("pleaseLogin.html");
            html = Formatter.format(html);
            response.getWriter().write(html);
            response.getWriter().write(html);
            return;
        }
        final Subject currentUser = wrapper.getUser(username);
        logger.info("get shiro user " + currentUser);
        if (currentUser.isAuthenticated()) {
            String accessToken = request.getParameter("accessToken");
            String sec = request.getParameter("sec");
            if (sec != null) {
                Result result = new Result();
                ObjectWriter writer = mapper.writer().withDefaultPrettyPrinter();
                switch (sec) {
                    case "1":
                        if (!currentUser.hasRole("secrole1")) {
                            result.setResult("User not allowed");
                            result.setStatus(503);
                            response.getWriter().write(writer.writeValueAsString(result));//loader.loadHtml("notAllowed.html"));
                        } else {
                            result.setResult("Success get first resource");
                            result.setStatus(200);
                            response.getWriter().write(writer.writeValueAsString(result));//loader.loadHtml("securityResource1.html"));
                        }
                        return;
                    case "2":
                        if (!currentUser.hasRole("secrole2")) {
                            result.setResult("User not allowed");
                            result.setStatus(503);
                            response.getWriter().write(writer.writeValueAsString(result));//loader.loadHtml("notAllowed.html"));
                        } else {
                            result.setResult("Success get second resource");
                            result.setStatus(200);
                            response.getWriter().write(writer.writeValueAsString(result));//loader.loadHtml("securityResource1.html"));
                        }
                        return;
                    default:
                        response.getWriter().write(loader.loadHtml("noSuchResource.html"));
                        return;
                }

            }
            if (accessToken != null && wrapper.getIssuedToken().values().contains(accessToken)) {
                String remoteAddress = request.getRemoteAddr();
                logger.info("check accessToken for address " + remoteAddress);
                String token = wrapper.getIssuedToken().get(remoteAddress);
                if (token == null) {
                    throw new Exception("address " + remoteAddress + " is not registered in system ");
                }
                if (!token.equalsIgnoreCase(accessToken)) {
                    throw new Exception("token " + accessToken + " stolen  by " + remoteAddress);
                }
                response.setStatus(HttpStatus.FOUND_302);
                logger.info("get access token " + accessToken);
                logger.info("remove token" + accessToken + " from issued token map for address " + remoteAddress);
                wrapper.getIssuedToken().remove(remoteAddress);
                logger.info("add token to access token list for futuree using");
                wrapper.getAccessTokens().add(accessToken);
                response.setHeader(Header.Location, PropertiesUtil.getString("clientHost") + "/secRes?result=success");
                final Writer writer = response.getWriter();
                writer.write("Completed.");
                return;
            }
            String result = request.getParameter("result");

            String html = loader.loadHtml("securityResource.html");
            html = Formatter.format(html);
            html = Formatter.format(html, "clientId", PropertiesUtil.getString("clientId"));
            html = Formatter.format(html, "requestToken", PropertiesUtil.getString("requestToken"));
            html = Formatter.format(html, "redirectUri", PropertiesUtil.getString("clientHost") + "/secRes");

            response.getWriter().write(html);
            return;
            //write page with sec resources
        } else {
            String html = loader.loadHtml("pleaseLogin.html");
            html = Formatter.format(html);
            response.getWriter().write(html);
            return;
            //write error page with link to login
        }
    }
}
