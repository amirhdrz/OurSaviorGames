package com.oursaviorgames.backend.servlet;

import org.apache.http.HttpStatus;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

public class BaseServlet extends HttpServlet {

    public static String ERROR_REDIRECT_URL = "http://oursaviorgames.com/";

    protected void setErrorRedirect(HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpStatus.SC_MOVED_PERMANENTLY);
        resp.sendRedirect(ERROR_REDIRECT_URL);
    }

}
