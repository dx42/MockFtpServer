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

import org.mockftpserver.core.command.AbstractCommandHandlerTestCase;
import org.mockftpserver.core.command.Command;
import org.mockftpserver.core.command.CommandNames;
import org.mockftpserver.core.command.ReplyCodes;
import org.mockito.Mockito;

/**
 * Tests for the StouCommandHandler class
 *
 * @author Chris Mair
 */
public final class StouCommandHandlerTest extends AbstractCommandHandlerTestCase {

    private StouCommandHandler commandHandler;

    /**
     * Perform initialization before each test
     *
     * @see org.mockftpserver.core.command.AbstractCommandHandlerTestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        commandHandler = new StouCommandHandler();
        commandHandler.setReplyTextBundle(replyTextBundle);
    }

    /**
     * Test the handleCommand() method, as well as the getFileContents() and clearFileContents() methods
     */
    public void testHandleCommand() throws Exception {
        final String DATA = "ABC";
        final String FILENAME = "abc.txt";

        when(session.readData()).thenReturn(DATA.getBytes());

        Command command = new Command(CommandNames.STOU, array(FILENAME1));
        commandHandler.setFilename(FILENAME);
        commandHandler.handleCommand(command, session);

        Mockito.verify(session).sendReply(ReplyCodes.TRANSFER_DATA_INITIAL_OK, replyTextFor(ReplyCodes.TRANSFER_DATA_INITIAL_OK));
        Mockito.verify(session).openDataConnection();
        Mockito.verify(session).closeDataConnection();
        Mockito.verify(session).sendReply(ReplyCodes.TRANSFER_DATA_FINAL_OK, formattedReplyTextFor("226.WithFilename", FILENAME));

        verifyNumberOfInvocations(commandHandler, 1);
        verifyOneDataElement(commandHandler.getInvocation(0), StouCommandHandler.FILE_CONTENTS_KEY, DATA.getBytes());
    }

}
