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
import org.mockftpserver.core.session.SessionKeys
import org.mockftpserver.fake.StubServerConfiguration
import org.mockftpserver.fake.user.UserAccount
import org.apache.log4j.Loggerimport org.mockftpserver.core.command.ReplyCodes
/**
 * Tests for NlstCommandHandler
 * 
 * @version $Revision: $ - $Date: $
 *
 * @author Chris Mair
 */
class NlstCommandHandlerTest extends AbstractLoginRequiredCommandHandlerTest {

    def DIR = "/usr"
    
    void testHandleCommand_SingleFile() {
        assert fileSystem.createFile("/usr/f1.txt")
        handleCommandAndVerifySessionReplies()
        assertSessionData("f1.txt")
	}

    void testHandleCommand_FilesAndDirectories() {
        assert fileSystem.createFile("/usr/f1.txt")
        assert fileSystem.createDirectory("/usr/OtherFiles")
        assert fileSystem.createFile("/usr/f2.txt")
        assert fileSystem.createDirectory("/usr/Archive")
        handleCommandAndVerifySessionReplies()
        
        def EXPECTED = [ "f1.txt", "OtherFiles", "f2.txt", "Archive" ] as Set
        def actual = session.sentData[0].tokenize(endOfLine()) as Set
        LOG.info("actual=$actual")
        assert actual == EXPECTED
	}

    void testHandleCommand_NoPath_UseCurrentDirectory() {
        assert fileSystem.createFile("/usr/f1.txt")
        session.setAttribute(SessionKeys.CURRENT_DIRECTORY, DIR)
        handleCommandAndVerifySessionReplies([])
        assertSessionData("f1.txt")
	}

    void testHandleCommand_EmptyDirectory() {
        handleCommandAndVerifySessionReplies()
        assertSessionData("")
	}
    
    void testHandleCommand_PathSpecifiesAFile() {
        assert fileSystem.createFile("/usr/f1.txt")
        handleCommandAndVerifySessionReplies(["/usr/f1.txt"])
        assertSessionData("")
	}
    
    void testHandleCommand_PathDoesNotExist() {
        handleCommandAndVerifySessionReplies(["/DoesNotExist"])
        assertSessionData("")
	}
    
    //-------------------------------------------------------------------------
    // Helper Methods
    //-------------------------------------------------------------------------
    
	CommandHandler createCommandHandler() {
	    new NlstCommandHandler()
	}
	
    Command createValidCommand() {
        return new Command(CommandNames.NLST, [DIR])
    }

    void setUp() {
        super.setUp()
        assert fileSystem.createDirectory("/usr")
    }
    
    private handleCommandAndVerifySessionReplies(parameters=[DIR]) {
        commandHandler.handleCommand(createCommand(parameters), session)        
        assertSessionReply(0, ReplyCodes.SEND_DATA_INITIAL_OK)
        assertSessionReply(1, ReplyCodes.SEND_DATA_FINAL_OK)
    }
    
}