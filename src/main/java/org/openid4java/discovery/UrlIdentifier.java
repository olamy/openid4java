/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.discovery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.HashSet;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class UrlIdentifier implements Identifier
{
    private static final Logger LOGGER = LoggerFactory.getLogger(UrlIdentifier.class);
    private static final Set<Character> UNRESERVED_CHARACTERS = new HashSet<>();

    static
    {
        for (char c = 'a'; c <= 'z'; c++)
            UNRESERVED_CHARACTERS.add(c);

        for (char c = 'A'; c <= 'Z'; c++)
            UNRESERVED_CHARACTERS.add(c);

        for (char c = '0'; c <= '9'; c++)
            UNRESERVED_CHARACTERS.add(c);

        UNRESERVED_CHARACTERS.add('-');
        UNRESERVED_CHARACTERS.add('.');
        UNRESERVED_CHARACTERS.add('_');
        UNRESERVED_CHARACTERS.add('~');
    }

    private final URL _urlIdentifier;

    public UrlIdentifier(String identifier) throws DiscoveryException
    {
        this(identifier, false);
    }

    public UrlIdentifier(String identifier, boolean removeFragment)
        throws DiscoveryException
    {
        _urlIdentifier = normalize(identifier, removeFragment);
    }

    public boolean equals(Object o)
    {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        final URL thisUrl = _urlIdentifier;
        final URL thatUrl = ((UrlIdentifier) o)._urlIdentifier;

        if (thisUrl == null || thatUrl == null)
            return thisUrl == thatUrl;
        else
            return stringsEqualIgnoreCase(thisUrl.getProtocol(), thatUrl.getProtocol()) &&
                   stringsEqualIgnoreCase(thisUrl.getHost(), thatUrl.getHost()) &&
                   (
                       (thisUrl.getPort() == -1 ? thisUrl.getDefaultPort() : thisUrl.getPort())
                        ==
                       (thatUrl.getPort() == -1 ? thatUrl.getDefaultPort() : thatUrl.getPort())
                   ) &&
                   stringsEqual(thisUrl.getFile(), thatUrl.getFile()) &&
                   stringsEqual(thisUrl.getRef(), thatUrl.getRef());
    }

    public int hashCode()
    {
        int result = 0;

        if (_urlIdentifier.getProtocol() != null)
            result += _urlIdentifier.getProtocol().hashCode();

        if (_urlIdentifier.getHost() != null)
            result += _urlIdentifier.getHost().toLowerCase().hashCode();

        if (_urlIdentifier.getFile() != null)
            result += _urlIdentifier.getFile().hashCode();

        if (_urlIdentifier.getPort() == -1)
            result += _urlIdentifier.getDefaultPort();
        else
            result += _urlIdentifier.getPort();

        if (_urlIdentifier.getRef() != null)
            result += _urlIdentifier.getRef().hashCode();

        return result;
    }

    public String getIdentifier()
    {
        return _urlIdentifier.toExternalForm();
    }

    public String toString()
    {
        return _urlIdentifier.toExternalForm();
    }

    public URL getUrl()
    {
        return _urlIdentifier;
    }

    public static URL normalize(String text) throws DiscoveryException
    {
        return normalize(text, false);
    }

    public static URL normalize(String text, boolean removeFragment)
        throws DiscoveryException
    {
        try
        {
            URI uri = new URI(text);
            URL url = uri.normalize().toURL();

            String protocol = url.getProtocol().toLowerCase();
            String host = url.getHost().toLowerCase();
            int port = url.getPort();
            String path = normalizeUrlEncoding(url.getPath());
            String query = normalizeUrlEncoding(url.getQuery());
            String fragment = normalizeUrlEncoding(url.getRef());

            if (port == url.getDefaultPort())
                port = -1;

            // start building the 'file' part for the URL constructor...
            String file = path;

            if ("".equals(file))
                file = "/";

            if (query != null)
                file = file + "?" + query;

            if (fragment != null && ! removeFragment)
                file = file + "#" + fragment;

            URL normalized = new URL(protocol, host, port, file);

            if (LOGGER.isDebugEnabled()) LOGGER.debug("Normalized: " + text + " to: " + normalized);

            return normalized;
        }
        catch (MalformedURLException|URISyntaxException e)
        {
            throw new DiscoveryException("Invalid URL identifier", e);
        }

    }

    private static boolean stringsEqual(String s1, String s2) {
        return s1 == null ? s2 == null : s1.equals(s2);
    }

    private static boolean stringsEqualIgnoreCase(String s1, String s2) {
        return s1 == null ? s2 == null : s1.equalsIgnoreCase(s2);
    }

    private static String normalizeUrlEncoding(String text)
    {
        if (text == null)
            return null;

        int len = text.length();
        StringBuilder normalized = new StringBuilder(len);

        for (int i = 0; i < len; i++)
        {
            char current = text.charAt(i);

            if (current == '%' && i < len - 2)
            {
                String percentCode = text.substring(i, i + 3).toUpperCase();

                String str = URLDecoder.decode(percentCode, StandardCharsets.ISO_8859_1);
                char chr = str.charAt(0);

                if (UNRESERVED_CHARACTERS.contains(chr))
                    normalized.append(chr);
                else
                    normalized.append(percentCode);

                i += 2;
            }
            else
            {
                normalized.append(current);
            }
        }

        return normalized.toString();
    }
}
