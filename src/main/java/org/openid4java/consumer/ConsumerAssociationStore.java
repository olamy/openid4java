/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.consumer;

import com.google.inject.ImplementedBy;

import org.openid4java.association.Association;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
@ImplementedBy(InMemoryConsumerAssociationStore.class)
public interface ConsumerAssociationStore
{
    void save(String opUrl, Association association);

    Association load(String opUrl, String handle);

    Association load(String opUrl);

    void remove(String opUrl, String handle);
}
