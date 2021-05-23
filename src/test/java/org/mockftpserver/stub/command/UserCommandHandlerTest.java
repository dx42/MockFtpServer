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

/**
 * Tests for the UserCommandHandler class
 * 
 * @author Chris Mair
 */
class UserCommandHandlerTest extends AbstractCommandHandlerTestCase {

    private static final String USERNAME1 = "user1";
    private static final String USERNAME2 = "user2";

    private UserCommandHandler commandHandler;
    private Command command1;
    private Command command2;

    @Test
    void testHandleCommand() throws Exception {
        commandHandler.handleCommand(command1, session);
        commandHandler.setPasswordRequired(false);
        commandHandler.handleCommand(command2, session);

        verify(session).sendReply(ReplyCodes.USER_NEED_PASSWORD_OK, replyTextFor(ReplyCodes.USER_NEED_PASSWORD_OK));
        verify(session).sendReply(ReplyCodes.USER_LOGGED_IN_OK, replyTextFor(ReplyCodes.USER_LOGGED_IN_OK));

        verifyNumberOfInvocations(commandHandler, 2);
        verifyOneDataElement(commandHandler.getInvocation(0), UserCommandHandler.USERNAME_KEY, USERNAME1);
        verifyOneDataElement(commandHandler.getInvocation(1), UserCommandHandler.USERNAME_KEY, USERNAME2);
    }

    @Test
    void testHandleCommand_MissingUsernameParameter() throws Exception {
        testHandleCommand_InvalidParameters(commandHandler, CommandNames.USER, EMPTY);
    }

    @Test
    void testSetPasswordRequired() {
        assertTrue(commandHandler.isPasswordRequired());
        commandHandler.setPasswordRequired(false);
        assertFalse(commandHandler.isPasswordRequired());
    }
    
    @BeforeEach
    void setUp() throws Exception {
        commandHandler = new UserCommandHandler();
        command1 = new Command(CommandNames.USER, array(USERNAME1));
        command2 = new Command(CommandNames.USER, array(USERNAME2));
        commandHandler.setReplyTextBundle(replyTextBundle);
    }

}
