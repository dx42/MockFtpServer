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

import org.junit.jupiter.api.Assertions;
import org.mockftpserver.core.MockFtpServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Abstract superclass for all project test classes
 *
 * @author Chris Mair
 */
public abstract class AbstractTestCase {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractTestCase.class);
    protected static final String[] EMPTY = new String[0];
    protected static final InetAddress DEFAULT_HOST = inetAddress(null);

    //-------------------------------------------------------------------------
    // Other Helper Methods
    //-------------------------------------------------------------------------

    protected void fail(String message) {
        Assertions.fail(message);
    }

    protected void assertTrue(String message, boolean b) {
        Assertions.assertTrue(b, message);
    }

    protected void assertSame(String message, Object o1, Object o2) {
        Assertions.assertSame(o1, o2, message);
    }

    protected void assertSame(Object o1, Object o2) {
        Assertions.assertSame(o1, o2);
    }

    protected void assertNotSame(String message, Object o1, Object o2) {
        Assertions.assertNotSame(o1, o2, message);
    }

    protected void assertNull(String message, Object o) {
        Assertions.assertNull(o, message);
    }

    protected void assertNull(Object o) {
        Assertions.assertNull(o);
    }

    protected void assertNotNull(String message, Object o) {
        Assertions.assertNotNull(o, message);
    }

    protected void assertTrue(boolean b) {
        Assertions.assertTrue(b);
    }

    protected void assertFalse(String message, boolean b) {
        Assertions.assertFalse(b, message);
    }

    protected void assertFalse(boolean b) {
        Assertions.assertFalse(b);
    }

    protected void assertEquals(String message, Object object1, Object object2) {
        Assertions.assertEquals(object1, object2, message);
    }

    protected void assertEquals(Object object1, Object object2) {
        Assertions.assertEquals(object1, object2);
    }

    /**
     * Assert that the two objects are not equal
     *
     * @param object1 - the first object
     * @param object2 - the second object
     */
    protected void assertNotEquals(String message, Object object1, Object object2) {
        Assertions.assertFalse(object1.equals(object2), message);
    }

    /**
     * Assert that the two byte arrays have the same length and content
     *
     * @param array1 - the first array
     * @param array2 - the second array
     */
    protected void assertEquals(String message, byte[] array1, byte[] array2) {
        Assertions.assertTrue(Arrays.equals(array1, array2), "Arrays not equal: " + message);
    }

    /**
     * Assert that the two Object arrays have the same length and content
     *
     * @param array1 - the first array
     * @param array2 - the second array
     */
    protected void assertEquals(String message, Object[] array1, Object[] array2) {
        Assertions.assertTrue(Arrays.equals(array1, array2), "Arrays not equal: " + message);
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
     * Create and return a List containing the Objects passed as arguments to this method
     *
     * @param e1- the first element to add
     * @param e2- the second element to add
     * @return the List containing the specified elements
     */
    protected static List list(Object e1, Object e2) {
        List list = new ArrayList();
        list.add(e1);
        list.add(e2);
        return list;
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
     *
     * @param e1 - the first element to add
     * @param e2 - the second element to add
     * @return the Set containing the specified elements
     */
    protected static Set set(Object e1, Object e2) {
        Set set = new HashSet();
        set.add(e1);
        set.add(e2);
        return set;
    }

    /**
     * Create and return a Set containing the Objects passed as arguments to this method
     *
     * @param e1 - the first element to add
     * @param e2 - the second element to add
     * @param e3 - the third element to add
     * @return the Set containing the specified elements
     */
    protected static Set set(Object e1, Object e2, Object e3) {
        Set set = set(e1, e2);
        set.add(e3);
        return set;
    }

    //-------------------------------------------------------------------------
    // Helper methods
    //-------------------------------------------------------------------------

    /**
     * Delete the named file if it exists
     *
     * @param filename - the full pathname of the file
     */
    protected void deleteFile(String filename) {
        File keyFile = new File(filename);
        boolean deleted = keyFile.delete();
        LOG.info("Deleted [" + filename + "]: " + deleted);
    }

    //-------------------------------------------------------------------------
    // Common validation helper methods
    //-------------------------------------------------------------------------

    /**
     * Verify that the named file exists
     *
     * @param filename - the full pathname of the file
     */
    protected void verifyFileExists(String filename) {
        File keyFile = new File(filename);
        Assertions.assertTrue(keyFile.exists(), "File does not exist [" + filename + "]");
    }

}
