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

/**
 * Tests for the MkdCommandHandler class
 * 
 * @author Chris Mair
 */
class MkdCommandHandlerTest extends AbstractCommandHandlerTestCase {

    private MkdCommandHandler commandHandler;
    private Command command1;
    private Command command2;

    @Test
    void testHandleCommand() throws Exception {
        commandHandler.handleCommand(command1, session);
        commandHandler.handleCommand(command2, session);

        verify(session).sendReply(ReplyCodes.MKD_OK, formattedReplyTextFor(ReplyCodes.MKD_OK, DIR1));

        verifyNumberOfInvocations(commandHandler, 2);
        verifyOneDataElement(commandHandler.getInvocation(0), MkdCommandHandler.PATHNAME_KEY, DIR1);
        verifyOneDataElement(commandHandler.getInvocation(1), MkdCommandHandler.PATHNAME_KEY, DIR2);
    }

    @Test
    void testHandleCommand_MissingPathnameParameter() throws Exception {
        testHandleCommand_InvalidParameters(commandHandler, CommandNames.MKD, EMPTY);
    }

    @BeforeEach
    void setUp() throws Exception {
        commandHandler = new MkdCommandHandler();
        commandHandler.setReplyTextBundle(replyTextBundle);
        commandHandler.setReplyTextBundle(replyTextBundle);
        command1 = new Command(CommandNames.MKD, array(DIR1));
        command2 = new Command(CommandNames.MKD, array(DIR2));
    }

}
