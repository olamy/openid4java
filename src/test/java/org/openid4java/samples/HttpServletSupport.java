/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.samples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class HttpServletSupport extends HttpServlet
{
    protected static final long serialVersionUID = 1L;
    protected static Exception lastException;

    protected static int count_;
    protected Logger logger_;

    public HttpServletSupport()
    {
        logger_ = LoggerFactory.getLogger(getClass());
    }

    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        logger_.info("begin onService");
        try
        {
            onService(req, resp);
        }
        catch (Exception exc)
        {
            lastException = exc;
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        finally
        {
            logger_.info("end onService");
        }
    }

    protected abstract void onService(HttpServletRequest req, HttpServletResponse resp) throws Exception;
}
