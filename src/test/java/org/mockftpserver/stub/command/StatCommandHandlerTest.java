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
 * Tests for the StatCommandHandler class
 * 
 * @author Chris Mair
 */
class StatCommandHandlerTest extends AbstractCommandHandlerTestCase {

    private static final String RESPONSE_DATA = "status info 123.456";
    private static final String PATHNAME = "dir/file";

    private StatCommandHandler commandHandler;

    @Test
    void testHandleCommand_NoPathname() throws Exception {
        final Command COMMAND = new Command(CommandNames.STAT, EMPTY);
        commandHandler.setStatus(RESPONSE_DATA);
        commandHandler.handleCommand(COMMAND, session);

        verify(session).sendReply(ReplyCodes.STAT_SYSTEM_OK, formattedReplyTextFor(ReplyCodes.STAT_SYSTEM_OK, RESPONSE_DATA));
        
        verifyNumberOfInvocations(commandHandler, 1);
        verifyOneDataElement(commandHandler.getInvocation(0), StatCommandHandler.PATHNAME_KEY, null);
    }

    @Test
    void testHandleCommand_Pathname() throws Exception {
        final Command COMMAND = new Command(CommandNames.STAT, array(PATHNAME));

        commandHandler.setStatus(RESPONSE_DATA);
        commandHandler.handleCommand(COMMAND, session);

        verify(session).sendReply(ReplyCodes.STAT_FILE_OK, formattedReplyTextFor(ReplyCodes.STAT_FILE_OK, RESPONSE_DATA));
        
        verifyNumberOfInvocations(commandHandler, 1);
        verifyOneDataElement(commandHandler.getInvocation(0), StatCommandHandler.PATHNAME_KEY, PATHNAME);
    }

    @Test
    void testHandleCommand_OverrideReplyCode() throws Exception {
        final Command COMMAND = new Command(CommandNames.STAT, EMPTY);
        commandHandler.setStatus(RESPONSE_DATA);
        commandHandler.setReplyCode(200);
        commandHandler.handleCommand(COMMAND, session);

        verify(session).sendReply(200, replyTextFor(200));

        verifyNumberOfInvocations(commandHandler, 1);
        verifyOneDataElement(commandHandler.getInvocation(0), StatCommandHandler.PATHNAME_KEY, null);
    }

    @BeforeEach
    void setUp() throws Exception {
        commandHandler = new StatCommandHandler();
        commandHandler.setReplyTextBundle(replyTextBundle);
    }

}
