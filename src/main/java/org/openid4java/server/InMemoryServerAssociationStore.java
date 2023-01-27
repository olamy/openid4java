/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.server;

import org.openid4java.association.Association;
import org.openid4java.association.AssociationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class InMemoryServerAssociationStore implements ServerAssociationStore
{
    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryServerAssociationStore.class);
    private String _timestamp;
    private int _counter;
    private Map _handleMap;

    public InMemoryServerAssociationStore()
    {
        _timestamp = Long.toString(new Date().getTime());
        _counter   = 0;
        _handleMap = new HashMap();
    }

    public synchronized Association generate(String type, int expiryIn)
            throws AssociationException
    {
        removeExpired();

        String handle = _timestamp + "-" + _counter++;

        Association association = Association.generate(type, handle, expiryIn);

        _handleMap.put(handle, association);

        if (LOGGER.isDebugEnabled()) LOGGER.debug("Generated association, handle: " + handle +
                              " type: " + type +
                              " expires in: " + expiryIn + " seconds.");

        return association;
    }

    public synchronized Association load(String handle)
    {
        removeExpired();

        return (Association) _handleMap.get(handle);
    }

    public synchronized void remove(String handle)
    {
        if (LOGGER.isDebugEnabled()) LOGGER.debug("Removing association, handle: {}", handle);

        _handleMap.remove(handle);

        removeExpired();
    }

    private synchronized void removeExpired()
    {
        Set handleToRemove = new HashSet();
        Iterator handles = _handleMap.keySet().iterator();
        while (handles.hasNext())
        {
            String handle = (String) handles.next();

            Association association = (Association) _handleMap.get(handle);

            if (association.hasExpired())
                handleToRemove.add(handle);
        }

        handles = handleToRemove.iterator();
        while (handles.hasNext())
        {
            String handle = (String) handles.next();

            if (LOGGER.isDebugEnabled()) LOGGER.debug("Removing expired association, handle: {}", handle);

            _handleMap.remove(handle);
        }
    }

    protected synchronized int size()
    {
        return _handleMap.size();
    }
}
