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

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockftpserver.core.command.AbstractCommandHandlerTestCase;
import org.mockftpserver.core.command.Command;
import org.mockftpserver.core.command.CommandNames;
import org.mockftpserver.core.command.ReplyCodes;
import org.mockftpserver.core.util.AssertFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the AlloCommandHandler class
 * 
 * @author Chris Mair
 */
class AlloCommandHandlerTest extends AbstractCommandHandlerTestCase {

    private static final Logger LOG = LoggerFactory.getLogger(AlloCommandHandlerTest.class);
    private static final int BYTES1 = 64;
    private static final int BYTES2 = 555;
    private static final int RECORD_SIZE = 77;

    private AlloCommandHandler commandHandler;
    private Command command1;
    private Command command2;

    @Test
    void testHandleCommand() throws Exception {
        commandHandler.handleCommand(command1, session);
        commandHandler.handleCommand(command2, session);

        verify(session, times(2)).sendReply(ReplyCodes.ALLO_OK, replyTextFor(ReplyCodes.ALLO_OK));

        verifyNumberOfInvocations(commandHandler, 2);
        verifyOneDataElement(commandHandler.getInvocation(0), AlloCommandHandler.NUMBER_OF_BYTES_KEY, new Integer(
                BYTES1));
        verifyTwoDataElements(commandHandler.getInvocation(1), AlloCommandHandler.NUMBER_OF_BYTES_KEY, new Integer(
                BYTES2), AlloCommandHandler.RECORD_SIZE_KEY, new Integer(RECORD_SIZE));
    }

    @Test
    void testHandleCommand_MissingNumberOfBytesParameter() throws Exception {
        testHandleCommand_InvalidParameters(commandHandler, CommandNames.ALLO, EMPTY);
    }

    @Test
    void testHandleCommand_RecordSizeDelimiterWithoutValue() throws Exception {
        try {
            commandHandler.handleCommand(new Command(CommandNames.ALLO, array("123 R ")), session);
            fail("Expected AssertFailedException");
        }
        catch (AssertFailedException expected) {
            LOG.info("Expected: " + expected);
        }
    }

    @Test
    void testHandleCommand_InvalidNumberOfBytesParameter() throws Exception {
        try {
            commandHandler.handleCommand(new Command(CommandNames.ALLO, array("xx")), session);
            fail("Expected NumberFormatException");
        }
        catch (NumberFormatException expected) {
            LOG.info("Expected: " + expected);
        }
    }

    @Test
    void testHandleCommand_InvalidRecordSizeParameter() throws Exception {
        try {
            commandHandler.handleCommand(new Command(CommandNames.ALLO, array("123 R xx")), session);
            fail("Expected NumberFormatException");
        }
        catch (NumberFormatException expected) {
            LOG.info("Expected: " + expected);
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        commandHandler = new AlloCommandHandler();
        commandHandler.setReplyTextBundle(replyTextBundle);
        command1 = new Command(CommandNames.ALLO, array(Integer.toString(BYTES1)));
        command2 = new Command(CommandNames.ALLO, array(Integer.toString(BYTES2) + " R " + Integer.toString(RECORD_SIZE)));
    }

}
