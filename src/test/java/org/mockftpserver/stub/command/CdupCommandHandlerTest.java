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
 * Tests for the CdupCommandHandler class
 * 
 * @author Chris Mair
 */
class CdupCommandHandlerTest extends AbstractCommandHandlerTestCase {

    private CdupCommandHandler commandHandler;
    private Command command1;
    private Command command2;
    
    @Test
    void testHandleCommand() throws Exception {
        commandHandler.handleCommand(command1, session);
        commandHandler.handleCommand(command2, session);

        verify(session, times(2)).sendReply(ReplyCodes.CDUP_OK, replyTextFor(ReplyCodes.CDUP_OK));

        verifyNumberOfInvocations(commandHandler, 2);
        verifyNoDataElements(commandHandler.getInvocation(0));
        verifyNoDataElements(commandHandler.getInvocation(1));
    }

    @BeforeEach
    void setUp() throws Exception {
        commandHandler = new CdupCommandHandler();
        commandHandler.setReplyTextBundle(replyTextBundle);
        command1 = new Command(CommandNames.CDUP, EMPTY);
        command2 = new Command(CommandNames.CDUP, EMPTY);
    }
}
