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

import java.net.InetAddress;

/**
 * Tests for the EpsvCommandHandler class
 *
 * @author Chris Mair
 */
public final class EpsvCommandHandlerTest extends AbstractCommandHandlerTestCase {

    private static final InetAddress SERVER = inetAddress("1080::8:800:200C:417A");
    private static final int PORT = 6275;

    private EpsvCommandHandler commandHandler;

    /**
     * Test the handleCommand() method
     */
    public void testHandleCommand() throws Exception {
        when(session.switchToPassiveMode()).thenReturn(PORT);
        when(session.getServerHost()).thenReturn(SERVER);

        final Command COMMAND = new Command(CommandNames.EPSV, EMPTY);

        commandHandler.handleCommand(COMMAND, session);
        verify(session).sendReply(ReplyCodes.EPSV_OK, formattedReplyTextFor(ReplyCodes.EPSV_OK, Integer.toString(PORT)));

        verifyNumberOfInvocations(commandHandler, 1);
        verifyNoDataElements(commandHandler.getInvocation(0));
    }

    /**
     * Perform initialization before each test
     *
     * @see org.mockftpserver.core.command.AbstractCommandHandlerTestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        commandHandler = new EpsvCommandHandler();
        commandHandler.setReplyTextBundle(replyTextBundle);
    }

}