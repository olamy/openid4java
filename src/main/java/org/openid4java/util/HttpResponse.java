/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */
package org.openid4java.util;

import org.apache.http.Header;

/**
 * Container class for HTTP responses.
 *
 * @author Marius Scurtescu, Johnny Bufu
 */
public interface HttpResponse
{
     /**
     * Gets the status code of the HttpResponse.
     */
    int getStatusCode();

    /**
     * Gets the final URI from where the document was obtained,
     * after following redirects.
     */
    String getFinalUri();
    /**
     * Gets the first header matching the provided headerName parameter,
     * or null if no header with that name exists.
     */
    Header getResponseHeader(String headerName);

    /**
     * Gets an array of Header objects for the provided headerName parameter.
     */
    Header[] getResponseHeaders(String headerName);

    /**
     * Gets the HttpResponse body.
     */
    String getBody();

    /**
     * Returns true if the HTTP response size exceeded the maximum
     * allowed by the (default) HttpRequestOptions.
     */
    boolean isBodySizeExceeded();

}
