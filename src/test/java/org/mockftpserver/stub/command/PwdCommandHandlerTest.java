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
 * Tests for the PwdCommandHandler class
 * 
 * @author Chris Mair
 */
class PwdCommandHandlerTest extends AbstractCommandHandlerTestCase {

    private PwdCommandHandler commandHandler;

    @Test
    void testHandleCommand() throws Exception {

        final String RESPONSE_DATA = "current dir 1";
        commandHandler.setDirectory(RESPONSE_DATA);

        final Command COMMAND = new Command(CommandNames.PWD, EMPTY);

        commandHandler.handleCommand(COMMAND, session);

        verify(session).sendReply(ReplyCodes.PWD_OK, formattedReplyTextFor(ReplyCodes.PWD_OK, "\"" + RESPONSE_DATA + "\""));
        
        verifyNumberOfInvocations(commandHandler, 1);
        verifyNoDataElements(commandHandler.getInvocation(0));
    }

    @BeforeEach
    void setUp() throws Exception {
        commandHandler = new PwdCommandHandler();
        commandHandler.setReplyTextBundle(replyTextBundle);
    }

}
