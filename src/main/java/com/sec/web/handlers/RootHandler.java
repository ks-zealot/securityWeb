package com.sec.web.handlers;

import com.sec.web.util.Formatter;
import com.sec.web.util.HtmlLoader;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.ContentType;

import java.io.IOException;

/**
 * Created by y.lybarskiy on 23.09.2015.
 */
public class RootHandler extends HttpHandler {
    private HtmlLoader loader = new HtmlLoader();
    @Override
    public void service(Request request, Response response) throws Exception {
        response.setContentType(ContentType.newContentType("text/html", "utf8"));
        response.suspend();
        String html = loader.loadHtml("index.html");
        response.getWriter().write(html);
        response.resume();
        return;

    }
}
