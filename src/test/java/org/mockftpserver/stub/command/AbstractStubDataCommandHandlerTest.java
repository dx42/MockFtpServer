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
package org.mockftpserver.stub.command;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockftpserver.core.command.Command;
import org.mockftpserver.core.command.InvocationRecord;
import org.mockftpserver.core.session.Session;
import org.mockftpserver.core.util.AssertFailedException;
import org.mockftpserver.test.AbstractTestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ListResourceBundle;
import java.util.ResourceBundle;

/**
 * Tests for AbstractStubDataCommandHandler.
 * 
 * @author Chris Mair
 */
class AbstractStubDataCommandHandlerTest extends AbstractTestCase {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractStubDataCommandHandlerTest.class);
    private static final Command COMMAND = new Command("command", EMPTY);
    private static final InvocationRecord INVOCATION_RECORD = new InvocationRecord(COMMAND, DEFAULT_HOST);

    private static final String REPLY_TEXT150 = "reply 150 ... abcdef";
    private static final String REPLY_TEXT226 = "reply 226 ... abcdef";
    private static final String REPLY_TEXT222 = "reply 222 ... abcdef";
    private static final String REPLY_TEXT333 = "reply 333 ... abcdef";
    private static final String REPLY_TEXT444 = "reply 444 ... abcdef";
    
    private Session session;
    private ResourceBundle replyTextBundle;
    private AbstractStubDataCommandHandler commandHandler;

    @Test
    void testHandleCommand() throws Exception {
        // Define CommandHandler test subclass
        commandHandler = new AbstractStubDataCommandHandler() {
            protected void beforeProcessData(Command c, Session s, InvocationRecord ir) {
                verifyParameters(c, s, ir);
                // Send unique reply code so that we can verify proper method invocation and ordering
                session.sendReply(222, REPLY_TEXT222);
            }

            protected void processData(Command c, Session s, InvocationRecord ir) {
                verifyParameters(c, s, ir);
                // Send unique reply code so that we can verify proper method invocation and ordering
                session.sendReply(333, REPLY_TEXT333);
            }

            protected void afterProcessData(Command c, Session s, InvocationRecord ir) {
                verifyParameters(c, s, ir);
                // Send unique reply code so that we can verify proper method invocation and ordering
                session.sendReply(444, REPLY_TEXT444);
            }

            private void verifyParameters(Command c, Session s, InvocationRecord ir) {
                assertSame("command", COMMAND, c);
                assertSame("session", session, s);
                assertSame("invocationRecord", INVOCATION_RECORD, ir);
            }
        };

        commandHandler.setReplyTextBundle(replyTextBundle);
        commandHandler.handleCommand(COMMAND, session, INVOCATION_RECORD);

        verify(session).sendReply(150, REPLY_TEXT150);
        verify(session).openDataConnection();
        verify(session).sendReply(222, REPLY_TEXT222);
        verify(session).sendReply(333, REPLY_TEXT333);
        verify(session).sendReply(444, REPLY_TEXT444);
        verify(session).closeDataConnection();
        verify(session).sendReply(226, REPLY_TEXT226);
    }

    @Test
    void testHandleCommand_OverrideInitialReplyCodeAndText() throws Exception {
        final int OVERRIDE_REPLY_CODE = 333;
        final String OVERRIDE_REPLY_TEXT = "reply text";

        commandHandler.setPreliminaryReplyCode(OVERRIDE_REPLY_CODE);
        commandHandler.setPreliminaryReplyText(OVERRIDE_REPLY_TEXT);
        commandHandler.setReplyTextBundle(replyTextBundle);
        commandHandler.handleCommand(COMMAND, session, INVOCATION_RECORD);

        verify(session).sendReply(OVERRIDE_REPLY_CODE, OVERRIDE_REPLY_TEXT);
        verify(session).openDataConnection();
        verify(session).closeDataConnection();
        verify(session).sendReply(226, REPLY_TEXT226);
    }

    @Test
    void testSetPreliminaryReplyCode_Invalid() {
        assertThrows(AssertFailedException.class, () -> commandHandler.setPreliminaryReplyCode(0));
    }

    @Test
    void testSetFinalReplyCode_Invalid() {
        assertThrows(AssertFailedException.class, () -> commandHandler.setFinalReplyCode(0));
    }

    //-------------------------------------------------------------------------
    // Test setup
    //-------------------------------------------------------------------------

    @BeforeEach
    void setUp() throws Exception {
        session = (Session) mock(Session.class);
        replyTextBundle = new ListResourceBundle() {
            protected Object[][] getContents() {
                return new Object[][] { 
                        { Integer.toString(150), REPLY_TEXT150 }, 
                        { Integer.toString(222), REPLY_TEXT222 }, 
                        { Integer.toString(226), REPLY_TEXT226 }, 
                        { Integer.toString(333), REPLY_TEXT333 }, 
                        { Integer.toString(444), REPLY_TEXT444 }, 
                };
            }
        };
        commandHandler = new AbstractStubDataCommandHandler() {
            protected void processData(Command c, Session s, InvocationRecord ir) {
            }
        };
    }
    
}
