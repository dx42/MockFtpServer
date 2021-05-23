/*
 * Copyright 2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mockftpserver.test;

import org.mockftpserver.core.MockFtpServerException;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Abstract superclass for all project test classes
 *
 * @author Chris Mair
 */
public abstract class AbstractTestCase {

    protected static final String[] EMPTY = new String[0];
    protected static final InetAddress DEFAULT_HOST = inetAddress(null);

    //-------------------------------------------------------------------------
    // Helper Methods
    //-------------------------------------------------------------------------

    protected void log(Object message) {
        System.out.println("[" + getClass().getSimpleName() + "]: " + message);
    }

    /**
     * Create and return a one-element Object[] containing the specified Object
     *
     * @param o - the object
     * @return the Object array, of length 1, containing o
     */
    protected static Object[] objArray(Object o) {
        return new Object[]{o};
    }

    /**
     * Create and return a one-element String[] containing the specified String
     *
     * @param s - the String
     * @return the String array, of length 1, containing s
     */
    protected static String[] array(String s) {
        return new String[]{s};
    }

    /**
     * Create and return a two-element String[] containing the specified Strings
     *
     * @param s1 - the first String
     * @param s2 - the second String
     * @return the String array, of length 2, containing s1 and s2
     */
    protected static String[] array(String s1, String s2) {
        return new String[]{s1, s2};
    }

    /**
     * Create a new InetAddress from the specified host String, using the
     * {@link InetAddress#getByName(String)} method, wrapping any checked
     * exception within a unchecked MockFtpServerException.
     *
     * @param host
     * @return an InetAddress for the specified host
     * @throws MockFtpServerException - if an UnknownHostException is thrown
     */
    protected static InetAddress inetAddress(String host) {
        try {
            return InetAddress.getByName(host);
        }
        catch (UnknownHostException e) {
            throw new MockFtpServerException(e);
        }
    }

    /**
     * Create and return a List containing the single Object passed as an argument to this method
     *
     * @param element- the element to add
     * @return the List containing the specified element
     */
    protected static List list(Object element) {
        return Collections.singletonList(element);
    }

    /**
     * Create and return a Set containing the Objects passed as arguments to this method
     */
    protected static Set set(Object e1, Object... otherElements) {
        Set set = new HashSet();
        set.add(e1);
        for (Object element : otherElements) {
            set.add(element);
        }
        return set;
    }

}
