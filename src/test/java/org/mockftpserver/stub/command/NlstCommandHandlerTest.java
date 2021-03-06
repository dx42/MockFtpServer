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
 * Tests for the NlstCommandHandler class
 *
 * @author Chris Mair
 */
class NlstCommandHandlerTest extends AbstractCommandHandlerTestCase {

    private NlstCommandHandler commandHandler;

    @Test
    void testHandleCommand() throws Exception {
        final String DIR_LISTING = " directory listing\nabc.txt\ndef.log\n";
        final String DIR_LISTING_TRIMMED = DIR_LISTING.trim();
        ((NlstCommandHandler) commandHandler).setDirectoryListing(DIR_LISTING);

        Command command1 = new Command(CommandNames.LIST, array(DIR1));
        Command command2 = new Command(CommandNames.LIST, EMPTY);
        commandHandler.handleCommand(command1, session);
        commandHandler.handleCommand(command2, session);

        verify(session, times(2)).sendReply(ReplyCodes.TRANSFER_DATA_INITIAL_OK, replyTextFor(ReplyCodes.TRANSFER_DATA_INITIAL_OK));
        verify(session, times(2)).openDataConnection();
        byte[] bytes = DIR_LISTING_TRIMMED.getBytes();
        verify(session, times(2)).sendData(bytes, bytes.length);
        verify(session, times(2)).closeDataConnection();
        verify(session, times(2)).sendReply(ReplyCodes.TRANSFER_DATA_FINAL_OK, replyTextFor(ReplyCodes.TRANSFER_DATA_FINAL_OK));

        verifyNumberOfInvocations(commandHandler, 2);
        verifyOneDataElement(commandHandler.getInvocation(0), NlstCommandHandler.PATHNAME_KEY, DIR1);
        verifyOneDataElement(commandHandler.getInvocation(1), NlstCommandHandler.PATHNAME_KEY, null);
    }

    @BeforeEach
    void setUp() throws Exception {
        commandHandler = new NlstCommandHandler();
        commandHandler.setReplyTextBundle(replyTextBundle);
    }

}
