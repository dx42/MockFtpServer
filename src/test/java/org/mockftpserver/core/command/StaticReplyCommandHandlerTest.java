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

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockftpserver.core.util.AssertFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the StaticReplyCommandHandler class
 * 
 * @author Chris Mair
 */
class StaticReplyCommandHandlerTest extends AbstractCommandHandlerTestCase {

    private static final Logger LOG = LoggerFactory.getLogger(StaticReplyCommandHandlerTest.class);
    private static final int REPLY_CODE = 999;
    private static final String REPLY_TEXT = "some text 123";
    private static final Command COMMAND = new Command("ANY", EMPTY);
    
    private StaticReplyCommandHandler commandHandler;
    
    @Test
    void testConstructor_String_InvalidReplyCode() {
        try {
            new StaticReplyCommandHandler(-1);
            fail("Expected AssertFailedException");
        }
        catch (AssertFailedException expected) {
            LOG.info("Expected: " + expected);
        }
    }

    @Test
    void testConstructor_StringString_InvalidReplyCode() {
        try {
            new StaticReplyCommandHandler(-99, "text");
            fail("Expected AssertFailedException");
        }
        catch (AssertFailedException expected) {
            LOG.info("Expected: " + expected);
        }
    }
    
    @Test
    void testSetReplyCode_Invalid() {
        try {
            commandHandler.setReplyCode(-1);
            fail("Expected AssertFailedException");
        }
        catch (AssertFailedException expected) {
            LOG.info("Expected: " + expected);
        }
    }
    
    @Test
    void testHandleCommand_ReplyTextNotSet() throws Exception {
        commandHandler.setReplyCode(250);

        commandHandler.handleCommand(COMMAND, session);

        verify(session).sendReply(250, replyTextFor(250));
        verifyNumberOfInvocations(commandHandler, 1);
        verifyNoDataElements(commandHandler.getInvocation(0));
    }
    
    @Test
    void testHandleCommand_SetReplyText() throws Exception {
        commandHandler.setReplyCode(REPLY_CODE);
        commandHandler.setReplyText(REPLY_TEXT);

        commandHandler.handleCommand(COMMAND, session);

        verify(session).sendReply(REPLY_CODE, REPLY_TEXT);
        verifyNumberOfInvocations(commandHandler, 1);
        verifyNoDataElements(commandHandler.getInvocation(0));
    }
    
    @Test
    void testHandleCommand_ReplyCodeNotSet() throws Exception {
        try {
            commandHandler.handleCommand(COMMAND, session);
            fail("Expected AssertFailedException");
        }
        catch (AssertFailedException expected) {
            LOG.info("Expected: " + expected);
        }
        
        verifyNumberOfInvocations(commandHandler, 1);
        verifyNoDataElements(commandHandler.getInvocation(0));
    }
    
    @BeforeEach
    void setUp() throws Exception {
        commandHandler = new StaticReplyCommandHandler();
        commandHandler.setReplyTextBundle(replyTextBundle);
    }
    
}
