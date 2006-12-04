/*
 * Copyright 2006 Sxip Identity Corporation
 */

package net.openid.association;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.SecretKey;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.io.Serializable;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class Association implements Serializable
{
    public static final String FAILED_ASSOC_HANDLE      = " ";
    public static final String TYPE_HMAC_SHA1           = "HMAC-SHA1";
    public static final String TYPE_HMAC_SHA256         = "HMAC-SHA256";

    public static final String HMAC_SHA1_ALGORITHM      = "HmacSHA1";
    public static final String HMAC_SHA256_ALGORITHM    = "HmacSHA256";
    public static final int HMAC_SHA1_KEYSIZE = 160;
    public static final int HMAC_SHA256_KEYSIZE = 256;

    private String _type;
    private String _handle;
    private SecretKey _macKey;
    private Date _expiry;

    private Association(String type, String handle, SecretKey macKey, Date expiry)
    {
        _type = type;
        _handle = handle;
        _macKey = macKey;
        _expiry = expiry;
    }

    private Association(String type, String handle, SecretKey macKey, int expiryIn)
    {
        this(type, handle, macKey, new Date(System.currentTimeMillis() + expiryIn * 1000));
    }

    public static Association getFailedAssociation(int expiryIn)
    {
        return new Association(null, FAILED_ASSOC_HANDLE, null,
                new Date(System.currentTimeMillis() + expiryIn * 1000));
    }

    public static Association generate(String type, String handle, int expiryIn) throws AssociationException
    {
        if (TYPE_HMAC_SHA1.equals(type))
        {
            return generateHmacSha1(handle, expiryIn);
        }
        else if (TYPE_HMAC_SHA256.equals(type))
        {
            return generateHmacSha256(handle, expiryIn);
        }
        else
        {
            throw new AssociationException("Unknown association type: " + type);
        }
    }

    public static Association generateHmacSha1(String handle, int expiryIn)
    {
        SecretKey macKey = generateMacSha1Key();

        return new Association(TYPE_HMAC_SHA1, handle, macKey, expiryIn);
    }

    public static Association createHmacSha1(String handle, byte[] macKeyBytes, int expiryIn)
    {
        SecretKey macKey = createMacKey(HMAC_SHA1_ALGORITHM, macKeyBytes);

        return new Association(TYPE_HMAC_SHA1, handle, macKey, expiryIn);
    }

    public static Association generateHmacSha256(String handle, int expiryIn)
    {
        SecretKey macKey = generateMacSha256Key();

        return new Association(TYPE_HMAC_SHA256, handle, macKey, expiryIn);
    }

    public static Association createHmacSha256(String handle, byte[] macKeyBytes, int expiryIn)
    {
        SecretKey macKey = createMacKey(HMAC_SHA256_ALGORITHM, macKeyBytes);

        return new Association(TYPE_HMAC_SHA256, handle, macKey, expiryIn);
    }

    protected static SecretKey generateMacKey(String algorithm, int keySize)
    {
        try
        {
            KeyGenerator keyGen = KeyGenerator.getInstance(algorithm);

            keyGen.init(keySize);

            return keyGen.generateKey();
        }
        catch (NoSuchAlgorithmException e)
        {
            // TODO: log
            return null;
        }
    }

    protected static SecretKey generateMacSha1Key()
    {
        return generateMacKey(HMAC_SHA1_ALGORITHM, HMAC_SHA1_KEYSIZE);
    }

    protected static SecretKey generateMacSha256Key()
    {
        return generateMacKey(HMAC_SHA256_ALGORITHM, HMAC_SHA256_KEYSIZE);
    }

    public static boolean isHmacSupported(String hMacType)
    {
        String hMacAlgorithm;

        if (TYPE_HMAC_SHA1.equals(hMacType))
            hMacAlgorithm = HMAC_SHA1_ALGORITHM;
        else if (TYPE_HMAC_SHA256.equals(hMacType))
            hMacAlgorithm = HMAC_SHA256_ALGORITHM;
        else
            return false;

        try
        {
            KeyGenerator.getInstance(hMacAlgorithm);
        } catch (NoSuchAlgorithmException e)
        {
            return false;
        }

        return true;
    }

    public static boolean isHmacSha256Supported()
    {
        try
        {
            KeyGenerator.getInstance(HMAC_SHA256_ALGORITHM);

            return true;
        }
        catch (NoSuchAlgorithmException e)
        {
            return false;
        }
    }

    public static boolean isHmacSha1Supported()
    {
        try
        {
            KeyGenerator.getInstance(HMAC_SHA1_ALGORITHM);

            return true;
        }
        catch (NoSuchAlgorithmException e)
        {
            return false;
        }
    }

    protected static SecretKey createMacKey(String algorithm, byte[] macKey)
    {
        return new SecretKeySpec(macKey, algorithm);
    }

    public String getType()
    {
        return _type;
    }

    public String getHandle()
    {
        return _handle;
    }

    public SecretKey getMacKey()
    {
        return _macKey;
    }

    public Date getExpiry()
    {
        return _expiry;
    }

    public boolean hasExpired()
    {
        Date now = new Date();

        return _expiry.before(now);
    }

    protected byte[] sign(byte[] data) throws AssociationException
    {
        try
        {
            String algorithm = _macKey.getAlgorithm();
            Mac mac = Mac.getInstance(algorithm);

            mac.init(_macKey);

            return mac.doFinal(data);
        }
        catch (GeneralSecurityException e)
        {
            throw new AssociationException("Cannot sign!", e);
        }
    }

    public String sign(String text) throws AssociationException
    {
        return new String(Base64.encodeBase64(sign(text.getBytes())));
    }

    public boolean verifySignature(String text, String signature) throws AssociationException
    {
        return signature.equals(sign(text));
    }
}
