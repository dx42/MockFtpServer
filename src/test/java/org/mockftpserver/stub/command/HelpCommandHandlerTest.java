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
 * Tests for the HelpCommandHandler class
 * 
 * @author Chris Mair
 */
class HelpCommandHandlerTest extends AbstractCommandHandlerTestCase {

    private HelpCommandHandler commandHandler;

    @Test
    void testHandleCommand() throws Exception {

        final String RESPONSE_DATA = "help for ABC...";
        commandHandler.setHelpMessage(RESPONSE_DATA);

        final Command COMMAND1 = new Command(CommandNames.HELP, EMPTY);
        final Command COMMAND2 = new Command(CommandNames.HELP, array("abc"));

        commandHandler.handleCommand(COMMAND1, session);
        commandHandler.handleCommand(COMMAND2, session);

        verify(session, times(2)).sendReply(ReplyCodes.HELP_OK, formattedReplyTextFor(ReplyCodes.HELP_OK, RESPONSE_DATA));
        
        verifyNumberOfInvocations(commandHandler, 2);
        verifyOneDataElement(commandHandler.getInvocation(0), HelpCommandHandler.COMMAND_NAME_KEY, null);
        verifyOneDataElement(commandHandler.getInvocation(1), HelpCommandHandler.COMMAND_NAME_KEY, "abc");
    }

    @BeforeEach
    void setUp() throws Exception {
        commandHandler = new HelpCommandHandler();
        commandHandler.setReplyTextBundle(replyTextBundle);
    }

}
