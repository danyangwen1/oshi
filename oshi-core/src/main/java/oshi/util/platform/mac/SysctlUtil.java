/*
 * MIT License
 *
 * Copyright (c) 2016-2022 The OSHI Project Contributors: https://github.com/oshi/oshi/graphs/contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package oshi.util.platform.mac;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.unix.LibCAPI.size_t;

import oshi.annotation.concurrent.ThreadSafe;
import oshi.jna.platform.mac.SystemB;

/**
 * Provides access to sysctl calls on macOS
 */
@ThreadSafe
public final class SysctlUtil {

    private static final Logger LOG = LoggerFactory.getLogger(SysctlUtil.class);

    private static final String SYSCTL_FAIL = "Failed sysctl call: {}, Error code: {}";

    private SysctlUtil() {
    }

    /**
     * Executes a sysctl call with an int result
     *
     * @param name
     *            name of the sysctl
     * @param def
     *            default int value
     * @return The int result of the call if successful; the default otherwise
     */
    public static int sysctl(String name, int def) {
        size_t.ByReference size = new size_t.ByReference(com.sun.jna.platform.mac.SystemB.INT_SIZE);
        Pointer p = new Memory(size.longValue());
        if (0 != SystemB.INSTANCE.sysctlbyname(name, p, size, null, size_t.ZERO)) {
            LOG.warn(SYSCTL_FAIL, name, Native.getLastError());
            return def;
        }
        return p.getInt(0);
    }

    /**
     * Executes a sysctl call with a long result
     *
     * @param name
     *            name of the sysctl
     * @param def
     *            default long value
     * @return The long result of the call if successful; the default otherwise
     */
    public static long sysctl(String name, long def) {
        size_t.ByReference size = new size_t.ByReference(com.sun.jna.platform.mac.SystemB.UINT64_SIZE);
        Pointer p = new Memory(size.longValue());
        if (0 != SystemB.INSTANCE.sysctlbyname(name, p, size, null, size_t.ZERO)) {
            LOG.error(SYSCTL_FAIL, name, Native.getLastError());
            return def;
        }
        return p.getLong(0);
    }

    /**
     * Executes a sysctl call with a String result
     *
     * @param name
     *            name of the sysctl
     * @param def
     *            default String value
     * @return The String result of the call if successful; the default otherwise
     */
    public static String sysctl(String name, String def) {
        // Call first time with null pointer to get value of size
        size_t.ByReference size = new size_t.ByReference();
        if (0 != SystemB.INSTANCE.sysctlbyname(name, null, size, null, size_t.ZERO)) {
            LOG.error(SYSCTL_FAIL, name, Native.getLastError());
            return def;
        }
        // Add 1 to size for null terminated string
        Pointer p = new Memory(size.longValue() + 1L);
        if (0 != SystemB.INSTANCE.sysctlbyname(name, p, size, null, size_t.ZERO)) {
            LOG.error(SYSCTL_FAIL, name, Native.getLastError());
            return def;
        }
        return p.getString(0);
    }

    /**
     * Executes a sysctl call with a Structure result
     *
     * @param name
     *            name of the sysctl
     * @param struct
     *            structure for the result
     * @return True if structure is successfuly populated, false otherwise
     */
    public static boolean sysctl(String name, Structure struct) {
        if (0 != SystemB.INSTANCE.sysctlbyname(name, struct.getPointer(), new size_t.ByReference(struct.size()), null,
                size_t.ZERO)) {
            LOG.error(SYSCTL_FAIL, name, Native.getLastError());
            return false;
        }
        struct.read();
        return true;
    }

    /**
     * Executes a sysctl call with a Pointer result
     *
     * @param name
     *            name of the sysctl
     * @return An allocated memory buffer containing the result on success, null
     *         otherwise. Its value on failure is undefined.
     */
    public static Memory sysctl(String name) {
        size_t.ByReference size = new size_t.ByReference();
        if (0 != SystemB.INSTANCE.sysctlbyname(name, null, size, null, size_t.ZERO)) {
            LOG.error(SYSCTL_FAIL, name, Native.getLastError());
            return null;
        }
        Memory m = new Memory(size.longValue());
        if (0 != SystemB.INSTANCE.sysctlbyname(name, m, size, null, size_t.ZERO)) {
            LOG.error(SYSCTL_FAIL, name, Native.getLastError());
            return null;
        }
        return m;
    }
}
