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
 * Tests for the StorCommandHandler class
 *
 * @author Chris Mair
 */
class StorCommandHandlerTest extends AbstractCommandHandlerTestCase {

    private StorCommandHandler commandHandler;

    @BeforeEach
    void setUp() throws Exception {
        commandHandler = new StorCommandHandler();
        commandHandler.setReplyTextBundle(replyTextBundle);
    }

    @Test
    void testHandleCommand() throws Exception {
        final String DATA = "ABC";

        when(session.readData()).thenReturn(DATA.getBytes());

        Command command = new Command(CommandNames.STOR, array(FILENAME1));
        commandHandler.handleCommand(command, session);

        verify(session).sendReply(ReplyCodes.TRANSFER_DATA_INITIAL_OK, replyTextFor(ReplyCodes.TRANSFER_DATA_INITIAL_OK));
        verify(session).openDataConnection();
        verify(session).closeDataConnection();
        verify(session).sendReply(ReplyCodes.TRANSFER_DATA_FINAL_OK, replyTextFor(ReplyCodes.TRANSFER_DATA_FINAL_OK));

        verifyNumberOfInvocations(commandHandler, 1);
        verifyTwoDataElements(commandHandler.getInvocation(0), StorCommandHandler.PATHNAME_KEY, FILENAME1,
                StorCommandHandler.FILE_CONTENTS_KEY, DATA.getBytes());
    }

    @Test
    void testHandleCommand_MissingPathnameParameter() throws Exception {
        testHandleCommand_InvalidParameters(commandHandler, CommandNames.STOR, EMPTY);
    }

}
