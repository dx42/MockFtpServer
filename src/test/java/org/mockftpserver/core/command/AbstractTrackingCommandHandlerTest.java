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
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockftpserver.core.session.Session;
import org.mockftpserver.core.util.AssertFailedException;
import org.mockftpserver.test.AbstractTestCase;

import java.util.ListResourceBundle;
import java.util.ResourceBundle;

/**
 * Tests for the AbstractTrackingCommandHandler class.
 *
 * @author Chris Mair
 */
class AbstractTrackingCommandHandlerTest extends AbstractTestCase {

    private static final String COMMAND_NAME = "abc";
    private static final Object ARG = "123";
    private static final Object[] ARGS = {ARG};
    private static final Command COMMAND = new Command(COMMAND_NAME, EMPTY);
    private static final Command COMMAND_WITH_ARGS = new Command(COMMAND_NAME, EMPTY);
    private static final int REPLY_CODE1 = 777;
    private static final int REPLY_CODE2 = 888;
    private static final int REPLY_CODE3 = 999;
    private static final String REPLY_TEXT1 = "reply1 ... abcdef";
    private static final String REPLY_TEXT2 = "abc {0} def";
    private static final String REPLY_TEXT2_FORMATTED = "abc 123 def";
    private static final String OVERRIDE_REPLY_TEXT = "overridden reply ... abcdef";
    private static final String MESSAGE_KEY = "key.123";
    private static final String MESSAGE_TEXT = "message.123";

    private AbstractTrackingCommandHandler commandHandler;
    private Session session;

    @Test
    void testHandleCommand() throws Exception {
        assertEquals("before", 0, commandHandler.numberOfInvocations());
        commandHandler.handleCommand(COMMAND, session);
        assertEquals("after", 1, commandHandler.numberOfInvocations());
        assertTrue("locked", commandHandler.getInvocation(0).isLocked());
    }

    @Test
    void testHandleCommand_NullCommand() {
        assertThrows(AssertFailedException.class, () -> commandHandler.handleCommand(null, session));
    }

    @Test
    void testHandleCommand_NullSession() {
        assertThrows(AssertFailedException.class, () -> commandHandler.handleCommand(COMMAND, null));
    }

    @Test
    void testInvocationHistory() throws Exception {
        assertEquals("none", 0, commandHandler.numberOfInvocations());
        commandHandler.handleCommand(COMMAND, session);
        assertEquals("1", 1, commandHandler.numberOfInvocations());
        commandHandler.handleCommand(COMMAND, session);
        assertEquals("2", 2, commandHandler.numberOfInvocations());
        commandHandler.clearInvocations();
        assertEquals("cleared", 0, commandHandler.numberOfInvocations());
    }

    @Test
    void testGetInvocation() throws Exception {
        commandHandler.handleCommand(COMMAND, session);
        commandHandler.handleCommand(COMMAND_WITH_ARGS, session);
        assertSame("1", COMMAND, commandHandler.getInvocation(0).getCommand());
        assertSame("2", COMMAND_WITH_ARGS, commandHandler.getInvocation(1).getCommand());
    }

    @Test
    void testGetInvocation_IndexOutOfBounds() throws Exception {
        commandHandler.handleCommand(COMMAND, session);
        assertThrows(IndexOutOfBoundsException.class, () -> commandHandler.getInvocation(2));
    }

    @Test
    void testSendReply() {
        commandHandler.sendReply(session, REPLY_CODE1, null, null, null);
        commandHandler.sendReply(session, REPLY_CODE1, MESSAGE_KEY, null, null);
        commandHandler.sendReply(session, REPLY_CODE1, MESSAGE_KEY, OVERRIDE_REPLY_TEXT, null);
        commandHandler.sendReply(session, REPLY_CODE3, null, null, null);

        verify(session).sendReply(REPLY_CODE1, REPLY_TEXT1);
        verify(session).sendReply(REPLY_CODE1, MESSAGE_TEXT);
        verify(session).sendReply(REPLY_CODE1, OVERRIDE_REPLY_TEXT);
        verify(session).sendReply(REPLY_CODE3, null);
    }

    @Test
    void testSendReply_WithMessageArguments() {
        commandHandler.sendReply(session, REPLY_CODE1, null, REPLY_TEXT2, ARGS);

        verify(session).sendReply(REPLY_CODE1, REPLY_TEXT2_FORMATTED);
    }

    @Test
    void testSendReply_NullSession() {
        assertThrows(AssertFailedException.class, () -> commandHandler.sendReply(null, REPLY_CODE1, REPLY_TEXT1, null, null));
    }

    @Test
    void testSendReply_InvalidReplyCode() {
        assertThrows(AssertFailedException.class, () -> commandHandler.sendReply(session, 0, REPLY_TEXT1, null, null));
    }

    //-------------------------------------------------------------------------
    // Test setup
    //-------------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        session = mock(Session.class);
        commandHandler = new AbstractTrackingCommandHandler() {
            public void handleCommand(Command command, Session session, InvocationRecord invocationRecord) {
            }
        };
        ResourceBundle replyTextBundle = new ListResourceBundle() {
            protected Object[][] getContents() {
                return new Object[][]{
                        {Integer.toString(REPLY_CODE1), REPLY_TEXT1},
                        {Integer.toString(REPLY_CODE2), REPLY_TEXT2},
                        {MESSAGE_KEY, MESSAGE_TEXT}
                };
            }
        };
        commandHandler.setReplyTextBundle(replyTextBundle);
    }

}
