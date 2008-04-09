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
import org.mockftpserver.core.command.CommandHandler
import org.mockftpserver.core.command.CommandNamesimport org.mockftpserver.core.session.StubSession
import org.mockftpserver.fake.StubServerConfiguration
import org.apache.log4j.Loggerimport org.mockftpserver.core.command.ReplyCodes
import org.mockftpserver.fake.user.UserAccount
import org.mockftpserver.fake.filesystem.FakeUnixFileSystem

/**
 * Abstract superclass for CommandHandler tests
 * 
 * @version $Revision: $ - $Date: $
 *
 * @author Chris Mair
 */
abstract class AbstractFakeCommandHandlerTest extends AbstractGroovyTest {

    protected session
    protected serverConfiguration
    protected commandHandler
    protected userAccount
    protected fileSystem

    //-------------------------------------------------------------------------
    // Tests (common to all subclasses)
    //-------------------------------------------------------------------------
    
    void testHandleCommand_ServerConfigurationIsNull() {
        commandHandler.serverConfiguration = null
        def command = createValidCommand()
        shouldFailWithMessageContaining("serverConfiguration") { commandHandler.handleCommand(command, session) }
    }
    
    void testHandleCommand_CommandIsNull() {
        shouldFailWithMessageContaining("command") { commandHandler.handleCommand(null, session) }
    }
    
    void testHandleCommand_SessionIsNull() {
        def command = createValidCommand()
        shouldFailWithMessageContaining("session") { commandHandler.handleCommand(command, null) }
    }
    
    //-------------------------------------------------------------------------
    // Abstract Method Declarations (must be implemented by all subclasses)
    //-------------------------------------------------------------------------
    
    /**
     * Create and return a new instance of the CommandHandler class under test. Concrete subclasses must implement.
     */
    abstract CommandHandler createCommandHandler()
    
    /**
     * Create and return a valid instance of the Command for the CommandHandler class 
     * under test. Concrete subclasses must implement.
     */
    abstract Command createValidCommand()
    
    //-------------------------------------------------------------------------
    // Test Setup
    //-------------------------------------------------------------------------
    
	void setUp() {
	    super.setUp()
	    session = new StubSession()
	    serverConfiguration = new StubServerConfiguration()
	    fileSystem = new FakeUnixFileSystem()
	    fileSystem.createParentDirectoriesAutomatically = true
	    serverConfiguration.setFileSystem(fileSystem)
	    
	    commandHandler = createCommandHandler()
	    commandHandler.serverConfiguration = serverConfiguration
	    userAccount = new UserAccount()
	}

    //-------------------------------------------------------------------------
    // Helper Methods
    //-------------------------------------------------------------------------

    /**
     * Perform a test of the handleCommand() method on the specified command
     * parameters, which are missing a required parameter for this CommandHandler.
     */
    protected void testHandleCommand_MissingRequiredParameter(List commandParameters) {
		commandHandler.handleCommand(createCommand(commandParameters), session)
        assertSessionReply(ReplyCodes.COMMAND_SYNTAX_ERROR)
    }
    
    /**
     * Perform a test of the handleCommand() method on the specified command
     * parameters, which are missing a required parameter for this CommandHandler.
     */
    protected testHandleCommand_MissingRequiredSessionAttribute() {
        def command = createValidCommand()
		commandHandler.handleCommand(command, session)
        assertSessionReply(ReplyCodes.ILLEGAL_STATE)
    }

    /**
     * Perform a test of the handleCommand() method on the specified command
     * parameters, when the current user has not yet logged in.
     */
    protected testHandleCommand_MissingRequiredLogin() {
        def command = createValidCommand()
		commandHandler.handleCommand(command, session)
        assertSessionReply(ReplyCodes.NOT_LOGGED_IN)
    }

    /**
     * @return a new Command with the specified parameters for this CommandHandler
     */
    protected Command createCommand(List commandParameters) {
        new Command(createValidCommand().name, commandParameters)
    }
    
    /**
     * Assert that the specified reply code (and default message) was sent to the session.
     */
    void assertSessionReply(int replyCode) {
        assertSessionReply(replyCode, replyCode as String)
    }
    
    /**
     * Assert that the specified reply code and message was sent to the session.
     */
    void assertSessionReply(int replyCode, String text) {
		LOG.info(session)
		def actual = session.sentReplies[0]
		def expected = [replyCode, text]
        assert actual == expected
    }
 }