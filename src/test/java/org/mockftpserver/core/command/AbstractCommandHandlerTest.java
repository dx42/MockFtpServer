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
import org.mockftpserver.core.session.Session;
import org.mockftpserver.core.util.AssertFailedException;
import org.mockftpserver.stub.command.AbstractStubCommandHandler;
import org.mockftpserver.test.AbstractTestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ListResourceBundle;
import java.util.ResourceBundle;

/**
 * Tests for the AbstractCommandHandler class.
 *
 * @author Chris Mair
 */
class AbstractCommandHandlerTest extends AbstractTestCase {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractTrackingCommandHandlerTest.class);
    private static final int REPLY_CODE1 = 777;
    private static final int REPLY_CODE2 = 888;
    private static final String REPLY_TEXT1 = "reply1 ... abcdef";
    private static final String REPLY_TEXT2 = "abc {0} def";
    private static final String MESSAGE_KEY = "key.123";
    private static final String MESSAGE_TEXT = "message.123";

    private AbstractCommandHandler commandHandler;

    @Test
    void testQuotes() {
        assertEquals("abc", "\"abc\"", AbstractStubCommandHandler.quotes("abc"));
        assertEquals("<empty>", "\"\"", AbstractStubCommandHandler.quotes(""));
    }

    @Test
    void testQuotes_Null() {
        assertThrows(AssertFailedException.class, () -> AbstractStubCommandHandler.quotes(null));
    }

    @Test
    void testAssertValidReplyCode() {
        // These are valid, so expect no exceptions
        commandHandler.assertValidReplyCode(1);
        commandHandler.assertValidReplyCode(100);

        // These are invalid
        testAssertValidReplyCodeWithInvalid(0);
        testAssertValidReplyCodeWithInvalid(-1);
    }

    private void testAssertValidReplyCodeWithInvalid(int invalidReplyCode) {
        assertThrows(AssertFailedException.class, () -> commandHandler.assertValidReplyCode(invalidReplyCode));
    }

    //-------------------------------------------------------------------------
    // Test setup
    //-------------------------------------------------------------------------

    @BeforeEach
    void setUp() throws Exception {
        commandHandler = new AbstractCommandHandler() {
            public void handleCommand(Command command, Session session) throws Exception {
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
