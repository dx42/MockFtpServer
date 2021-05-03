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
 * Tests for the AbstractStaticReplyCommandHandler class.
 *
 * @author Chris Mair
 */
class AbstractStaticReplyCommandHandlerTest extends AbstractTestCase {

    private static final int REPLY_CODE1 = 777;
    private static final int REPLY_CODE2 = 888;
    private static final String REPLY_TEXT1 = "reply1 ... abcdef";
    private static final String REPLY_TEXT2 = "abc {0} def";
    private static final String REPLY_TEXT2_FORMATTED = "abc 123 def";
    private static final String MESSAGE_KEY = "key.123";
    private static final String MESSAGE_TEXT = "message.123";
    private static final String OVERRIDE_REPLY_TEXT = "overridden reply ... abcdef";
    private static final Object ARG = "123";

    private AbstractStaticReplyCommandHandler commandHandler;
    private Session session;


    @Test
    void testSendReply() {
        commandHandler.setReplyCode(REPLY_CODE1);
        commandHandler.sendReply(session);
        verify(session).sendReply(REPLY_CODE1, REPLY_TEXT1);
    }

    @Test
    void testSendReply_SetReplyText() {
        commandHandler.setReplyCode(REPLY_CODE1);
        commandHandler.setReplyText(OVERRIDE_REPLY_TEXT);
        commandHandler.sendReply(session);
        verify(session).sendReply(REPLY_CODE1, OVERRIDE_REPLY_TEXT);
    }

    @Test
    void testSendReply_SetReplyMessageKey() {
        commandHandler.setReplyCode(REPLY_CODE1);
        commandHandler.setReplyMessageKey(Integer.toString(REPLY_CODE2));
        commandHandler.sendReply(session);

        verify(session).sendReply(REPLY_CODE1, REPLY_TEXT2);
    }

    @Test
    void testSendReply_ReplyCodeNotSet() {
        assertThrows(AssertFailedException.class, () -> commandHandler.sendReply(session));
    }

    @Test
    void testSendReply_MessageParameter() {
        commandHandler.setReplyCode(REPLY_CODE2);
        commandHandler.sendReply(session);
        commandHandler.sendReply(session, ARG);

        verify(session).sendReply(REPLY_CODE2, REPLY_TEXT2);
        verify(session).sendReply(REPLY_CODE2, REPLY_TEXT2_FORMATTED);
    }

    @Test
    void testSetReplyCode_Invalid() {
        assertThrows(AssertFailedException.class, () -> commandHandler.setReplyCode(0));
    }

    //-------------------------------------------------------------------------
    // Test setup
    //-------------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        session = mock(Session.class);
        commandHandler = new AbstractStaticReplyCommandHandler() {
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
