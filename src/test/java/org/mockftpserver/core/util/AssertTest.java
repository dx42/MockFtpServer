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
package org.mockftpserver.core.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.mockftpserver.test.AbstractTestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Tests for the Assert class
 *
 * @author Chris Mair
 */
class AssertTest extends AbstractTestCase {

    private static final Logger LOG = LoggerFactory.getLogger(AssertTest.class);
    private static final String MESSAGE = "exception message";

    @Test
    void testAssertNull() {
        Assert.isNull(null, MESSAGE);

        Throwable t = assertThrows(AssertFailedException.class, () -> Assert.isNull("OK", MESSAGE));
        assertExceptionMessageContains(t, MESSAGE);
    }

    @Test
    void testAssertNotNull() {
        Assert.notNull("OK", MESSAGE);

        Throwable t = assertThrows(AssertFailedException.class, () -> Assert.notNull(null, MESSAGE));
        assertExceptionMessageContains(t, MESSAGE);
    }

    @Test
    void testAssertTrue() throws Exception {
        Assert.isTrue(true, MESSAGE);

        Throwable t = assertThrows(AssertFailedException.class, () -> Assert.isTrue(false, MESSAGE));
        assertExceptionMessageContains(t, MESSAGE);
    }

    @Test
    void testAssertFalse() throws Exception {
        Assert.isFalse(false, MESSAGE);

        Throwable t = assertThrows(AssertFailedException.class, () -> Assert.isFalse(true, MESSAGE));
        assertExceptionMessageContains(t, MESSAGE);
    }

    @Test
    void testAssertNotNullOrEmpty_Collection() throws Exception {
        final Collection COLLECTION = Collections.singletonList("item");
        Assert.notNullOrEmpty(COLLECTION, MESSAGE);

        Throwable t = assertThrows(AssertFailedException.class, () -> Assert.notNullOrEmpty((Collection) null, MESSAGE));
        assertExceptionMessageContains(t, MESSAGE);

        t = assertThrows(AssertFailedException.class, () -> Assert.notNullOrEmpty(new ArrayList(), MESSAGE));
        assertExceptionMessageContains(t, MESSAGE);
    }

    @Test
    void testAssertNotNullOrEmpty_Map() throws Exception {
        final Map MAP = Collections.singletonMap("key", "value");
        Assert.notNullOrEmpty(MAP, MESSAGE);

        Throwable t = assertThrows(AssertFailedException.class, () -> Assert.notNullOrEmpty((Map) null, MESSAGE));
        assertExceptionMessageContains(t, MESSAGE);

        t = assertThrows(AssertFailedException.class, () -> Assert.notNullOrEmpty(new HashMap(), MESSAGE));
        assertExceptionMessageContains(t, MESSAGE);
    }

    @Test
    void testAssertNotNullOrEmpty_array() throws Exception {
        final Object[] ARRAY = {"1", "2"};
        Assert.notNullOrEmpty(ARRAY, MESSAGE);

        Throwable t = assertThrows(AssertFailedException.class, () -> Assert.notNullOrEmpty((Object[]) null, MESSAGE));
        assertExceptionMessageContains(t, MESSAGE);

        t = assertThrows(AssertFailedException.class, () -> Assert.notNullOrEmpty(new String[]{}, MESSAGE));
        assertExceptionMessageContains(t, MESSAGE);
    }

    @Test
    void testAssertNotNullOrEmpty_String() throws Exception {
        Assert.notNullOrEmpty("OK", MESSAGE);

        Throwable t = assertThrows(AssertFailedException.class, () -> Assert.notNullOrEmpty((String) null, MESSAGE));
        assertExceptionMessageContains(t, MESSAGE);

         t = assertThrows(AssertFailedException.class, () -> Assert.notNullOrEmpty("", MESSAGE));
        assertExceptionMessageContains(t, MESSAGE);
    }

    //-------------------------------------------------------------------------
    // Helper Methods
    //-------------------------------------------------------------------------

    private void assertExceptionMessageContains(Throwable exception, String text) {
        String message = exception.getMessage();
        assertTrue("Exception message [" + message + "] does not contain [" + text + "]", message.indexOf(text) != -1);
    }

}
