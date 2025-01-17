/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class InMemoryNonceVerifier extends AbstractNonceVerifier
{
    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryNonceVerifier.class);
    private static final boolean DEBUG = LOGGER.isDebugEnabled();

    private Map _opMap = new HashMap();

    public InMemoryNonceVerifier() {
      this(60);
    }

    public InMemoryNonceVerifier(int maxAge)
    {
        super(maxAge);
    }

    protected synchronized int seen(Date now, String opUrl, String nonce)
    {
        removeAged(now);

        Set seenSet = (Set) _opMap.get(opUrl);

        if (seenSet == null)
        {
            seenSet = new HashSet();

            _opMap.put(opUrl, seenSet);
        }

        if (seenSet.contains(nonce))
        {
            LOGGER.error("Possible replay attack! Already seen nonce: " + nonce);
            return SEEN;
        }

        seenSet.add(nonce);

        if (DEBUG) LOGGER.debug("Nonce verified: " + nonce);

        return OK;
    }

    private synchronized void removeAged(Date now)
    {
        Set opToRemove = new HashSet();
        Iterator opUrls = _opMap.keySet().iterator();
        while (opUrls.hasNext())
        {
            String opUrl = (String) opUrls.next();

            Set seenSet = (Set) _opMap.get(opUrl);
            Set nonceToRemove = new HashSet();

            Iterator nonces = seenSet.iterator();
            while (nonces.hasNext())
            {
                String nonce = (String) nonces.next();

                try
                {
                    Date nonceDate = _dateFormat.parse(nonce);

                    if (isTooOld(now, nonceDate))
                    {
                        nonceToRemove.add(nonce);
                    }
                }
                catch (ParseException e)
                {
                    nonceToRemove.add(nonce);
                }
            }

            nonces = nonceToRemove.iterator();
            while (nonces.hasNext())
            {
                String nonce = (String) nonces.next();

                if (DEBUG)
                    LOGGER.debug("Removing nonce: " + nonce +
                               " from OP: " + opUrl);
                seenSet.remove(nonce);
            }

            if (seenSet.size() == 0)
                opToRemove.add(opUrl);
        }

        opUrls = opToRemove.iterator();
        while (opUrls.hasNext())
        {
            String opUrl = (String) opUrls.next();

            if (DEBUG) LOGGER.debug("Removed all nonces from OP: " + opUrl);

            _opMap.remove(opUrl);
        }
    }

    protected synchronized int size()
    {
        int total = 0;

        Iterator opUrls = _opMap.keySet().iterator();
        while (opUrls.hasNext())
        {
            String opUrl = (String) opUrls.next();

            Set seenSet = (Set) _opMap.get(opUrl);

            total += seenSet.size();
        }

        return total;
    }
}
