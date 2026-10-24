/*
 * Copyright 2004-2024 H2 Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (https://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.util;

import java.util.UUID;

/**
 * Manipulations with bytes and arrays.
 */
public final class Bits {

    /**
     * Reads a big-endian 32-bit integer.
     *
     * @param b the byte array
     * @param off the byte offset
     * @return the value
     */
    public static int getInt(byte[] b, int off) {
        return (b[off] & 0xff) << 24 | (b[off + 1] & 0xff) << 16
                | (b[off + 2] & 0xff) << 8 | (b[off + 3] & 0xff);
    }

    /**
     * Writes a big-endian 32-bit integer.
     *
     * @param b the byte array
     * @param off the byte offset
     * @param v the value
     */
    public static void putInt(byte[] b, int off, int v) {
        b[off] = (byte) (v >>> 24);
        b[off + 1] = (byte) (v >>> 16);
        b[off + 2] = (byte) (v >>> 8);
        b[off + 3] = (byte) v;
    }

    /**
     * Reads a little-endian 32-bit integer.
     *
     * @param b the byte array
     * @param off the byte offset
     * @return the value
     */
    public static int getIntLE(byte[] b, int off) {
        return (b[off] & 0xff) | (b[off + 1] & 0xff) << 8
                | (b[off + 2] & 0xff) << 16 | (b[off + 3] & 0xff) << 24;
    }

    /**
     * Writes a little-endian 32-bit integer.
     *
     * @param b the byte array
     * @param off the byte offset
     * @param v the value
     */
    public static void putIntLE(byte[] b, int off, int v) {
        b[off] = (byte) v;
        b[off + 1] = (byte) (v >>> 8);
        b[off + 2] = (byte) (v >>> 16);
        b[off + 3] = (byte) (v >>> 24);
    }

    /**
     * Reads a big-endian 64-bit integer.
     *
     * @param b the byte array
     * @param off the byte offset
     * @return the value
     */
    public static long getLong(byte[] b, int off) {
        return (long) getInt(b, off) << 32 | getInt(b, off + 4) & 0xffffffffL;
    }

    /**
     * Writes a big-endian 64-bit integer.
     *
     * @param b the byte array
     * @param off the byte offset
     * @param v the value
     */
    public static void putLong(byte[] b, int off, long v) {
        putInt(b, off, (int) (v >>> 32));
        putInt(b, off + 4, (int) v);
    }

    /**
     * Reads a little-endian 64-bit integer.
     *
     * @param b the byte array
     * @param off the byte offset
     * @return the value
     */
    public static long getLongLE(byte[] b, int off) {
        return getIntLE(b, off) & 0xffffffffL | (long) getIntLE(b, off + 4) << 32;
    }

    /**
     * Writes a little-endian 64-bit integer.
     *
     * @param b the byte array
     * @param off the byte offset
     * @param v the value
     */
    public static void putLongLE(byte[] b, int off, long v) {
        putIntLE(b, off, (int) v);
        putIntLE(b, off + 4, (int) (v >>> 32));
    }

    /**
     * Reads a big-endian 64-bit floating point value.
     *
     * @param b the byte array
     * @param off the byte offset
     * @return the value
     */
    public static double getDouble(byte[] b, int off) {
        return Double.longBitsToDouble(getLong(b, off));
    }

    /**
     * Writes a big-endian 64-bit floating point value.
     *
     * @param b the byte array
     * @param off the byte offset
     * @param v the value
     */
    public static void putDouble(byte[] b, int off, double v) {
        putLong(b, off, Double.doubleToRawLongBits(v));
    }

    /**
     * Reads a little-endian 64-bit floating point value.
     *
     * @param b the byte array
     * @param off the byte offset
     * @return the value
     */
    public static double getDoubleLE(byte[] b, int off) {
        return Double.longBitsToDouble(getLongLE(b, off));
    }

    /**
     * Converts UUID value to byte array in big-endian order.
     *
     * @param msb
     *            most significant part of UUID
     * @param lsb
     *            least significant part of UUID
     * @return byte array representation
     */
    public static byte[] uuidToBytes(long msb, long lsb) {
        byte[] buff = new byte[16];
        putLong(buff, 0, msb);
        putLong(buff, 8, lsb);
        return buff;
    }

    /**
     * Converts UUID value to byte array in big-endian order.
     *
     * @param uuid
     *            UUID value
     * @return byte array representation
     */
    public static byte[] uuidToBytes(UUID uuid) {
        return uuidToBytes(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
    }

    private Bits() {
    }

}
