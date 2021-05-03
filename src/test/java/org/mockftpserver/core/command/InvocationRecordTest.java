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
package org.mockftpserver.core.command;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockftpserver.core.util.AssertFailedException;
import org.mockftpserver.test.AbstractTestCase;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Tests for InvocationRecord
 * 
 * @author Chris Mair
 */
class InvocationRecordTest extends AbstractTestCase {

    private static final Command COMMAND = new Command("command", EMPTY);
    private static final String KEY1 = "key1";
    private static final String KEY2 = "key2";
    private static final String STRING = "abc123";
    private static final Integer INT = new Integer(77);

    private InvocationRecord invocationRecord;

    @Test
    void testConstructor() {
        final Command COMMAND = new Command("ABC", EMPTY);
        long beforeTime = System.currentTimeMillis();
        InvocationRecord commandInvocation = new InvocationRecord(COMMAND, DEFAULT_HOST);
        long afterTime = System.currentTimeMillis();
        log(commandInvocation.toString());
        assertEquals("Command", COMMAND, commandInvocation.getCommand());
        assertTrue("time", commandInvocation.getTime().getTime() >= beforeTime
                && commandInvocation.getTime().getTime() <= afterTime);
        assertEquals("host", DEFAULT_HOST, commandInvocation.getClientHost());
    }

    @Test
    void testSet_NullKey() {
        assertThrows(AssertFailedException.class, () -> invocationRecord.set(null, STRING));
    }

    @Test
    void testSet_NullValue() {
        invocationRecord.set(KEY1, null);
        assertNull(KEY1, invocationRecord.getObject(KEY1));
    }

    @Test
    void testContainsKey() {
        invocationRecord.set(KEY1, STRING);
        assertTrue(KEY1, invocationRecord.containsKey(KEY1));
        assertFalse(KEY2, invocationRecord.containsKey(KEY2));
    }

    @Test
    void testContainsKey_Null() {
        assertThrows(AssertFailedException.class, () -> invocationRecord.containsKey(null));
    }

    @Test
    void testGetString() {
        assertNull("undefined", invocationRecord.getString("UNDEFINED"));
        invocationRecord.set(KEY1, STRING);
        assertEquals(KEY1, STRING, invocationRecord.getString(KEY1));
    }

    @Test
    void testGetString_Null() {
        assertThrows(AssertFailedException.class, () -> invocationRecord.getString(null));
    }

    @Test
    void testGetString_NotAString() {
        invocationRecord.set(KEY1, INT);
        assertThrows(ClassCastException.class, () -> invocationRecord.getString(KEY1));
    }

    @Test
    void testGetObject() {
        assertNull("undefined", invocationRecord.getObject("UNDEFINED"));
        invocationRecord.set(KEY1, STRING);
        assertEquals(KEY1, STRING, invocationRecord.getObject(KEY1));
    }

    @Test
    void testKeySet() {
        Set set = new HashSet();
        assertEquals("empty", set, invocationRecord.keySet());
        invocationRecord.set(KEY1, STRING);
        invocationRecord.set(KEY2, STRING);
        set.add(KEY1);
        set.add(KEY2);
        assertEquals("2", set, invocationRecord.keySet());
    }

    @Test
    void testKeySet_Immutability() {
        Set keySet = invocationRecord.keySet();
        assertThrows(UnsupportedOperationException.class, () -> keySet.add("abc"));
    }

    @Test
    void testGetObject_Null() {
        assertThrows(AssertFailedException.class, () -> invocationRecord.getObject(null));
    }

    @Test
    void testLock() {
        assertFalse("locked - before", invocationRecord.isLocked());
        invocationRecord.set(KEY1, STRING);
        invocationRecord.lock();
        assertTrue("locked - after", invocationRecord.isLocked());
        assertThrows(AssertFailedException.class, () -> invocationRecord.set(KEY2, "abc"));
    }

    @Test
    void testGetTime_Immutability() {
        Date timestamp = invocationRecord.getTime();
        long timeInMillis = timestamp.getTime();
        timestamp.setTime(12345L);
        assertEquals("time", timeInMillis, invocationRecord.getTime().getTime());
    }

    @BeforeEach
    void setUp() {
        invocationRecord = new InvocationRecord(COMMAND, DEFAULT_HOST);
    }
}
