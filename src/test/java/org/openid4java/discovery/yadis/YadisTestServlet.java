/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.discovery.yadis;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Simple servlet that builds up responses from varios test-data files
 * for testing the Yadis protocol.
 *
 * @author Marius Scurtescu, Johnny Bufu
 */
public class YadisTestServlet extends HttpServlet
{
    String _testDataPath;

    int port;

    public YadisTestServlet() throws ServletException
    {
        _testDataPath = System.getProperty("YADIS_TEST_DATA", "target/test-data");

        if (_testDataPath == null)
            throw new ServletException("YADIS_TEST_DATA path not initialized");
    }

    public void doHead(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        // set the headers
        String headersFile = request.getParameter("headers");
        setHeadersFromFile(headersFile, response);

    }

    /**
     * Builds a response based on the parameters received in the request,
     * with the following conventions:
     *
     * - the header name-values are extracted from a file with the name specified
     * by the "headers" or "getheaders" (if they need to be different
     * for HEAD and GET requests) parameters;
     * the file should contain a "headername=value" pair on each line
     * Status code should be given on a line with the header name "status"
     *
     * - if there is a "xrds" parameter, its value should point to a file
     * which is streamed for download
     *
     * - otherwise, if there is a "html" parameter, its value should point
     * to a file which is returned as a HTML resonse
     *
     * Headers will always be set if specified; only one of "xrds" and "html"
     * (in this order) will be handled.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        String headersFile = request.getParameter("headers");
        String getHeadersFile = request.getParameter("getheaders");
        String xrdsFile = request.getParameter("xrds");
        String htmlFile = request.getParameter("html");

        // set the headers
        if (getHeadersFile != null) {
            setHeadersFromFile(getHeadersFile, response);
        }
        else if (headersFile != null) {
            setHeadersFromFile(headersFile, response);
        }
        // XRDS download
        if (xrdsFile != null)
        {
            String respStr = Files.readString(Path.of(_testDataPath + "/xrds/" + xrdsFile));
            respStr = respStr.replace("localhost:SERVLET_PORT", "localhost:"+port);
            response.getWriter().print(respStr);

        } else if (htmlFile != null) // HTML response
        {
            String respStr = Files.readString(Path.of(_testDataPath + "/html/" + htmlFile));
            respStr = respStr.replace("localhost:SERVLET_PORT", "localhost:"+port);
            response.getWriter().print(respStr);
        }

    }

    private void setHeadersFromFile(String filename,
                                    HttpServletResponse response)
            throws IOException
    {
        BufferedReader input = new BufferedReader(
                new FileReader(_testDataPath + "/headers/" + filename));

        String line;
        while ((line = input.readLine()) != null)
        {
            int equalPos = line.indexOf("=");
            if (equalPos > -1)
            {
                String headerName = line.substring(0, equalPos);
                String headerValue = line.substring(equalPos + 1);

                if (headerName.equals("status")) {
                    response.setStatus(Integer.parseInt(headerValue));
                } else if (headerName.equals("Location")) {
                    response.addHeader(headerName, headerValue.replace("localhost:SERVLET_PORT", "localhost:"+port));
                } else if (headerName.equals("X-XRDS-Location")) {
                    response.addHeader(headerName, headerValue.replace("localhost:SERVLET_PORT", "localhost:"+port));
                } else {
                    response.addHeader(headerName, headerValue);
                }
            }
        }
    }
}
