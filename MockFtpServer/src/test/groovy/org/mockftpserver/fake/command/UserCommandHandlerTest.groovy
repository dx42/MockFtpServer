/*
 * Copyright 2008 the original author or authors.
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
package org.mockftpserver.fake.command

import org.mockftpserver.test.AbstractGroovyTest
import org.mockftpserver.core.command.Command
import org.mockftpserver.core.command.CommandNamesimport org.mockftpserver.core.session.StubSession
import org.mockftpserver.core.session.SessionKeys
import org.mockftpserver.fake.StubServerConfiguration
import org.mockftpserver.fake.user.UserAccount
import org.apache.log4j.Loggerimport org.mockftpserver.core.command.ReplyCodesimport com.sun.corba.se.impl.activation.CommandHandlerimport org.mockftpserver.core.util.AssertFailedException

/**
 * Tests for UserCommandHandler
 * 
 * @version $Revision: $ - $Date: $
 *
 * @author Chris Mair
 */
class UserCommandHandlerTest extends AbstractCommandHandlerTest {

     def USERNAME = "user123"
    
    void testHandleCommand_UserExists() {
        serverConfiguration.userAccounts[USERNAME] = new UserAccount()
        def command = new Command(CommandNames.USER, [USERNAME]);
		commandHandler.handleCommand(command, session)
        assertSessionReply(ReplyCodes.USER_NEED_PASSWORD_OK)
        assert session.getAttribute(SessionKeys.USERNAME) == USERNAME
	}

    void testHandleCommand_NoSuchUser() {
        def command = new Command(CommandNames.USER, [USERNAME]);
		commandHandler.handleCommand(command, session)
		// Will return OK, even if username is not recognized
        assertSessionReply(ReplyCodes.USER_NEED_PASSWORD_OK)
        assert session.getAttribute(SessionKeys.USERNAME) == USERNAME
	}

    void testHandleCommand_MissingUsernameParameter() {
        def command = new Command(CommandNames.USER, []);
        shouldFail(AssertFailedException) { commandHandler.handleCommand(command, session) }
    }
    
    void testHandleCommand_EmptyUsernameParameter() {
        def command = new Command(CommandNames.USER, [""]);
        shouldFail { commandHandler.handleCommand(command, session) }
    }
    
    void testHandleCommand_ServerConfigurationIsNull() {
        commandHandler.serverConfiguration = null
        def command = new Command(CommandNames.USER, [USERNAME]);
        shouldFailWithMessageContaining("serverConfiguration") { commandHandler.handleCommand(command, session) }
    }
    
    void testHandleCommand_CommandIsNull() {
        shouldFailWithMessageContaining("command") { commandHandler.handleCommand(null, session) }
    }
    
    void testHandleCommand_SessionIsNull() {
        def command = new Command(CommandNames.USER, [USERNAME]);
        shouldFailWithMessageContaining("session") { commandHandler.handleCommand(command, null) }
    }
    
	void setUp() {
	    super.setUp()
	}

	protected createCommandHandler() {
	    new UserCommandHandler()
	}
	
 }