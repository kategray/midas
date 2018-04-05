package com.codebykate.smartcard;

import javacard.framework.*;
import javacard.security.*;

/**
 *
 */
public class GPKey {
    public static final byte KEY_SOURCE_DEFAULT  = 0x00;    /* Key is applet default key */
    public static final byte KEY_SOURCE_INTERNAL = 0x01;    /* Key was generated internally */
    public static final byte KEY_SOURCE_EXTERNAL = 0x02;    /* Key was imported to card */

    private byte keyIdentifier = 0x00;
    private byte keyType       = 0x00;
    private byte keySource     = 0x00;
    private byte keyVersion    = 0x00;
    private short keyLength    = (short)0x00;

    Key wrappedKey = null;

    public GPKey(byte type, short length) {
        keyType = type;
        keyLength = length;

        wrappedKey = KeyBuilder.buildKey(type, length, MidasApplet.NO_KEY_ENCRYPTION);
    }

    public byte getKeySource () {
        return keySource;
    }

    public void setKey(byte[] key, byte source) {
        switch (keyType) {
            case KeyBuilder.TYPE_AES:
                ((AESKey)wrappedKey).setKey(key, MidasApplet.NO_OFFSET);
                break;
            case KeyBuilder.TYPE_DES:
                ((DESKey)wrappedKey).setKey(key, MidasApplet.NO_OFFSET);
                break;
            default:
                ISOException.throwIt(ISO7816.SW_WRONG_DATA);
        }

        keySource = source;
    }

    public short sign (Signature engine, byte[] buffer, short length) {
        byte[] data = JCSystem.makeTransientByteArray(length, JCSystem.CLEAR_ON_DESELECT);;

        // Initialize the signature engine
        engine.init(wrappedKey, Signature.MODE_SIGN);

        // Both incoming data and outgoing data come from the APDU buffer, so copy the incoming data to data
        Util.arrayCopy(buffer, ISO7816.OFFSET_CDATA, data, MidasApplet.NO_OFFSET, length);

        // 	sign(byte[] inBuff, short inOffset, short inLength, byte[] sigBuff, short sigOffset)
        short macLength = engine.sign(data, MidasApplet.NO_OFFSET, (short)data.length, buffer, ISO7816.OFFSET_CDATA);

        if (macLength <= 0) {
            ISOException.throwIt(ISO7816.SW_UNKNOWN);
        }

        return macLength;
    }
}
