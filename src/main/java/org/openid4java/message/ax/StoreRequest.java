/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.message.ax;

import org.openid4java.message.MessageException;
import org.openid4java.message.Parameter;
import org.openid4java.message.ParameterList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the extension for Attribute Exchange store requests.
 *
 * @author Marius Scurtescu, Johnny Bufu
 */
public class StoreRequest extends AxPayload
{
    private static final Logger LOGGER = LoggerFactory.getLogger(StoreRequest.class);

    /**
     * Constructs a Store Request with an empty parameter list.
     */
    protected StoreRequest()
    {
        _parameters.set(new Parameter("mode", "store_request"));

        if (LOGGER.isDebugEnabled()) LOGGER.debug("Created empty store request.");
    }

    /**
     * Constructs a Store Request with an empty parameter list.
     */
    public static StoreRequest createStoreRequest()
    {
        return new StoreRequest();
    }

    /**
     * Constructs a StoreRequest from a parameter list.
     * <p>
     * The parameter list can be extracted from a received message with the
     * getExtensionParams method of the Message class, and MUST NOT contain
     * the "openid.extension_alias." prefix.
     */
    protected StoreRequest(ParameterList params)
    {
        _parameters = params;
    }

    /**
     * Constructs a StoreRequest from a parameter list.
     * <p>
     * The parameter list can be extracted from a received message with the
     * getExtensionParams method of the Message class, and MUST NOT contain
     * the "openid.extension_alias." prefix.
     */
    public static StoreRequest createStoreRequest(ParameterList params)
            throws MessageException
    {
        StoreRequest req = new StoreRequest(params);

        if (! req.isValid())
            throw new MessageException("Invalid parameters for a store request");

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Created store request from parameter list:\n {}", params);

        return req;
    }

    /**
     * Checks the validity of the extension.
     * <p>
     * Used when constructing a extension from a parameter list.
     *
     * @return      True if the extension is valid, false otherwise.
     */
    public boolean isValid()
    {
        if ( ! _parameters.hasParameter("mode") ||
                ! "store_request".equals(_parameters.getParameterValue("mode")))
        {
            LOGGER.warn("Invalid mode value in store_request: "
                      + _parameters.getParameterValue("mode"));
            return false;
        }

        return super.isValid();
    }

}
