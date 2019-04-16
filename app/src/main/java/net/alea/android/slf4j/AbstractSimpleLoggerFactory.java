/*
 * Created 21.10.2009
 *
 * Copyright (c) 2009-2012 SLF4J.ORG
 *
 * All rights reserved.
 *
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 *
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 *
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package net.alea.android.slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;



public abstract class AbstractSimpleLoggerFactory<T extends Logger> implements ILoggerFactory
{
    private final ConcurrentMap<String, T> loggerMap;

    static final int TAG_MAX_LENGTH = 23; // tag names cannot be longer on Android platform
    // see also android/system/core/include/cutils/property.h
    // and android/frameworks/base/core/jni/android_util_Log.cpp

    public AbstractSimpleLoggerFactory()
    {
        loggerMap = new ConcurrentHashMap<>();
    }

    public abstract T generateLogger(String name);

    /* @see org.slf4j.ILoggerFactory#getLogger(java.lang.String) */
    public T getLogger(final String name)
    {
        final String tag = forceValidName(name); // fix for bug #173

        T logger = loggerMap.get(tag);
        if (logger != null) return logger;

        logger = generateLogger(tag);
        T loggerPutBefore = loggerMap.putIfAbsent(tag, logger);
        if (null == loggerPutBefore) {
            return logger;
        }
        return loggerPutBefore;
    }

    /**
     * Trim name in case it exceeds maximum length of {@value #TAG_MAX_LENGTH} characters.
     */
    private String forceValidName(String name)
    {
        name = name.substring(name.lastIndexOf(".")+1);
        if (name.length() > TAG_MAX_LENGTH) {
            name = name.substring(0, TAG_MAX_LENGTH - 1) + '*';
        }
        return name;
    }
}
