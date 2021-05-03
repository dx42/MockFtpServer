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
import org.mockftpserver.core.command.AbstractCommandHandlerTestCase;
import org.mockftpserver.core.command.Command;
import org.mockftpserver.core.command.CommandNames;
import org.mockftpserver.core.command.ReplyCodes;
import org.mockftpserver.core.util.AssertFailedException;

/**
 * Tests for the SystCommandHandler class
 * 
 * @author Chris Mair
 */
class SystCommandHandlerTest extends AbstractCommandHandlerTestCase {

    private SystCommandHandler commandHandler;

    @Test
    void testHandleCommand() throws Exception {
        final String SYSTEM_NAME = "UNIX";
        commandHandler.setSystemName(SYSTEM_NAME);

        final Command COMMAND = new Command(CommandNames.SYST, EMPTY);

        commandHandler.handleCommand(COMMAND, session);

        verify(session).sendReply(ReplyCodes.SYST_OK, formattedReplyTextFor(ReplyCodes.SYST_OK, "\"" + SYSTEM_NAME + "\""));
        
        verifyNumberOfInvocations(commandHandler, 1);
        verifyNoDataElements(commandHandler.getInvocation(0));
    }
    
    @Test
    void testSetSystemName_Null() {
        assertThrows(AssertFailedException.class, () -> commandHandler.setSystemName(null));
    }
    
    @BeforeEach
    void setUp() throws Exception {
        commandHandler = new SystCommandHandler();
        commandHandler.setReplyTextBundle(replyTextBundle);
    }

}
